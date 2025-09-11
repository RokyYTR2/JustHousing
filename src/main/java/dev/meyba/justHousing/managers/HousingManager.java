package dev.meyba.justHousing.managers;

import dev.meyba.justHousing.JustHousing;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HousingManager {
    private final Map<UUID, String> playerHousingMap;
    private final Map<String, Housing> housings;
    private final JustHousing plugin;

    public HousingManager(JustHousing plugin) {
        this.plugin = plugin;
        this.playerHousingMap = new HashMap<>();
        this.housings = new HashMap<>();
        loadHousings();
    }

    public void createHousing(Player player, String name) {
        String housingId = "housing_" + player.getUniqueId();
        World existingWorld = Bukkit.getWorld(housingId);
        if (existingWorld != null) {
            return;
        }

        WorldCreator creator = new WorldCreator(housingId);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);

        World world = creator.createWorld();

        if (world != null) {
            world.setPVP(false);
            world.getWorldBorder().setCenter(0, 0);
            world.getWorldBorder().setSize(100);

            world.setAutoSave(true);

            Location center = new Location(world, 0.5, -60, 0.5);
            world.setSpawnLocation(center.getBlockX(), center.getBlockY(), center.getBlockZ());

            Housing newHousing = new Housing(housingId, player.getUniqueId(), center, name);
            this.housings.put(housingId, newHousing);
            this.playerHousingMap.put(player.getUniqueId(), housingId);

            world.save();
            saveHousings();

            player.teleport(center);
        }
    }

    public void deleteHousing(Player player) {
        String housingId = this.playerHousingMap.get(player.getUniqueId());
        if (housingId != null) {
            Housing housing = this.housings.remove(housingId);
            if (housing != null) {
                this.playerHousingMap.remove(player.getUniqueId());
                housing.getMembers().forEach((uuid, member) -> this.playerHousingMap.remove(uuid));

                World worldToDelete = Bukkit.getWorld(housingId);
                if (worldToDelete != null) {
                    for (Player p : worldToDelete.getPlayers()) {
                        p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    }

                    worldToDelete.save();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (Bukkit.unloadWorld(worldToDelete, false)) {
                            File worldFolder = worldToDelete.getWorldFolder();
                            deleteWorldFolder(worldFolder);
                        }
                    }, 20L);
                }
                saveHousings();
            }
        }
    }

    private void deleteWorldFolder(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteWorldFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }
    }

    public void invitePlayer(String housingId, Player target) {
        Housing housing = this.housings.get(housingId);
        if (housing != null) {
            housing.addMember(target.getUniqueId());
            this.playerHousingMap.put(target.getUniqueId(), housingId);
            saveHousings();
        }
    }

    public Housing findHousingByOwner(OfflinePlayer player) {
        if (player == null) return null;
        String housingId = this.playerHousingMap.get(player.getUniqueId());
        if (housingId != null) {
            return this.housings.get(housingId);
        }
        return null;
    }

    public Housing getHousingById(String id) {
        return this.housings.get(id);
    }

    public Map<String, Housing> getHousings() {
        return this.housings;
    }

    public void saveHousings() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File housingFile = new File(dataFolder, "housings.yml");
        YamlConfiguration housingConfig = new YamlConfiguration();

        for (Housing housing : housings.values()) {
            String path = "housings." + housing.getId();
            housingConfig.set(path + ".owner", housing.getOwner().toString());
            housingConfig.set(path + ".name", housing.getName());
            housingConfig.set(path + ".votes", housing.getVotes());
            housingConfig.set(path + ".supervote", housing.getSuperVotes());

            World world = Bukkit.getWorld(housing.getId());
            if (world != null) {
                housingConfig.set(path + ".center.world", world.getName());
                housingConfig.set(path + ".center.x", housing.getCenter().getX());
                housingConfig.set(path + ".center.y", housing.getCenter().getY());
                housingConfig.set(path + ".center.z", housing.getCenter().getZ());

                housingConfig.set(path + ".worldExists", true);
            } else {
                housingConfig.set(path + ".worldExists", false);
            }

            housingConfig.set(path + ".breakBlocksEnabled", housing.isBreakBlocksEnabled());
            housingConfig.set(path + ".placeBlocksEnabled", housing.isPlaceBlocksEnabled());
            housingConfig.set(path + ".mobSpawningEnabled", housing.isMobSpawningEnabled());
            housingConfig.set(path + ".pvpEnabled", housing.isPvpEnabled());
            housingConfig.set(path + ".fallDamageEnabled", housing.isFallDamageEnabled());
            housingConfig.set(path + ".naturalRegenerationEnabled", housing.isNaturalRegenerationEnabled());
            housingConfig.set(path + ".defaultGameMode", housing.getDefaultGameMode().name());

            housingConfig.set(path + ".members", housing.getMembers().entrySet().stream()
                    .map(entry -> entry.getKey().toString() + ":" + entry.getValue().isAdmin())
                    .collect(Collectors.toList()));
            housingConfig.set(path + ".banned", new ArrayList<>(housing.getBannedPlayers()));
        }

        try {
            housingConfig.save(housingFile);
        } catch (IOException e) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    housingConfig.save(housingFile);
                } catch (IOException ignored) {
                }
            }, 20L);
        }
    }

    public void loadHousings() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File housingFile = new File(dataFolder, "housings.yml");
        if (!housingFile.exists()) {
            try {
                housingFile.createNewFile();
            } catch (IOException ignored) {
            }
        }
        YamlConfiguration housingConfig = YamlConfiguration.loadConfiguration(housingFile);
        if (housingConfig.contains("housings")) {
            for (String id : housingConfig.getConfigurationSection("housings").getKeys(false)) {
                try {
                    String path = "housings." + id;
                    UUID ownerId = UUID.fromString(housingConfig.getString(path + ".owner"));
                    String name = housingConfig.getString(path + ".name", "Unknown Housing");
                    int votes = housingConfig.getInt(path + ".votes", 0);
                    int supervotes = housingConfig.getInt(path + ".supervote", 0);
                    boolean worldExists = housingConfig.getBoolean(path + ".worldExists", true);

                    World world = null;
                    File worldFolder = new File(Bukkit.getWorldContainer(), id);

                    if (worldExists && worldFolder.exists()) {
                        world = Bukkit.getWorld(id);
                        if (world == null) {
                            WorldCreator creator = new WorldCreator(id);
                            creator.environment(World.Environment.NORMAL);
                            creator.type(WorldType.FLAT);
                            creator.generateStructures(false);
                            world = creator.createWorld();
                        }
                    } else if (!worldExists || !worldFolder.exists()) {
                        continue;
                    }

                    if (world == null) {
                        continue;
                    }

                    world.getWorldBorder().setCenter(0, 0);
                    world.getWorldBorder().setSize(64);
                    world.setPVP(housingConfig.getBoolean(path + ".pvpEnabled", false));
                    world.setAutoSave(true);

                    double x = housingConfig.getDouble(path + ".center.x", 0.5);
                    double y = housingConfig.getDouble(path + ".center.y", -60);
                    double z = housingConfig.getDouble(path + ".center.z", 0.5);
                    Location center = new Location(world, x, y, z);

                    world.setSpawnLocation(center.getBlockX(), center.getBlockY(), center.getBlockZ());

                    Housing housing = new Housing(id, ownerId, center, name);
                    housing.setVotes(votes);
                    housing.setSuperVotes(supervotes);
                    housing.setBreakBlocksEnabled(housingConfig.getBoolean(path + ".breakBlocksEnabled", true));
                    housing.setPlaceBlocksEnabled(housingConfig.getBoolean(path + ".placeBlocksEnabled", true));
                    housing.setMobSpawningEnabled(housingConfig.getBoolean(path + ".mobSpawningEnabled", false));
                    housing.setPvpEnabled(housingConfig.getBoolean(path + ".pvpEnabled", false));
                    housing.setFallDamageEnabled(housingConfig.getBoolean(path + ".fallDamageEnabled", false));
                    housing.setNaturalRegenerationEnabled(housingConfig.getBoolean(path + ".naturalRegenerationEnabled", true));
                    String gmStr = housingConfig.getString(path + ".defaultGameMode", "SURVIVAL");
                    housing.setDefaultGameMode(GameMode.valueOf(gmStr.toUpperCase()));

                    List<String> membersData = housingConfig.getStringList(path + ".members");
                    for (String memberData : membersData) {
                        try {
                            String[] parts = memberData.split(":");
                            UUID memberId = UUID.fromString(parts[0]);
                            boolean isAdmin = Boolean.parseBoolean(parts[1]);
                            Member member = new Member();
                            member.setAdmin(isAdmin);
                            housing.getMembers().put(memberId, member);
                            this.playerHousingMap.put(memberId, id);
                        } catch (Exception ignored) {
                        }
                    }

                    List<String> bannedData = housingConfig.getStringList(path + ".banned");
                    for (String bannedId : bannedData) {
                        try {
                            housing.getBannedPlayers().add(UUID.fromString(bannedId));
                        } catch (Exception ignored) {
                        }
                    }

                    this.housings.put(id, housing);
                    this.playerHousingMap.put(ownerId, id);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static class Housing {
        private final String id;
        private final UUID owner;
        private final Location center;
        private final String name;
        private final Map<UUID, Member> members;
        private final Set<UUID> bannedPlayers;
        private boolean breakBlocksEnabled;
        private boolean placeBlocksEnabled;
        private boolean mobSpawningEnabled;
        private boolean pvpEnabled;
        private boolean fallDamageEnabled;
        private boolean naturalRegenerationEnabled;
        private GameMode defaultGameMode = GameMode.SURVIVAL;
        private int votes;
        private int superVotes;

        public Housing(String id, UUID owner, Location center, String name) {
            this.id = id;
            this.owner = owner;
            this.center = center;
            this.name = name;
            this.members = new HashMap<>();
            this.bannedPlayers = new HashSet<>();
            this.breakBlocksEnabled = false;
            this.placeBlocksEnabled = false;
            this.mobSpawningEnabled = false;
            this.pvpEnabled = false;
            this.fallDamageEnabled = false;
            this.naturalRegenerationEnabled = true;
            this.defaultGameMode = GameMode.SURVIVAL;
            this.votes = 0;
            this.superVotes = 0;
        }

        public UUID getOwner() {
            return owner;
        }

        public String getName() {
            return name;
        }

        public Map<UUID, Member> getMembers() {
            return this.members;
        }

        public boolean isMemberAdmin(UUID playerId) {
            Member member = this.members.get(playerId);
            return member != null && member.isAdmin();
        }

        public void addMember(UUID playerId) {
            this.members.put(playerId, new Member());
        }

        public void removeMember(UUID playerId) {
            this.members.remove(playerId);
        }

        public String getId() {
            return this.id;
        }

        public Location getCenter() {
            return this.center;
        }

        public boolean isBreakBlocksEnabled() {
            return breakBlocksEnabled;
        }

        public void setBreakBlocksEnabled(boolean breakBlocksEnabled) {
            this.breakBlocksEnabled = breakBlocksEnabled;
        }

        public boolean isPlaceBlocksEnabled() {
            return placeBlocksEnabled;
        }

        public void setPlaceBlocksEnabled(boolean placeBlocksEnabled) {
            this.placeBlocksEnabled = placeBlocksEnabled;
        }

        public boolean isMobSpawningEnabled() {
            return mobSpawningEnabled;
        }

        public void setMobSpawningEnabled(boolean mobSpawningEnabled) {
            this.mobSpawningEnabled = mobSpawningEnabled;
        }

        public boolean isPvpEnabled() {
            return pvpEnabled;
        }

        public void setPvpEnabled(boolean pvpEnabled) {
            this.pvpEnabled = pvpEnabled;
            World world = Bukkit.getWorld(this.id);
            if (world != null) {
                world.setPVP(pvpEnabled);
            }
        }

        public boolean isFallDamageEnabled() {
            return fallDamageEnabled;
        }

        public void setFallDamageEnabled(boolean fallDamageEnabled) {
            this.fallDamageEnabled = fallDamageEnabled;
        }

        public boolean isNaturalRegenerationEnabled() {
            return naturalRegenerationEnabled;
        }

        public void setNaturalRegenerationEnabled(boolean naturalRegenerationEnabled) {
            this.naturalRegenerationEnabled = naturalRegenerationEnabled;
        }

        public GameMode getDefaultGameMode() {
            return defaultGameMode;
        }

        public void setDefaultGameMode(GameMode defaultGameMode) {
            this.defaultGameMode = defaultGameMode;
        }

        public Set<UUID> getBannedPlayers() {
            return bannedPlayers;
        }

        public boolean isBanned(UUID playerId) {
            return bannedPlayers.contains(playerId);
        }

        public void banPlayer(UUID playerId) {
            bannedPlayers.add(playerId);
        }

        public void unbanPlayer(UUID playerId) {
            bannedPlayers.remove(playerId);
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }

        public void addVote() {
            this.votes++;
        }

        public int getSuperVotes() {
            return superVotes;
        }

        public void setSuperVotes(int superVotes) {
            this.superVotes = superVotes;
        }

        public void addSuperVote() {
            this.superVotes++;
        }
    }

    public static class Member {
        private boolean admin;

        public Member() {
            this.admin = false;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }
    }
}
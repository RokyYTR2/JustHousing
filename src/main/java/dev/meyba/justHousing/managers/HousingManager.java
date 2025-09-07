package dev.meyba.justHousing.managers;

import dev.meyba.justHousing.JustHousing;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HousingManager {
    private final Map<UUID, String> playerHousingMap;
    private final Map<String, Housing> housings;
    private final JustHousing plugin;

    public HousingManager(JustHousing plugin) {
        this.plugin = plugin;
        this.playerHousingMap = new HashMap<>();
        this.housings = new HashMap<>();
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
            world.getWorldBorder().setSize(64);
            Location center = new Location(world, 0.5, -60, 0.5);
            Housing newHousing = new Housing(housingId, player.getUniqueId(), center, name);
            this.housings.put(housingId, newHousing);
            this.playerHousingMap.put(player.getUniqueId(), housingId);
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
                    Bukkit.unloadWorld(worldToDelete, true);
                }
            }
        }
    }

    public void invitePlayer(String housingId, Player target) {
        Housing housing = this.housings.get(housingId);
        if (housing != null) {
            housing.addMember(target.getUniqueId());
        }
    }

    public Housing findHousingByOwner(Player player) {
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
        File housingFile = new File(plugin.getDataFolder(), "housings.yml");
        YamlConfiguration housingConfig = new YamlConfiguration();
        for (Housing housing : housings.values()) {
            String path = "housings." + housing.getId();
            housingConfig.set(path + ".owner", housing.getOwner().toString());
            housingConfig.set(path + ".name", housing.getName());
            housingConfig.set(path + ".center.world", housing.getCenter().getWorld().getName());
            housingConfig.set(path + ".center.x", housing.getCenter().getX());
            housingConfig.set(path + ".center.y", housing.getCenter().getY());
            housingConfig.set(path + ".center.z", housing.getCenter().getZ());
            housingConfig.set(path + ".breakBlocksEnabled", housing.isBreakBlocksEnabled());
            housingConfig.set(path + ".placeBlocksEnabled", housing.isPlaceBlocksEnabled());
            housingConfig.set(path + ".mobSpawningEnabled", housing.isMobSpawningEnabled());
            housingConfig.set(path + ".pvpEnabled", housing.isPvpEnabled());
            List<String> members = new ArrayList<>();
            for (Map.Entry<UUID, Member> entry : housing.getMembers().entrySet()) {
                String memberData = entry.getKey().toString() + ":" + entry.getValue().isAdmin();
                members.add(memberData);
            }
            housingConfig.set(path + ".members", members);
        }
        try {
            housingConfig.save(housingFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save housings to file!");
            e.printStackTrace();
        }
    }

    public void loadHousings() {
        File housingFile = new File(plugin.getDataFolder(), "housings.yml");
        if (!housingFile.exists()) {
            return;
        }

        YamlConfiguration housingConfig = YamlConfiguration.loadConfiguration(housingFile);
        if (!housingConfig.isConfigurationSection("housings")) {
            return;
        }

        for (String id : housingConfig.getConfigurationSection("housings").getKeys(false)) {
            String path = "housings." + id;
            UUID ownerId = UUID.fromString(housingConfig.getString(path + ".owner"));
            String name = housingConfig.getString(path + ".name", "Unknown Housing");
            String worldName = housingConfig.getString(path + ".center.world");
            double x = housingConfig.getDouble(path + ".center.x");
            double y = housingConfig.getDouble(path + ".center.y");
            double z = housingConfig.getDouble(path + ".center.z");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                WorldCreator creator = new WorldCreator(id);
                world = creator.createWorld();
            }

            if (world != null) {
                Location center = new Location(world, x, y, z);
                Housing housing = new Housing(id, ownerId, center, name);
                housing.setBreakBlocksEnabled(housingConfig.getBoolean(path + ".breakBlocksEnabled", true));
                housing.setPlaceBlocksEnabled(housingConfig.getBoolean(path + ".placeBlocksEnabled", true));
                housing.setMobSpawningEnabled(housingConfig.getBoolean(path + ".mobSpawningEnabled", false));
                housing.setPvpEnabled(housingConfig.getBoolean(path + ".pvpEnabled", false));

                List<String> membersData = housingConfig.getStringList(path + ".members");
                for (String memberData : membersData) {
                    String[] parts = memberData.split(":");
                    UUID memberId = UUID.fromString(parts[0]);
                    boolean isAdmin = Boolean.parseBoolean(parts[1]);
                    Member member = new Member();
                    member.setAdmin(isAdmin);
                    housing.getMembers().put(memberId, member);
                }

                this.housings.put(id, housing);
                this.playerHousingMap.put(ownerId, id);
                for (UUID memberId : housing.getMembers().keySet()) {
                    this.playerHousingMap.put(memberId, id);
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
        private boolean breakBlocksEnabled;
        private boolean placeBlocksEnabled;
        private boolean mobSpawningEnabled;
        private boolean pvpEnabled;

        public Housing(String id, UUID owner, Location center, String name) {
            this.id = id;
            this.owner = owner;
            this.center = center;
            this.name = name;
            this.members = new HashMap<>();
            this.breakBlocksEnabled = true;
            this.placeBlocksEnabled = true;
            this.mobSpawningEnabled = false;
            this.pvpEnabled = false;
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
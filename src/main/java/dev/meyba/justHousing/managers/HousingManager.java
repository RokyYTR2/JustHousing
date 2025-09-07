package dev.meyba.justHousing.managers;

import dev.meyba.justHousing.JustHousing;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HousingManager {
    private final Map<UUID, String> playerHousingMap;
    private final Map<String, Housing> housings;
    private final JustHousing plugin;

    public HousingManager(JustHousing plugin) {
        this.plugin = plugin;
        this.playerHousingMap = new HashMap<>();
        this.housings = new HashMap<>();
    }

    public void createHousing(Player player) {
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
            world.getWorldBorder().setCenter(0, 0);
            world.getWorldBorder().setSize(64);
            Location center = new Location(world, 0, 64, 0);
            Housing newHousing = new Housing(housingId, player.getUniqueId(), center);
            this.housings.put(housingId, newHousing);
            this.playerHousingMap.put(player.getUniqueId(), housingId);
            player.teleport(center);
        }
    }

    public void deleteHousing(String housingId) {
        Housing housing = this.housings.remove(housingId);
        if (housing != null) {
            this.playerHousingMap.remove(housing.getOwner());
            housing.getMembers().forEach((uuid, member) -> this.playerHousingMap.remove(uuid));
            Bukkit.unloadWorld(housing.getId(), false);
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

    public Housing getHousingById(String housingId) {
        return this.housings.get(housingId);
    }

    // Nová metoda pro získání všech housingů
    public Map<String, Housing> getHousings() {
        return this.housings;
    }

    public static class Member {
        private boolean isAdmin;

        public Member() {
            this.isAdmin = false;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }
    }

    public static class Housing {
        private final String id;
        private final UUID owner;
        private final Location center;
        private final Map<UUID, Member> members;
        private boolean breakBlocksEnabled = true;
        private boolean placeBlocksEnabled = true;

        public Housing(String id, UUID owner, Location center) {
            this.id = id;
            this.owner = owner;
            this.center = center;
            this.members = new HashMap<>();
        }

        public boolean isInHousing(Location location) {
            return location.getWorld().getName().equals(this.center.getWorld().getName());
        }

        public UUID getOwner() {
            return this.owner;
        }

        public Map<UUID, Member> getMembers() {
            return this.members;
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
    }
}
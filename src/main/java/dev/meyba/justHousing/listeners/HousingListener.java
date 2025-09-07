package dev.meyba.justHousing.listeners;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HousingListener implements Listener {
    private final HousingManager housingManager;
    private final JustHousing plugin;

    public HousingListener(HousingManager housingManager, JustHousing plugin) {
        this.housingManager = housingManager;
        this.plugin = plugin;
    }

    private void updatePlayerVisibility(Player player) {
        String playerWorldName = player.getWorld().getName();
        boolean playerIsInHousing = playerWorldName.startsWith("housing_");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (player.equals(onlinePlayer)) continue;

            String onlinePlayerWorldName = onlinePlayer.getWorld().getName();
            boolean onlinePlayerIsInHousing = onlinePlayerWorldName.startsWith("housing_");

            if (playerIsInHousing) {
                if (playerWorldName.equals(onlinePlayerWorldName)) {
                    player.showPlayer(plugin, onlinePlayer);
                    onlinePlayer.showPlayer(plugin, player);
                } else {
                    player.hidePlayer(plugin, onlinePlayer);
                    onlinePlayer.hidePlayer(plugin, player);
                }
            } else {
                if (onlinePlayerIsInHousing) {
                    player.hidePlayer(plugin, onlinePlayer);
                    onlinePlayer.hidePlayer(plugin, player);
                } else {
                    player.showPlayer(plugin, onlinePlayer);
                    onlinePlayer.showPlayer(plugin, player);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        HousingManager.Housing housing = housingManager.getHousingById(event.getBlock().getWorld().getName());
        if (housing != null) {
            boolean isOwner = player.getUniqueId().equals(housing.getOwner());
            boolean isAdmin = housing.isMemberAdmin(player.getUniqueId());
            if (!housing.isBreakBlocksEnabled() && !isOwner && !isAdmin) {
                event.setCancelled(true);
                String prefix = plugin.getConfig().getString("prefix");
                String msg = plugin.getConfig().getString("messages.break-blocks-denied");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        HousingManager.Housing housing = housingManager.getHousingById(event.getBlock().getWorld().getName());
        if (housing != null) {
            boolean isOwner = player.getUniqueId().equals(housing.getOwner());
            boolean isAdmin = housing.isMemberAdmin(player.getUniqueId());
            if (!housing.isPlaceBlocksEnabled() && !isOwner && !isAdmin) {
                event.setCancelled(true);
                String prefix = plugin.getConfig().getString("prefix");
                String msg = plugin.getConfig().getString("messages.place-blocks-denied");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        HousingManager.Housing housing = housingManager.getHousingById(event.getLocation().getWorld().getName());
        if (housing != null) {
            if (!housing.isMobSpawningEnabled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        if (worldName.startsWith("housing_")) {
            event.getRecipients().clear();
            event.getRecipients().addAll(event.getPlayer().getWorld().getPlayers());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            HousingManager.Housing housing = housingManager.getHousingById(damager.getWorld().getName());
            if (housing != null) {
                if (!housing.isPvpEnabled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerVisibility(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() == null || event.getTo().getWorld() == null || event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerVisibility(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quittingPlayer = event.getPlayer();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(quittingPlayer)) {
                onlinePlayer.showPlayer(plugin, quittingPlayer);
            }
        }
    }
}
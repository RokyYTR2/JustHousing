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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HousingListener implements Listener {
    private final HousingManager housingManager;
    private final JustHousing plugin;

    public HousingListener(HousingManager housingManager, JustHousing plugin) {
        this.housingManager = housingManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        HousingManager.Housing housing = housingManager.getHousingById(event.getBlock().getWorld().getName());
        if (housing != null) {
            boolean isOwner = housing.getOwner().equals(player.getUniqueId());
            boolean isMember = housing.getMembers().containsKey(player.getUniqueId());

            if (!isOwner && !isMember) {
                String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission-break");
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        HousingManager.Housing housing = housingManager.getHousingById(event.getBlock().getWorld().getName());
        if (housing != null) {
            boolean isOwner = housing.getOwner().equals(player.getUniqueId());
            boolean isMember = housing.getMembers().containsKey(player.getUniqueId());

            if (!isOwner && !isMember) {
                String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission-place");
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        HousingManager.Housing housing = housingManager.findHousingByOwner(player);
        if (housing == null) return;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!housing.getMembers().containsKey(onlinePlayer.getUniqueId())) {
                player.hidePlayer(plugin, onlinePlayer);
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
            player.showPlayer(plugin, onlinePlayer);
        }
    }
}
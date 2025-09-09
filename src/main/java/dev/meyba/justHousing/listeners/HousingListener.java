package dev.meyba.justHousing.listeners;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.ChatManager;
import dev.meyba.justHousing.managers.HousingManager;
import dev.meyba.justHousing.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.*;

public class HousingListener implements Listener {
    private final HousingManager housingManager;
    private final JustHousing plugin;
    private final ScoreboardManager scoreboardManager;
    private final ChatManager chatManager;

    public HousingListener(HousingManager housingManager, JustHousing plugin, ChatManager chatManager) {
        this.housingManager = housingManager;
        this.plugin = plugin;
        this.scoreboardManager = new ScoreboardManager(plugin, housingManager);
        this.chatManager = chatManager;
    }

    private void updatePlayerVisibility(Player player) {
        String playerWorldName = player.getWorld().getName();
        boolean playerIsInHousing = playerWorldName.startsWith("housing_");
        HousingManager.Housing playerHousing = playerIsInHousing ? housingManager.getHousingById(playerWorldName) : null;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (player.equals(onlinePlayer)) continue;

            String onlinePlayerWorldName = onlinePlayer.getWorld().getName();
            boolean onlinePlayerIsInHousing = onlinePlayerWorldName.startsWith("housing_");
            HousingManager.Housing onlinePlayerHousing = onlinePlayerIsInHousing ? housingManager.getHousingById(onlinePlayerWorldName) : null;

            if (playerIsInHousing && onlinePlayerIsInHousing) {
                if (playerHousing != null && onlinePlayerHousing != null && !playerHousing.getId().equals(onlinePlayerHousing.getId())) {
                    player.hidePlayer(plugin, onlinePlayer);
                    onlinePlayer.hidePlayer(plugin, player);
                } else {
                    player.showPlayer(plugin, onlinePlayer);
                    onlinePlayer.showPlayer(plugin, player);
                }
            } else if (playerIsInHousing || onlinePlayerIsInHousing) {
                player.hidePlayer(plugin, onlinePlayer);
                onlinePlayer.hidePlayer(plugin, player);
            } else {
                player.showPlayer(plugin, onlinePlayer);
                onlinePlayer.showPlayer(plugin, player);
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
        Player sender = event.getPlayer();
        String senderWorldName = sender.getWorld().getName();
        boolean senderIsInHousing = senderWorldName.startsWith("housing_");
        HousingManager.Housing senderHousing = senderIsInHousing ? housingManager.getHousingById(senderWorldName) : null;

        String formattedMessage = chatManager.formatChatMessage(sender, event.getMessage());
        event.setFormat(formattedMessage);

        event.getRecipients().clear();
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            String recipientWorldName = recipient.getWorld().getName();
            boolean recipientIsInHousing = recipientWorldName.startsWith("housing_");
            HousingManager.Housing recipientHousing = recipientIsInHousing ? housingManager.getHousingById(recipientWorldName) : null;

            if (senderIsInHousing && recipientIsInHousing) {
                if (senderHousing != null && recipientHousing != null && senderHousing.getId().equals(recipientHousing.getId())) {
                    event.getRecipients().add(recipient);
                }
            } else if (!senderIsInHousing && !recipientIsInHousing) {
                event.getRecipients().add(recipient);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player) {
            HousingManager.Housing housing = housingManager.getHousingById(damager.getWorld().getName());
            if (housing != null) {
                if (!housing.isPvpEnabled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            HousingManager.Housing housing = housingManager.getHousingById(player.getWorld().getName());
            if (housing != null && !housing.isFallDamageEnabled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        if (worldName.startsWith("housing_")) {
            HousingManager.Housing housing = housingManager.getHousingById(worldName);
            if (housing != null) {
                if (housing.getOwner().equals(player.getUniqueId())) {
                    event.setRespawnLocation(housing.getCenter());
                }
                player.setGameMode(housing.getDefaultGameMode());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    updatePlayerVisibility(player);
                    scoreboardManager.updateScoreboard(player);
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        if (worldName.startsWith("housing_")) {
            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updatePlayerVisibility(player);
            scoreboardManager.updateScoreboard(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() == null || event.getTo().getWorld() == null || event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        Player player = event.getPlayer();
        String toWorldName = event.getTo().getWorld().getName();
        String fromWorldName = event.getFrom().getWorld().getName();
        if (toWorldName.startsWith("housing_") && !fromWorldName.startsWith("housing_")) {
            HousingManager.Housing housing = housingManager.getHousingById(toWorldName);
            if (housing != null) {
                player.setGameMode(housing.getDefaultGameMode());
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updatePlayerVisibility(event.getPlayer());
            scoreboardManager.updateScoreboard(event.getPlayer());
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quittingPlayer = event.getPlayer();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(quittingPlayer)) {
                onlinePlayer.showPlayer(plugin, quittingPlayer);
            }
        }
        scoreboardManager.clearScoreboard(quittingPlayer);
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player player) {
            HousingManager.Housing housing = housingManager.getHousingById(player.getWorld().getName());
            if (housing != null && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED && !housing.isNaturalRegenerationEnabled()) {
                event.setCancelled(true);
            }
        }
    }
}
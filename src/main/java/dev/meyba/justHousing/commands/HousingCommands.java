package dev.meyba.justHousing.commands;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.guis.HousingListGUI;
import dev.meyba.justHousing.guis.HousingSettingsGUI;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HousingCommands implements CommandExecutor, TabCompleter {
    private final HousingManager housingManager;
    private final JustHousing plugin;
    private final HousingListGUI housingListGUI;

    public HousingCommands(HousingManager housingManager, JustHousing plugin, HousingListGUI housingListGUI) {
        this.housingManager = housingManager;
        this.plugin = plugin;
        this.housingListGUI = housingListGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("prefix"));

        if (!(sender instanceof Player player)) {
            String notAPlayerMsg = this.plugin.getConfig().getString("messages.commands.permissions.not-a-player");
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notAPlayerMsg));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("housing.list")) {
                String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                return true;
            }
            housingListGUI.open(player);
            return true;
        }

        if (!player.hasPermission("housing.use")) {
            String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
            return true;
        }

        HousingManager.Housing playerHousing = housingManager.findHousingByOwner(player);
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!player.hasPermission("housing.reload")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                plugin.reloadConfig();
                housingManager.loadHousings();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (plugin.getScoreboardManager() != null) {
                        plugin.getScoreboardManager().updateScoreboard(onlinePlayer);
                    }
                }
                String reloadSuccessMsg = this.plugin.getConfig().getString("messages.commands.reload.reload-success");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', reloadSuccessMsg));
                return true;
            case "help":
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7ʜᴇʟᴘ ᴍᴇɴᴜ:"));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing create <ɴᴀᴍᴇ> - ᴄʀᴇᴀᴛᴇꜱ ᴀ ɴᴇᴡ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing delete - ᴅᴇʟᴇᴛᴇꜱ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing invite <ᴘʟᴀʏᴇʀ> - ɪɴᴠɪᴛᴇꜱ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing join <ᴘʟᴀʏᴇʀ> - ᴊᴏɪɴꜱ ᴀ ᴘʟᴀʏᴇʀ'ꜱ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing settings - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing permissions - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing list - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing kick <ᴘʟᴀʏᴇʀ> [ʀᴇᴀꜱᴏɴ] - ᴋɪᴄᴋ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing ban <ᴘʟᴀʏᴇʀ> [ʀᴇᴀꜱᴏɴ] - ʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing unban <ᴘʟᴀʏᴇʀ> - ᴜɴʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing gamemode <ᴄʀᴇᴀᴛɪᴠᴇ|ᴀᴅᴠᴇɴᴛᴜʀᴇ|ꜱᴜʀᴠɪᴠᴀʟ> [ᴘʟᴀʏᴇʀ] - ꜱᴇᴛ ɢᴀᴍᴇᴍᴏᴅᴇ ꜰᴏʀ ʏᴏᴜ ᴏʀ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing time <ᴅᴀʏ|ɴɪɢʜᴛ|ᴅᴀᴡɴ|ɴᴏᴏɴ> - ꜱᴇᴛ ᴡᴏʀʟᴅ ᴛɪᴍᴇ."));
                return true;
            case "create":
                if (!player.hasPermission("housing.create")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (args.length != 2) {
                    String createUsageMsg = this.plugin.getConfig().getString("messages.commands.create.create-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', createUsageMsg));
                    return true;
                }
                if (playerHousing != null) {
                    String alreadyHasHousingMsg = this.plugin.getConfig().getString("messages.commands.create.already-has-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyHasHousingMsg));
                    return true;
                }
                housingManager.createHousing(player, args[1]);
                String createdMsg = this.plugin.getConfig().getString("messages.commands.create.housing-created").replace("%name%", args[1]);
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', createdMsg));
                return true;
            case "delete":
                if (!player.hasPermission("housing.delete")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingToDeleteMsg = this.plugin.getConfig().getString("messages.commands.delete.no-housing-to-delete");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingToDeleteMsg));
                    return true;
                }
                housingManager.deleteHousing(player);
                String housingDeletedMsg = this.plugin.getConfig().getString("messages.commands.delete.housing-deleted");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingDeletedMsg));
                return true;
            case "invite":
                if (!player.hasPermission("housing.invite")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (args.length != 2) {
                    String inviteUsageMsg = this.plugin.getConfig().getString("messages.commands.invite.invite-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteUsageMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                Player targetInvite = Bukkit.getPlayer(args[1]);
                if (targetInvite == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.commands.permissions.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                if (targetInvite == player) {
                    String cannotInviteSelfMsg = this.plugin.getConfig().getString("messages.commands.invite.cannot-invite-self");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', cannotInviteSelfMsg));
                    return true;
                }
                if (playerHousing.getMembers().containsKey(targetInvite.getUniqueId())) {
                    String alreadyInvitedMsg = this.plugin.getConfig().getString("messages.commands.invite.already-invited").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyInvitedMsg));
                    return true;
                }
                housingManager.invitePlayer(playerHousing.getId(), targetInvite);
                String inviteSentMsg = this.plugin.getConfig().getString("messages.commands.invite.invite-sent").replace("%player%", targetInvite.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteSentMsg));
                String inviteReceivedMsg = this.plugin.getConfig().getString("messages.commands.invite.invite-received").replace("%housing%", playerHousing.getName());
                targetInvite.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteReceivedMsg));
                return true;
            case "join":
                if (!player.hasPermission("housing.join")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (args.length != 2) {
                    String joinUsageMsg = this.plugin.getConfig().getString("messages.commands.join.join-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinUsageMsg));
                    return true;
                }
                Player targetJoinPlayer = Bukkit.getPlayer(args[1]);
                OfflinePlayer targetJoin;
                if (targetJoinPlayer != null) {
                    targetJoin = targetJoinPlayer;
                } else {
                    @SuppressWarnings("deprecation")
                    OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);
                    targetJoin = offlineTarget;
                }
                HousingManager.Housing targetHousing = housingManager.findHousingByOwner(targetJoin);
                if (targetHousing == null) {
                    String housingNotFoundMsg = this.plugin.getConfig().getString("messages.commands.permissions.housing-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingNotFoundMsg));
                    return true;
                }
                player.teleport(targetHousing.getCenter());
                String joinedMsg = this.plugin.getConfig().getString("messages.commands.join.joined-housing").replace("%player%", args[1]);
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinedMsg));
                return true;
            case "settings":
                if (!player.hasPermission("housing.settings")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                new HousingSettingsGUI(plugin, playerHousing).open(player);
                return true;
            case "list":
                if (!player.hasPermission("housing.list")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                housingListGUI.open(player);
                return true;
            case "gamemode":
            case "gm":
                if (!player.hasPermission("housing.gamemode")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                if (args.length < 2 || args.length > 3) {
                    String gamemodeUsageMsg = this.plugin.getConfig().getString("messages.commands.gamemode.gamemode-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeUsageMsg));
                    return true;
                }
                String mode = args[1].toLowerCase();
                Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : player;
                if (target == null && args.length > 2) {
                    String playerNotFound = this.plugin.getConfig().getString("messages.commands.permissions.player-not-found").replace("%player%", args[2]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFound));
                    return true;
                }
                switch (mode) {
                    case "creative":
                    case "c":
                        target.setGameMode(GameMode.CREATIVE);
                        String gamemodeCreative = this.plugin.getConfig().getString("messages.commands.gamemode.gamemode-creative").replace("%player%", target.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeCreative));
                        break;
                    case "adventure":
                    case "a":
                        target.setGameMode(GameMode.ADVENTURE);
                        String gamemodeAdventure = this.plugin.getConfig().getString("messages.commands.gamemode.gamemode-adventure").replace("%player%", target.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeAdventure));
                        break;
                    case "survival":
                    case "s":
                        target.setGameMode(GameMode.SURVIVAL);
                        String gamemodeSurvival = this.plugin.getConfig().getString("messages.commands.gamemode.gamemode-survival").replace("%player%", target.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeSurvival));
                        break;
                    default:
                        String gamemodeUsageMsgInvalid = this.plugin.getConfig().getString("messages.commands.gamemode-usage-invalid");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeUsageMsgInvalid));
                        return true;
                }
                return true;
            case "time":
                if (!player.hasPermission("housing.time")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                if (args.length != 2) {
                    String timeUsageMsg = this.plugin.getConfig().getString("messages.commands.time.time-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', timeUsageMsg));
                    return true;
                }
                World housingWorld = Bukkit.getWorld(playerHousing.getId());
                switch (args[1].toLowerCase()) {
                    case "day":
                        housingWorld.setTime(1000);
                        String timeDay = this.plugin.getConfig().getString("messages.commands.time.time-day");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', timeDay));
                        break;
                    case "night":
                        housingWorld.setTime(13000);
                        String timeNight = this.plugin.getConfig().getString("messages.commands.time.time-night");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', timeNight));
                        break;
                    case "dawn":
                        housingWorld.setTime(23000);
                        String timeDawn = this.plugin.getConfig().getString("messages.commands.time.time-dawn");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', timeDawn));
                        break;
                    case "noon":
                        housingWorld.setTime(6000);
                        String timeNoon = this.plugin.getConfig().getString("messages.commands.time.time-noon");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', timeNoon));
                        break;
                    default:
                        String timeUsageMsgInvalid = this.plugin.getConfig().getString("messages.commands.time-usage");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', timeUsageMsgInvalid));
                        return true;
                }
                return true;
            case "kick":
                if (!player.hasPermission("housing.kick")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (args.length < 2) {
                    String kickUsageMsg = this.plugin.getConfig().getString("messages.commands.kick.kick-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', kickUsageMsg));
                    return true;
                }
                Player targetKick = Bukkit.getPlayer(args[1]);
                if (targetKick == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.commands.permissions.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                if (targetKick.equals(player)) {
                    String cannotKickSelfMsg = this.plugin.getConfig().getString("messages.commands.kick.cannot-kick-self");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', cannotKickSelfMsg));
                    return true;
                }
                if (!targetKick.getWorld().getName().equals(playerHousing.getId())) {
                    String playerNotInHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.player-not-in-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotInHousingMsg));
                    return true;
                }
                Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
                targetKick.teleport(spawnLocation);

                String kickedMsg = this.plugin.getConfig().getString("messages.commands.kick.player-kicked").replace("%player%", targetKick.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', kickedMsg));

                if (args.length > 2) {
                    StringBuilder reason = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        reason.append(args[i]).append(" ");
                    }
                    String youWereKickedMsg = this.plugin.getConfig().getString("messages.commands.kick.you-were-kicked-reason")
                            .replace("%housing%", playerHousing.getName())
                            .replace("%reason%", reason.toString().trim());
                    targetKick.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', youWereKickedMsg));
                } else {
                    String youWereKickedMsg = this.plugin.getConfig().getString("messages.commands.kick.you-were-kicked").replace("%housing%", playerHousing.getName());
                    targetKick.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', youWereKickedMsg));
                }
                return true;
            case "ban":
                if (!player.hasPermission("housing.ban")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (args.length < 2) {
                    String banUsageMsg = this.plugin.getConfig().getString("messages.commands.ban.ban-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', banUsageMsg));
                    return true;
                }
                Player targetBanPlayer = Bukkit.getPlayer(args[1]);
                OfflinePlayer targetBan;
                if (targetBanPlayer != null) {
                    targetBan = targetBanPlayer;
                } else {
                    @SuppressWarnings("deprecation")
                    OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);
                    targetBan = offlineTarget;
                }
                if (targetBan.getUniqueId().equals(player.getUniqueId())) {
                    String cannotBanSelfMsg = this.plugin.getConfig().getString("messages.commands.ban.cannot-ban-self");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', cannotBanSelfMsg));
                    return true;
                }
                if (playerHousing.isBanned(targetBan.getUniqueId())) {
                    String alreadyBannedMsg = this.plugin.getConfig().getString("messages.commands.ban.already-banned").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyBannedMsg));
                    return true;
                }
                playerHousing.banPlayer(targetBan.getUniqueId());
                playerHousing.removeMember(targetBan.getUniqueId());
                housingManager.saveHousings();

                String bannedMsg = this.plugin.getConfig().getString("messages.commands.ban.player-banned").replace("%player%", args[1]);
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', bannedMsg));

                if (targetBan.isOnline()) {
                    Player onlineTarget = targetBan.getPlayer();
                    if (onlineTarget.getWorld().getName().equals(playerHousing.getId())) {
                        Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
                        onlineTarget.teleport(spawn);
                    }

                    if (args.length > 2) {
                        StringBuilder reason = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            reason.append(args[i]).append(" ");
                        }
                        String youWereBannedMsg = this.plugin.getConfig().getString("messages.commands.ban.you-were-banned-reason")
                                .replace("%housing%", playerHousing.getName())
                                .replace("%reason%", reason.toString().trim());
                        onlineTarget.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', youWereBannedMsg));
                    } else {
                        String youWereBannedMsg = this.plugin.getConfig().getString("messages.commands.ban.you-were-banned").replace("%housing%", playerHousing.getName());
                        onlineTarget.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', youWereBannedMsg));
                    }
                }
                return true;
            case "unban":
                if (!player.hasPermission("housing.unban")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.commands.permissions.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (args.length != 2) {
                    String unbanUsageMsg = this.plugin.getConfig().getString("messages.commands.unban.unban-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', unbanUsageMsg));
                    return true;
                }
                Player targetUnbanPlayer = Bukkit.getPlayer(args[1]);
                OfflinePlayer targetUnban;
                if (targetUnbanPlayer != null) {
                    targetUnban = targetUnbanPlayer;
                } else {
                    @SuppressWarnings("deprecation")
                    OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);
                    targetUnban = offlineTarget;
                }
                if (!playerHousing.isBanned(targetUnban.getUniqueId())) {
                    String playerNotBannedMsg = this.plugin.getConfig().getString("messages.commands.unban.player-not-banned").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotBannedMsg));
                    return true;
                }
                playerHousing.unbanPlayer(targetUnban.getUniqueId());
                housingManager.saveHousings();

                String unbannedMsg = this.plugin.getConfig().getString("messages.commands.unban.player-unbanned").replace("%player%", args[1]);
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', unbannedMsg));
                return true;
            default:
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7ʜᴇʟᴘ ᴍᴇɴᴜ:"));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing create <ɴᴀᴍᴇ> - ᴄʀᴇᴀᴛᴇꜱ ᴀ ɴᴇᴡ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing delete - ᴅᴇʟᴇᴛᴇꜱ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing invite <ᴘʟᴀʏᴇʀ> - ɪɴᴠɪᴛᴇꜱ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing join <ᴘʟᴀʏᴇʀ> - ᴊᴏɪɴꜱ ᴀ ᴘʟᴀʏᴇʀ'ꜱ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing settings - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing permissions - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing list - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing kick <ᴘʟᴀʏᴇʀ> [ʀᴇᴀꜱᴏɴ] - ᴋɪᴄᴋ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing ban <ᴘʟᴀʏᴇʀ> [ʀᴇᴀꜱᴏɴ] - ʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing unban <ᴘʟᴀʏᴇʀ> - ᴜɴʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing gamemode <ᴄʀᴇᴀᴛɪᴠᴇ|ᴀᴅᴠᴇɴᴛᴜʀᴇ|ꜱᴜʀᴠɪᴠᴀʟ> [ᴘʟᴀʏᴇʀ] - ꜱᴇᴛ ɢᴀᴍᴇᴍᴏᴅᴇ ꜰᴏʀ ʏᴏᴜ ᴏʀ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing time <ᴅᴀʏ|ɴɪɢʜᴛ|ᴅᴀᴡɴ|ɴᴏᴏɴ> - ꜱᴇᴛ ᴡᴏʀʟᴅ ᴛɪᴍᴇ."));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("housing.use")) {
                completions.add("create");
                completions.add("invite");
                completions.add("join");
                completions.add("settings");
                completions.add("permissions");
                completions.add("list");
                completions.add("delete");
                completions.add("kick");
                completions.add("ban");
                completions.add("unban");
                completions.add("gamemode");
                completions.add("gm");
                completions.add("time");
            }
            if (sender.hasPermission("housing.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("housing.help")) {
                completions.add("help");
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (!(sender instanceof Player p)) {
                return new ArrayList<>();
            }
            if (args[0].equalsIgnoreCase("invite") && sender.hasPermission("housing.use")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("join") && sender.hasPermission("housing.use")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if ((args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban")) && sender.hasPermission("housing.use")) {
                HousingManager.Housing h = housingManager.findHousingByOwner(p);
                if (h == null) {
                    return new ArrayList<>();
                }
                List<String> suggestions = new ArrayList<>();
                if (args[0].equalsIgnoreCase("unban")) {
                    for (UUID bannedId : h.getBannedPlayers()) {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(bannedId);
                        String name = op.getName();
                        if (name != null && name.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(name);
                        }
                    }
                } else {
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(pl -> h.getMembers().containsKey(pl.getUniqueId()))
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                return suggestions;
            } else if ((args[0].equalsIgnoreCase("gamemode") || args[0].equalsIgnoreCase("gm")) && sender.hasPermission("housing.gamemode")) {
                List<String> modes = Arrays.asList("creative", "c", "adventure", "a", "survival", "s");
                return modes.stream()
                        .filter(m -> m.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("time") && sender.hasPermission("housing.time")) {
                List<String> times = Arrays.asList("day", "night", "dawn", "noon");
                return times.stream()
                        .filter(t -> t.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("create") && sender.hasPermission("housing.create")) {
                return new ArrayList<>();
            }
        } else if (args.length > 2) {
            if ((args[0].equalsIgnoreCase("gamemode") || args[0].equalsIgnoreCase("gm")) && sender.hasPermission("housing.gamemode")) {
                if (!(sender instanceof Player)) {
                    return new ArrayList<>();
                }
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if ((args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("ban")) && sender.hasPermission("housing.use")) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}
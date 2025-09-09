package dev.meyba.justHousing.commands;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.guis.HousingListGUI;
import dev.meyba.justHousing.guis.HousingPermissionsGUI;
import dev.meyba.justHousing.guis.HousingSettingsGUI;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
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
            String notAPlayerMsg = this.plugin.getConfig().getString("messages.not-a-player");
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notAPlayerMsg));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("housing.list")) {
                String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                return true;
            }
            housingListGUI.open(player);
            return true;
        }

        if (!player.hasPermission("housing.use")) {
            String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
            return true;
        }

        HousingManager.Housing playerHousing = housingManager.findHousingByOwner(player);
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!player.hasPermission("housing.reload")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                this.plugin.reloadConfig();
                plugin.getServer().getOnlinePlayers().forEach(plugin.getScoreboardManager()::updateScoreboard);
                String reloadSuccessMsg = this.plugin.getConfig().getString("messages.reload-success");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', reloadSuccessMsg));
                return true;
            case "help":
                if (!player.hasPermission("housing.help")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "ʜᴇʟᴘ ᴍᴇɴᴜ:"));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing create <ɴᴀᴍᴇ> - ᴄʀᴇᴀᴛᴇꜱ ᴀ ɴᴇᴡ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing delete - ᴅᴇʟᴇᴛᴇꜱ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing invite <ᴘʟᴀʏᴇʀ> - ɪɴᴠɪᴛᴇꜱ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing join <ᴘʟᴀʏᴇʀ> - ᴊᴏɪɴꜱ ᴀ ᴘʟᴀʏᴇʀ'ꜱ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing settings - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing permissions - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing list - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing kick <ᴘʟᴀʏᴇʀ> [reason] - ᴋɪᴄᴋ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing ban <ᴘʟᴀʏᴇʀ> [reason] - ʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing unban <ᴘʟᴀʏᴇʀ> - ᴜɴʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing gamemode <creative|adventure|survival> [ᴘʟᴀʏᴇʀ] - Set gamemode for you or a player."));
                return true;
            case "create":
                if (!player.hasPermission("housing.create")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (args.length != 2) {
                    String createUsageMsg = this.plugin.getConfig().getString("messages.create-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', createUsageMsg));
                    return true;
                }
                if (playerHousing != null) {
                    String alreadyHasHousingMsg = this.plugin.getConfig().getString("messages.already-has-housing", "&cʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʜᴀᴠᴇ ᴀ ʜᴏᴜꜱɪɴɢ!");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyHasHousingMsg));
                    return true;
                }
                housingManager.createHousing(player, args[1]);
                String createdMsg = this.plugin.getConfig().getString("messages.housing-created").replace("%name%", args[1]);
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', createdMsg));
                return true;
            case "delete":
                if (!player.hasPermission("housing.delete")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingToDeleteMsg = this.plugin.getConfig().getString("messages.no-housing-to-delete");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingToDeleteMsg));
                    return true;
                }
                housingManager.deleteHousing(player);
                String housingDeletedMsg = this.plugin.getConfig().getString("messages.housing-deleted");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingDeletedMsg));
                return true;
            case "invite":
                if (!player.hasPermission("housing.invite")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (args.length != 2) {
                    String inviteUsageMsg = this.plugin.getConfig().getString("messages.invite-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteUsageMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                Player targetInvite = Bukkit.getPlayer(args[1]);
                if (targetInvite == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                if (targetInvite == player) {
                    String cannotInviteSelfMsg = this.plugin.getConfig().getString("messages.cannot-invite-self");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', cannotInviteSelfMsg));
                    return true;
                }
                if (playerHousing.getMembers().containsKey(targetInvite.getUniqueId())) {
                    String alreadyInvitedMsg = this.plugin.getConfig().getString("messages.already-invited").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyInvitedMsg));
                    return true;
                }
                housingManager.invitePlayer(playerHousing.getId(), targetInvite);
                String inviteSentMsg = this.plugin.getConfig().getString("messages.invite-sent").replace("%player%", targetInvite.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteSentMsg));
                String inviteReceivedMsg = this.plugin.getConfig().getString("messages.invite-received").replace("%housing%", playerHousing.getName());
                targetInvite.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteReceivedMsg));
                return true;
            case "join":
                if (!player.hasPermission("housing.join")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (args.length != 2) {
                    String joinUsageMsg = this.plugin.getConfig().getString("messages.join-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinUsageMsg));
                    return true;
                }
                OfflinePlayer targetJoin = Bukkit.getOfflinePlayer(args[1]);
                HousingManager.Housing targetHousing = housingManager.findHousingByOwner(targetJoin);
                if (targetHousing == null) {
                    String housingNotFoundMsg = this.plugin.getConfig().getString("messages.housing-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingNotFoundMsg));
                    return true;
                }
                player.teleport(targetHousing.getCenter());
                String joinedMsg = this.plugin.getConfig().getString("messages.joined-housing").replace("%player%", targetJoin.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinedMsg));
                return true;
            case "settings":
                if (!player.hasPermission("housing.settings")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                new HousingSettingsGUI(plugin, playerHousing).open(player);
                return true;
            case "permissions":
                if (!player.hasPermission("housing.permissions")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                new HousingPermissionsGUI(plugin, playerHousing).open(player);
                return true;
            case "list":
                if (!player.hasPermission("housing.list")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                housingListGUI.open(player);
                return true;
            case "kick":
                if (!player.hasPermission("housing.kick")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                if (args.length < 2) {
                    String kickUsageMsg = this.plugin.getConfig().getString("messages.kick-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', kickUsageMsg));
                    return true;
                }
                Player targetKick = Bukkit.getPlayer(args[1]);
                if (targetKick == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                if (!playerHousing.getMembers().containsKey(targetKick.getUniqueId())) {
                    String notMemberMsg = this.plugin.getConfig().getString("messages.not-member");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notMemberMsg));
                    return true;
                }
                String reasonKick = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
                playerHousing.removeMember(targetKick.getUniqueId());
                targetKick.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                String kickedMsg = this.plugin.getConfig().getString("messages.player-kicked").replace("%player%", targetKick.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', kickedMsg));
                String bannedFromHousingMsg = this.plugin.getConfig().getString("messages.kicked").replace("%housing%", playerHousing.getName()).replace("%reason%", reasonKick);
                targetKick.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', bannedFromHousingMsg));
                housingManager.saveHousings();
                return true;
            case "ban":
                if (!player.hasPermission("housing.ban")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                if (args.length < 2) {
                    String banUsageMsg = this.plugin.getConfig().getString("messages.ban-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', banUsageMsg));
                    return true;
                }
                Player targetBan = Bukkit.getPlayer(args[1]);
                if (targetBan == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                if (playerHousing.isBanned(targetBan.getUniqueId())) {
                    String alreadyBannedMsg = this.plugin.getConfig().getString("messages.already-banned");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyBannedMsg));
                    return true;
                }
                String reasonBan = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
                playerHousing.banPlayer(targetBan.getUniqueId());
                if (targetBan.getWorld().getName().equals(playerHousing.getId())) {
                    targetBan.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    String bannedMsg = this.plugin.getConfig().getString("messages.banned").replace("%housing%", playerHousing.getName()).replace("%reason%", reasonBan);
                    targetBan.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', bannedMsg));
                }
                String playerBannedMsg = this.plugin.getConfig().getString("messages.player-banned").replace("%player%", targetBan.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerBannedMsg));
                housingManager.saveHousings();
                return true;
            case "unban":
                if (!player.hasPermission("housing.unban")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                if (args.length != 2) {
                    String unbanUsageMsg = this.plugin.getConfig().getString("messages.unban-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', unbanUsageMsg));
                    return true;
                }
                Player targetUnban = Bukkit.getPlayer(args[1]);
                if (targetUnban == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                if (!playerHousing.isBanned(targetUnban.getUniqueId())) {
                    String notBannedMsg = this.plugin.getConfig().getString("messages.not-banned");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notBannedMsg));
                    return true;
                }
                playerHousing.unbanPlayer(targetUnban.getUniqueId());
                String playerUnbannedMsg = this.plugin.getConfig().getString("messages.player-unbanned").replace("%player%", targetUnban.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerUnbannedMsg));
                housingManager.saveHousings();
                return true;
            case "gamemode":
                if (!player.hasPermission("housing.gamemode")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (playerHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                if (!player.getWorld().getName().equals(playerHousing.getId())) {
                    String notInHousingMsg = this.plugin.getConfig().getString("messages.not-in-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notInHousingMsg));
                    return true;
                }
                if (args.length < 2 || args.length > 3) {
                    String gamemodeUsageMsg = this.plugin.getConfig().getString("messages.gamemode-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeUsageMsg));
                    return true;
                }
                String mode = args[1].toLowerCase();
                Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : player;
                if (target == null && args.length > 2) {
                    String playerNotFound = this.plugin.getConfig().getString("messages.player-not-found").replace("%player%", args[2]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFound));
                    return true;
                }
                switch (mode) {
                    case "creative":
                    case "c":
                        target.setGameMode(GameMode.CREATIVE);
                        String gamemodeCreative = this.plugin.getConfig().getString("messages.gamemode-creative").replace("%player%", target.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeCreative));
                        break;
                    case "adventure":
                    case "a":
                        target.setGameMode(GameMode.ADVENTURE);
                        String gamemodeAdventure = this.plugin.getConfig().getString("messages.gamemode-adventure").replace("%player%", target.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeAdventure));
                        break;
                    case "survival":
                    case "s":
                        target.setGameMode(GameMode.SURVIVAL);
                        String gamemodeSurvival = this.plugin.getConfig().getString("messages.gamemode-survival").replace("%player%", target.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeSurvival));
                        break;
                    default:
                        String gamemodeUsageMsgInvalid = this.plugin.getConfig().getString("messages.gamemode-usage-invalid");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', gamemodeUsageMsgInvalid));
                        return true;
                }
                return true;
            default:
                String usageMsg = this.plugin.getConfig().getString("messages.usage");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', usageMsg));
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
            if (!(sender instanceof Player)) {
                return new ArrayList<>();
            }
            Player p = (Player) sender;
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
            } else if (args[0].equalsIgnoreCase("gamemode") && sender.hasPermission("housing.gamemode")) {
                List<String> modes = Arrays.asList("creative", "c", "adventure", "a", "survival", "s");
                return modes.stream()
                        .filter(m -> m.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("create") && sender.hasPermission("housing.create")) {
                return new ArrayList<>();
            }
        } else if (args.length > 2) {
            if (args[0].equalsIgnoreCase("gamemode") && sender.hasPermission("housing.gamemode")) {
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
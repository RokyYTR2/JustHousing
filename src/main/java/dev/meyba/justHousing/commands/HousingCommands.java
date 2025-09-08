package dev.meyba.justHousing.commands;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.guis.HousingListGUI;
import dev.meyba.justHousing.guis.HousingPermissionsGUI;
import dev.meyba.justHousing.guis.HousingSettingsGUI;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing create <name> - ᴄʀᴇᴀᴛᴇꜱ ᴀ ɴᴇᴡ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing delete - ᴅᴇʟᴇᴛᴇꜱ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing invite <player> - ɪɴᴠɪᴛᴇꜱ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing join <player> - ᴊᴏɪɴꜱ ᴀ ᴘʟᴀʏᴇʀ'ꜱ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing settings - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing permissions - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing list - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing kick <player> [reason] - ᴋɪᴄᴋ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing ban <player> [reason] - ʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing unban <player> - ᴜɴʙᴀɴ ᴀ ᴘʟᴀʏᴇʀ"));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&7/housing reload - ʀᴇʟᴏᴀᴅ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ"));
                return true;
            case "create":
                if (args.length != 2) {
                    String createUsageMsg = this.plugin.getConfig().getString("messages.create-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', createUsageMsg));
                    return true;
                }
                housingManager.createHousing(player, args[1]);
                String housingCreatedMsg = this.plugin.getConfig().getString("messages.housing-created");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingCreatedMsg));
                return true;
            case "delete":
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
                if (args.length != 2) {
                    String inviteUsageMsg = this.plugin.getConfig().getString("messages.invite-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteUsageMsg));
                    return true;
                }
                if (args[1].equals(player.getName())) {
                    String playerYouMsg = this.plugin.getConfig().getString("messages.player-you");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerYouMsg));
                    return true;
                }
                Player targetInvite = plugin.getServer().getPlayer(args[1]);
                if (targetInvite != null) {
                    if (playerHousing.getMembers().containsKey(targetInvite.getUniqueId())) {
                        String alreadyInvitedMsg = this.plugin.getConfig().getString("messages.already-invited").replace("%player%", args[1]);
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyInvitedMsg));
                        return true;
                    }
                    housingManager.invitePlayer(playerHousing.getId(), targetInvite);
                    String inviteSentMsg = this.plugin.getConfig().getString("messages.invite-sent").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteSentMsg));
                    String inviteReceivedMsg = this.plugin.getConfig().getString("messages.invite-received");
                    targetInvite.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', inviteReceivedMsg));
                } else {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                }
                return true;
            case "join":
                if (args.length != 2) {
                    String joinUsageMsg = this.plugin.getConfig().getString("messages.join-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinUsageMsg));
                    return true;
                }
                Player targetJoin = plugin.getServer().getPlayer(args[1]);
                if (targetJoin != null) {
                    HousingManager.Housing targetHousing = housingManager.findHousingByOwner(targetJoin);
                    if (targetHousing != null && playerHousing.getMembers().containsKey(player.getUniqueId())) {
                        player.teleport(targetHousing.getCenter());
                        String joinedHousingMsg = this.plugin.getConfig().getString("messages.joined-housing").replace("%player%", targetJoin.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinedHousingMsg));
                    } else {
                        String housingNotFoundMsg = this.plugin.getConfig().getString("messages.housing-not-found").replace("%player%", targetJoin.getName());
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingNotFoundMsg));
                    }
                } else {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found").replace("%player%", args[1]);
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                }
                return true;
            case "settings":
                new HousingSettingsGUI(plugin, playerHousing).open(player);
                return true;
            case "permissions":
                new HousingPermissionsGUI(plugin, playerHousing).open(player);
                return true;
            case "list":
                housingListGUI.open(player);
                return true;
            case "kick":
                if (args.length < 2) {
                    String kickUsageMsg = this.plugin.getConfig().getString("messages.kick-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', kickUsageMsg));
                    return true;
                }
                Player targetKick = plugin.getServer().getPlayer(args[1]);
                if (targetKick == null || !playerHousing.getMembers().containsKey(targetKick.getUniqueId())) {
                    String playerNotInHousingMsg = this.plugin.getConfig().getString("messages.player-not-in-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotInHousingMsg));
                    return true;
                }
                String reasonKick = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "ɴᴏ ʀᴇᴀꜱᴏɴ";
                playerHousing.removeMember(targetKick.getUniqueId());
                targetKick.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                String kickedMsg = this.plugin.getConfig().getString("messages.kicked").replace("%housing%", playerHousing.getName()).replace("%reason%", reasonKick);
                targetKick.sendMessage(ChatColor.RED + kickedMsg);
                String playerKickedMsg = this.plugin.getConfig().getString("messages.player-kicked").replace("%player%", targetKick.getName());
                player.sendMessage(ChatColor.GREEN + playerKickedMsg);
                housingManager.saveHousings();
                return true;
            case "ban":
                if (args.length < 2) {
                    String banUsageMsg = this.plugin.getConfig().getString("messages.ban-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', banUsageMsg));
                    return true;
                }
                Player targetBan = plugin.getServer().getPlayer(args[1]);
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
                String reasonBan = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "ɴᴏ ʀᴇᴀꜱᴏɴ";
                playerHousing.banPlayer(targetBan.getUniqueId());
                if (playerHousing.getMembers().containsKey(targetBan.getUniqueId())) {
                    playerHousing.removeMember(targetBan.getUniqueId());
                }
                if (targetBan.getWorld().getName().equals(playerHousing.getId())) {
                    targetBan.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    String bannedMsg = this.plugin.getConfig().getString("messages.banned").replace("%housing%", playerHousing.getName()).replace("%reason%", reasonBan);
                    targetBan.sendMessage(ChatColor.RED + bannedMsg);
                }
                String playerBannedMsg = this.plugin.getConfig().getString("messages.player-banned").replace("%player%", targetBan.getName());
                player.sendMessage(ChatColor.GREEN + playerBannedMsg);
                housingManager.saveHousings();
                return true;
            case "unban":
                if (args.length != 2) {
                    String unbanUsageMsg = this.plugin.getConfig().getString("messages.unban-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', unbanUsageMsg));
                    return true;
                }
                Player targetUnban = plugin.getServer().getPlayer(args[1]);
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
                player.sendMessage(ChatColor.GREEN + playerUnbannedMsg);
                housingManager.saveHousings();
                return true;
        }
        String usageMsg = this.plugin.getConfig().getString("messages.usage");
        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', usageMsg));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
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
            }
            if (sender.hasPermission("housing.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("housing.help")) {
                completions.add("help");
            }
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("invite") && sender.hasPermission("housing.use")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("join") && sender.hasPermission("housing.use")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length >= 2 && (args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban")) && sender.hasPermission("housing.use")) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> {
                        HousingManager.Housing h = housingManager.findHousingByOwner(p);
                        return h != null && h.getId().equals(((Player) sender).getWorld().getName());
                    })
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
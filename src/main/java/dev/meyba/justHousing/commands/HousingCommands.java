package dev.meyba.justHousing.commands;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.guis.HousingListGUI;
import dev.meyba.justHousing.guis.HousingPermissionsGUI;
import dev.meyba.justHousing.guis.HousingSettingsGUI;
import dev.meyba.justHousing.managers.HousingManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!player.hasPermission("housing.reload")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                this.plugin.reloadConfig();
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
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing create [name] - ᴄʀᴇᴀᴛᴇꜱ ᴀ ɴᴇᴡ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing invite <player> - ɪɴᴠɪᴛᴇꜱ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing join <player> - ᴊᴏɪɴꜱ ᴀ ᴘʟᴀʏᴇʀ'ꜱ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing settings - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing permissions - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing list - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing delete - ᴅᴇʟᴇᴛᴇꜱ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                return true;
            case "create":
                if (!player.hasPermission("housing.create")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (housingManager.findHousingByOwner(player) != null) {
                    String alreadyOwnMsg = this.plugin.getConfig().getString("messages.already-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyOwnMsg));
                    return true;
                }
                String housingName = args.length > 1 ? String.join(" ", args).substring(args[0].length() + 1) : player.getName() + "'s Housing";
                housingManager.createHousing(player, housingName);
                player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.housing-created")), 1, 1);
                String createdMsg = this.plugin.getConfig().getString("messages.housing-created");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', createdMsg));
                return true;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&cᴜꜱᴀɢᴇ: /housing invite <player>"));
                    return true;
                }
                if (housingManager.findHousingByOwner(player) == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    String notFoundMsg = this.plugin.getConfig().getString("messages.player-not-found");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notFoundMsg));
                    return true;
                }
                if (player.getUniqueId().equals(target.getUniqueId())) {
                    String cantInviteSelfMsg = this.plugin.getConfig().getString("messages.player-you");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', cantInviteSelfMsg));
                    return true;
                }
                if (housingManager.getHousingById(housingManager.findHousingByOwner(player).getId()).getMembers().containsKey(target.getUniqueId())) {
                    String alreadyInvitedMsg = this.plugin.getConfig().getString("messages.already-invited").replace("%player%", target.getName());
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyInvitedMsg));
                    return true;
                }
                housingManager.invitePlayer(housingManager.findHousingByOwner(player).getId(), target);
                String invitedMsg = this.plugin.getConfig().getString("messages.player-invited").replace("%player%", target.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', invitedMsg));

                String inviteReceivedMsg = this.plugin.getConfig().getString("messages.invite-received").replace("%player%", player.getName());
                TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', prefix + inviteReceivedMsg));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/housing join " + player.getName()));
                target.spigot().sendMessage(ChatMessageType.CHAT, message);
                target.playSound(target.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.invite-received")), 1, 1);
                return true;
            case "join":
                if (!player.hasPermission("housing.use")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (args.length < 2) {
                    String usageMsg = this.plugin.getConfig().getString("messages.join-usage");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', usageMsg));
                    return true;
                }
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    String notFoundMsg = this.plugin.getConfig().getString("messages.player-not-found");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notFoundMsg));
                    return true;
                }
                HousingManager.Housing housingToJoin = this.housingManager.findHousingByOwner(targetPlayer);
                if (housingToJoin == null) {
                    String notFoundMsg = this.plugin.getConfig().getString("messages.housing-not-found").replace("%player%", targetPlayer.getName());
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notFoundMsg));
                    return true;
                }
                player.teleport(housingToJoin.getCenter());
                String joinedMsg = this.plugin.getConfig().getString("messages.joined-housing").replace("%player%", targetPlayer.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinedMsg));
                return true;
            case "settings":
                if (!player.hasPermission("housing.settings")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (housingManager.findHousingByOwner(player) == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                HousingSettingsGUI settingsGUI = new HousingSettingsGUI(plugin, housingManager.findHousingByOwner(player));
                settingsGUI.open(player);
                return true;
            case "permissions":
                if (!player.hasPermission("housing.permissions")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                HousingManager.Housing housing = housingManager.findHousingByOwner(player);
                if (housing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                HousingPermissionsGUI permissionsGUI = new HousingPermissionsGUI(plugin, housing);
                permissionsGUI.open(player);
                return true;
            case "list":
                if (!player.hasPermission("housing.list")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                housingListGUI.open(player);
                return true;
            case "delete":
                if (!player.hasPermission("housing.delete")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                if (housingManager.findHousingByOwner(player) == null) {
                    String noHousingToDeleteMsg = this.plugin.getConfig().getString("messages.no-housing-to-delete");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingToDeleteMsg));
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
                    if (!player.hasPermission("housing.deleteconfirm")) {
                        String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                        return true;
                    }
                    housingManager.deleteHousing(player);
                    String deletedMsg = this.plugin.getConfig().getString("messages.housing-deleted");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', deletedMsg));
                } else {
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "ᴅᴏ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ᴅᴇʟᴇᴛᴇ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ?"));
                    TextComponent confirmMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&aᴄʟɪᴄᴋ ᴛᴏ ᴄᴏɴꜰɪʀᴍ"));
                    confirmMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/housing delete confirm"));
                    player.spigot().sendMessage(ChatMessageType.CHAT, confirmMessage);
                }
                return true;
//            case "kick":
//                Player kickTarget = plugin.getServer().getPlayer(args[1]);
//                if (kickTarget == null || !housingManager.containsKey(kickTarget.getUniqueId())) {
//                    player.sendMessage(ChatColor.RED + "Hráč není ve vašem housingu!");
//                    return true;
//                }
//                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
//                housingManager.removeMember(kickTarget.getUniqueId());
//                kickTarget.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
//                kickTarget.sendMessage(ChatColor.RED + "Byl jste vyhozen z " + housingManager.getName() + "! Důvod: " + reason);
//                player.sendMessage(ChatColor.GREEN + "Hráč " + kickTarget.getName() + " byl vyhozen!");
//                housingManager.saveHousings();
//                return true;
//            case "ban":
//                Player banTarget = plugin.getServer().getPlayer(args[1]);
//                if (banTarget == null) {
//                    player.sendMessage(ChatColor.RED + "Hráč nenalezen!");
//                    return true;
//                }
//                if (housingManager.isBanned(banTarget.getUniqueId())) {
//                    player.sendMessage(ChatColor.RED + "Hráč již je zabanován!");
//                    return true;
//                }
//                String banReason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
//                housingManager.banPlayer(banTarget.getUniqueId());
//                if (housingManager.getMembers().containsKey(banTarget.getUniqueId())) {
//                    housingManager.removeMember(banTarget.getUniqueId());
//                }
//                if (banTarget.getWorld().getName().equals(housingManager.getId())) {
//                    banTarget.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
//                    banTarget.sendMessage(ChatColor.RED + "Byl jste zabanován z " + housingManager.getName() + "! Důvod: " + banReason);
//                }
//                player.sendMessage(ChatColor.GREEN + "Hráč " + banTarget.getName() + " byl zabanován!");
//                housingManager.saveHousings();
//                return true;
//            case "unban":
//                Player unbanTarget = plugin.getServer().getPlayer(args[1]);
//                if (unbanTarget == null) {
//                    player.sendMessage(ChatColor.RED + "Hráč nenalezen!");
//                    return true;
//                }
//                if (!housingManager.isBanned(unbanTarget.getUniqueId())) {
//                    player.sendMessage(ChatColor.RED + "Hráč není zabanován!");
//                    return true;
//                }
//                housingManager.unbanPlayer(unbanTarget.getUniqueId());
//                player.sendMessage(ChatColor.GREEN + "Hráč " + unbanTarget.getName() + " byl odbanován!");
//                housingManager.saveHousings();
//                return true;
        }
        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&cᴜꜱᴀɢᴇ: /housing help"));
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
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
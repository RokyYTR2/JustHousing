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
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing create - ᴄʀᴇᴀᴛᴇꜱ ᴀ ɴᴇᴡ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing invite <player> - ɪɴᴠɪᴛᴇꜱ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing join <player> - ᴊᴏɪɴꜱ ᴀ ᴘʟᴀʏᴇʀ'ꜱ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing settings - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing permissions - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing list - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ ᴍᴇɴᴜ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing delete - ᴅᴇʟᴇᴛᴇꜱ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing reload - ʀᴇʟᴏᴀᴅꜱ ᴛʜᴇ ᴄᴏɴꜰɪɢ."));
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing help - ꜱʜᴏᴡꜱ ᴛʜɪꜱ ᴍᴇɴᴜ."));
                return true;
            case "list":
                if (!player.hasPermission("housing.list")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                housingListGUI.open(player);
                return true;
            case "create":
                HousingManager.Housing existingHousing = housingManager.findHousingByOwner(player);
                if (existingHousing != null) {
                    String alreadyOwnHousingMsg = this.plugin.getConfig().getString("messages.already-own-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyOwnHousingMsg));
                    return true;
                }
                housingManager.createHousing(player);
                String housingCreatedMsg = this.plugin.getConfig().getString("messages.housing-created");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingCreatedMsg));
                String msgAction = this.plugin.getConfig().getString("actionbar.housing-created");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', prefix + msgAction)));
                String soundName = this.plugin.getConfig().getString("sounds.housing-created");
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    player.playSound(player.getLocation(), sound, 1f, 1f);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name in config: " + soundName);
                }
                return true;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&cᴜꜱᴀɢᴇ: /housing invite <player>"));
                    return true;
                }
                HousingManager.Housing housing = housingManager.findHousingByOwner(player);
                if (housing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                if (housing.getMembers().containsKey(target.getUniqueId())) {
                    String alreadyInvitedMsg = this.plugin.getConfig().getString("messages.already-invited").replace("%player%", target.getName());
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadyInvitedMsg));
                    return true;
                }
                housingManager.invitePlayer(housing.getId(), target);
                String playerInvitedMsg = this.plugin.getConfig().getString("messages.player-invited").replace("%player%", target.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerInvitedMsg));
                String invitedAction = this.plugin.getConfig().getString("actionbar.player-invited").replace("%player%", target.getName());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', prefix + invitedAction)));
                String invitedSound = this.plugin.getConfig().getString("sounds.player-invited");
                try {
                    Sound sound = Sound.valueOf(invitedSound.toUpperCase());
                    player.playSound(player.getLocation(), sound, 1f, 1f);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name in config: " + invitedSound);
                }
                String inviteReceivedMsg = this.plugin.getConfig().getString("messages.invite-received").replace("%player%", player.getName());
                String clickHereMsg = this.plugin.getConfig().getString("messages.click-here");
                TextComponent inviteComponent = new TextComponent();
                inviteComponent.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', prefix + inviteReceivedMsg)));
                TextComponent clickHere = new TextComponent(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', clickHereMsg));
                clickHere.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/housing join " + player.getName()));
                inviteComponent.addExtra(clickHere);
                target.spigot().sendMessage(inviteComponent);
                String inviteSoundName = this.plugin.getConfig().getString("sounds.invite-received");
                try {
                    Sound sound = Sound.valueOf(inviteSoundName.toUpperCase());
                    target.playSound(target.getLocation(), sound, 1f, 1f);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name in config: " + inviteSoundName);
                }
                return true;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&cᴜꜱᴀɢᴇ: /housing join <player>"));
                    return true;
                }
                Player owner = Bukkit.getPlayer(args[1]);
                if (owner == null) {
                    String playerNotFoundMsg = this.plugin.getConfig().getString("messages.player-not-found");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                    return true;
                }
                HousingManager.Housing housingToJoin = housingManager.findHousingByOwner(owner);
                if (housingToJoin == null) {
                    String housingNotFoundMsg = this.plugin.getConfig().getString("messages.housing-not-found").replace("%player%", owner.getName());
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingNotFoundMsg));
                    return true;
                }
                if (!housingToJoin.getOwner().equals(player.getUniqueId()) && !housingToJoin.getMembers().containsKey(player.getUniqueId())) {
                    String noPermissionJoinMsg = this.plugin.getConfig().getString("messages.no-permission-join");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionJoinMsg));
                    return true;
                }
                player.teleport(housingToJoin.getCenter());
                String joinedHousingMsg = this.plugin.getConfig().getString("messages.joined-housing").replace("%player%", owner.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', joinedHousingMsg));
                return true;
            case "settings":
                if (!player.hasPermission("housing.settings")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                HousingManager.Housing currentHousing = housingManager.findHousingByOwner(player);
                if (currentHousing == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                new HousingSettingsGUI(plugin, currentHousing).open(player);
                return true;
            case "permissions":
                if (!player.hasPermission("housing.permissions")) {
                    String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                HousingManager.Housing currentHousingPermissions = housingManager.findHousingByOwner(player);
                if (currentHousingPermissions == null) {
                    String noHousingMsg = this.plugin.getConfig().getString("messages.no-housing");
                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noHousingMsg));
                    return true;
                }
                new HousingPermissionsGUI(plugin, currentHousingPermissions).open(player);
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
                TextComponent deletePrompt = new TextComponent(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("messages.delete-confirm-prompt")));
                deletePrompt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/housing delete confirm"));
                player.spigot().sendMessage(deletePrompt);
                return true;
            case "deleteconfirm":
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
                housingManager.deleteHousing(player);
                String housingDeletedMsg = this.plugin.getConfig().getString("messages.housing-deleted");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', housingDeletedMsg));
                return true;
            default:
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&cᴜꜱᴀɢᴇ: /housing help"));
                return true;
        }
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
        } else if (args.length == 2 && args[0].equalsIgnoreCase("invite") && sender.hasPermission("housing.use")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("join") && sender.hasPermission("housing.use")) {
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
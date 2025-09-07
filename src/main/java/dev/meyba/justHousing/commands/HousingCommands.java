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

    public HousingCommands(HousingManager housingManager, JustHousing plugin) {
        this.housingManager = housingManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("prefix"));

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (!sender.hasPermission("housing.reload")) {
                        String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                        return true;
                    }
                    this.plugin.reloadConfig();
                    String successMsg = this.plugin.getConfig().getString("messages.reload-success");
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', successMsg));
                    return true;
                case "help":
                    if (!sender.hasPermission("housing.help")) {
                        String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                        return true;
                    }
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "ʜᴇʟᴘ ᴍᴇɴᴜ:"));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing create - ᴄʀᴇᴀᴛᴇꜱ ᴀ ɴᴇᴡ ʜᴏᴜꜱɪɴɢ."));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing invite <player> - ɪɴᴠɪᴛᴇꜱ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʏᴏᴜʀ ʜᴏᴜꜱɪɴɢ."));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing join <player> - ᴊᴏɪɴꜱ ᴀ ᴘʟᴀʏᴇʀ'ꜱ ʜᴏᴜꜱɪɴɢ."));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing settings - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ."));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing permissions - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ ᴍᴇɴᴜ."));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing list - ᴏᴘᴇɴꜱ ᴛʜᴇ ʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ ᴍᴇɴᴜ."));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing reload - ʀᴇʟᴏᴀᴅꜱ ᴛʜᴇ ᴄᴏɴꜰɪɢ."));
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "/housing help - ꜱʜᴏᴡꜱ ᴛʜɪꜱ ᴍᴇɴᴜ."));
                    return true;
                case "list":
                    if (!sender.hasPermission("housing.list")) {
                        String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        String notAPlayerMsg = this.plugin.getConfig().getString("messages.not-a-player");
                        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notAPlayerMsg));
                        return true;
                    }
                    new HousingListGUI(plugin, housingManager).open((Player) sender);
                    return true;
                default:
                    break;
            }
        }

        if (!(sender instanceof Player player)) {
            String msg = this.plugin.getConfig().getString("messages.not-a-player");
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        if (!player.hasPermission("housing.use")) {
            String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("create")) {
            HousingManager.Housing existingHousing = housingManager.findHousingByOwner(player);
            if (existingHousing != null) {
                String msg = this.plugin.getConfig().getString("messages.already-own-housing");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            housingManager.createHousing(player);
            String msg = this.plugin.getConfig().getString("messages.housing-created");
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));

            String msgAction = this.plugin.getConfig().getString("actionbar.housing-created");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', prefix + msgAction)));

            String soundName = this.plugin.getConfig().getString("sounds.housing-created");
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, 1f, 1f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config: " + soundName);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("invite") && args.length == 2) {
            HousingManager.Housing housing = housingManager.findHousingByOwner(player);
            if (housing == null) {
                String msg = this.plugin.getConfig().getString("messages.no-housing");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                String msg = this.plugin.getConfig().getString("messages.player-not-found");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            if (housing.getMembers().containsKey(target.getUniqueId())) {
                String msg = this.plugin.getConfig().getString("messages.already-invited").replace("%player%", target.getName());
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            housingManager.invitePlayer(housing.getId(), target);
            String msg = this.plugin.getConfig().getString("messages.player-invited").replace("%player%", target.getName());
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));

            String msgAction = this.plugin.getConfig().getString("actionbar.player-invited").replace("%player%", target.getName());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', prefix + msgAction)));

            String soundName = this.plugin.getConfig().getString("sounds.player-invited");
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, 1f, 1f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config: " + soundName);
            }

            String inviteMsg = this.plugin.getConfig().getString("messages.invite-received").replace("%player%", player.getName());
            String clickHereMsg = this.plugin.getConfig().getString("messages.click-here");

            TextComponent inviteComponent = new TextComponent();
            inviteComponent.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', prefix + inviteMsg)));
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
        }

        if (args[0].equalsIgnoreCase("join") && args.length == 2) {
            String targetPlayerName = args[1];
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
            if (targetPlayer == null) {
                String msg = this.plugin.getConfig().getString("messages.player-not-found");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
            HousingManager.Housing housing = housingManager.findHousingByOwner(targetPlayer);
            if (housing == null) {
                String msg = this.plugin.getConfig().getString("messages.housing-not-found").replace("%player%", targetPlayerName);
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
            if (!housing.getOwner().equals(player.getUniqueId()) && !housing.getMembers().containsKey(player.getUniqueId())) {
                String msg = this.plugin.getConfig().getString("messages.no-permission-join");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            player.teleport(housing.getCenter());
            String msg = this.plugin.getConfig().getString("messages.joined-housing").replace("%player%", targetPlayerName);
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        if (args[0].equalsIgnoreCase("settings")) {
            if (!player.hasPermission("housing.settings")) {
                String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                return true;
            }
            HousingManager.Housing housing = housingManager.findHousingByOwner(player);
            if (housing == null) {
                String msg = this.plugin.getConfig().getString("messages.no-housing");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
            HousingSettingsGUI gui = new HousingSettingsGUI(plugin, housing);
            gui.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("permissions")) {
            if (!player.hasPermission("housing.permissions")) {
                String noPermissionMsg = this.plugin.getConfig().getString("messages.no-permission");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                return true;
            }
            HousingManager.Housing housing = housingManager.findHousingByOwner(player);
            if (housing == null) {
                String msg = this.plugin.getConfig().getString("messages.no-housing");
                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
            HousingPermissionsGUI gui = new HousingPermissionsGUI(plugin, housing);
            gui.open(player);
            return true;
        }

        return false;
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
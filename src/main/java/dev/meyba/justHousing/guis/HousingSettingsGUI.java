package dev.meyba.justHousing.guis;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HousingSettingsGUI implements Listener {
    private static final String GUI_NAME = ChatColor.translateAlternateColorCodes('&', "&lʜᴏᴜꜱɪɴɢ ꜱᴇᴛᴛɪɴɢꜱ");
    private final JustHousing plugin;
    private final HousingManager.Housing housing;
    private final Inventory inventory;

    private static final Map<Player, HousingSettingsGUI> openGUIs = new HashMap<>();

    public HousingSettingsGUI(JustHousing plugin, HousingManager.Housing housing) {
        this.plugin = plugin;
        this.housing = housing;
        this.inventory = Bukkit.createInventory(null, 45, GUI_NAME);
        setupInventory();
    }

    private void setupInventory() {
        ItemStack grayGlassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", Collections.emptyList());
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, grayGlassPane);
        }

        String breakBlocksName = plugin.getConfig().getString("gui.settings.break-blocks.name");
        List<String> breakBlocksLore = plugin.getConfig().getStringList("gui.settings.break-blocks.lore").stream()
                .map(line -> line.replace("%status%", housing.isBreakBlocksEnabled() ? "§aenabled" : "§cdisabled")
                        .replace("%toggletype%", "Click to toggle!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack breakBlocksItem = createItem(
                housing.isBreakBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                breakBlocksName,
                breakBlocksLore
        );
        inventory.setItem(19, breakBlocksItem);

        String placeBlocksName = plugin.getConfig().getString("gui.settings.place-blocks.name");
        List<String> placeBlocksLore = plugin.getConfig().getStringList("gui.settings.place-blocks.lore").stream()
                .map(line -> line.replace("%status%", housing.isPlaceBlocksEnabled() ? "§aenabled" : "§cdisabled")
                        .replace("%toggletype%", "Click to toggle!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack placeBlocksItem = createItem(
                housing.isPlaceBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                placeBlocksName,
                placeBlocksLore
        );
        inventory.setItem(21, placeBlocksItem);

        String mobSpawningName = plugin.getConfig().getString("gui.settings.mob-spawning.name");
        List<String> mobSpawningLore = plugin.getConfig().getStringList("gui.settings.mob-spawning.lore").stream()
                .map(line -> line.replace("%status%", housing.isMobSpawningEnabled() ? "§aenabled" : "§cdisabled")
                        .replace("%toggletype%", "Click to toggle!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack mobSpawningItem = createItem(
                housing.isMobSpawningEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                mobSpawningName,
                mobSpawningLore
        );
        inventory.setItem(23, mobSpawningItem);

        String pvpName = plugin.getConfig().getString("gui.settings.pvp.name");
        List<String> pvpLore = plugin.getConfig().getStringList("gui.settings.pvp.lore").stream()
                .map(line -> line.replace("%status%", housing.isPvpEnabled() ? "§aenabled" : "§cdisabled")
                        .replace("%toggletype%", "Click to toggle!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack pvpItem = createItem(
                housing.isPvpEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                pvpName,
                pvpLore
        );
        inventory.setItem(25, pvpItem);

        String fallDamageName = plugin.getConfig().getString("gui.settings.fall-damage.name");
        List<String> fallDamageLore = plugin.getConfig().getStringList("gui.settings.fall-damage.lore").stream()
                .map(line -> line.replace("%status%", housing.isFallDamageEnabled() ? "§aenabled" : "§cdisabled")
                        .replace("%toggletype%", "Click to toggle!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack fallDamageItem = createItem(
                housing.isFallDamageEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                fallDamageName,
                fallDamageLore
        );
        inventory.setItem(27, fallDamageItem);
    }

    public void open(Player player) {
        player.openInventory(inventory);
        openGUIs.put(player, this);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, plugin);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(GUI_NAME)) {
            openGUIs.remove((Player) event.getPlayer());
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_NAME)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        HousingSettingsGUI gui = openGUIs.get(player);
        if (gui == null || !event.getClickedInventory().equals(gui.inventory)) return;

        HousingManager.Housing currentHousing = gui.housing;
        String prefix = plugin.getConfig().getString("prefix");

        int slot = event.getSlot();
        switch (slot) {
            case 19:
                currentHousing.setBreakBlocksEnabled(!currentHousing.isBreakBlocksEnabled());
                String msgBreak = plugin.getConfig().getString("messages.break-blocks-toggled").replace("%status%", currentHousing.isBreakBlocksEnabled() ? "§aenabled" : "§cdisabled");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgBreak));
                inventory.setItem(19, createItem(
                        currentHousing.isBreakBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.break-blocks.name"),
                        plugin.getConfig().getStringList("gui.settings.break-blocks.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isBreakBlocksEnabled() ? "§aenabled" : "§cdisabled")
                                        .replace("%toggletype%", "Click to toggle!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 21:
                currentHousing.setPlaceBlocksEnabled(!currentHousing.isPlaceBlocksEnabled());
                String msgPlace = plugin.getConfig().getString("messages.place-blocks-toggled").replace("%status%", currentHousing.isPlaceBlocksEnabled() ? "§aenabled" : "§cdisabled");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgPlace));
                inventory.setItem(21, createItem(
                        currentHousing.isPlaceBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.place-blocks.name"),
                        plugin.getConfig().getStringList("gui.settings.place-blocks.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isPlaceBlocksEnabled() ? "§aenabled" : "§cdisabled")
                                        .replace("%toggletype%", "Click to toggle!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 23:
                currentHousing.setMobSpawningEnabled(!currentHousing.isMobSpawningEnabled());
                String msgMobSpawning = plugin.getConfig().getString("messages.mob-spawning-toggled").replace("%status%", currentHousing.isMobSpawningEnabled() ? "§aenabled" : "§cdisabled");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgMobSpawning));
                inventory.setItem(23, createItem(
                        currentHousing.isMobSpawningEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.mob-spawning.name"),
                        plugin.getConfig().getStringList("gui.settings.mob-spawning.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isMobSpawningEnabled() ? "§aenabled" : "§cdisabled")
                                        .replace("%toggletype%", "Click to toggle!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 25:
                currentHousing.setPvpEnabled(!currentHousing.isPvpEnabled());
                String msgPvp = plugin.getConfig().getString("messages.pvp-toggled").replace("%status%", currentHousing.isPvpEnabled() ? "§aenabled" : "§cdisabled");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgPvp));
                inventory.setItem(25, createItem(
                        currentHousing.isPvpEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.pvp.name"),
                        plugin.getConfig().getStringList("gui.settings.pvp.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isPvpEnabled() ? "§aenabled" : "§cdisabled")
                                        .replace("%toggletype%", "Click to toggle!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 27:
                currentHousing.setFallDamageEnabled(!currentHousing.isFallDamageEnabled());
                String msgFallDamage = plugin.getConfig().getString("messages.fall-damage-toggled").replace("%status%", currentHousing.isPvpEnabled() ? "§aenabled" : "§cdisabled");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgFallDamage));
                inventory.setItem(27, createItem(
                        currentHousing.isFallDamageEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.fall-damage.name"),
                        plugin.getConfig().getStringList("gui.settings.fall-damage.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isFallDamageEnabled() ? "§aenabled" : "§cdisabled")
                                        .replace("%toggletype%", "Click to toggle!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            default:
                break;
        }
    }
}
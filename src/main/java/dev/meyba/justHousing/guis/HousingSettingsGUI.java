package dev.meyba.justHousing.guis;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, grayGlassPane);
        }

        for (int i = 36; i < 45; i++) {
            inventory.setItem(i, grayGlassPane);
        }

        inventory.setItem(9, grayGlassPane);
        inventory.setItem(18, grayGlassPane);
        inventory.setItem(27, grayGlassPane);

        inventory.setItem(17, grayGlassPane);
        inventory.setItem(26, grayGlassPane);
        inventory.setItem(35, grayGlassPane);

        String breakBlocksName = plugin.getConfig().getString("gui.settings.break-blocks.name");
        List<String> breakBlocksLore = plugin.getConfig().getStringList("gui.settings.break-blocks.lore").stream()
                .map(line -> line.replace("%status%", housing.isBreakBlocksEnabled() ? "&a●" : "&c●")
                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack breakBlocksItem = createItem(
                housing.isBreakBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                breakBlocksName,
                breakBlocksLore
        );
        inventory.setItem(10, breakBlocksItem);

        String placeBlocksName = plugin.getConfig().getString("gui.settings.place-blocks.name");
        List<String> placeBlocksLore = plugin.getConfig().getStringList("gui.settings.place-blocks.lore").stream()
                .map(line -> line.replace("%status%", housing.isPlaceBlocksEnabled() ? "&a●" : "&c●")
                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack placeBlocksItem = createItem(
                housing.isPlaceBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                placeBlocksName,
                placeBlocksLore
        );
        inventory.setItem(12, placeBlocksItem);

        String mobSpawningName = plugin.getConfig().getString("gui.settings.mob-spawning.name");
        List<String> mobSpawningLore = plugin.getConfig().getStringList("gui.settings.mob-spawning.lore").stream()
                .map(line -> line.replace("%status%", housing.isMobSpawningEnabled() ? "&a●" : "&c●")
                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack mobSpawningItem = createItem(
                housing.isMobSpawningEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                mobSpawningName,
                mobSpawningLore
        );
        inventory.setItem(14, mobSpawningItem);

        String pvpName = plugin.getConfig().getString("gui.settings.pvp.name");
        List<String> pvpLore = plugin.getConfig().getStringList("gui.settings.pvp.lore").stream()
                .map(line -> line.replace("%status%", housing.isPvpEnabled() ? "&a●" : "&c●")
                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack pvpItem = createItem(
                housing.isPvpEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                pvpName,
                pvpLore
        );
        inventory.setItem(16, pvpItem);

        String fallDamageName = plugin.getConfig().getString("gui.settings.fall-damage.name");
        List<String> fallDamageLore = plugin.getConfig().getStringList("gui.settings.fall-damage.lore").stream()
                .map(line -> line.replace("%status%", housing.isFallDamageEnabled() ? "&a●" : "&c●")
                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack fallDamageItem = createItem(
                housing.isFallDamageEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                fallDamageName,
                fallDamageLore
        );
        inventory.setItem(20, fallDamageItem);

        String naturalRegenerationName = plugin.getConfig().getString("gui.settings.natural-regeneration.name");
        List<String> naturalRegenerationLore = plugin.getConfig().getStringList("gui.settings.natural-regeneration.lore").stream()
                .map(line -> line.replace("%status%", housing.isNaturalRegenerationEnabled() ? "&a●" : "&c●")
                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack naturalRegenerationItem = createItem(
                housing.isNaturalRegenerationEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                naturalRegenerationName,
                naturalRegenerationLore
        );
        inventory.setItem(22, naturalRegenerationItem);

        String defaultGamemodeName = plugin.getConfig().getString("gui.settings.default-gamemode.name");
        List<String> defaultGamemodeLore = plugin.getConfig().getStringList("gui.settings.default-gamemode.lore").stream()
                .map(line -> line.replace("%gamemode%", housing.getDefaultGameMode().name().toLowerCase()))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        Material defaultGamemodeMaterial;
        switch (housing.getDefaultGameMode()) {
            case CREATIVE:
                defaultGamemodeMaterial = Material.COMMAND_BLOCK;
                break;
            case ADVENTURE:
                defaultGamemodeMaterial = Material.FILLED_MAP;
                break;
            default:
                defaultGamemodeMaterial = Material.GRASS_BLOCK;
                break;
        }
        ItemStack defaultGamemodeItem = createItem(
                defaultGamemodeMaterial,
                defaultGamemodeName,
                defaultGamemodeLore
        );
        inventory.setItem(24, defaultGamemodeItem);

        String keepInventoryName = plugin.getConfig().getString("gui.settings.keep-inventory.name");
        List<String> keepInventoryLore = plugin.getConfig().getStringList("gui.settings.keep-inventory.lore").stream()
                .map(line -> line.replace("%status%", housing.isKeepInventoryEnabled() ? "&a●" : "&c●"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack keepInventoryItem = createItem(
                housing.isKeepInventoryEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                keepInventoryName,
                keepInventoryLore
        );
        inventory.setItem(28, keepInventoryItem);

        String daylightCycleName = plugin.getConfig().getString("gui.settings.daylight-cycle.name");
        List<String> daylightCycleLore = plugin.getConfig().getStringList("gui.settings.daylight-cycle.lore").stream()
                .map(line -> line.replace("%status%", housing.isDaylightCycleEnabled() ? "&a●" : "&c●"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack daylightCycleItem = createItem(
                housing.isDaylightCycleEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                daylightCycleName,
                daylightCycleLore
        );
        inventory.setItem(30, daylightCycleItem);

        String weatherCycleName = plugin.getConfig().getString("gui.settings.weather-cycle.name");
        List<String> weatherCycleLore = plugin.getConfig().getStringList("gui.settings.weather-cycle.lore").stream()
                .map(line -> line.replace("%status%", housing.isWeatherCycleEnabled() ? "&a●" : "&c●"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack weatherCycleItem = createItem(
                housing.isWeatherCycleEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                weatherCycleName,
                weatherCycleLore
        );
        inventory.setItem(32, weatherCycleItem);

        String doFireTickName = plugin.getConfig().getString("gui.settings.do-fire-tick.name");
        List<String> doFireTickLore = plugin.getConfig().getStringList("gui.settings.do-fire-tick.lore").stream()
                .map(line -> line.replace("%status%", housing.isDoFireTickEnabled() ? "&a●" : "&c●"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
        ItemStack doFireTickItem = createItem(
                housing.isDoFireTickEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                doFireTickName,
                doFireTickLore
        );
        inventory.setItem(34, doFireTickItem);
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
            case 10:
                currentHousing.setBreakBlocksEnabled(!currentHousing.isBreakBlocksEnabled());
                String msgBreak = plugin.getConfig().getString("messages.toggles.break-blocks-toggled").replace("%status%", currentHousing.isBreakBlocksEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgBreak));
                inventory.setItem(10, createItem(
                        currentHousing.isBreakBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.break-blocks.name"),
                        plugin.getConfig().getStringList("gui.settings.break-blocks.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isBreakBlocksEnabled() ? "&a●" : "&c●")
                                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 28:
                currentHousing.setKeepInventoryEnabled(!currentHousing.isKeepInventoryEnabled());
                String msgKeepInventory = plugin.getConfig().getString("messages.toggles.keep-inventory-toggled").replace("%status%", currentHousing.isKeepInventoryEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgKeepInventory));
                inventory.setItem(28, createItem(
                        currentHousing.isKeepInventoryEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.keep-inventory.name"),
                        plugin.getConfig().getStringList("gui.settings.keep-inventory.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isKeepInventoryEnabled() ? "&a●" : "&c●"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 30:
                currentHousing.setDaylightCycleEnabled(!currentHousing.isDaylightCycleEnabled());
                String msgDaylightCycle = plugin.getConfig().getString("messages.toggles.daylight-cycle-toggled").replace("%status%", currentHousing.isDaylightCycleEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgDaylightCycle));
                inventory.setItem(30, createItem(
                        currentHousing.isDaylightCycleEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.daylight-cycle.name"),
                        plugin.getConfig().getStringList("gui.settings.daylight-cycle.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isDaylightCycleEnabled() ? "&a●" : "&c●"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 32:
                currentHousing.setWeatherCycleEnabled(!currentHousing.isWeatherCycleEnabled());
                String msgWeatherCycle = plugin.getConfig().getString("messages.toggles.weather-cycle-toggled").replace("%status%", currentHousing.isWeatherCycleEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgWeatherCycle));
                inventory.setItem(32, createItem(
                        currentHousing.isWeatherCycleEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.weather-cycle.name"),
                        plugin.getConfig().getStringList("gui.settings.weather-cycle.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isWeatherCycleEnabled() ? "&a●" : "&c●"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 34:
                currentHousing.setDoFireTickEnabled(!currentHousing.isDoFireTickEnabled());
                String msgDoFireTick = plugin.getConfig().getString("messages.toggles.do-fire-tick-toggled").replace("%status%", currentHousing.isDoFireTickEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgDoFireTick));
                inventory.setItem(34, createItem(
                        currentHousing.isDoFireTickEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.do-fire-tick.name"),
                        plugin.getConfig().getStringList("gui.settings.do-fire-tick.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isDoFireTickEnabled() ? "&a●" : "&c●"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 12:
                currentHousing.setPlaceBlocksEnabled(!currentHousing.isPlaceBlocksEnabled());
                String msgPlace = plugin.getConfig().getString("messages.toggles.place-blocks-toggled").replace("%status%", currentHousing.isPlaceBlocksEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgPlace));
                inventory.setItem(12, createItem(
                        currentHousing.isPlaceBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.place-blocks.name"),
                        plugin.getConfig().getStringList("gui.settings.place-blocks.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isPlaceBlocksEnabled() ? "&a●" : "&c●")
                                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 14:
                currentHousing.setMobSpawningEnabled(!currentHousing.isMobSpawningEnabled());
                String msgMobSpawning = plugin.getConfig().getString("messages.toggles.mob-spawning-toggled").replace("%status%", currentHousing.isMobSpawningEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgMobSpawning));
                inventory.setItem(14, createItem(
                        currentHousing.isMobSpawningEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.mob-spawning.name"),
                        plugin.getConfig().getStringList("gui.settings.mob-spawning.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isMobSpawningEnabled() ? "&a●" : "&c●")
                                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 16:
                currentHousing.setPvpEnabled(!currentHousing.isPvpEnabled());
                String msgPvp = plugin.getConfig().getString("messages.toggles.pvp-toggled").replace("%status%", currentHousing.isPvpEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgPvp));
                inventory.setItem(16, createItem(
                        currentHousing.isPvpEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.pvp.name"),
                        plugin.getConfig().getStringList("gui.settings.pvp.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isPvpEnabled() ? "&a●" : "&c●")
                                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 20:
                currentHousing.setFallDamageEnabled(!currentHousing.isFallDamageEnabled());
                String msgFallDamage = plugin.getConfig().getString("messages.toggles.fall-damage-toggled").replace("%status%", currentHousing.isPvpEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgFallDamage));
                inventory.setItem(20, createItem(
                        currentHousing.isFallDamageEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.fall-damage.name"),
                        plugin.getConfig().getStringList("gui.settings.fall-damage.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isFallDamageEnabled() ? "&a●" : "&c●")
                                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 22:
                currentHousing.setNaturalRegenerationEnabled(!currentHousing.isNaturalRegenerationEnabled());
                String msgNaturalRegeneration = plugin.getConfig().getString("messages.toggles.natural-regeneration-toggled").replace("%status%", currentHousing.isNaturalRegenerationEnabled() ? "&a●" : "&c●");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgNaturalRegeneration));
                inventory.setItem(22, createItem(
                        currentHousing.isNaturalRegenerationEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        plugin.getConfig().getString("gui.settings.natural-regeneration.name"),
                        plugin.getConfig().getStringList("gui.settings.natural-regeneration.lore").stream()
                                .map(line -> line.replace("%status%", currentHousing.isNaturalRegenerationEnabled() ? "&a●" : "&c●")
                                        .replace("%toggletype%", "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ!"))
                                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                .collect(Collectors.toList())
                ));
                break;
            case 24:
                GameMode[] modes = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE};
                int currentIndex = 0;
                for (int i = 0; i < modes.length; i++) {
                    if (modes[i] == currentHousing.getDefaultGameMode()) {
                        currentIndex = i;
                        break;
                    }
                }
                GameMode newMode = modes[(currentIndex + 1) % modes.length];
                currentHousing.setDefaultGameMode(newMode);
                String msgDefaultGamemode = plugin.getConfig().getString("messages.toggles.default-gamemode-changed");
                msgDefaultGamemode = msgDefaultGamemode.replace("%gamemode%", newMode.name().toLowerCase());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgDefaultGamemode));
                List<String> updatedLore = plugin.getConfig().getStringList("gui.settings.default-gamemode.lore").stream()
                        .map(line -> line.replace("%gamemode%", newMode.name().toLowerCase()))
                        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                        .collect(Collectors.toList());
                Material newMaterial;
                switch (newMode) {
                    case CREATIVE:
                        newMaterial = Material.COMMAND_BLOCK;
                        break;
                    case ADVENTURE:
                        newMaterial = Material.FILLED_MAP;
                        break;
                    default:
                        newMaterial = Material.GRASS_BLOCK;
                        break;
                }
                inventory.setItem(24, createItem(
                        newMaterial,
                        plugin.getConfig().getString("gui.settings.default-gamemode.name"),
                        updatedLore
                ));
                break;
            default:
                break;
        }
    }
}
package dev.meyba.justHousing.guis;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        this.inventory = Bukkit.createInventory(null, 27, GUI_NAME);
        setupInventory();
    }

    private void setupInventory() {
        ItemStack grayGlassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", Collections.emptyList());
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, grayGlassPane);
        }

        String breakBlocksName = plugin.getConfig().getString("gui.settings.break-blocks.name");
        List<String> breakBlocksLore = plugin.getConfig().getStringList("gui.settings.break-blocks.lore");
        ItemStack breakBlocksItem = createItem(
                housing.isBreakBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                breakBlocksName,
                breakBlocksLore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList())
        );
        inventory.setItem(11, breakBlocksItem);

        String placeBlocksName = plugin.getConfig().getString("gui.settings.place-blocks.name");
        List<String> placeBlocksLore = plugin.getConfig().getStringList("gui.settings.place-blocks.lore");
        ItemStack placeBlocksItem = createItem(
                housing.isPlaceBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                placeBlocksName,
                placeBlocksLore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList())
        );
        inventory.setItem(13, placeBlocksItem);

        String mobSpawningName = plugin.getConfig().getString("gui.settings.mob-spawning.name");
        List<String> mobSpawningLore = plugin.getConfig().getStringList("gui.settings.mob-spawning.lore");
        ItemStack mobSpawningItem = createItem(
                housing.isMobSpawningEnabled() ? Material.ZOMBIE_HEAD : Material.SKELETON_SKULL,
                mobSpawningName,
                mobSpawningLore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList())
        );
        inventory.setItem(15, mobSpawningItem);
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

    public void open(Player player) {
        player.openInventory(inventory);
        openGUIs.put(player, this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals(GUI_NAME)) {
            event.setCancelled(true);
            HousingSettingsGUI gui = openGUIs.get(player);
            if (gui == null) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().equals(Material.AIR)) return;

            HousingManager.Housing currentHousing = gui.housing;
            String prefix = plugin.getConfig().getString("prefix");

            switch (event.getRawSlot()) {
                case 11:
                    boolean currentStateBreak = currentHousing.isBreakBlocksEnabled();
                    currentHousing.setBreakBlocksEnabled(!currentStateBreak);
                    gui.setupInventory();
                    String msgBreak = plugin.getConfig().getString("messages.break-blocks-toggled").replace("%status%", currentHousing.isBreakBlocksEnabled() ? "enabled" : "disabled");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgBreak));
                    break;
                case 13:
                    boolean currentStatePlace = currentHousing.isPlaceBlocksEnabled();
                    currentHousing.setPlaceBlocksEnabled(!currentStatePlace);
                    gui.setupInventory();
                    String msgPlace = plugin.getConfig().getString("messages.place-blocks-toggled").replace("%status%", currentHousing.isPlaceBlocksEnabled() ? "enabled" : "disabled");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgPlace));
                    break;
                case 15:
                    boolean currentStateMobSpawning = currentHousing.isMobSpawningEnabled();
                    currentHousing.setMobSpawningEnabled(!currentStateMobSpawning);
                    gui.setupInventory();
                    String msgMobSpawning = plugin.getConfig().getString("messages.mob-spawning-toggled").replace("%status%", currentHousing.isMobSpawningEnabled() ? "enabled" : "disabled");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msgMobSpawning));
                    break;
            }
        }
    }
}
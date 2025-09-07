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
import java.util.Map;

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
        ItemStack grayGlassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17) {
                inventory.setItem(i, grayGlassPane);
            }
        }

        String breakBlocksName = plugin.getConfig().getString("gui.settings.break-blocks.name");
        String breakBlocksLore = plugin.getConfig().getString("gui.settings.break-blocks.lore");
        ItemStack breakItem = createToggleItem(
                housing.isBreakBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                breakBlocksName,
                breakBlocksLore
        );
        inventory.setItem(12, breakItem);

        String placeBlocksName = plugin.getConfig().getString("gui.settings.place-blocks.name");
        String placeBlocksLore = plugin.getConfig().getString("gui.settings.place-blocks.lore");
        ItemStack placeItem = createToggleItem(
                housing.isPlaceBlocksEnabled() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                placeBlocksName,
                placeBlocksLore
        );
        inventory.setItem(14, placeItem);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
        if (!openGUIs.containsKey(player)) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
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

            if (event.getRawSlot() == 12) {
                boolean currentState = currentHousing.isBreakBlocksEnabled();
                currentHousing.setBreakBlocksEnabled(!currentState);
                setupInventory();
                String msg = plugin.getConfig().getString("messages.break-blocks-toggled").replace("%status%", currentHousing.isBreakBlocksEnabled() ? "enabled" : "disabled");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            } else if (event.getRawSlot() == 14) {
                boolean currentState = currentHousing.isPlaceBlocksEnabled();
                currentHousing.setPlaceBlocksEnabled(!currentState);
                setupInventory();
                String msg = plugin.getConfig().getString("messages.place-blocks-toggled").replace("%status%", currentHousing.isPlaceBlocksEnabled() ? "enabled" : "disabled");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        }
    }
}
package dev.meyba.justHousing.guis;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HousingListGUI implements Listener {
    private static final String GUI_NAME = ChatColor.translateAlternateColorCodes('&', "&lʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ");
    private final JustHousing plugin;
    private final HousingManager housingManager;
    private final Inventory inventory;

    private static final Map<Player, HousingListGUI> openGUIs = new HashMap<>();

    public HousingListGUI(JustHousing plugin, HousingManager housingManager) {
        this.plugin = plugin;
        this.housingManager = housingManager;
        this.inventory = Bukkit.createInventory(null, 54, GUI_NAME);
        setupInventory();
    }

    private void setupInventory() {
        inventory.clear();
        ItemStack grayGlassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, grayGlassPane);
        }

        int slot = 0;
        for (HousingManager.Housing housing : housingManager.getHousings().values()) {
            if (slot >= 54) break;
            OfflinePlayer owner = Bukkit.getOfflinePlayer(housing.getOwner());

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta == null) continue;

            skullMeta.setOwningPlayer(owner);
            skullMeta.setDisplayName(ChatColor.AQUA + owner.getName() + "'s Housing");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to join this housing.");
            skullMeta.setLore(lore);
            skull.setItemMeta(skullMeta);

            inventory.setItem(slot, skull);
            slot++;
        }
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
            HousingListGUI gui = openGUIs.get(player);
            if (gui == null) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
            if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;

            OfflinePlayer housingOwner = skullMeta.getOwningPlayer();
            HousingManager.Housing housing = gui.housingManager.findHousingByOwner(housingOwner.getPlayer());
            if (housing != null) {
                player.teleport(housing.getCenter());
                String msg = gui.plugin.getConfig().getString("messages.joined-housing").replace("%player%", housingOwner.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            } else {
                String msg = gui.plugin.getConfig().getString("messages.housing-not-found").replace("%player%", housingOwner.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
            player.closeInventory();
        }
    }
}
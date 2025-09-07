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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        ItemStack grayGlassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", Collections.emptyList());
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, grayGlassPane);
        }

        int slot = 0;
        for (HousingManager.Housing housing : housingManager.getHousings().values()) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(housing.getOwner());
            if (owner != null) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(owner);
                    String name = plugin.getConfig().getString("gui.list.item.name", "&a%player%'s Housing").replace("%player%", owner.getName());
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                    List<String> loreConfig = plugin.getConfig().getStringList("gui.list.item.lore");
                    List<String> lore = loreConfig.stream()
                            .map(line -> line.replace("%owner%", owner.getName())
                                    .replace("%members_count%", String.valueOf(housing.getMembers().size())))
                            .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                            .collect(Collectors.toList());
                    meta.setLore(lore);

                    playerHead.setItemMeta(meta);
                    inventory.setItem(slot, playerHead);
                    slot++;
                }
            }
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open(Player player) {
        setupInventory();
        player.openInventory(inventory);
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
                String prefix = plugin.getConfig().getString("prefix");
                String msg = gui.plugin.getConfig().getString("messages.joined-housing", "&aYou have joined %player%'s housing!").replace("%player%", housingOwner.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
            } else {
                String prefix = plugin.getConfig().getString("prefix");
                String msg = gui.plugin.getConfig().getString("messages.housing-not-found", "&cHousing for %player% not found!").replace("%player%", housingOwner.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
            }
            player.closeInventory();
        }
    }
}
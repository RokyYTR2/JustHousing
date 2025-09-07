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

import java.util.*;

public class HousingPermissionsGUI implements Listener {
    private static final String GUI_NAME = ChatColor.translateAlternateColorCodes('&', "&lʜᴏᴜꜱɪɴɢ ᴘᴇʀᴍɪꜱꜱɪᴏɴꜱ");
    private final JustHousing plugin;
    private final HousingManager.Housing housing;
    private final Inventory inventory;

    private static final Map<Player, HousingPermissionsGUI> openGUIs = new HashMap<>();

    public HousingPermissionsGUI(JustHousing plugin, HousingManager.Housing housing) {
        this.plugin = plugin;
        this.housing = housing;
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
        for (Map.Entry<UUID, HousingManager.Member> entry : housing.getMembers().entrySet()) {
            UUID memberId = entry.getKey();
            HousingManager.Member member = entry.getValue();
            OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberId);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta == null) continue;

            skullMeta.setOwningPlayer(memberPlayer);
            skullMeta.setDisplayName(ChatColor.AQUA + memberPlayer.getName());

            List<String> lore = new ArrayList<>();
            if (member.isAdmin()) {
                lore.add(ChatColor.GREEN + "Admin: " + ChatColor.BOLD + "✔");
            } else {
                lore.add(ChatColor.RED + "Admin: " + ChatColor.BOLD + "❌");
            }
            lore.add("");
            lore.add(ChatColor.GRAY + "Click to toggle admin status.");
            lore.add(ChatColor.DARK_RED + "Shift + Click to remove from housing.");
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
            HousingPermissionsGUI gui = openGUIs.get(player);
            if (gui == null) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
            if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;

            UUID memberId = skullMeta.getOwningPlayer().getUniqueId();
            String memberName = skullMeta.getOwningPlayer().getName();

            if (event.isShiftClick()) {
                gui.housing.removeMember(memberId);
                gui.setupInventory();
                player.sendMessage(ChatColor.RED + "You have removed " + memberName + " from your housing.");
            } else {
                HousingManager.Member member = gui.housing.getMembers().get(memberId);
                if (member != null) {
                    member.setAdmin(!member.isAdmin());
                    gui.setupInventory();
                    player.sendMessage(ChatColor.GREEN + "Admin status for " + memberName + " has been " + (member.isAdmin() ? "granted." : "revoked."));
                }
            }
        }
    }
}
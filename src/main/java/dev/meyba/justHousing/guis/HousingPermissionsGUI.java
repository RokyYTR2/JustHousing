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
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, grayGlassPane);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, grayGlassPane);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, grayGlassPane);
            inventory.setItem(i + 8, grayGlassPane);
        }

        int slot = 10;
        List<UUID> memberIds = new ArrayList<>(housing.getMembers().keySet());
        for (UUID memberId : memberIds) {
            if (slot > 43) break;
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            if (member != null) {
                ItemStack playerHead = createPlayerHead(member, housing.getMembers().get(memberId));
                inventory.setItem(slot, playerHead);
                if ((slot + 1) % 9 == 0) {
                    slot += 2;
                } else {
                    slot++;
                }
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
        openGUIs.put(player, this);
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

    private ItemStack createPlayerHead(OfflinePlayer member, HousingManager.Member memberData) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(member);
            String displayName = ChatColor.translateAlternateColorCodes('&', "&a" + member.getName());
            List<String> lore = Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "§7ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ᴀᴅᴍɪɴ ꜱᴛᴀᴛᴜꜱ"),
                    ChatColor.translateAlternateColorCodes('&', "§7ꜱʜɪꜰᴛ + ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴍᴏᴠᴇ"),
                    "",
                    ChatColor.translateAlternateColorCodes('&', "§7ᴀᴅᴍɪɴ ꜱᴛᴀᴛᴜꜱ: " + (memberData.isAdmin() ? "§aᴛʀᴜᴇ" : "§cꜰᴀʟꜱᴇ"))
            );
            skullMeta.setDisplayName(displayName);
            skullMeta.setLore(lore);
            playerHead.setItemMeta(skullMeta);
        }
        return playerHead;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(GUI_NAME)) {
            openGUIs.remove((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_NAME)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        HousingPermissionsGUI gui = openGUIs.get(player);
        if (gui == null || !event.getClickedInventory().equals(gui.inventory)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

        SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
        if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;

        UUID memberId = skullMeta.getOwningPlayer().getUniqueId();
        String memberName = skullMeta.getOwningPlayer().getName();

        if (event.isShiftClick()) {
            gui.housing.removeMember(memberId);
            player.sendMessage(ChatColor.RED + "You have removed " + memberName + " from your housing.");
        } else {
            HousingManager.Member member = gui.housing.getMembers().get(memberId);
            if (member != null) {
                member.setAdmin(!member.isAdmin());
                player.sendMessage(ChatColor.GREEN + "Admin status for " + memberName + " has been " + (member.isAdmin() ? "enabled" : "disabled") + ".");
            }
        }
        gui.setupInventory();
    }
}
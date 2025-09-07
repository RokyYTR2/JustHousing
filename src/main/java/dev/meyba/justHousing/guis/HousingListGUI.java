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
import java.util.stream.Collectors;

public class HousingListGUI implements Listener {
    private static final String GUI_NAME = ChatColor.translateAlternateColorCodes('&', "&lʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ");
    private final JustHousing plugin;
    private final HousingManager housingManager;
    private final Inventory inventory;

    private static final Map<Player, HousingListGUI> openGUIs = new HashMap<>();
    private static final Map<Player, SortMode> playerSortModes = new HashMap<>();

    private enum SortMode {
        DEFAULT("§aDefault Sort", Material.PLAYER_HEAD, "MHF_ArrowRight"),
        MOST_PLAYERS("§aMost Players", Material.PLAYER_HEAD, "MHF_ArrowUp"),
        LEAST_PLAYERS("§aLeast Players", Material.PLAYER_HEAD, "MHF_ArrowDown"),
        ALPHABETICAL("§aAlphabetical", Material.PLAYER_HEAD, "MHF_Book");

        private final String displayName;
        private final Material icon;
        private final String skullOwner;

        SortMode(String displayName, Material icon, String skullOwner) {
            this.displayName = displayName;
            this.icon = icon;
            this.skullOwner = skullOwner;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }
    }

    public HousingListGUI(JustHousing plugin, HousingManager housingManager) {
        this.plugin = plugin;
        this.housingManager = housingManager;
        this.inventory = Bukkit.createInventory(null, 54, GUI_NAME);
    }

    private void setupInventory(Player viewer) {
        inventory.clear();

        SortMode sortMode = playerSortModes.getOrDefault(viewer, SortMode.DEFAULT);

        ItemStack sortButton = createSortButton(sortMode);
        inventory.setItem(10, sortButton);

        ItemStack grayGlassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", Collections.emptyList());
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
        inventory.setItem(11, grayGlassPane);
        inventory.setItem(20, grayGlassPane);
        inventory.setItem(29, grayGlassPane);
        inventory.setItem(38, grayGlassPane);

        List<HousingManager.Housing> housings = new ArrayList<>(housingManager.getHousings().values());
        switch (sortMode) {
            case MOST_PLAYERS:
                housings.sort((h1, h2) -> Integer.compare(
                        h2.getMembers().size() + (Bukkit.getPlayer(h2.getOwner()) != null ? 1 : 0),
                        h1.getMembers().size() + (Bukkit.getPlayer(h1.getOwner()) != null ? 1 : 0)
                ));
                break;
            case LEAST_PLAYERS:
                housings.sort(Comparator.comparingInt(h -> h.getMembers().size() + (Bukkit.getPlayer(h.getOwner()) != null ? 1 : 0)));
                break;
            case ALPHABETICAL:
                housings.sort((h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));
                break;
            case DEFAULT:
            default:
                break;
        }

        int slot = 12;
        for (HousingManager.Housing housing : housings) {
            if (slot > 43) break;
            OfflinePlayer owner = Bukkit.getOfflinePlayer(housing.getOwner());
            if (owner != null) {
                ItemStack playerHead = createPlayerHead(owner, housing);
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
        setupInventory(player);
        player.openInventory(inventory);
        openGUIs.put(player, this);
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

    private ItemStack createPlayerHead(OfflinePlayer owner, HousingManager.Housing housing) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(owner);
            String displayName = ChatColor.translateAlternateColorCodes('&', "&a" + housing.getName());
            List<String> lore = plugin.getConfig().getStringList("gui.list.item.lore");
            List<String> formattedLore = lore.stream()
                    .map(line -> line.replace("%owner%", owner.getName() != null ? owner.getName() : "Unknown"))
                    .map(line -> line.replace("%online_status%", owner.isOnline() ? "Online" : "Offline"))
                    .map(line -> line.replace("%player_count%", String.valueOf(housing.getMembers().size() + (owner.isOnline() ? 1 : 0))))
                    .map(line -> line.replace("%members_count%", String.valueOf(housing.getMembers().size())))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .collect(Collectors.toList());
            skullMeta.setDisplayName(displayName);
            skullMeta.setLore(formattedLore);
            playerHead.setItemMeta(skullMeta);
        }
        return playerHead;
    }

    private ItemStack createSortButton(SortMode mode) {
        String configPath = "gui.list.sort-modes." + mode.name().toLowerCase();
        String displayName = plugin.getConfig().getString(configPath + ".name", mode.getDisplayName());
        List<String> lore = new ArrayList<>();

        lore.addAll(plugin.getConfig().getStringList(configPath + ".lore").stream()
                .filter(line -> !line.contains("%"))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList()));

        ItemStack item = new ItemStack(mode.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
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
        HousingListGUI gui = openGUIs.get(player);
        if (gui == null || !event.getClickedInventory().equals(gui.inventory)) return;

        if (event.getSlot() == 10) {
            SortMode currentMode = playerSortModes.getOrDefault(player, SortMode.DEFAULT);
            SortMode[] modes = SortMode.values();
            int currentIndex = currentMode.ordinal();
            SortMode nextMode;
            if (event.isLeftClick()) {
                nextMode = modes[(currentIndex + 1) % modes.length];
            } else if (event.isRightClick()) {
                nextMode = modes[(currentIndex - 1 + modes.length) % modes.length];
            } else {
                return;
            }
            playerSortModes.put(player, nextMode);
            gui.setupInventory(player);
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

        SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
        if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;

        OfflinePlayer housingOwner = skullMeta.getOwningPlayer();
        HousingManager.Housing housing = gui.housingManager.findHousingByOwner(housingOwner.getPlayer());
        if (housing != null) {
            player.teleport(housing.getCenter());
            String prefix = plugin.getConfig().getString("prefix");
            String msg = gui.plugin.getConfig().getString("messages.joined-housing", "&aYou have joined %player%'s housing!").replace("%player%", housingOwner.getName() != null ? housingOwner.getName() : "Unknown");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
        } else {
            String prefix = plugin.getConfig().getString("prefix");
            String msg = gui.plugin.getConfig().getString("messages.housing-not-found", "&cHousing for %player% not found!").replace("%player%", housingOwner.getName() != null ? housingOwner.getName() : "Unknown");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
        }
        player.closeInventory();
    }
}
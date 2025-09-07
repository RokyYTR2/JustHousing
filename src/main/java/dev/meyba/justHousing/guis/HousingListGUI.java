package dev.meyba.justHousing.guis;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.*;
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
        DEFAULT("§aDefault Order", Material.BARRIER),
        ONLINE_FIRST("§aOnline First", Material.LIME_DYE),
        PLAYER_COUNT("§aMost Players", Material.PLAYER_HEAD);

        private final String displayName;
        private final Material icon;

        SortMode(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
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
        inventory.setItem(0, sortButton);

        ItemStack grayGlassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", Collections.emptyList());
        for (int i = 1; i < 9; i++) {
            inventory.setItem(i, grayGlassPane);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, grayGlassPane);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, grayGlassPane);
            inventory.setItem(i + 8, grayGlassPane);
        }

        List<HousingManager.Housing> housings = new ArrayList<>(housingManager.getHousings().values());
        switch (sortMode) {
            case ONLINE_FIRST:
                housings.sort((h1, h2) -> {
                    boolean h1Online = Bukkit.getPlayer(h1.getOwner()) != null;
                    boolean h2Online = Bukkit.getPlayer(h2.getOwner()) != null;
                    if (h1Online && !h2Online) {
                        return -1;
                    } else if (!h1Online && h2Online) {
                        return 1;
                    }
                    return 0;
                });
                break;
            case PLAYER_COUNT:
                housings.sort((h1, h2) -> Integer.compare(h2.getMembers().size() + (Bukkit.getPlayer(h2.getOwner()) != null ? 1 : 0), h1.getMembers().size() + (Bukkit.getPlayer(h1.getOwner()) != null ? 1 : 0)));
                break;
            case DEFAULT:
            default:
                break;
        }

        int slot = 10;
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
            String displayName = plugin.getConfig().getString("gui.list.player-head.name", "&a%player%'s Housing").replace("%player%", owner.getName());
            List<String> lore = plugin.getConfig().getStringList("gui.list.player-head.lore");
            List<String> formattedLore = lore.stream()
                    .map(line -> line.replace("%owner%", owner.getName()))
                    .map(line -> line.replace("%is_online%", owner.isOnline() ? "Online" : "Offline"))
                    .map(line -> line.replace("%member_count%", String.valueOf(housing.getMembers().size())))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .collect(Collectors.toList());
            skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            skullMeta.setLore(formattedLore);
            playerHead.setItemMeta(skullMeta);
        }
        return playerHead;
    }

    private ItemStack createSortButton(SortMode mode) {
        String displayName = mode.getDisplayName();
        List<String> lore = new ArrayList<>();
        for (SortMode m : SortMode.values()) {
            if (m == mode) {
                lore.add("§b» " + m.getDisplayName());
            } else {
                lore.add("§7" + m.getDisplayName());
            }
        }
        return createItem(mode.getIcon(), displayName, lore);
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

        if (event.getSlot() == 0) {
            SortMode currentMode = playerSortModes.getOrDefault(player, SortMode.DEFAULT);
            SortMode nextMode = switch (currentMode) {
                case DEFAULT -> SortMode.ONLINE_FIRST;
                case ONLINE_FIRST -> SortMode.PLAYER_COUNT;
                case PLAYER_COUNT -> SortMode.DEFAULT;
            };
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
package dev.meyba.justHousing.guis;

import dev.meyba.justHousing.JustHousing;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class HousingListGUI implements Listener {
    private static final String GUI_NAME_PREFIX = ChatColor.translateAlternateColorCodes('&', "&lʜᴏᴜꜱɪɴɢ ʟɪꜱᴛ");
    private final JustHousing plugin;
    private final HousingManager housingManager;

    private static final Map<Player, HousingListGUI> openGUIs = new HashMap<>();
    private static final Map<Player, SortMode> playerSortModes = new HashMap<>();
    private static final Map<Player, Integer> playerPages = new HashMap<>();
    private static final Map<UUID, UUID> playerLastVoted = new HashMap<>();

    private enum SortMode {
        DEFAULT("§aᴅᴇꜰᴀᴜʟᴛ ꜱᴏʀᴛ", Material.PLAYER_HEAD, "MHF_ArrowRight"),
        MOST_PLAYERS("§aᴍᴏꜱᴛ ᴘʟᴀʏᴇʀꜱ", Material.PLAYER_HEAD, "MHF_ArrowUp"),
        LEAST_PLAYERS("§aʟᴇᴀꜱᴛ ᴘʟᴀʏᴇʀꜱ", Material.PLAYER_HEAD, "MHF_ArrowDown"),
        ALPHABETICAL("§aᴀʟᴘʜᴀʙᴇᴛɪᴄᴀʟ", Material.PLAYER_HEAD, "MHF_Book"),
        MOST_VOTES("§aᴍᴏꜱᴛ ᴠᴏᴛᴇꜱ", Material.PLAYER_HEAD, "MHF_Star"),
        MOST_SUPER_VOTES("§aᴍᴏꜱᴛ ꜱᴜᴘᴇʀ ᴠᴏᴛᴇꜱ", Material.PLAYER_HEAD, "MHF_Star");

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
    }

    private Inventory createInventory(Player viewer, int page) {
        String guiName = GUI_NAME_PREFIX + ChatColor.translateAlternateColorCodes('&', " &8(ᴘᴀɢᴇ " + page + ")");
        Inventory inventory = Bukkit.createInventory(null, 54, guiName);
        setupInventory(inventory, viewer, page);
        return inventory;
    }

    private void setupInventory(Inventory inventory, Player viewer, int page) {
        inventory.clear();

        SortMode sortMode = playerSortModes.getOrDefault(viewer, SortMode.DEFAULT);

        ItemStack sortButton = createSortButton(sortMode);
        inventory.setItem(10, sortButton);

        ItemStack randomButton = createRandomButton();
        inventory.setItem(19, randomButton);

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
            case MOST_VOTES:
                housings.sort((h1, h2) -> Integer.compare(h2.getVotes(), h1.getVotes()));
                break;
            case MOST_SUPER_VOTES:
                housings.sort((h1, h2) -> Integer.compare(h2.getSuperVotes(), h1.getSuperVotes()));
                break;
            case DEFAULT:
            default:
                break;
        }

        int itemsPerPage = 20;
        int totalPages = (int) Math.ceil((double) housings.size() / itemsPerPage);
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, housings.size());

        int[] slotOrder = {12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 39, 40, 41, 42, 43};
        int slotIndex = 0;

        for (int i = startIndex; i < endIndex; i++) {
            HousingManager.Housing housing = housings.get(i);
            OfflinePlayer owner = Bukkit.getOfflinePlayer(housing.getOwner());
            if (owner != null) {
                ItemStack playerHead = createPlayerHead(viewer, owner, housing);
                inventory.setItem(slotOrder[slotIndex], playerHead);
                slotIndex++;
            }
        }

        if (page > 1) {
            String name = plugin.getConfig().getString("gui.list.previous-page.name");
            List<String> lore = plugin.getConfig().getStringList("gui.list.previous-page.item.lore");

            ItemStack previousPage = createItem(Material.ARROW, name, lore);
            inventory.setItem(49, previousPage);
        }

        if (page < totalPages) {
            String name = plugin.getConfig().getString("gui.list.next-page.name");
            List<String> lore = plugin.getConfig().getStringList("gui.list.next-page.item.lore");

            ItemStack nextPage = createItem(Material.ARROW, name, lore);
            inventory.setItem(51, nextPage);
        }

        String materialName = plugin.getConfig().getString("gui.list.page-info.material");
        Material pageInfoMaterial = Material.matchMaterial(materialName);
        if (pageInfoMaterial == null) {
            pageInfoMaterial = Material.PAPER;
        }

        String pageInfoName = plugin.getConfig().getString("gui.list.page-info.name")
                .replace("%page%", String.valueOf(page))
                .replace("%total_pages%", String.valueOf(totalPages));

        List<String> pageInfoLore = plugin.getConfig().getStringList("gui.list.page-info.lore");
        List<String> formattedLore = new ArrayList<>();
        for (String line : pageInfoLore) {
            formattedLore.add(line.replace("%total_houses%", String.valueOf(housings.size())));
        }

        ItemStack pageInfo = createItem(pageInfoMaterial, pageInfoName, formattedLore);
        inventory.setItem(50, pageInfo);
    }

    public void open(Player player) {
        int page = playerPages.getOrDefault(player, 1);
        Inventory inventory = createInventory(player, page);
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

    private ItemStack createPlayerHead(Player viewer, OfflinePlayer owner, HousingManager.Housing housing) {
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
                    .map(line -> line.replace("%votes%", String.valueOf(housing.getVotes())))
                    .map(line -> line.replace("%super_votes%", String.valueOf(housing.getSuperVotes())))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .collect(Collectors.toList());
            skullMeta.setDisplayName(displayName);
            skullMeta.setLore(formattedLore);
            if (viewer.isOp()) {
                List<String> opLore = new ArrayList<>(formattedLore);
                opLore.add(ChatColor.translateAlternateColorCodes('&', "&eʀɪɢʜᴛ-ᴄʟɪᴄᴋ ꜱʜɪꜰᴛ ᴛᴏ ꜱᴜᴘᴇʀ ᴠᴏᴛᴇ ꜰᴏʀ ᴛʜɪꜱ ʜᴏᴜꜱɪɴɢ!"));
                skullMeta.setLore(opLore);
            } else {
                skullMeta.setLore(formattedLore);
            }
            if (housing.getSuperVotes() > 0) {
                skullMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                skullMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            playerHead.setItemMeta(skullMeta);
        }
        return playerHead;
    }

    private ItemStack createRandomButton() {
        @SuppressWarnings("deprecation")

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            String skullOwner = plugin.getConfig().getString("gui.list.random-housing.skull-owner");
            meta.setOwner(skullOwner);

            String displayName = plugin.getConfig().getString("gui.list.random-housing.name");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

            List<String> lore = plugin.getConfig().getStringList("gui.list.random-housing.lore");
            List<String> formattedLore = lore.stream()
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .collect(Collectors.toList());
            meta.setLore(formattedLore);

            item.setItemMeta(meta);
        }
        return item;
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
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(GUI_NAME_PREFIX)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        HousingListGUI gui = this;
        if (gui == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        int currentPage = playerPages.getOrDefault(player, 1);

        if (event.getSlot() == 49 && event.getCurrentItem() != null &&
                event.getCurrentItem().getType() == Material.ARROW) {
            if (currentPage > 1) {
                playerPages.put(player, currentPage - 1);
                Inventory newInventory = createInventory(player, currentPage - 1);
                player.openInventory(newInventory);
                openGUIs.put(player, this);
            }
            return;
        }

        if (event.getSlot() == 51 && event.getCurrentItem() != null &&
                event.getCurrentItem().getType() == Material.ARROW) {
            List<HousingManager.Housing> housings = new ArrayList<>(housingManager.getHousings().values());
            int itemsPerPage = 20;
            int totalPages = Math.max(1, (int) Math.ceil((double) housings.size() / itemsPerPage));

            if (currentPage < totalPages) {
                playerPages.put(player, currentPage + 1);
                Inventory newInventory = createInventory(player, currentPage + 1);
                player.openInventory(newInventory);
                openGUIs.put(player, this);
            }
            return;
        }

        if (event.getSlot() == 19) {
            List<HousingManager.Housing> housings = new ArrayList<>(housingManager.getHousings().values());
            if (housings.isEmpty()) {
                String prefix = plugin.getConfig().getString("prefix");
                String msg = plugin.getConfig().getString("messages.commands.permissions.no-housings");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
                player.closeInventory();
                return;
            }

            Random random = new Random();
            HousingManager.Housing randomHousing = housings.get(random.nextInt(housings.size()));

            player.teleport(randomHousing.getCenter());
            OfflinePlayer owner = Bukkit.getOfflinePlayer(randomHousing.getOwner());
            String prefix = plugin.getConfig().getString("prefix");
            String msg = plugin.getConfig().getString("messages.commands.join.joined-housing")
                    .replace("%player%", owner.getName() != null ? owner.getName() : "Unknown");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
            player.closeInventory();
            return;
        }

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

            int CurrentPage = playerPages.getOrDefault(player, 1);

            Inventory newInventory = createInventory(player, CurrentPage);
            player.openInventory(newInventory);
            openGUIs.put(player, this);
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

        SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
        if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;

        OfflinePlayer housingOwner = skullMeta.getOwningPlayer();
        HousingManager.Housing housing = gui.housingManager.findHousingByOwner(housingOwner.getPlayer());
        if (housing != null) {
            String prefix = plugin.getConfig().getString("prefix");
            if (event.isRightClick() && !event.isShiftClick()) {
                if (housing.getOwner().equals(player.getUniqueId())) {
                    String ownerCannotVoteMsg = plugin.getConfig().getString("messages.vote.owner-cannot-vote");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + ownerCannotVoteMsg));
                    player.closeInventory();
                    return;
                }
                UUID lastVotedHousing = playerLastVoted.get(player.getUniqueId());
                if (lastVotedHousing != null && lastVotedHousing.equals(housing.getOwner())) {
                    String alreadyVotedMsg = plugin.getConfig().getString("messages.vote.already-voted");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + alreadyVotedMsg));
                    player.closeInventory();
                } else {
                    housing.addVote();
                    playerLastVoted.put(player.getUniqueId(), housing.getOwner());
                    gui.housingManager.saveHousings();
                    String votedMsg = plugin.getConfig().getString("messages.vote.voted");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + votedMsg.replace("%player%", housingOwner.getName() != null ? housingOwner.getName() : "Unknown")));
                    player.openInventory(createInventory(player, currentPage));
                    return;
                }
            } else if (event.isRightClick() && event.isShiftClick() && player.isOp()) {
                if (housing.getOwner().equals(player.getUniqueId())) {
                    String ownerCannotVoteMsg = plugin.getConfig().getString("messages.vote.owner-cannot-supervote");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + ownerCannotVoteMsg));
                    player.closeInventory();
                    return;
                }
                housing.addSuperVote();
                gui.housingManager.saveHousings();
                String superVotedMsg = plugin.getConfig().getString("messages.vote.super-voted");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + superVotedMsg.replace("%player%", housingOwner.getName() != null ? housingOwner.getName() : "Unknown")));
                player.openInventory(createInventory(player, currentPage));
                return;
            } else if (event.isLeftClick()) {
                player.teleport(housing.getCenter());
                String msg = gui.plugin.getConfig().getString("messages.commands.join.joined-housing").replace("%player%", housingOwner.getName() != null ? housingOwner.getName() : "Unknown");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
            }
        } else {
            String prefix = plugin.getConfig().getString("prefix");
            String msg = gui.plugin.getConfig().getString("messages.commands.permissions.housing-not-found").replace("%player%", housingOwner.getName() != null ? housingOwner.getName() : "Unknown");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
        }
        player.closeInventory();
    }
}
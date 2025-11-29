package dev.meyba.justHousing.managers;

import dev.meyba.justHousing.JustHousing;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatManager {
    private final JustHousing plugin;
    private final HousingManager housingManager;

    public ChatManager(JustHousing plugin, HousingManager housingManager) {
        this.plugin = plugin;
        this.housingManager = housingManager;
    }

    public String formatChatMessage(Player player, String message) {
        String worldName = player.getWorld().getName();
        HousingManager.Housing housing = housingManager.getHousingById(worldName);

        String rank;
        if (housing == null) {
            rank = plugin.getConfig().getString("chat.default-rank", "");
        } else if (player.getUniqueId().equals(housing.getOwner())) {
            rank = plugin.getConfig().getString("chat.ranks.owner", "&4Owner");
        } else {
            HousingManager.Member member = housing.getMembers().get(player.getUniqueId());
            if (member != null) {
                String roleKey = "chat.ranks." + member.getRole().getConfigKey();
                rank = plugin.getConfig().getString(roleKey, "&7Member");
            } else {
                rank = plugin.getConfig().getString("chat.default-rank", "");
            }
        }

        String format = plugin.getConfig().getString("chat.format", "%rank% &f%player% &8» &r%message%");
        String formattedRank = ChatColor.translateAlternateColorCodes('&', rank);
        // Add spacing after rank if rank is not empty
        if (!rank.isEmpty() && !formattedRank.isEmpty()) {
            formattedRank = formattedRank + " ";
        }

        return ChatColor.translateAlternateColorCodes('&', format)
                .replace("%rank%", formattedRank)
                .replace("%player%", player.getName())
                .replace("%message%", message);
    }
}
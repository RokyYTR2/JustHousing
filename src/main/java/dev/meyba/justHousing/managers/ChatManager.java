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
            rank = plugin.getConfig().getString("chat.default-rank");
        } else if (player.getUniqueId().equals(housing.getOwner())) {
            rank = plugin.getConfig().getString("chat.ranks.owner");
        } else {
            rank = plugin.getConfig().getString("chat.ranks.member");
        }

        String format = plugin.getConfig().getString("chat.format");
        return ChatColor.translateAlternateColorCodes('&', format)
                .replace("%rank%", rank)
                .replace("%player%", player.getName())
                .replace("%message%", message);
    }
}
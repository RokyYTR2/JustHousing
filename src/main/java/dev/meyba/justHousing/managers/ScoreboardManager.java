package dev.meyba.justHousing.managers;

import dev.meyba.justHousing.JustHousing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;

public class ScoreboardManager {
    private final JustHousing plugin;
    private final HousingManager housingManager;

    public ScoreboardManager(JustHousing plugin, HousingManager housingManager) {
        this.plugin = plugin;
        this.housingManager = housingManager;
    }

    public void updateScoreboard(Player player) {
        String worldName = player.getWorld().getName();
        HousingManager.Housing housing = housingManager.getHousingById(worldName);

        if (housing == null) {
            clearScoreboard(player);
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        String title = plugin.getConfig().getString("scoreboard.title");
        title = ChatColor.translateAlternateColorCodes('&', title)
                .replace("%name%", housing.getName());
        Objective objective = scoreboard.registerNewObjective("housingInfo", Criteria.DUMMY, title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(housing.getOwner());
        String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
        int playerCount = housing.getMembers().size() + (owner.isOnline() ? 1 : 0);

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int score = lines.size();

        for (String line : lines) {
            String formattedText = ChatColor.translateAlternateColorCodes('&', line)
                    .replace("%name%", housing.getName())
                    .replace("%owner%", ownerName)
                    .replace("%votes%", String.valueOf(housing.getVotes()))
                    .replace("%super_votes%", String.valueOf(housing.getSuperVotes()))
                    .replace("%player_count%", String.valueOf(playerCount))
                    .replace("%members_count%", String.valueOf(housing.getMembers().size()));

            if (formattedText.length() > 40) {
                formattedText = formattedText.substring(0, 40);
            }

            String entryName = "line_" + score;
            Team team = scoreboard.registerNewTeam(entryName);
            team.addEntry(ChatColor.values()[score % ChatColor.values().length].toString());
            team.setPrefix(formattedText);
            objective.getScore(ChatColor.values()[score % ChatColor.values().length].toString()).setScore(score);
            score--;
        }

        player.setScoreboard(scoreboard);
    }

    public void clearScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
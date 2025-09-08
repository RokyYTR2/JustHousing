package dev.meyba.justHousing.managers;

import dev.meyba.justHousing.JustHousing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

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

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("housingInfo", "dummy", ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "&lHousing Info")));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (housing != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(housing.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
            int playerCount = housing.getMembers().size() + (owner.isOnline() ? 1 : 0);

            addScoreboardLine(scoreboard, objective, "name", "&7Name: &a" + housing.getName(), 5);
            addScoreboardLine(scoreboard, objective, "owner", "&7Owner: &a" + ownerName, 4);
            addScoreboardLine(scoreboard, objective, "votes", "&7Votes: &a" + housing.getVotes(), 3);
            addScoreboardLine(scoreboard, objective, "players", "&7Players: &a" + playerCount, 2);
            addScoreboardLine(scoreboard, objective, "members", "&7Members: &a" + housing.getMembers().size(), 1);
        } else {
            addScoreboardLine(scoreboard, objective, "no_housing", "&cNot in a housing", 1);
        }

        player.setScoreboard(scoreboard);
    }

    private void addScoreboardLine(Scoreboard scoreboard, Objective objective, String entryName, String displayText, int score) {
        String formattedText = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.lines." + entryName, displayText));
        if (formattedText.length() > 40) {
            formattedText = formattedText.substring(0, 40);
        }
        Team team = scoreboard.registerNewTeam(entryName);
        team.addEntry(ChatColor.values()[score].toString());
        team.setPrefix(formattedText);
        objective.getScore(ChatColor.values()[score].toString()).setScore(score);
    }

    public void clearScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
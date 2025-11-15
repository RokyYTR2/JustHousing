package dev.meyba.justHousing;

import dev.meyba.justHousing.commands.HousingCommands;
import dev.meyba.justHousing.guis.HousingListGUI;
import dev.meyba.justHousing.listeners.HousingListener;
import dev.meyba.justHousing.managers.ChatManager;
import dev.meyba.justHousing.managers.HousingManager;
import dev.meyba.justHousing.managers.ScoreboardManager;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustHousing extends JavaPlugin {
    private HousingManager housingManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        housingManager = new HousingManager(this);
        HousingListGUI housingListGUI = new HousingListGUI(this, housingManager);
        ChatManager chatManager = new ChatManager(this, housingManager);

        getCommand("housing").setExecutor(new HousingCommands(housingManager, this, housingListGUI));

        getServer().getPluginManager().registerEvents(new HousingListener(housingManager, this, chatManager), this);
        getServer().getPluginManager().registerEvents(housingListGUI, this);

        getLogger().info("JustHousing has been enabled!");
    }

    @Override
    public void onDisable() {
        if (housingManager != null) {
            for (String id : housingManager.getHousings().keySet()) {
                World world = getServer().getWorld(id);
                if (world != null) {
                    world.save();
                }
            }
            housingManager.saveHousings();
        }
        getLogger().info("JustHousing has been disabled!");
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
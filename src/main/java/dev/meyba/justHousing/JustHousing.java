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
    private HousingListGUI housingListGUI;
    private ChatManager chatManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.housingManager = new HousingManager(this);
        this.housingListGUI = new HousingListGUI(this, this.housingManager);
        this.chatManager = new ChatManager(this, this.housingManager);

        this.getCommand("housing").setExecutor(new HousingCommands(this.housingManager, this, this.housingListGUI));

        this.getServer().getPluginManager().registerEvents(new HousingListener(this.housingManager, this, this.chatManager), this);
        this.getServer().getPluginManager().registerEvents(this.housingListGUI, this);

        getLogger().info("Housing has been enabled!");
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
        getLogger().info("Housing has been disabled!");
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
package dev.meyba.justHousing;

import dev.meyba.justHousing.commands.HousingCommands;
import dev.meyba.justHousing.guis.HousingListGUI;
import dev.meyba.justHousing.listeners.HousingListener;
import dev.meyba.justHousing.managers.HousingManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustHousing extends JavaPlugin {
    private HousingManager housingManager;
    private HousingListGUI housingListGUI;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.housingManager = new HousingManager(this);
        this.housingListGUI = new HousingListGUI(this, this.housingManager);

        this.getCommand("housing").setExecutor(new HousingCommands(this.housingManager, this, this.housingListGUI));

        this.getServer().getPluginManager().registerEvents(new HousingListener(this.housingManager, this), this);
        this.getServer().getPluginManager().registerEvents(this.housingListGUI, this);

        getLogger().info("HousingPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HousingPlugin has been disabled!");
    }
}
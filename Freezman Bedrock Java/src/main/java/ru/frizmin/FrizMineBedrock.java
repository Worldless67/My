package ru.frizmin;

import org.bukkit.plugin.java.JavaPlugin;
import ru.frizmin.economy.FrizMineEconomy;

public class FrizMineBedrock extends JavaPlugin {

    private static FrizMineBedrock instance;
    private FrizMineEconomy economy;

    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация экономики
        economy = new FrizMineEconomy(this);
        
        getLogger().info("FrizMine Bedrock has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FrizMine Bedrock has been disabled!");
    }

    public static FrizMineBedrock getInstance() {
        return instance;
    }

    public FrizMineEconomy getEconomy() {
        return economy;
    }
}

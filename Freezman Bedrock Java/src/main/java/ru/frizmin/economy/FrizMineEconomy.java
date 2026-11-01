package ru.frizmin.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class FrizMineEconomy {
    private final Plugin plugin;
    // Храним баланс в памяти (для теста). В будущем нужно сохранять в базу данных или файл.
    private final HashMap<UUID, Double> balances = new HashMap<>();

    public FrizMineEconomy(Plugin plugin) {
        this.plugin = plugin;
        // Пример: даем стартовый баланс всем онлайн игрокам
        Bukkit.getOnlinePlayers().forEach(player -> balances.put(player.getUniqueId(), 100.0));
    }

    public boolean hasEnough(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    public boolean withdraw(Player player, double amount) {
        if (!hasEnough(player, amount)) return false;
        balances.put(player.getUniqueId(), getBalance(player) - amount);
        return true;
    }

    public void deposit(Player player, double amount) {
        balances.put(player.getUniqueId(), getBalance(player) + amount);
    }

    public double getBalance(Player player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }
}

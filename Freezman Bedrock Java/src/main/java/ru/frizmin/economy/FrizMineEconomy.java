package ru.frizmin.economy;

import cn.nukkit.Player;

import java.util.HashMap;
import java.util.UUID;

public class FrizMineEconomy {
    private final HashMap<UUID, Double> balances = new HashMap<>();

    public double getOrCreateBalance(Player player) {
        return balances.computeIfAbsent(player.getUniqueId(), uuid -> 100.0);
    }

    public double getBalance(Player player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
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
}

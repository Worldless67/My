package ru.frizmin.economy;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FrizMineEconomy {

    private final HashMap<UUID, Double> balances = new HashMap<>();
    private final HashMap<UUID, Double> donate = new HashMap<>();
    private final Config config;

    public FrizMineEconomy(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        config = new Config(new File(dataFolder, "balances.yml"), Config.YAML);
        for (String key : config.getKeys(false)) {
            try {
                String[] parts = key.split("\\.");
                UUID id = UUID.fromString(parts[0]);
                double value = Double.parseDouble(config.getString(key));
                if (parts.length == 2 && "donate".equals(parts[1])) {
                    donate.put(id, value);
                } else if (parts.length == 2 && "money".equals(parts[1])) {
                    balances.put(id, value);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Double> e : balances.entrySet()) {
            config.set(e.getKey().toString() + ".money", String.valueOf(e.getValue()));
        }
        for (Map.Entry<UUID, Double> e : donate.entrySet()) {
            config.set(e.getKey().toString() + ".donate", String.valueOf(e.getValue()));
        }
        config.save(true);
    }

    // Игровая валюта (монеты)

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

    // Донат-валюта (ФК-коины)

    public double getDonate(Player player) {
        return donate.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void addDonate(Player player, double amount) {
        donate.put(player.getUniqueId(), getDonate(player) + amount);
    }

    public boolean takeDonate(Player player, double amount) {
        if (getDonate(player) < amount) return false;
        donate.put(player.getUniqueId(), getDonate(player) - amount);
        return true;
    }
}

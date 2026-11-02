package ru.frizmin.economy;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FrizMineHud {

    private final FrizMineEconomy economy;
    private final Map<String, Long> joinTimes = new HashMap<>();

    public FrizMineHud(FrizMineEconomy economy) {
        this.economy = economy;
    }

    public void create(Player p) {
        joinTimes.putIfAbsent(p.getName(), System.currentTimeMillis());
        // Приветственное сообщение
        p.sendMessage(TextFormat.GOLD + "[FrizMine] " + TextFormat.WHITE + "Добро пожаловать на сервер!");
        update(p);
    }

    public void update(Player p) {
        if (p == null || !p.isOnline()) return;

        joinTimes.putIfAbsent(p.getName(), System.currentTimeMillis());
        long played = System.currentTimeMillis() - joinTimes.get(p.getName());
        long minutes = played / 60000;
        String timeStr = minutes < 60 ? minutes + "м" : (minutes / 60) + "ч " + (minutes % 60) + "м";

        String date = new SimpleDateFormat("HH:mm").format(new Date());
        String rank = p.isOp() ? TextFormat.YELLOW + "Создатель" : TextFormat.WHITE + "Игрок";

        // Формируем строку для ActionBar (верхняя полоса)
        String actionBarText = TextFormat.GRAY + "[" + TextFormat.DARK_RED + "Friz" + TextFormat.DARK_AQUA + "Mine" + TextFormat.GRAY + "] " +
                TextFormat.WHITE + "ФК: " + TextFormat.DARK_RED + (int) economy.getDonate(p) +
                TextFormat.GRAY + " | " + TextFormat.WHITE + "Монеты: " + TextFormat.GREEN + (int) economy.getBalance(p) +
                TextFormat.GRAY + " | " + TextFormat.WHITE + "Время: " + TextFormat.AQUA + timeStr;

        // Отправляем в ActionBar (работает на всех версиях Nukkit)
        p.sendActionBar(actionBarText);

        // Дополнительно: раз в 10 секунд можно слать статус в чат, если нужно, 
        // но для производительности лучше оставить только ActionBar.
    }

    public void remove(Player p) {
        joinTimes.remove(p.getName());
        if (p != null && p.isOnline()) {
            p.sendActionBar(TextFormat.GRAY + "[FrizMine] До встречи!");
        }
    }
}

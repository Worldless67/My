package ru.frizmin.economy;

import cn.nukkit.Player;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.scoreboard.data.SortType;
import cn.nukkit.scoreboard.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class FrizMineHud {

    private final FrizMineEconomy economy;
    private final HashMap<String, Scoreboard> boards = new HashMap<>();
    private final HashMap<String, Long> joinTimes = new HashMap<>();

    public FrizMineHud(FrizMineEconomy economy) {
        this.economy = economy;
    }

    public void create(Player p) {
        remove(p);
        joinTimes.put(p.getName(), System.currentTimeMillis());
        Scoreboard sb = new Scoreboard(
                "friz_hud_" + p.getName().toLowerCase(),
                "§d§lFriz§fMine",
                DisplaySlot.SIDEBAR,
                SortType.DESCENDING
        );
        boards.put(p.getName(), sb);
        sb.addViewer(p);
        update(p);
    }

    public void remove(Player p) {
        Scoreboard sb = boards.remove(p.getName());
        joinTimes.remove(p.getName());
        if (sb != null) {
            sb.remove();
        }
    }

    public void update(Player p) {
        Scoreboard sb = boards.get(p.getName());
        if (sb == null) return;

        long played = System.currentTimeMillis() - joinTimes.getOrDefault(p.getName(), System.currentTimeMillis());
        long minutes = played / 60000;
        String timeStr = minutes < 60 ? minutes + "м" : (minutes / 60) + "ч " + (minutes % 60) + "м";

        String date = new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date());
        String rank = p.isOp() ? "§6Создатель" : "§fИгрок";

        sb.setLine(1, "§fНик: §b" + p.getName());
        sb.setLine(2, "§fРанг: " + rank);
        sb.setLine(3, "§fБаланс: §a" + (int) economy.getBalance(p));
        sb.setLine(4, "§fФК-коины: §d" + (int) economy.getDonate(p));
        sb.setLine(5, "§fКоорд: §eX:" + (int) p.getX() + " Y:" + (int) p.getY() + " Z:" + (int) p.getZ());
        sb.setLine(6, "§fНаиграно: §b" + timeStr);
        sb.setLine(7, "§fОнлайн: §6" + p.getServer().getOnlinePlayers().size());
        sb.setLine(8, "§f" + date);
    }
}

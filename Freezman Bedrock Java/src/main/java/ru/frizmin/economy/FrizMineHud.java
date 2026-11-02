package ru.frizmin.economy;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.types.DisplaySlot;
import cn.nukkit.network.protocol.types.SortOrder;
import cn.nukkit.scoreboard.scoreboard.Scoreboard;
import cn.nukkit.scoreboard.scorer.FakeScorer;

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
        joinTimes.putIfAbsent(p.getName(), System.currentTimeMillis());
        update(p);
    }

    public void remove(Player p) {
        joinTimes.remove(p.getName());
        Scoreboard sb = boards.remove(p.getName());
        if (sb != null) {
            try {
                sb.removeViewer(p, DisplaySlot.SIDEBAR);
            } catch (Exception ignored) {
            }
        }
    }

    public void update(Player p) {
        // Убираем старую панель и создаём новую с актуальными строками
        Scoreboard old = boards.remove(p.getName());
        if (old != null) {
            try {
                old.removeViewer(p, DisplaySlot.SIDEBAR);
            } catch (Exception ignored) {
            }
        }

        joinTimes.putIfAbsent(p.getName(), System.currentTimeMillis());
        long played = System.currentTimeMillis() - joinTimes.get(p.getName());
        long minutes = played / 60000;
        String timeStr = minutes < 60 ? minutes + "м" : (minutes / 60) + "ч " + (minutes % 60) + "м";

        String date = new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date());
        String rank = p.isOp() ? "§6Создатель" : "§fИгрок";

        Scoreboard sb = new Scoreboard(
                "friz_hud_" + p.getName().toLowerCase(),
                "§d§lFriz§fMine",
                "dummy",
                SortOrder.DESCENDING
        );

        sb.addLine(new FakeScorer("§f" + date), 9);
        sb.addLine(new FakeScorer("§fОнлайн: §6" + p.getServer().getOnlinePlayers().size()), 8);
        sb.addLine(new FakeScorer("§fНаиграно: §b" + timeStr), 7);
        sb.addLine(new FakeScorer("§fКоорд: §eX:" + p.getFloorX() + " Y:" + p.getFloorY() + " Z:" + p.getFloorZ()), 6);
        sb.addLine(new FakeScorer("§fФК-коины: §d" + (int) economy.getDonate(p)), 5);
        sb.addLine(new FakeScorer("§fБаланс: §a" + (int) economy.getBalance(p)), 4);
        sb.addLine(new FakeScorer("§fРанг: " + rank), 3);
        sb.addLine(new FakeScorer("§fНик: §b" + p.getName()), 2);

        sb.addViewer(p, DisplaySlot.SIDEBAR);
        boards.put(p.getName(), sb);
    }
}


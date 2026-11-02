package ru.frizmin.economy;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.RemoveObjectivePacket;
import cn.nukkit.network.protocol.SetDisplayObjectivePacket;
import cn.nukkit.network.protocol.SetScorePacket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrizMineHud {

    private static final String OBJECTIVE_ID = "friz_hud";

    private final FrizMineEconomy economy;
    private final Map<String, Long> joinTimes = new HashMap<>();
    private final Map<String, String> linesCache = new HashMap<>();

    public FrizMineHud(FrizMineEconomy economy) {
        this.economy = economy;
    }

    public void create(Player p) {
        joinTimes.putIfAbsent(p.getName(), System.currentTimeMillis());
        sendObjective(p);
        update(p);
    }

    private void sendObjective(Player p) {
        RemoveObjectivePacket remove = new RemoveObjectivePacket();
        remove.objectiveName = OBJECTIVE_ID;
        p.dataPacket(remove);

        SetDisplayObjectivePacket dp = new SetDisplayObjectivePacket();
        dp.displaySlot = "sidebar";
        dp.objectiveName = OBJECTIVE_ID;
        dp.criteria = "dummy";
        dp.displayName = "§d§lFriz§fMine";
        dp.sortOrder = 1; // 1 = по убыванию, дата и онлайн будут сверху
        p.dataPacket(dp);
    }

    public void update(Player p) {
        if (p == null || !p.isOnline()) return;

        joinTimes.putIfAbsent(p.getName(), System.currentTimeMillis());
        long played = System.currentTimeMillis() - joinTimes.get(p.getName());
        long minutes = played / 60000;
        String timeStr = minutes < 60 ? minutes + "м" : (minutes / 60) + "ч " + (minutes % 60) + "м";

        String date = new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date());
        String rank = p.isOp() ? "§6Создатель" : "§fИгрок";

        List<String> lines = new ArrayList<>();
        lines.add("§f" + date);
        lines.add("§fОнлайн: §6" + p.getServer().getOnlinePlayers().size());
        lines.add("§fНаиграно: §b" + timeStr);
        lines.add("§fКоорд: §eX:" + p.getFloorX() + " Y:" + p.getFloorY() + " Z:" + p.getFloorZ());
        lines.add("§fФК-коины: §d" + (int) economy.getDonate(p));
        lines.add("§fБаланс: §a" + (int) economy.getBalance(p));
        lines.add("§fРанг: " + rank);
        lines.add("§fНик: §b" + p.getName());

        // Не спамим пакетами: отправляем только если строки изменились
        String content = String.join("\n", lines);
        if (content.equals(linesCache.get(p.getName()))) return;
        linesCache.put(p.getName(), content);

        SetScorePacket sp = new SetScorePacket();
        sp.action = 0; // 0 = установить
        long scoreId = 1;
        for (int i = lines.size() - 1; i >= 0; i--) {
            SetScorePacket.ScoreEntry entry = new SetScorePacket.ScoreEntry();
            entry.scoreId = scoreId++;
            entry.objectiveId = OBJECTIVE_ID;
            entry.score = i + 1;
            entry.scoreboardName = lines.get(i);
            entry.entryType = 3; // 3 = текстовая строка (fake player)
            sp.entries.add(entry);
        }
        p.dataPacket(sp);
    }

    public void remove(Player p) {
        joinTimes.remove(p.getName());
        linesCache.remove(p.getName());
        if (p != null && p.isOnline()) {
            RemoveObjectivePacket remove = new RemoveObjectivePacket();
            remove.objectiveName = OBJECTIVE_ID;
            p.dataPacket(remove);
        }
    }
}

package ru.frizmin.economy;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.SimpleForm;
import cn.nukkit.item.Item;

public class FrizMineSeller {

    public static final String FORM_TITLE = "§6Скупщик";

    private static final int[] ITEM_IDS = {
            Item.DIAMOND, Item.EMERALD, Item.GOLD_INGOT, Item.IRON_INGOT,
            Item.COAL, Item.REDSTONE, Item.OBSIDIAN, Item.NETHERITE_INGOT
    };
    private static final String[] NAMES = {
            "Алмаз", "Изумруд", "Золотой слиток", "Железный слиток",
            "Уголь", "Красная пыль", "Обсидиан", "Незеритовый слиток"
    };
    private static final double[] PRICES = {25, 30, 10, 8, 2, 3, 15, 200};

    public static SimpleForm createForm(Player p, double balance) {
        SimpleForm form = new SimpleForm(FORM_TITLE,
                "§fСкупщик покупает ресурсы за монеты.\n§fТвой баланс: §a" + (int) balance
                        + " монет\n\n§7Нажми на ресурс, чтобы продать ВСЕ такие предметы из инвентаря:");
        for (int i = 0; i < NAMES.length; i++) {
            form.addButton(new ElementButton(NAMES[i] + " §7— §a" + (int) PRICES[i] + " монет/шт"));
        }
        return form;
    }

    public static void sell(FrizMineEconomy economy, Player p, int index) {
        if (index < 0 || index >= ITEM_IDS.length) return;
        int itemId = ITEM_IDS[index];
        int total = 0;

        for (Item slot : p.getInventory().getContents().values()) {
            if (slot != null && !slot.isNull() && slot.getId() == itemId) {
                total += slot.getCount();
            }
        }

        if (total == 0) {
            p.sendMessage("§c[Скупщик] §fУ тебя нет: §b" + NAMES[index]);
            return;
        }

        // Убираем все предметы этого типа из инвентаря
        p.getInventory().remove(new Item(itemId, 0, 1));

        double earned = total * PRICES[index];
        economy.deposit(p, earned);
        p.sendMessage("§6[Скупщик] §fПродано: §b" + total + " x " + NAMES[index]
                + " §fза §a" + (int) earned + " монет.");
    }
}

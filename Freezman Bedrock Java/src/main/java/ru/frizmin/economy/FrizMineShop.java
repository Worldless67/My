package ru.frizmin.economy;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.potion.Effect;

import java.util.ArrayList;

public class FrizMineShop {

    public static final String FORM_TITLE = "§dДонат-магазин";

    public static class ShopEntry {
        final String name;
        final int price;
        final Item item;
        final int effectId; // -1 если не талисман

        ShopEntry(String name, int price, Item item, int effectId) {
            this.name = name;
            this.price = price;
            this.item = item;
            this.effectId = effectId;
        }
    }

    private static final ArrayList<ShopEntry> ENTRIES = new ArrayList<>();

    static {
        ENTRIES.add(new ShopEntry("Алмазы x8", 10, Item.get(Item.DIAMOND, 0, 8), -1));
        ENTRIES.add(new ShopEntry("Золото x32", 5, Item.get(Item.GOLD_INGOT, 0, 32), -1));
        ENTRIES.add(new ShopEntry("Изумруды x4", 15, Item.get(Item.EMERALD, 0, 4), -1));
        ENTRIES.add(new ShopEntry("Железо x64", 3, Item.get(Item.IRON_INGOT, 0, 64), -1));
        ENTRIES.add(new ShopEntry("Обсидиан x16", 8, Item.get(Item.OBSIDIAN, 0, 16), -1));
        ENTRIES.add(new ShopEntry("Зачар. золотое яблоко x1", 25, Item.get(466, 0, 1), -1));
        ENTRIES.add(new ShopEntry("Тотем бессмертия x1", 40, Item.get(449, 0, 1), -1));

        Item godSword = Item.get(Item.NETHERITE_SWORD, 0, 1);
        godSword.setCustomName("§6Меч Богов");
        godSword.addEnchantment(Enchantment.getEnchantment(9).setLevel(7)); // 9 = острота
        ENTRIES.add(new ShopEntry("Меч Богов (Острота 7)", 120, godSword, -1));

        Item drill = Item.get(Item.NETHERITE_PICKAXE, 0, 1);
        drill.setCustomName("§cБур");
        drill.addEnchantment(Enchantment.getEnchantment(15).setLevel(7)); // 15 = эффективность
        ENTRIES.add(new ShopEntry("Бур (Эффективность 7)", 100, drill, -1));

        Item titanArmor = Item.get(Item.NETHERITE_CHESTPLATE, 0, 1);
        titanArmor.setCustomName("§bБроня Титана");
        titanArmor.addEnchantment(Enchantment.getEnchantment(0).setLevel(7)); // 0 = защита
        ENTRIES.add(new ShopEntry("Броня Титана (Защита 7)", 150, titanArmor, -1));

        Item t1 = Item.get(Item.NETHER_STAR, 0, 1);
        t1.setCustomName("§eТалисман Скорости");
        ENTRIES.add(new ShopEntry("Талисман Скорости", 60, t1, 1)); // 1 = скорость

        Item t2 = Item.get(Item.NETHER_STAR, 0, 1);
        t2.setCustomName("§aТалисман Рудокопа");
        ENTRIES.add(new ShopEntry("Талисман Рудокопа (спешка)", 60, t2, 3)); // 3 = спешка

        Item t3 = Item.get(Item.NETHER_STAR, 0, 1);
        t3.setCustomName("§cТалисман Жизни");
        ENTRIES.add(new ShopEntry("Талисман Жизни (регенерация)", 80, t3, 10)); // 10 = регенерация
    }

    public static FormWindowSimple createForm(Player p, double donateBalance) {
        FormWindowSimple form = new FormWindowSimple(FORM_TITLE,
                "§fТвой баланс ФК-коинов: §d" + (int) donateBalance
                        + "\n§7Талисманы работают, пока лежат в инвентаре!\n\n§fВыбери товар:");
        for (ShopEntry e : ENTRIES) {
            form.addButton(new ElementButton(e.name + " §7(§d" + e.price + " ФК§7)"));
        }
        return form;
    }

    public static void buy(FrizMineEconomy economy, Player p, int index) {
        if (index < 0 || index >= ENTRIES.size()) return;
        ShopEntry entry = ENTRIES.get(index);
        if (!economy.takeDonate(p, entry.price)) {
            p.sendMessage("§c[Магазин] §fНедостаточно ФК-коинов! Нужно: §d" + entry.price);
            return;
        }
        p.getInventory().addItem(entry.item.clone());
        p.sendMessage("§a[Магазин] §fКуплено: §b" + entry.name + " §fза §d" + entry.price + " ФК-коинов.");
    }

    public static void applyTalismans(Player p) {
        for (Item item : p.getInventory().getContents().values()) {
            if (item == null || !item.hasCustomName()) continue;
            String name = item.getCustomName();
            for (ShopEntry e : ENTRIES) {
                if (e.effectId < 0 || e.item.getCustomName() == null) continue;
                if (name.equals(e.item.getCustomName())) {
                    Effect effect = Effect.getEffect(e.effectId);
                    if (effect != null) {
                        effect.setDuration(80);
                        effect.setVisible(false);
                        p.addEffect(effect);
                    }
                    break;
                }
            }
        }
    }
}

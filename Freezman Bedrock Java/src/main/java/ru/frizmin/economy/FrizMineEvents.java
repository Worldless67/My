package ru.frizmin.economy;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import ru.frizmin.FrizMineBedrock;

import java.util.Random;

public class FrizMineEvents {

    private static final Random RANDOM = new Random();
    private static Position lastMeteorPos = null;
    private static Position lastBeaconPos = null;
    private static Position lastMysticPos = null;
    private static Position lastBossPos = null;

    public static Position getLastMeteor() { return lastMeteorPos; }
    public static Position getLastBeacon() { return lastBeaconPos; }
    public static Position getLastMystic() { return lastMysticPos; }
    public static Position getLastBoss() { return lastBossPos; }

    public static void runMeteorEvent(FrizMineBedrock plugin) {
        Level level = Server.getInstance().getDefaultLevel();
        if (level == null) return;

        int x = RANDOM.nextInt(2000) - 1000;
        int z = RANDOM.nextInt(2000) - 1000;
        int y = level.getHighestBlockAt(x, z) + 100;

        lastMeteorPos = new Position(x, y, z, level);

        Entity fireball = level.spawnEntity("fireball", new Vector3(x, y, z));
        fireball.setMotion(new Vector3(0, -1, 0));

        spawnLootChest(level, x + 2, z + 2);

        Server.getInstance().broadcastMessage(
                "§c[Ивент] §fМетеорит упал! Координаты: §aX: " + x + ", Y: " + (y - 100) + ", Z: " + z);
    }

    public static void runBeaconEvent(FrizMineBedrock plugin) {
        Level level = Server.getInstance().getDefaultLevel();
        if (level == null) return;

        int x = RANDOM.nextInt(2000) - 1000;
        int z = RANDOM.nextInt(2000) - 1000;
        int y = level.getHighestBlockAt(x, z) + 1;

        lastBeaconPos = new Position(x, y, z, level);

        level.setBlock(new Vector3(x, y, z), Block.get(Block.BEACON));

        spawnLootChest(level, x + 2, z + 2);

        Server.getInstance().broadcastMessage(
                "§6[Ивент] §fМаяк-убийца появился! Координаты: §aX: " + x + ", Y: " + y + ", Z: " + z);
    }

    public static void runMysticEvent(FrizMineBedrock plugin) {
        Level level = Server.getInstance().getDefaultLevel();
        if (level == null) return;

        int x = RANDOM.nextInt(2000) - 1000;
        int z = RANDOM.nextInt(2000) - 1000;
        int y = level.getHighestBlockAt(x, z) + 1;

        lastMysticPos = new Position(x, y, z, level);

        level.setBlock(new Vector3(x, y, z), Block.get(Block.CHEST));

        BlockEntityChest chest = (BlockEntityChest) level.getBlockEntity(new Vector3(x, y, z));
        if (chest != null) {
            int[][] pool = {
                    {Item.DIAMOND, 8}, {Item.EMERALD, 6}, {Item.GOLD_INGOT, 24},
                    {Item.IRON_INGOT, 32}, {Item.NETHERITE_INGOT, 2},
                    {466, 2}, {449, 1}, {Item.OBSIDIAN, 16}
            };
            for (int i = 0; i < 3; i++) {
                int[] pick = pool[RANDOM.nextInt(pool.length)];
                chest.getInventory().addItem(Item.get(pick[0], 0, pick[1]));
            }
        }

        Server.getInstance().broadcastMessage(
                "§5[Мистик] §fМистический сундук появился! Координаты: §aX: " + x + ", Y: " + y + ", Z: " + z);
    }

    public static void runBossEvent(FrizMineBedrock plugin) {
        Level level = Server.getInstance().getDefaultLevel();
        if (level == null) return;

        int x = RANDOM.nextInt(1000) - 500;
        int z = RANDOM.nextInt(1000) - 500;
        int y = level.getHighestBlockAt(x, z) + 1;

        lastBossPos = new Position(x, y, z, level);

        Entity boss = level.spawnEntity("Zombie", new Vector3(x, y, z));
        boss.setNameTag("§4БОСС Хранитель Подземелий");
        boss.setNameTagVisible(true);
        boss.setMaxHealth(300);
        boss.setHealth(300);

        Server.getInstance().broadcastMessage(
                "§4[Босс] §fБосс появился! Координаты: §aX: " + x + ", Y: " + y + ", Z: " + z);
    }

    private static void spawnLootChest(Level level, int x, int z) {
        int y = level.getHighestBlockAt(x, z) + 1;

        level.setBlock(new Vector3(x, y, z), Block.get(Block.CHEST));

        BlockEntityChest chest = (BlockEntityChest) level.getBlockEntity(new Vector3(x, y, z));
        if (chest != null) {
            chest.getInventory().addItem(
                    Item.get(Item.DIAMOND, 0, 16),
                    Item.get(Item.NETHERITE_INGOT, 0, 8),
                    Item.get(724, 0, 4),
                    Item.get(Item.GOLDEN_APPLE, 0, 4),
                    Item.get(449, 0, 2)
            );
        }
    }
}

package ru.frizmin.economy;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockBeacon;
import cn.nukkit.block.BlockChest;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityFireball;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;

import java.util.Random;

public class FrizMineEvents {

    private static final Random RANDOM = new Random();
    private static Position lastMeteorPos = null;
    private static Position lastBeaconPos = null;
    private static Position lastMysticPos = null;
    private static Position lastBossPos = null;

    public static Position getLastMeteor() {
        return lastMeteorPos;
    }

    public static Position getLastBeacon() {
        return lastBeaconPos;
    }

    public static Position getLastMystic() {
        return lastMysticPos;
    }

    public static Position getLastBoss() {
        return lastBossPos;
    }

    public static void runMeteorEvent(FrizMineBedrock plugin) {
        Level level = Server.getInstance().getDefaultLevel();
        if (level == null) return;

        int x = RANDOM.nextInt(2000) - 1000;
        int z = RANDOM.nextInt(2000) - 1000;
        int y = level.getHighestBlockAt(x, z) + 100;

        Position pos = new Position(x, y, z, level);
        lastMeteorPos = pos;

        Entity fireball = level.spawnEntity("fireball", pos);
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

        Position pos = new Position(x, y, z, level);
        lastBeaconPos = pos;

        Block beaconBlock = new BlockBeacon();
        beaconBlock.setPosition(pos);
        level.setBlock(pos, beaconBlock, true, true);

        spawnLootChest(level, x + 2, z + 2);

        Server.getInstance().broadcastMessage(
                "§6[Ивент] §fМаяк-убийца появился! Координаты: §aX: " + x + ", Y: " + y + ", Z: " + z);
    }

    // Мистический сундук (мистик) — рандомный лут
    public static void runMysticEvent(FrizMineBedrock plugin) {
        Level level = Server.getInstance().getDefaultLevel();
        if (level == null) return;

        int x = RANDOM.nextInt(2000) - 1000;
        int z = RANDOM.nextInt(2000) - 1000;
        int y = level.getHighestBlockAt(x, z) + 1;

        Position pos = new Position(x, y, z, level);
        lastMysticPos = pos;

        Block chestBlock = new BlockChest();
        chestBlock.setPosition(pos);
        level.setBlock(pos, chestBlock, true, true);

        BlockEntityChest chest = (BlockEntityChest) level.getBlockEntity(pos);
        if (chest != null) {
            int[][] pool = {
                    {Item.DIAMOND, 8}, {Item.EMERALD, 6}, {Item.GOLD_INGOT, 24},
                    {Item.IRON_INGOT, 32}, {Item.NETHERITE_INGOT, 2},
                    {Item.ENCHANT_GOLDEN_APPLE, 2}, {Item.TOTEM, 1}, {Item.OBSIDIAN, 16}
            };
            for (int i = 0; i < 3; i++) {
                int[] pick = pool[RANDOM.nextInt(pool.length)];
                chest.getInventory().addItem(Item.get(pick[0], 0, pick[1]));
            }
        }

        Server.getInstance().broadcastMessage(
                "§5[Мистик] §fМистический сундук появился! Координаты: §aX: " + x + ", Y: " + y + ", Z: " + z);
    }

    // Босс — усиленный зомби, убийца получает 250 ФК
    public static void runBossEvent(FrizMineBedrock plugin) {
        Level level = Server.getInstance().getDefaultLevel();
        if (level == null) return;

        int x = RANDOM.nextInt(1000) - 500;
        int z = RANDOM.nextInt(1000) - 500;
        int y = level.getHighestBlockAt(x, z) + 1;

        Position pos = new Position(x, y, z, level);
        lastBossPos = pos;

        Entity boss = level.spawnEntity("Zombie", pos);
        boss.setNameTag("§4БОСС Хранитель Подземелий");
        boss.setNameTagVisible(true);
        boss.setMaxHealth(300);
        boss.setHealth(300);

        Server.getInstance().broadcastMessage(
                "§4[Босс] §fБосс появился! Координаты: §aX: " + x + ", Y: " + y + ", Z: " + z);
    }

    private static void spawnLootChest(Level level, int x, int z) {
        int y = level.getHighestBlockAt(x, z) + 1;
        Position chestPos = new Position(x, y, z, level);

        Block chestBlock = new BlockChest();
        chestBlock.setPosition(chestPos);
        level.setBlock(chestPos, chestBlock, true, true);

        BlockEntityChest chest = (BlockEntityChest) level.getBlockEntity(chestPos);
        if (chest != null) {
            chest.getInventory().addItem(
                    Item.get(Item.DIAMOND, 0, 16),
                    Item.get(Item.NETHERITE_INGOT, 0, 8),
                    Item.get(Item.ENCHANTED_BOOK, 0, 4),
                    Item.get(Item.GOLDEN_APPLE, 0, 4),
                    Item.get(Item.TOTEM, 0, 2)
            );
        }
    }
}

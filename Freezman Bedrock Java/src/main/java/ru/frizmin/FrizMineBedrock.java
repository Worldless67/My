package ru.frizmin;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.damage.EntityDamageByEntityEvent;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import ru.frizmin.economy.FrizMineEconomy;
import ru.frizmin.economy.FrizMineEvents;
import ru.frizmin.economy.FrizMineHud;
import ru.frizmin.economy.FrizMineSeller;
import ru.frizmin.economy.FrizMineShop;

import java.util.HashMap;
import java.util.Random;

public class FrizMineBedrock extends PluginBase implements Listener {

    private FrizMineEconomy economy;
    private FrizMineHud hud;
    private final Random RANDOM = new Random();
    private final HashMap<String, Integer> killCount = new HashMap<>();

    @Override
    public void onEnable() {
        economy = new FrizMineEconomy(getDataFolder());
        hud = new FrizMineHud(economy);
        getServer().getPluginManager().registerEvents(this, this);

        // Ивенты
        getServer().getScheduler().scheduleRepeatingTask(this,
                () -> FrizMineEvents.runBeaconEvent(this), 72000, 72000);
        getServer().getScheduler().scheduleRepeatingTask(this,
                () -> FrizMineEvents.runMeteorEvent(this), 144000, 144000);
        getServer().getScheduler().scheduleRepeatingTask(this,
                () -> FrizMineEvents.runMysticEvent(this), 36000, 36000);
        getServer().getScheduler().scheduleRepeatingTask(this,
                () -> FrizMineEvents.runBossEvent(this), 216000, 216000);

        // HUD + талисманы (каждые 3 секунды)
        getServer().getScheduler().scheduleRepeatingTask(this, () -> {
            for (Player p : getServer().getOnlinePlayers().values()) {
                hud.update(p);
                FrizMineShop.applyTalismans(p);
            }
        }, 60, 60);

        getServer().getScheduler().scheduleRepeatingTask(this,
                () -> economy.saveAll(), 1200, 1200);

        getLogger().info("FrizMine Bedrock has been enabled!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        economy.getOrCreateBalance(event.getPlayer());
        hud.create(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        hud.remove(event.getPlayer());
        economy.saveAll();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // ИСПРАВЛЕНИЕ: getKiller() часто отсутствует в форках. Используем Cause.
        Entity killer = event.getCause().getEntity();
        
        if (!(killer instanceof Player)) return;
        Player player = (Player) killer;

        String tag = event.getEntity().getNameTag() == null ? "" : event.getEntity().getNameTag();
        if (tag.contains("БОСС")) {
            economy.addDonate(player, 250);
            getServer().broadcastMessage("§4[Босс] §fИгрок §b" + player.getName()
                    + " §fубил Босса и получил §d250 ФК§f!");
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            int count = killCount.merge(player.getName(), 1, Integer::sum);
            if (count == 10) {
                economy.addDonate(player, 50);
                player.sendMessage("§a[Испытание] §fУбито 10 монстров! Награда: §d50 ФК");
                killCount.put(player.getName(), 0);
            }
        }
    }

    @EventHandler
    public void onForm(PlayerFormRespondedEvent event) {
        if (!(event.getWindow() instanceof FormWindowSimple)) return;
        if (event.getResponse() == null) return;
        int buttonId = ((FormResponseSimple) event.getResponse()).getClickedButtonId();

        String title = event.getWindow().getTitle();
        if (FrizMineShop.FORM_TITLE.equals(title)) {
            FrizMineShop.buy(economy, event.getPlayer(), buttonId);
        } else if (FrizMineSeller.FORM_TITLE.equals(title)) {
            FrizMineSeller.sell(economy, event.getPlayer(), buttonId);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args) {
        switch (command.getName().toLowerCase()) {
            case "economy": {
                if (sender instanceof Player) {
                    sender.sendMessage("Твой баланс: §a" + (int) economy.getBalance((Player) sender) + " монет");
                } else {
                    sender.sendMessage("Эту команду может использовать только игрок");
                }
                return true;
            }
            case "kit": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                player.getInventory().addItem(
                        Item.get(Item.IRON_SWORD),
                        Item.get(Item.IRON_PICKAXE),
                        Item.get(Item.IRON_AXE),
                        Item.get(Item.IRON_CHESTPLATE),
                        Item.get(Item.IRON_BOOTS),
                        Item.get(Item.BREAD, 0, 32),
                        Item.get(Item.TORCH, 0, 16)
                );
                player.sendMessage("§a[Кит] §fКит старт выдан! Удачной игры!");
                return true;
            }
            case "eventtp": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                if (!sender.hasPermission("frizmine.event.tp")) return noPerm(sender);
                if (args.length < 1) {
                    sender.sendMessage("Использование: /eventtp <meteor|beacon|mystic|boss>");
                    return true;
                }
                Player player = (Player) sender;
                Position target = null;
                if (args.equalsIgnoreCase("meteor")) target = FrizMineEvents.getLastMeteor();
                else if (args.equalsIgnoreCase("beacon")) target = FrizMineEvents.getLastBeacon();
                else if (args.equalsIgnoreCase("mystic")) target = FrizMineEvents.getLastMystic();
                else if (args.equalsIgnoreCase("boss")) target = FrizMineEvents.getLastBoss();
                if (target == null) {
                    sender.sendMessage("§cИвент ещё не запускался с момента запуска сервера.");
                    return true;
                }
                player.teleport(target);
                sender.sendMessage("§aТелепортация на ивент выполнена!");
                return true;
            }
            case "spawnmeteor": {
                if (!sender.hasPermission("frizmine.event.admin")) return noPerm(sender);
                FrizMineEvents.runMeteorEvent(this);
                sender.sendMessage("§a[Админ] §fМетеорит заспавнен!");
                return true;
            }
            case "spawnbeacon": {
                if (!sender.hasPermission("frizmine.event.admin")) return noPerm(sender);
                FrizMineEvents.runBeaconEvent(this);
                sender.sendMessage("§a[Админ] §fМаяк-убийца заспавнен!");
                return true;
            }
            case "mystic": {
                if (!sender.hasPermission("frizmine.event.admin")) return noPerm(sender);
                FrizMineEvents.runMysticEvent(this);
                sender.sendMessage("§a[Админ] §fМистик заспавнен!");
                return true;
            }
            case "boss": {
                if (!sender.hasPermission("frizmine.event.admin")) return noPerm(sender);
                FrizMineEvents.runBossEvent(this);
                sender.sendMessage("§a[Админ] §fБосс заспавнен!");
                return true;
            }
            case "donate": case "dm": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                if (command.getName().equalsIgnoreCase("donate")
                        && args.length >= 3 && args.equalsIgnoreCase("give")) {
                    if (!sender.hasPermission("frizmine.donate.give")) return noPerm(sender);
                    return giveCoins(player, args, args, sender);
                }
                player.showFormWindow(FrizMineShop.createForm(player, economy.getDonate(player)));
                return true;
            }
            case "shop": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                ((Player) sender).showFormWindow(FrizMineShop.createForm((Player) sender,
                        economy.getDonate((Player) sender)));
                return true;
            }
            case "sell": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                ((Player) sender).showFormWindow(FrizMineSeller.createForm((Player) sender,
                        economy.getBalance((Player) sender)));
                return true;
            }
            case "case": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                openCase((Player) sender);
                return true;
            }
            case "challenges": {
                int kills = killCount.getOrDefault(sender.getName(), 0);
                sender.sendMessage("§6[Испытания дня]§f");
                sender.sendMessage("§7• Убей 10 монстров — §a50 ФК §7(прогресс: §e"
                        + Math.min(kills, 10) + "§7/§e10§7)");
                sender.sendMessage("§7• Убей Босса — §a250 ФК");
                sender.sendMessage("§7• Обнаружь мистик — лут в сундуке");
                return true;
            }
            case "fccoin": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                if (args.length >= 3 && args.equalsIgnoreCase("give")) {
                    if (!sender.hasPermission("frizmine.donate.give")) return noPerm(sender);
                    return giveCoins(player, args, args, sender);
                }
                sender.sendMessage("Использование: /fccoin give <ник> <сумма>");
                sender.sendMessage("Твой баланс ФК-коинов: §d" + (int) economy.getDonate(player));
                return true;
            }
            case "gif": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                if (!sender.hasPermission("frizmine.gif.use")) return noPerm(sender);
                if (args.length < 1) {
                    sender.sendMessage("Использование: /gif <предмет> [кол-во]");
                    return true;
                }
                Player player = (Player) sender;
                Item item = Item.fromString(args);
                if (item.isNull()) {
                    sender.sendMessage("§cНеизвестный предмет: " + args);
                    return true;
                }
                int count = 1;
                if (args.length >= 2) {
                    try {
                        count = Integer.parseInt(args);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cКоличество должно быть числом.");
                        return true;
                    }
                }
                item.setCount(count);
                player.getInventory().addItem(item);
                sender.sendMessage("§aВыдано: §b" + count + " x " + args);
                return true;
            }
            case "map": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                ((Player) sender).getInventory().addItem(Item.get(Item.FILLED_MAP, 0, 1));
                ((Player) sender).sendMessage("§a[Карта] §fКарта выдана! Исследуй мир.");
                return true;
            }
            case "feed": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                player.getFoodData().setLevel(20);
                player.sendMessage("§a[Воин] §fГолод утолён!");
                return true;
            }
            case "nv": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                cn.nukkit.effect.Effect nv = cn.nukkit.effect.Effect.getEffect(16);
                if (nv != null) {
                    nv.setDuration(20 * 300);
                    nv.setVisible(false);
                    player.addEffect(nv);
                }
                player.sendMessage("§a[Воин] §fНочное зрение включено на 5 минут!");
                return true;
            }
            case "ec": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                ((Player) sender).addWindow(((Player) sender).getEnderChestInventory());
                return true;
            }
            case "exp": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                player.addExperience(500);
                player.sendMessage("§a[Странник] §fПолучено 500 опыта!");
                return true;
            }
            case "craft": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                ((Player) sender).sendMessage("§7Верстак по команде временно недоступен.");
                return true;
            }
            case "anvil": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                ((Player) sender).sendMessage("§7Наковальня по команде временно недоступна.");
                return true;
            }
            case "heal": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setLevel(20);
                player.sendMessage("§a[Лорд] §fВы полностью исцелены!");
                return true;
            }
            case "ptime": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                // ИСПРАВЛЕНИЕ: setPlayerTime -> setTime
                if (args.length >= 1 && args.equalsIgnoreCase("night")) {
                    player.setTime(14000, false);
                    player.sendMessage("§a[Лорд] §fВаше личное время: ночь.");
                } else {
                    player.setTime(0, false);
                    player.sendMessage("§a[Лорд] §fВаше личное время: день.");
                }
                return true;
            }
            case "top": {
                if (!(sender instanceof Player)) return notPlayer(sender);
                Player player = (Player) sender;
                int x = player.getFloorX();
                int z = player.getFloorZ();
                int y = player.getLevel().getHighestBlockAt(x, z) + 1;
                player.teleport(new Position(x + 0.5, y, z + 0.5, player.getLevel()));
                player.sendMessage("§a[Лорд] §fВы поднялись наверх!");
                return true;
            }
        }
        return false;
    }

    private boolean notPlayer(CommandSender sender) {
        sender.sendMessage("Эту команду может использовать только игрок");
        return true;
    }

    private boolean noPerm(CommandSender sender) {
        sender.sendMessage("§cНедостаточно прав!");
        return true;
    }

    private boolean giveCoins(Player sender, String nick, String amountStr, CommandSender console) {
        Player receiver = sender.getServer().getPlayerExact(nick);
        if (receiver == null) {
            console.sendMessage("§cИгрок не найден или не в сети.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            console.sendMessage("§cСумма должна быть числом.");
            return true;
        }
        economy.addDonate(receiver, amount);
        console.sendMessage("§aВы выдали §d" + amount + " §aФК-коинов игроку §b" + receiver.getName());
        receiver.sendMessage("§d[ФК-коины] §aВам начислено §d" + amount + " §aФК-коинов!");
        return true;
    }

    private void openCase(Player player) {
        if (!economy.takeDonate(player, 100)) {
            player.sendMessage("§c[Кейс] §fНужно §d100 ФК§f, у тебя: §d"
                    + (int) economy.getDonate(player));
            return;
        }
        int roll = RANDOM.nextInt(100);
        if (roll < 35) {
            int money = 300 + RANDOM.nextInt(700);
            economy.deposit(player, money);
            player.sendMessage("§6[Кейс] §fВыпало: §a" + money + " монет!");
        } else if (roll < 60) {
            player.getInventory().addItem(Item.get(Item.DIAMOND, 0, 16));
            player.sendMessage("§6[Кейс] §fВыпало: §b16 алмазов!");
        } else if (roll < 80) {
            player.getInventory().addItem(Item.get(Item.NETHERITE_INGOT, 0, 4));
            player.sendMessage("§6[Кейс] §fВыпало: §d4 незеритовых слитка!");
        } else if (roll < 92) {
            economy.addDonate(player, 50);
            player.sendMessage("§6[Кейс] §fВыпало: §d50 ФК-коинов!");
        } else {
            Item sword = Item.get(Item.NETHERITE_SWORD, 0, 1);
            sword.addEnchantment(Enchantment.getEnchantment(9).setLevel(5));
            player.getInventory().addItem(sword);
            player.sendMessage("§6[Кейс] §f§6ДЖЕКПОТ! §fНезеритовый меч с остротой 5!");
        }
    }
}

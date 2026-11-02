package ru.frizmin;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import ru.frizmin.economy.FrizMineEconomy;

public class FrizMineBedrock extends PluginBase implements Listener {

    private FrizMineEconomy economy;

    @Override
    public void onEnable() {
        economy = new FrizMineEconomy();
        getServer().getPluginManager().registerEvents(this, this);

        PluginCommand cmd = (PluginCommand) getServer().getPluginCommand("economy");
        if (cmd != null) {
            cmd.setExecutor(this);
        }

        getLogger().info("FrizMine Bedrock has been enabled!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        economy.getOrCreateBalance(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("economy")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                sender.sendMessage("Твой баланс: " + economy.getBalance(player) + " монет");
            } else {
                sender.sendMessage("Эту команду может использовать только игрок");
            }
            return true;
        }
        return false;
    }
}

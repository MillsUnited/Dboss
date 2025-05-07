package com.mills.dboss;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("dboss.admin")) {
                player.sendMessage(Main.prefix + "recieved summoning eye!");
                player.getInventory().addItem(SummonerSpawning.eyeItem());
            } else {
                player.sendMessage(Main.prefix + "you dont have permission to use this command!");
            }
        }

        return false;
    }
}

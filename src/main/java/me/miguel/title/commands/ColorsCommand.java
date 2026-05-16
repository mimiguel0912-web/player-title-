package me.miguel.title.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ColorsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        sender.sendMessage("§6===== CORES DISPONÍVEIS =====");

        for (ChatColor color : ChatColor.values()) {

            if (color.isColor()) {
                sender.sendMessage(color + color.name());
            }
        }

        return true;
    }
}

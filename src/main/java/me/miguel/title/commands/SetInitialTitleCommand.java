package me.miguel.title.commands;

import me.miguel.title.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetInitialTitleCommand implements CommandExecutor {

    private final Main plugin;

    public SetInitialTitleCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 3) {
            sender.sendMessage("§cUso: /settitleinicial <grupo> <titulo> <cor>");
            return true;
        }

        String group = args[0];
        String title = args[1];
        String color = args[2].toUpperCase();

        plugin.getConfig().set("initial.group", group);
        plugin.getConfig().set("initial.title", title);
        plugin.getConfig().set("initial.color", color);

        plugin.saveConfig();

        sender.sendMessage("§aTítulo inicial definido!");

        return true;
    }
}

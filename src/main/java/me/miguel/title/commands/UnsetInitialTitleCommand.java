package me.miguel.title.commands;

import me.miguel.title.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnsetInitialTitleCommand implements CommandExecutor {

    private final Main plugin;

    public UnsetInitialTitleCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        plugin.getConfig().set("initial", null);
        plugin.saveConfig();

        sender.sendMessage("§cTítulo inicial removido!");

        return true;
    }
}

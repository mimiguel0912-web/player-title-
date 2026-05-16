package me.miguel.title.commands;

import me.miguel.title.manager.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TitleCommand implements CommandExecutor {

    private final TitleManager manager;

    public TitleCommand(TitleManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("title.admin")) {
            sender.sendMessage("§cSem permissão!");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§eUso: /title <grupo> <player> <titulo> <cor>");
            return true;
        }

        String group = args[0];
        Player target = Bukkit.getPlayer(args[1]);
        String title = args[2];
        String colorName = args[3].toUpperCase();

        if (target == null) {
            sender.sendMessage("§cJogador offline!");
            return true;
        }

        ChatColor color;

        try {
            color = ChatColor.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cCor inválida!");
            return true;
        }

        manager.setTitle(target, group, title, color);

        sender.sendMessage("§aTítulo aplicado!");

        return true;
    }
}

package me.miguel.playtitle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayTitle extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("playtitle").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cSomente OPs podem usar isso!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUse: /playtitle <jogador> <titulo>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cJogador offline!");
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        
        // Converte o & para § e adiciona colchetes cinzas
        String titulo = " §7[" + sb.toString().trim().replace("&", "§") + "§7]";

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(target.getName());
        if (team == null) team = board.registerNewTeam(target.getName());

        team.setSuffix(titulo);
        team.addEntry(target.getName());

        sender.sendMessage("§aTítulo aplicado!");
        return true;
    }
}

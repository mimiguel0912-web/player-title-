package me.miguel.playtitle; // Mudei para o seu nome

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.*;

public class Main extends JavaPlugin implements CommandExecutor, Listener {

    private Map<UUID, Double> damageBoost = new HashMap<>();
    private Scoreboard sb;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.sb = Bukkit.getScoreboardManager().getMainScoreboard();
        
        getCommand("title").setExecutor(this);
        getCommand("untitle").setExecutor(this);
        getCommand("titulos").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("titulos")) {
            p.sendMessage("§b§l--- TÍTULOS DISPONÍVEIS ---");
            p.sendMessage("§f• §7Comum: §f/title [player] [texto]");
            p.sendMessage("§f• §6§lOP: §f/title op [player] [texto] §e(+0.9x Vida | +0.5x Dano)");
            p.sendMessage("§f• §b§lGOD: §f/title god [player] [texto] §e(+2x Vida | +1.5x Dano)");
            return true;
        }

        if (!p.isOp()) {
            p.sendMessage("§cSem permissão!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("untitle")) {
            if (args.length == 0) { p.sendMessage("§cUse: /untitle [jogador]"); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                resetPlayer(target);
                p.sendMessage("§aTítulo de " + target.getName() + " removido!");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("title")) {
            if (args.length < 2) {
                p.sendMessage("§cUse: /title [op/god/texto] [jogador] (texto)");
                return true;
            }

            String sub = args[0].toLowerCase();

            if (sub.equals("op")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { p.sendMessage("§cJogador offline!"); return true; }
                applyStats(target, getMsg(args, 2), "§6§l[OP] ", 1.9, 1.5);
                p.sendMessage("§aTítulo OP aplicado em " + target.getName());
            } 
            else if (sub.equals("god")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { p.sendMessage("§cJogador offline!"); return true; }
                applyStats(target, getMsg(args, 2), "§b§l[GOD] ", 3.0, 2.5);
                p.sendMessage("§aTítulo GOD aplicado em " + target.getName());
            } 
            else {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) { p.sendMessage("§cJogador offline!"); return true; }
                applyStats(target, getMsg(args, 1), "", 1.0, 1.0);
                p.sendMessage("§aTítulo comum aplicado em " + target.getName());
            }
        }
        return true;
    }

    private String getMsg(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) builder.append(args[i]).append(" ");
        return builder.toString().trim().replace("&", "§");
    }

    private void applyStats(Player target, String texto, String prefix, double hp, double dmg) {
        resetPlayer(target);
        String fullPrefix = prefix + texto + " ";
        if (fullPrefix.length() > 64) fullPrefix = fullPrefix.substring(0, 64);

        Team team = sb.getTeam(target.getName());
        if (team == null) team = sb.registerNewTeam(target.getName());
        team.setPrefix(fullPrefix);
        team.addEntry(target.getName());

        target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 * hp);
        target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        damageBoost.put(target.getUniqueId(), dmg);
    }

    private void resetPlayer(Player p) {
        Team team = sb.getTeam(p.getName());
        if (team != null) team.unregister();
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        damageBoost.remove(p.getUniqueId());
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (damageBoost.containsKey(p.getUniqueId())) {
                e.setDamage(e.getDamage() * damageBoost.get(p.getUniqueId()));
            }
        }
    }
}

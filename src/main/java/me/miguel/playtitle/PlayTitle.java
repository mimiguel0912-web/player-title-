package com.humanangel;

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
            p.sendMessage("§b§l--- LISTA DE TÍTULOS ---");
            p.sendMessage("§f• §7Título Comum: §f/title [texto]");
            p.sendMessage("§f• §6§lOP: §f/title op [texto] §e(+0.9x Vida | +0.5x Dano)");
            p.sendMessage("§f• §b§lGOD: §f/title god [texto] §e(+2x Vida | +1.5x Dano)");
            return true;
        }

        if (!p.isOp()) {
            p.sendMessage("§cSomente ADMs!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("untitle")) {
            if (args.length == 0) return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) resetPlayer(target);
            p.sendMessage("§aTítulo removido!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("title")) {
            if (args.length == 0) return false;

            String type = args[0].toLowerCase();
            String texto;
            
            if (type.equals("op")) {
                texto = getMessage(args, 1);
                applyStats(p, texto, "§6§l[OP] ", 1.9, 1.5);
            } else if (type.equals("god")) {
                texto = getMessage(args, 1);
                applyStats(p, texto, "§b§l[GOD] ", 3.0, 2.5);
            } else {
                texto = getMessage(args, 0);
                applyStats(p, texto, "", 1.0, 1.0);
            }
        }
        return true;
    }

    private String getMessage(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) sb.append(args[i]).append(" ");
        return sb.toString().trim().replace("&", "§");
    }

    private void applyStats(Player p, String texto, String prefix, double hp, double dmg) {
        resetPlayer(p); // Limpa antes de aplicar novo
        
        String fullPrefix = prefix + texto + " ";
        if (fullPrefix.length() > 64) fullPrefix = fullPrefix.substring(0, 64);

        // Sistema de Scoreboard para ficar em cima do nome
        Team team = sb.getTeam(p.getName());
        if (team == null) team = sb.registerNewTeam(p.getName());
        team.setPrefix(fullPrefix);
        team.addEntry(p.getName());

        // Atributos de Vida
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 * hp);
        p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        
        // Atributo de Dano
        damageBoost.put(p.getUniqueId(), dmg);
        
        p.sendMessage("§aTítulo '" + fullPrefix + "§a' aplicado!");
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

package me.miguel.title;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private final HashMap<UUID, String> playerGroups = new HashMap<>();
    private final HashMap<UUID, String> playerTitles = new HashMap<>();
    private final HashMap<UUID, ChatColor> playerColors = new HashMap<>();

    private Scoreboard scoreboard;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);

        ScoreboardManager manager = Bukkit.getScoreboardManager();

        if (manager != null) {
            scoreboard = manager.getMainScoreboard();
        }

        createDefaultGroups();

        getLogger().info("Plugin iniciado!");
    }

    private void createDefaultGroups() {

        if (!getConfig().contains("groups.comum")) {

            getConfig().set("groups.comum.health", 20);
            getConfig().set("groups.comum.strength", 0);
            getConfig().set("groups.comum.color", "GRAY");
        }

        if (!getConfig().contains("groups.op")) {

            getConfig().set("groups.op.health", 30);
            getConfig().set("groups.op.strength", 1);
            getConfig().set("groups.op.color", "GREEN");
        }

        if (!getConfig().contains("groups.god")) {

            getConfig().set("groups.god.health", 40);
            getConfig().set("groups.god.strength", 2);
            getConfig().set("groups.god.color", "RED");
        }

        saveConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();

        if (getConfig().contains("players." + uuid)) {

            String group = getConfig().getString("players." + uuid + ".group");
            String title = getConfig().getString("players." + uuid + ".title");
            String colorName = getConfig().getString("players." + uuid + ".color");

            ChatColor color = ChatColor.valueOf(colorName);

            setPlayerData(player, group, title, color);
        }

        applyGroup(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();

        String title = playerTitles.getOrDefault(uuid, "MEMBRO");

        ChatColor color = playerColors.getOrDefault(uuid, ChatColor.GRAY);

        event.setFormat(
                color + "[" + title + "] §f" +
                player.getName() +
                "§7: §f" +
                event.getMessage()
        );
    }

    public void setPlayerData(Player player, String group, String title, ChatColor color) {

        UUID uuid = player.getUniqueId();

        playerGroups.put(uuid, group);
        playerTitles.put(uuid, title);
        playerColors.put(uuid, color);

        getConfig().set("players." + uuid + ".group", group);
        getConfig().set("players." + uuid + ".title", title);
        getConfig().set("players." + uuid + ".color", color.name());

        saveConfig();

        String prefix = color + "[" + title + "] §f";

        player.setDisplayName(prefix + player.getName());

        player.setPlayerListName(prefix + player.getName());

        Team team = scoreboard.getTeam(group);

        if (team == null) {
            team = scoreboard.registerNewTeam(group);
        }

        team.setPrefix(prefix);

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        player.setScoreboard(scoreboard);

        applyGroup(player);
    }

    public void applyGroup(Player player) {

        UUID uuid = player.getUniqueId();

        String group = playerGroups.getOrDefault(uuid, "comum");

        ConfigurationSection section = getConfig().getConfigurationSection("groups." + group);

        if (section == null) return;

        double health = section.getDouble("health");

        int strength = section.getInt("strength");

        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);

            player.setHealth(Math.min(health, player.getHealth()));
        }

        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);

        if (strength > 0) {

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    Integer.MAX_VALUE,
                    strength - 1
            ));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("cores")) {

            sender.sendMessage("§6===== CORES =====");

            for (ChatColor color : ChatColor.values()) {

                if (color.isColor()) {

                    sender.sendMessage(color + color.name());
                }
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("criargrupo")) {

            if (args.length < 4) {

                sender.sendMessage("§cUso: /criargrupo <nome> <vida> <forca> <cor>");

                return true;
            }

            String name = args[0];

            double health = Double.parseDouble(args[1]);

            int strength = Integer.parseInt(args[2]);

            String color = args[3].toUpperCase();

            getConfig().set("groups." + name + ".health", health);

            getConfig().set("groups." + name + ".strength", strength);

            getConfig().set("groups." + name + ".color", color);

            saveConfig();

            sender.sendMessage("§aGrupo criado!");

            return true;
        }

        if (command.getName().equalsIgnoreCase("title")) {

            if (args.length < 4) {

                sender.sendMessage("§cUso: /title <grupo> <player> <titulo> <cor>");

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

            } catch (Exception e) {

                sender.sendMessage("§cCor invalida!");

                return true;
            }

            setPlayerData(target, group, title, color);

            sender.sendMessage("§aTitulo aplicado!");

            return true;
        }

        return false;
    }
}

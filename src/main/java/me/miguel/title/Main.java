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

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import org.bukkit.plugin.java.JavaPlugin;

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

        loadPlayerData();

        getLogger().info("PlayTitle iniciado!");
    }

    @Override
    public void onDisable() {

        saveConfig();
    }

    public void createDefaultGroups() {

        if (!getConfig().contains("groups.comum")) {

            getConfig().set("groups.comum.health", 20);
            getConfig().set("groups.comum.strength", 0);
            getConfig().set("groups.comum.color", "GRAY");
        }

        if (!getConfig().contains("groups.op")) {

            getConfig().set("groups.op.health", 30);
            getConfig().set("groups.op.strength", 2);
            getConfig().set("groups.op.color", "GREEN");
        }

        if (!getConfig().contains("groups.god")) {

            getConfig().set("groups.god.health", 40);
            getConfig().set("groups.god.strength", 4);
            getConfig().set("groups.god.color", "RED");
        }

        saveConfig();
    }

    public void loadPlayerData() {

        if (!getConfig().contains("players")) return;

        for (String uuidString :
                getConfig().getConfigurationSection("players").getKeys(false)) {

            UUID uuid = UUID.fromString(uuidString);

            String group =
                    getConfig().getString("players." + uuidString + ".group");

            String title =
                    getConfig().getString("players." + uuidString + ".title");

            String colorName =
                    getConfig().getString("players." + uuidString + ".color");

            ChatColor color = ChatColor.WHITE;

            try {
                color = ChatColor.valueOf(colorName);
            } catch (Exception ignored) {}

            playerGroups.put(uuid, group);
            playerTitles.put(uuid, title);
            playerColors.put(uuid, color);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();

        if (getConfig().contains("players." + uuid)) {

            String group =
                    getConfig().getString("players." + uuid + ".group");

            String title =
                    getConfig().getString("players." + uuid + ".title");

            String colorName =
                    getConfig().getString("players." + uuid + ".color");

            ChatColor color = ChatColor.WHITE;

            try {
                color = ChatColor.valueOf(colorName);
            } catch (Exception ignored) {}

            setPlayerData(player, group, title, color);
        }

        else if (getConfig().contains("initial")) {

            String group =
                    getConfig().getString("initial.group");

            String title =
                    getConfig().getString("initial.title");

            String colorName =
                    getConfig().getString("initial.color");

            ChatColor color = ChatColor.GRAY;

            try {
                color = ChatColor.valueOf(colorName);
            } catch (Exception ignored) {}

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

        String prefix = color + "§l[" + title + "] §r§f";

        event.setFormat(
                prefix +
                        player.getName() +
                        "§7: §f" +
                        event.getMessage()
        );
    }

    public void setPlayerData(Player player,
                              String group,
                              String title,
                              ChatColor color) {

        UUID uuid = player.getUniqueId();

        playerGroups.put(uuid, group);
        playerTitles.put(uuid, title);
        playerColors.put(uuid, color);

        getConfig().set("players." + uuid + ".group", group);
        getConfig().set("players." + uuid + ".title", title);
        getConfig().set("players." + uuid + ".color", color.name());

        saveConfig();

        String prefix = color + "§l[" + title + "] §r§f";

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

        ConfigurationSection section =
                getConfig().getConfigurationSection("groups." + group);

        if (section == null) return;

        double health = section.getDouble("health");

        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                    .setBaseValue(health);

            if (player.getHealth() > health) {
                player.setHealth(health);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();

        UUID uuid = player.getUniqueId();

        String group = playerGroups.getOrDefault(uuid, "comum");

        ConfigurationSection section =
                getConfig().getConfigurationSection("groups." + group);

        if (section == null) return;

        int strength = section.getInt("strength");

        double extraDamage = strength * 1.5;

        event.setDamage(event.getDamage() + extraDamage);
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

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

                sender.sendMessage(
                        "§cUso: /criargrupo <nome> <vida> <forca> <cor>"
                );

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

                sender.sendMessage(
                        "§cUso: /title <grupo> <player> <titulo> <cor>"
                );

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

                sender.sendMessage("§cCor inválida!");

                return true;
            }

            setPlayerData(target, group, title, color);

            sender.sendMessage("§aTítulo aplicado!");

            return true;
        }

        if (command.getName().equalsIgnoreCase("untitle")) {

            if (args.length < 1) {

                sender.sendMessage("§cUso: /untitle <player>");

                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {

                sender.sendMessage("§cJogador offline!");

                return true;
            }

            UUID uuid = target.getUniqueId();

            playerGroups.remove(uuid);
            playerTitles.remove(uuid);
            playerColors.remove(uuid);

            getConfig().set("players." + uuid, null);

            saveConfig();

            Team team = scoreboard.getEntryTeam(target.getName());

            if (team != null) {
                team.removeEntry(target.getName());
            }

            target.setDisplayName(target.getName());

            target.setPlayerListName(target.getName());

            if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {

                target.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                        .setBaseValue(20.0);
            }

            if (target.getHealth() > 20.0) {
                target.setHealth(20.0);
            }

            sender.sendMessage("§aTítulo removido!");

            return true;
        }

        if (command.getName().equalsIgnoreCase("settitleinicial")) {

            if (args.length < 3) {

                sender.sendMessage(
                        "§cUso: /settitleinicial <grupo> <titulo> <cor>"
                );

                return true;
            }

            String group = args[0];

            String title = args[1];

            String color = args[2].toUpperCase();

            getConfig().set("initial.group", group);
            getConfig().set("initial.title", title);
            getConfig().set("initial.color", color);

            saveConfig();

            sender.sendMessage("§aTítulo inicial definido!");

            return true;
        }

        if (command.getName().equalsIgnoreCase("unsettitleinicial")) {

            getConfig().set("initial", null);

            saveConfig();

            sender.sendMessage("§aTítulo inicial removido!");

            return true;
        }

        return false;
    }
}

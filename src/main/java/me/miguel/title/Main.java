package me.miguel.title;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Scoreboard;
import org.bukkit.ScoreboardManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private final HashMap<UUID, String> titles = new HashMap<>();
    private final HashMap<UUID, String> groups = new HashMap<>();
    private final HashMap<UUID, ChatColor> colors = new HashMap<>();

    private Scoreboard scoreboard;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);

        ScoreboardManager manager = Bukkit.getScoreboardManager();

        if (manager != null) {
            scoreboard = manager.getMainScoreboard();
        }

        getLogger().info("Plugin de Titulos iniciado!");
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!titles.containsKey(uuid)) {

            if (getConfig().contains("initial")) {

                String group = getConfig().getString("initial.group");
                String title = getConfig().getString("initial.title");
                String colorName = getConfig().getString("initial.color");

                ChatColor color;

                try {
                    color = ChatColor.valueOf(colorName.toUpperCase());
                } catch (Exception e) {
                    color = ChatColor.WHITE;
                }

                setPlayerTitle(player, group, title, color);
            }
        }

        applyGroupBuffs(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String title = titles.getOrDefault(uuid, "MEMBRO");
        ChatColor color = colors.getOrDefault(uuid, ChatColor.GRAY);

        String prefix = color + "[" + title + "] §f";

        event.setFormat(prefix + player.getName() + "§7: §f" + event.getMessage());
    }

    public void setPlayerTitle(Player player, String group, String title, ChatColor color) {

        UUID uuid = player.getUniqueId();

        titles.put(uuid, title);
        groups.put(uuid, group);
        colors.put(uuid, color);

        getConfig().set("players." + uuid + ".group", group);
        getConfig().set("players." + uuid + ".title", title);
        getConfig().set("players." + uuid + ".color", color.name());

        saveConfig();

        String prefix = color + "[" + title + "] §f";

        player.setDisplayName(prefix + player.getName());
        player.setPlayerListName(prefix + player.getName());

        setupTeam(player, title, color);

        applyGroupBuffs(player);
    }

    private void setupTeam(Player player, String title, ChatColor color) {

        String teamName = title.toLowerCase();

        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.setPrefix(color + "[" + title + "] §f");

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        player.setScoreboard(scoreboard);
    }

    private void applyGroupBuffs(Player player) {

        UUID uuid = player.getUniqueId();

        String group = groups.getOrDefault(uuid, "comum");

        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);

        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        }

        if (group.equalsIgnoreCase("god")) {

            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            }

            player.setHealth(40.0);

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    Integer.MAX_VALUE,
                    1
            ));
        }

        if (group.equalsIgnoreCase("op")) {

            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
            }

            player.setHealth(30.0);

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE,
                    Integer.MAX_VALUE,
                    0
            ));
        }

        if (group.equalsIgnoreCase("comum")) {

            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            }

            player.setHealth(20.0);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("cores")) {

            sender.sendMessage("§6===== CORES DISPONÍVEIS =====");

            for (ChatColor color : ChatColor.values()) {

                if (color.isColor()) {
                    sender.sendMessage(color + color.name());
                }
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("settitleinicial")) {

            if (args.length < 3) {
                sender.sendMessage("§cUso: /settitleinicial <grupo> <titulo> <cor>");
                return true;
            }

            String group = args[0];
            String title = args[1];
            String color = args[2];

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

            sender.sendMessage("§cTítulo inicial removido!");

            return true;
        }

        if (command.getName().equalsIgnoreCase("title")) {

            if (!sender.hasPermission("title.admin")) {
                sender.sendMessage("§cSem permissão!");
                return true;
            }

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
                sender.sendMessage("§cCor inválida!");
                return true;
            }

            setPlayerTitle(target, group, title, color);

            sender.sendMessage("§aTítulo aplicado em " + target.getName());

            return true;
        }

        return false;
    }
        }

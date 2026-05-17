package me.miguel.title.managers;

import me.miguel.title.Main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

public class TitleManager {

    private final Main plugin;

    private final HashMap<UUID, String> groups = new HashMap<>();
    private final HashMap<UUID, String> titles = new HashMap<>();
    private final HashMap<UUID, ChatColor> colors = new HashMap<>();

    private final Scoreboard scoreboard;

    public TitleManager(Main plugin) {

        this.plugin = plugin;

        ScoreboardManager manager =
                plugin.getServer().getScoreboardManager();

        scoreboard = manager.getMainScoreboard();

        load();
    }

    public void load() {

        if (!plugin.getConfig().contains("players")) return;

        for (String uuidString :
                plugin.getConfig()
                        .getConfigurationSection("players")
                        .getKeys(false)) {

            UUID uuid = UUID.fromString(uuidString);

            String group = plugin.getConfig()
                    .getString("players." + uuidString + ".group");

            String title = plugin.getConfig()
                    .getString("players." + uuidString + ".title");

            String colorName = plugin.getConfig()
                    .getString("players." + uuidString + ".color");

            ChatColor color = ChatColor.WHITE;

            try {
                color = ChatColor.valueOf(colorName);
            } catch (Exception ignored) {}

            groups.put(uuid, group);
            titles.put(uuid, title);
            colors.put(uuid, color);
        }
    }

    public void set(Player player,
                    String group,
                    String title,
                    ChatColor color) {

        UUID uuid = player.getUniqueId();

        groups.put(uuid, group);
        titles.put(uuid, title);
        colors.put(uuid, color);

        plugin.getConfig().set("players." + uuid + ".group", group);
        plugin.getConfig().set("players." + uuid + ".title", title);
        plugin.getConfig().set("players." + uuid + ".color", color.name());

        plugin.saveConfig();

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

        plugin.getGroupManager().apply(player, group);
    }

    public void remove(Player player) {

        UUID uuid = player.getUniqueId();

        groups.remove(uuid);
        titles.remove(uuid);
        colors.remove(uuid);

        plugin.getConfig().set("players." + uuid, null);

        plugin.saveConfig();

        Team team = scoreboard.getEntryTeam(player.getName());

        if (team != null) {
            team.removeEntry(player.getName());
        }

        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }

    public String getTitle(Player player) {
        return titles.get(player.getUniqueId());
    }

    public ChatColor getColor(Player player) {
        return colors.getOrDefault(player.getUniqueId(), ChatColor.GRAY);
    }

    public String getGroup(Player player) {
        return groups.getOrDefault(player.getUniqueId(), "comum");
    }
}

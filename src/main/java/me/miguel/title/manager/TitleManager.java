package me.miguel.title.manager;

import me.miguel.title.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TitleManager {

    private final Main plugin;

    public TitleManager(Main plugin) {
        this.plugin = plugin;
    }

    public void setTitle(Player player, String group, String title, ChatColor color) {

        UUID uuid = player.getUniqueId();

        plugin.getConfig().set("players." + uuid + ".group", group);
        plugin.getConfig().set("players." + uuid + ".title", title);
        plugin.getConfig().set("players." + uuid + ".color", color.name());

        plugin.saveConfig();

        player.setPlayerListName(color + "[" + title + "] " + player.getName());
    }

    public boolean hasTitle(Player player) {
        return plugin.getConfig().contains("players." + player.getUniqueId());
    }
}

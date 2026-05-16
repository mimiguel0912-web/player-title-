package me.miguel.title.listeners;

import me.miguel.title.Main;
import me.miguel.title.manager.TitleManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Main plugin;
    private final TitleManager manager;

    public PlayerJoinListener(Main plugin, TitleManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (manager.hasTitle(event.getPlayer())) return;

        if (!plugin.getConfig().contains("initial")) return;

        String group = plugin.getConfig().getString("initial.group");
        String title = plugin.getConfig().getString("initial.title");
        String colorName = plugin.getConfig().getString("initial.color");

        ChatColor color = ChatColor.valueOf(colorName);

        manager.setTitle(event.getPlayer(), group, title, color);
    }
}

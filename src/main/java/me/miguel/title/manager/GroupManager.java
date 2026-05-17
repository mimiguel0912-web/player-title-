package me.miguel.title.managers;

import me.miguel.title.Main;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class GroupManager {

    private final Main plugin;

    public GroupManager(Main plugin) {

        this.plugin = plugin;

        createDefaults();
    }

    public void createDefaults() {

        if (!plugin.getConfig().contains("groups.comum")) {

            plugin.getConfig().set("groups.comum.health", 20);
            plugin.getConfig().set("groups.comum.strength", 0);
        }

        if (!plugin.getConfig().contains("groups.op")) {

            plugin.getConfig().set("groups.op.health", 30);
            plugin.getConfig().set("groups.op.strength", 2);
        }

        if (!plugin.getConfig().contains("groups.god")) {

            plugin.getConfig().set("groups.god.health", 40);
            plugin.getConfig().set("groups.god.strength", 4);
        }

        plugin.saveConfig();
    }

    public void apply(Player player, String group) {

        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection(
                        "groups." + group
                );

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

    public int getStrength(String group) {

        return plugin.getConfig()
                .getInt("groups." + group + ".strength");
    }
}

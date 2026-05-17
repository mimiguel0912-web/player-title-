package me.miguel.title;

import me.miguel.title.commands.*;
import me.miguel.title.listeners.*;
import me.miguel.title.managers.GroupManager;
import me.miguel.title.managers.TitleManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private GroupManager groupManager;
    private TitleManager titleManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        groupManager = new GroupManager(this);
        titleManager = new TitleManager(this);

        Bukkit.getPluginManager().registerEvents(
                new JoinListener(this), this
        );

        Bukkit.getPluginManager().registerEvents(
                new ChatListener(this), this
        );

        Bukkit.getPluginManager().registerEvents(
                new DamageListener(this), this
        );

        getCommand("title")
                .setExecutor(new TitleCommand(this));

        getCommand("untitle")
                .setExecutor(new UnTitleCommand(this));

        getCommand("cores")
                .setExecutor(new CoresCommand());

        getCommand("criargrupo")
                .setExecutor(new CriarGrupoCommand(this));

        getCommand("settitleinicial")
                .setExecutor(new SetTitleInicialCommand(this));

        getCommand("unsettitleinicial")
                .setExecutor(new UnsetTitleInicialCommand(this));

        getLogger().info("PlayTitle iniciado!");
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
}

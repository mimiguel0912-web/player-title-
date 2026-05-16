package me.miguel.title;

import me.miguel.title.commands.ColorsCommand;
import me.miguel.title.commands.SetInitialTitleCommand;
import me.miguel.title.commands.TitleCommand;
import me.miguel.title.commands.UnsetInitialTitleCommand;
import me.miguel.title.listeners.PlayerJoinListener;
import me.miguel.title.manager.TitleManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private TitleManager titleManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        titleManager = new TitleManager(this);

        getCommand("title").setExecutor(new TitleCommand(titleManager));
        getCommand("cores").setExecutor(new ColorsCommand());
        getCommand("settitleinicial").setExecutor(new SetInitialTitleCommand(this));
        getCommand("unsettitleinicial").setExecutor(new UnsetInitialTitleCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, titleManager), this);

        getLogger().info("Plugin de Titulos iniciado!");
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public static Main getInstance() {
        return instance;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
}

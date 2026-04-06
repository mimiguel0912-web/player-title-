package me.miguel.playtitle;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements CommandExecutor, Listener {

    private final Map<UUID, String> listaDeTitulos = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadData();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("title").setExecutor(this);
        getCommand("untitle").setExecutor(this);
        getLogger().info("PlayTitle - MODO FORCA BRUTA ATIVADO");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String titulo = listaDeTitulos.get(uuid);
        if (titulo == null) {
            titulo = getConfig().getString("titulos-salvos." + uuid.toString());
        }

        if (titulo != null) {
            final String tituloFinal = titulo;
            listaDeTitulos.put(uuid, tituloFinal); // Garante que esta na memoria

            // 1. Chat e Lista (Bukkit)
            player.setDisplayName(tituloFinal + "§f§l" + player.getName());
            player.setPlayerListName(tituloFinal + "§f§l" + player.getName());

            // 2. Ciclo de Força no TAB (Tenta 3 vezes para garantir)
            for (int delay : new int[]{20, 100, 200}) { // 1s, 5s e 10s
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline()) {
                        forceTabPrefix(player, tituloFinal);
                    }
                }, (long) delay);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("untitle")) {
            if (args.length < 1) return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                removePlayerTitle(target);
                sender.sendMessage("§aTítulo removido com sucesso!");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("title")) {
            if (args.length < 4) return false;

            String tipo = args[0].toLowerCase();
            Player target = Bukkit.getPlayer(args[1]);
            String texto = args[2];
            String corNome = args[3].toUpperCase();

            if (target == null) return true;

            ChatColor cor;
            try { cor = ChatColor.valueOf(corNome); } catch (Exception e) { return true; }

            String prefixo = "";
            if (tipo.equals("god")) {
                prefixo = "§b§lGOD ";
                applyAttributes(target, 40.0, 2.5);
            } else if (tipo.equals("op")) {
                prefixo = "§6§lOP ";
                applyAttributes(target, 38.0, 1.5);
            }

            String tituloFormatado = "§7[" + prefixo + cor + "§l" + texto + "§7] ";
            
            target.setDisplayName(tituloFormatado + "§f§l" + target.getName());
            target.setPlayerListName(tituloFormatado + "§f§l" + target.getName());
            
            forceTabPrefix(target, tituloFormatado);

            listaDeTitulos.put(target.getUniqueId(), tituloFormatado);
            getConfig().set("titulos-salvos." + target.getUniqueId().toString(), tituloFormatado);
            saveConfig();

            target.sendTitle(cor + "§l" + texto, "§fTítulo Ativado!", 10, 40, 10);
            return true;
        }
        return true;
    }

    private void forceTabPrefix(Player player, String prefix) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("TAB")) {
                TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
                if (tabPlayer != null && TabAPI.getInstance().getNameTagManager() != null) {
                    // Usamos setPrefix e tambem tentamos atualizar o valor visual
                    TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, prefix);
                }
            }
        } catch (Exception ignored) {}
    }

    private void removePlayerTitle(Player p) {
        listaDeTitulos.remove(p.getUniqueId());
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        forceTabPrefix(p, "");
        getConfig().set("titulos-salvos." + p.getUniqueId().toString(), null);
        saveConfig();
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
    }

    private void applyAttributes(Player p, double health, double damage) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        p.setHealth(health);
        p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage + 1.0);
    }

    private void loadData() {
        if (getConfig().contains("titulos-salvos")) {
            for (String key : getConfig().getConfigurationSection("titulos-salvos").getKeys(false)) {
                listaDeTitulos.put(UUID.fromString(key), getConfig().getString("titulos-salvos." + key));
            }
        }
    }
}

package me.miguel.playtitle;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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
        getLogger().info("PlayTitle - Sistema de Persistencia Reforcado!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Tenta buscar da memoria ou direto da config se a memoria falhar
        String titulo = listaDeTitulos.get(uuid);
        if (titulo == null) {
            titulo = getConfig().getString("titulos-salvos." + uuid.toString());
        }

        if (titulo != null) {
            final String tituloFinal = titulo;
            
            // Aplica no chat imediatamente
            player.setDisplayName(tituloFinal + "§f§l" + player.getName());
            player.setPlayerListName(tituloFinal + "§f§l" + player.getName());

            // TENTATIVA 1: Aos 2 segundos
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) updateTabPrefix(player, tituloFinal);
            }, 40L);

            // TENTATIVA 2: Aos 7 segundos (Garante que o TAB da Reis Host nao mude depois)
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) updateTabPrefix(player, tituloFinal);
            }, 140L);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("untitle")) {
            if (args.length < 1) {
                sender.sendMessage("§eUse: /untitle <jogador>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                removePlayerTitle(target);
                sender.sendMessage("§aTitulo removido!");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("title")) {
            if (args.length < 4) {
                sender.sendMessage("§eUse: /title <comum/op/god> <jogador> <texto> <cor>");
                return true;
            }

            String tipo = args[0].toLowerCase();
            Player target = Bukkit.getPlayer(args[1]);
            String texto = args[2];
            String corNome = args[3].toUpperCase();

            if (target == null) {
                sender.sendMessage("§cJogador offline!");
                return true;
            }

            ChatColor cor;
            try { cor = ChatColor.valueOf(corNome); } catch (Exception e) {
                sender.sendMessage("§cCor invalida!");
                return true;
            }

            String prefixo = "";
            if (tipo.equals("god")) prefixo = "§b§lGOD ";
            else if (tipo.equals("op")) prefixo = "§6§lOP ";

            String tituloFormatado = "§7[" + prefixo + cor + "§l" + texto + "§7] ";
            
            target.setDisplayName(tituloFormatado + "§f§l" + target.getName());
            target.setPlayerListName(tituloFormatado + "§f§l" + target.getName());
            updateTabPrefix(target, tituloFormatado);

            // SALVAMENTO GARANTIDO
            listaDeTitulos.put(target.getUniqueId(), tituloFormatado);
            getConfig().set("titulos-salvos." + target.getUniqueId().toString(), tituloFormatado);
            saveConfig();

            target.sendTitle(cor + "§l" + texto, "§fTitulo Ativado!", 10, 40, 10);
            return true;
        }
        return true;
    }

    private void updateTabPrefix(Player player, String prefix) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("TAB")) {
                TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
                if (tabPlayer != null && TabAPI.getInstance().getNameTagManager() != null) {
                    TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, prefix);
                }
            }
        } catch (Exception e) {}
    }

    private void removePlayerTitle(Player p) {
        listaDeTitulos.remove(p.getUniqueId());
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        updateTabPrefix(p, "");
        getConfig().set("titulos-salvos." + p.getUniqueId().toString(), null);
        saveConfig();
        
        // Atributos de volta ao normal
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0);
    }

    private void loadData() {
        FileConfiguration config = getConfig();
        if (config.contains("titulos-salvos")) {
            for (String key : config.getConfigurationSection("titulos-salvos").getKeys(false)) {
                listaDeTitulos.put(UUID.fromString(key), config.getString("titulos-salvos." + key));
            }
        }
    }
}

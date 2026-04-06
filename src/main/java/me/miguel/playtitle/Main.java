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
        getCommand("titulos").setExecutor(this);
        getLogger().info("PlayTitle V3 - Modo Persistencia Total Ativado!");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String titulo = listaDeTitulos.get(player.getUniqueId());

        if (titulo == null) {
            titulo = getConfig().getString("titulos-salvos." + player.getUniqueId());
        }

        if (titulo != null) {
            final String tag = titulo;
            listaDeTitulos.put(player.getUniqueId(), tag);

            // Aplica no Chat/Lista do Bukkit
            player.setDisplayName(tag + "§f" + player.getName());
            player.setPlayerListName(tag + "§f" + player.getName());

            // Força o TAB a reconhecer o título com repetição (Delay para evitar bugs)
            for (int delay : new int[]{40, 100, 200}) { // 2s, 5s e 10s
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline()) {
                        setTabPrefix(player, tag);
                    }
                }, delay);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        // COMANDO /TITULOS (LISTAR)
        if (cmd.getName().equalsIgnoreCase("titulos")) {
            sender.sendMessage("§b§l=== JOGADORES COM TITULO ===");
            if (listaDeTitulos.isEmpty()) {
                sender.sendMessage("§7Nenhum titulo ativo no momento.");
            } else {
                for (UUID id : listaDeTitulos.keySet()) {
                    String nome = Bukkit.getOfflinePlayer(id).getName();
                    sender.sendMessage("§e" + nome + ": " + listaDeTitulos.get(id));
                }
            }
            return true;
        }

        // COMANDO /UNTITLE (REMOVER)
        if (cmd.getName().equalsIgnoreCase("untitle")) {
            if (args.length < 1) {
                sender.sendMessage("§eUse: /untitle <jogador>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                removeTitle(target);
                sender.sendMessage("§aTitulo removido de " + target.getName());
            }
            return true;
        }

        // COMANDO /TITLE (APLICAR)
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
            if (tipo.equals("god")) {
                prefixo = "§b§lGOD ";
                target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
                target.setHealth(40.0);
            } else if (tipo.equals("op")) {
                prefixo = "§6§lOP ";
                target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
            }

            String formatado = "§7[" + prefixo + cor + "§l" + texto + "§7] ";
            
            // 1. Salva no nosso plugin
            listaDeTitulos.put(target.getUniqueId(), formatado);
            getConfig().set("titulos-salvos." + target.getUniqueId(), formatado);
            saveConfig();

            // 2. Aplica no Bukkit
            target.setDisplayName(formatado + "§f" + target.getName());
            target.setPlayerListName(formatado + "§f" + target.getName());

            // 3. O SEGREDO: Manda o comando interno do TAB salvar também!
            setTabPrefix(target, formatado);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + target.getName() + " prefix " + formatado);

            target.sendTitle(cor + "§l" + texto, "§fTitulo Ativado!", 10, 40, 10);
            sender.sendMessage("§aTitulo aplicado e salvo para sempre!");
            return true;
        }
        return true;
    }

    private void setTabPrefix(Player player, String prefix) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("TAB")) {
                TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
                if (tabPlayer != null && TabAPI.getInstance().getNameTagManager() != null) {
                    TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, prefix);
                }
            }
        } catch (Exception ignored) {}
    }

    private void removeTitle(Player p) {
        listaDeTitulos.remove(p.getUniqueId());
        getConfig().set("titulos-salvos." + p.getUniqueId(), null);
        saveConfig();
        
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        
        setTabPrefix(p, "");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + p.getName() + " remove");
    }

    private void loadData() {
        if (getConfig().contains("titulos-salvos")) {
            for (String key : getConfig().getConfigurationSection("titulos-salvos").getKeys(false)) {
                listaDeTitulos.put(UUID.fromString(key), getConfig().getString("titulos-salvos." + key));
            }
        }
    }
}

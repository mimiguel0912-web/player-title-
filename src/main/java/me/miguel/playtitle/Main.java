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
        getLogger().info("PlayTitle ON - Integrado com TAB e Salvamento!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (listaDeTitulos.containsKey(uuid)) {
            String titulo = listaDeTitulos.get(uuid);
            
            // Aplica no Chat e Lista do TAB
            player.setDisplayName(titulo + "§f§l" + player.getName());
            player.setPlayerListName(titulo + "§f§l" + player.getName());

            // Espera o TAB carregar o player para aplicar na cabeça
            Bukkit.getScheduler().runTaskLater(this, () -> {
                updateTabPrefix(player, titulo);
            }, 40L); 
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
                sender.sendMessage("§aTítulo de " + target.getName() + " removido!");
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
            try {
                cor = ChatColor.valueOf(corNome);
            } catch (Exception e) {
                sender.sendMessage("§cCor inválida! Use nomes em inglês.");
                return true;
            }

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
            
            updateTabPrefix(target, tituloFormatado);

            listaDeTitulos.put(target.getUniqueId(), tituloFormatado);
            getConfig().set("titulos-salvos." + target.getUniqueId(), tituloFormatado);
            saveConfig();

            target.sendTitle(cor + "§l" + texto, "§fTítulo Ativado!", 10, 40, 10);
            sender.sendMessage("§aTítulo salvo e aplicado!");
            return true;
        }
        return true;
    }

    private void updateTabPrefix(Player player, String prefix) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("TAB")) {
                TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
                if (tabPlayer != null) {
                    TabAPI.getInstance().getGroupManager().setPrefix(tabPlayer, prefix);
                }
            }
        } catch (Exception e) {
            getLogger().warning("Erro ao atualizar prefixo no TAB: " + e.getMessage());
        }
    }

    private void removePlayerTitle(Player p) {
        listaDeTitulos.remove(p.getUniqueId());
        removeAttributes(p);
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        
        updateTabPrefix(p, "");
        
        getConfig().set("titulos-salvos." + p.getUniqueId(), null);
        saveConfig();
    }

    private void applyAttributes(Player p, double health, double damage) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        p.setHealth(health);
        p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage + 1.0);
    }

    private void removeAttributes(Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0);
    }

    private void loadData() {
        if (getConfig().getConfigurationSection("titulos-salvos") == null) return;
        for (String key : getConfig().getConfigurationSection("titulos-salvos").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String tag = getConfig().getString("titulos-salvos." + key);
                listaDeTitulos.put(uuid, tag);
            } catch (Exception e) {
                // Pula se o UUID for inválido
            }
        }
    }
}

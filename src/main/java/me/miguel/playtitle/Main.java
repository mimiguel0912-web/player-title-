package me.miguel.playtitle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, String> listaDeTitulos = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadData();
        getCommand("title").setExecutor(this);
        getCommand("titulos").setExecutor(this);
        getCommand("untitle").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("titulos")) {
            if (listaDeTitulos.isEmpty()) {
                sender.sendMessage("§c[!] Nenhum título ativo.");
                return true;
            }
            sender.sendMessage("§b§l--- TÍTULOS ATIVOS ---");
            for (Map.Entry<UUID, String> entry : listaDeTitulos.entrySet()) {
                String nome = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                sender.sendMessage("§e" + nome + " §7- " + ChatColor.translateAlternateColorCodes('&', entry.getValue()));
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("untitle")) {
            if (args.length < 1) {
                sender.sendMessage("§eUse: /untitle <jogador>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                listaDeTitulos.remove(target.getUniqueId());
                removeAttributes(target);
                
                // Reseta o nome no Chat e TAB
                target.setDisplayName(target.getName());
                target.setPlayerListName(target.getName());
                
                getConfig().set("titulos-salvos." + target.getUniqueId(), null);
                saveConfig();
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
                sender.sendMessage("§cCor inválida!");
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

            // Monta o Título: Ex: [GOD REI]
            String tituloFormatado = "§7[" + prefixo + cor + "§l" + texto + "§7] ";
            
            // Aplica no CHAT e no TAB (Fica em negrito e com cor)
            target.setDisplayName(tituloFormatado + "§f§l" + target.getName());
            target.setPlayerListName(tituloFormatado + "§f§l" + target.getName());

            listaDeTitulos.put(target.getUniqueId(), tituloFormatado);
            getConfig().set("titulos-salvos." + target.getUniqueId(), tituloFormatado);
            saveConfig();

            // Ainda manda o efeito na tela para avisar
            target.sendTitle(cor + "§l" + texto, "§fTítulo Ativado!", 10, 40, 10);
            
            sender.sendMessage("§aTítulo aplicado ao nome de " + target.getName());
            return true;
        }
        return true;
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
            UUID uuid = UUID.fromString(key);
            String tag = getConfig().getString("titulos-salvos." + key);
            listaDeTitulos.put(uuid, tag);
            
            // Tenta aplicar ao jogador se ele já estiver online
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.setDisplayName(tag + "§f§l" + p.getName());
                p.setPlayerListName(tag + "§f§l" + p.getName());
            }
        }
    }
}

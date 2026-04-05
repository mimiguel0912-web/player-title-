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

        // COMANDO: /titulos
        if (cmd.getName().equalsIgnoreCase("titulos")) {
            if (listaDeTitulos.isEmpty()) {
                sender.sendMessage("§c[!] Nenhum título ativo no momento.");
                return true;
            }
            sender.sendMessage("§b§l--- TÍTULOS ATIVOS ---");
            for (Map.Entry<UUID, String> entry : listaDeTitulos.entrySet()) {
                String nome = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                sender.sendMessage("§f• " + nome + ": " + ChatColor.translateAlternateColorCodes('&', entry.getValue()));
            }
            return true;
        }

        // COMANDO: /untitle <jogador>
        if (cmd.getName().equalsIgnoreCase("untitle")) {
            if (args.length < 1) {
                sender.sendMessage("§eUse: /untitle <jogador>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                listaDeTitulos.remove(target.getUniqueId());
                removeAttributes(target);
                getConfig().set("titulos-salvos." + target.getUniqueId(), null);
                saveConfig();
                target.sendMessage("§cSeu título foi removido!");
                sender.sendMessage("§aTítulo de " + target.getName() + " removido!");
            }
            return true;
        }

        // COMANDO: /title <comum/op/god> <jogador> <texto> <cor>
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
                sender.sendMessage("§cCor inválida! Use RED, GOLD, AQUA, etc.");
                return true;
            }

            String prefixo = "";
            if (tipo.equals("god")) {
                prefixo = "§b§lGOD: ";
                applyAttributes(target, 40.0, 2.5);
            } else if (tipo.equals("op")) {
                prefixo = "§6§lOP: ";
                applyAttributes(target, 38.0, 1.5);
            }

            // O Título final que será salvo e exibido
            String tituloPrincipal = prefixo + cor + "§l" + texto;
            String subtituloNick = "§f" + target.getName(); // Nick em branco embaixo

            listaDeTitulos.put(target.getUniqueId(), tituloPrincipal);
            getConfig().set("titulos-salvos." + target.getUniqueId(), tituloPrincipal);
            saveConfig();

            // EXIBIÇÃO NA TELA: Título em cima, Nick embaixo
            target.sendTitle(tituloPrincipal, subtituloNick, 10, 70, 20);
            
            sender.sendMessage("§aTítulo enviado para " + target.getName());
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
            listaDeTitulos.put(UUID.fromString(key), getConfig().getString("titulos-salvos." + key));
        }
    }
}

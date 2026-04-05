package me.miguel.playtitle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        loadData(); // Carrega os títulos salvos ao ligar o servidor
        
        getCommand("title").setExecutor(this);
        getCommand("titulos").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        // COMANDO: /titulos
        if (cmd.getName().equalsIgnoreCase("titulos")) {
            if (listaDeTitulos.isEmpty()) {
                sender.sendMessage("§c[!] Nenhum título foi enviado ainda.");
                return true;
            }
            sender.sendMessage("§6§l=== TÍTULOS ATIVOS ===");
            for (Map.Entry<UUID, String> entry : listaDeTitulos.entrySet()) {
                String nome = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                if (nome == null) nome = "Jogador Desconhecido";
                // Mostra o nome e o título (que já inclui a cor e o negrito)
                sender.sendMessage("§e" + nome + " §7- " + ChatColor.translateAlternateColorCodes('&', entry.getValue()));
            }
            return true;
        }

        // COMANDO: /title <jogador> <texto> <cor>
        if (cmd.getName().equalsIgnoreCase("title")) {
            if (args.length < 3) {
                sender.sendMessage("§eUse: /title <jogador> <texto> <cor>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cJogador offline!");
                return true;
            }

            String texto = args[1];
            String corNome = args[2].toUpperCase();
            ChatColor cor;

            try {
                cor = ChatColor.valueOf(corNome);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cCor inválida! Use: RED, GREEN, BLUE, GOLD, etc.");
                return true;
            }

            // O segredo: Cor + Negrito + Texto
            String tituloFinal = cor + "§l" + texto;

            // Salva na lista e no arquivo
            listaDeTitulos.put(target.getUniqueId(), tituloFinal);
            saveData(target.getUniqueId(), tituloFinal);

            target.sendTitle(tituloFinal, "", 10, 70, 20);
            sender.sendMessage("§aTítulo enviado para " + target.getName() + " e salvo permanentemente!");
            return true;
        }

        return true;
    }

    // Salva um novo título no config.yml
    private void saveData(UUID uuid, String titulo) {
        getConfig().set("titulos-salvos." + uuid.toString(), titulo);
        saveConfig();
    }

    // Carrega todos os títulos do config.yml para a memória
    private void loadData() {
        if (getConfig().getConfigurationSection("titulos-salvos") == null) return;
        
        for (String key : getConfig().getConfigurationSection("titulos-salvos").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String titulo = getConfig().getString("titulos-salvos." + key);
            listaDeTitulos.put(uuid, titulo);
        }
    }
}
 
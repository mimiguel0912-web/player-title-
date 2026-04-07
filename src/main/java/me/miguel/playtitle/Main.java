package me.miguel.playtitle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Registra o comando conforme o seu plugin.yml
        if (getCommand("playtitle") != null) {
            getCommand("playtitle").setExecutor(this);
        }
        Bukkit.getConsoleSender().sendMessage("§a[PlayTitle] Rodando sem interferir no TPA!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("playtitle")) {
            if (!sender.hasPermission("playtitle.admin")) {
                sender.sendMessage("§cSem permissão.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("§cUse: /playtitle (jogador) (mensagem)");
                return true;
            }

            // O SEGREDO: Pegar o jogador pelo nome exato para não confundir o Bukkit
            Player target = Bukkit.getPlayerExact(args[0]);
            
            if (target == null) {
                sender.sendMessage("§cJogador offline ou nome inválido.");
                return true;
            }

            // Reconstrói a mensagem com cores
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            String mensagem = sb.toString().trim().replace("&", "§");

            // --- CORREÇÃO DO TPA ---
            // Usamos o runTask (Síncrono) para enviar o pacote de Title 
            // de forma que o Bukkit não perca a referência do jogador.
            // O uso de 'target.sendTitle' nativo NÃO altera o nome do player,
            // então o /tpa continuará achando o nome original dele.
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    // Envia apenas o visual. O "RG" do jogador no servidor não muda.
                    target.sendTitle(mensagem, "", 10, 70, 20);
                } catch (Exception e) {
                    // Se a versão for incompatível, apenas ignora
                }
            });

            sender.sendMessage("§aTítulo enviado para " + target.getName());
            return true;
        }
        return false;
    }
}

package com.humanangel;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import java.io.*;
import java.util.*;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private File dadosFile;
    private FileConfiguration dados;
    private boolean zueiraAtiva = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        carregarDados();
        getServer().getPluginManager().registerEvents(this, this);
        
        // Loop de Avisos (30 em 30 min)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            List<String> lista = dados.getStringList("avisos");
            if (!lista.isEmpty()) {
                Bukkit.broadcastMessage("§6§l[AVISO] §f" + lista.get(new Random().nextInt(lista.size())).replace("&", "§"));
            }
        }, 0L, 36000L);
    }

    private void carregarDados() {
        dadosFile = new File(getDataFolder(), "dados.yml");
        if (!dadosFile.exists()) saveResource("dados.yml", false);
        dados = YamlConfiguration.loadConfiguration(dadosFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String nomeCmd = cmd.getName().toLowerCase();

        switch (nomeCmd) {
            case "lista":
                p.sendMessage("§d§lHUMAN ANGEL §7- §fComandos");
                p.sendMessage("§f/home, /sethome, /spawn, /luz, /lixeira, /chapeu, /morte, /perfil");
                if (p.hasPermission("humanangel.admin")) p.sendMessage("§c§lADM: §f/control, /zueira, /modo, /anuncio");
                break;

            case "sethome":
                String hNome = (args.length > 0) ? args[0] : "home";
                getConfig().set("homes." + p.getUniqueId() + "." + hNome, p.getLocation());
                saveConfig();
                p.sendMessage("§aHome §e" + hNome + " §asetada!");
                break;

            case "home":
                String hIr = (args.length > 0) ? args[0] : "home";
                Location loc = getConfig().getLocation("homes." + p.getUniqueId() + "." + hIr);
                if (loc != null) p.teleport(loc); else p.sendMessage("§cHome inexistente.");
                break;

            case "control":
                if (p.hasPermission("humanangel.admin")) abrirMenuPlayers(p);
                break;

            case "zueira":
                if (p.hasPermission("humanangel.admin")) {
                    zueiraAtiva = !zueiraAtiva;
                    p.sendMessage(zueiraAtiva ? "§aZueira ON" : "§cZueira OFF");
                }
                break;

            case "luz":
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
                break;

            case "spawn":
                p.teleport(p.getWorld().getSpawnLocation());
                break;
        }
        return true;
    }

    public void abrirMenuPlayers(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Controle de Jogadores");
        for (Player online : Bukkit.getOnlinePlayers()) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta m = (SkullMeta) item.getItemMeta();
            m.setOwningPlayer(online);
            m.setDisplayName("§e" + online.getName());
            item.setItemMeta(m);
            inv.addItem(item);
        }
        p.openInventory(inv);
    }

    public void abrirMenuAcoes(Player adm, Player alvo) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Ações: " + alvo.getName());
        inv.setItem(10, criarItem(Material.COMPASS, "§aIr até ele"));
        inv.setItem(12, criarItem(Material.PAPER, "§bPerfil e IP"));
        inv.setItem(14, criarItem(Material.BARRIER, "§cBanir"));
        inv.setItem(16, criarItem(Material.BEDROCK, "§6Listar Homes"));
        adm.openInventory(inv);
    }

    private ItemStack criarItem(Material mat, String nome) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta(); m.setDisplayName(nome); i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void clicar(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§8Controle de Jogadores")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            Player alvo = Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName().replace("§e", ""));
            if (alvo != null) abrirMenuAcoes((Player) e.getWhoClicked(), alvo);
        } else if (e.getView().getTitle().contains("§8Ações:")) {
            e.setCancelled(true);
            Player adm = (Player) e.getWhoClicked();
            Player alvo = Bukkit.getPlayer(e.getView().getTitle().split(": ")[1]);
            if (alvo == null) return;
            if (e.getRawSlot() == 10) adm.teleport(alvo);
            if (e.getRawSlot() == 12) adm.sendMessage("§bIP: §f" + alvo.getAddress().getHostString());
        }
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e) {
        if (zueiraAtiva && (e.getMessage().contains("lixo") || e.getMessage().contains("hack"))) {
            e.setMessage("§dEu amo esse servidor! ❤");
        }
    }
}

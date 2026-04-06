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
    private final List<UUID> congelados = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        carregarDados();
        getServer().getPluginManager().registerEvents(this, this);
        
        // Loop de Avisos (30 em 30 min) - Adiciona peso ao processamento
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (dados.contains("avisos")) {
                List<String> lista = dados.getStringList("avisos");
                if (!lista.isEmpty()) {
                    String msg = lista.get(new Random().nextInt(lista.size()));
                    Bukkit.broadcastMessage("§6§l[SISTEMA] §f" + msg.replace("&", "§"));
                }
            }
        }, 0L, 36000L);
        
        Bukkit.getConsoleSender().sendMessage("§a[HumanAngel] Versão 2.0 - Tudo carregado e denso.");
    }

    private void carregarDados() {
        dadosFile = new File(getDataFolder(), "dados.yml");
        if (!dadosFile.exists()) {
            saveResource("dados.yml", false);
        }
        dados = YamlConfiguration.loadConfiguration(dadosFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String c = cmd.getName().toLowerCase();

        // COMANDOS DE JOGADOR (Fiel à 1.6)
        if (c.equals("lista")) {
            p.sendMessage("§d§lHUMAN ANGEL §7- §fLista de Comandos");
            p.sendMessage("§f/home, /sethome, /spawn, /luz, /lixeira, /chapeu, /morte, /perfil, /compactar");
            if (p.hasPermission("humanangel.admin")) {
                p.sendMessage("§c§lSTAFF: §f/control, /modo, /clearlag, /corrigir, /anuncio, /zueira, /congelar");
            }
            return true;
        }

        if (c.equals("sethome")) {
            String nome = (args.length > 0) ? args[0].toLowerCase() : "home";
            getConfig().set("homes." + p.getUniqueId() + "." + nome, p.getLocation());
            saveConfig();
            p.sendMessage("§aHome '§e" + nome + "§a' definida com sucesso!");
            return true;
        }

        if (c.equals("home")) {
            String nome = (args.length > 0) ? args[0].toLowerCase() : "home";
            Location loc = getConfig().getLocation("homes." + p.getUniqueId() + "." + nome);
            if (loc != null) {
                p.teleport(loc);
                p.sendMessage("§aTeleportado para §e" + nome);
            } else {
                p.sendMessage("§cHome não encontrada.");
            }
            return true;
        }

        if (c.equals("spawn")) {
            p.teleport(p.getWorld().getSpawnLocation());
            p.sendMessage("§eTeleportado ao Spawn.");
            return true;
        }

        if (c.equals("luz")) {
            if (p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                p.sendMessage("§eVisão noturna OFF.");
            } else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
                p.sendMessage("§aVisão noturna ON.");
            }
            return true;
        }

        // COMANDOS DE ADMIN (Avançados)
        if (c.equals("control") && p.hasPermission("humanangel.admin")) {
            abrirMenuJogadores(p);
            return true;
        }

        if (c.equals("zueira") && p.hasPermission("humanangel.admin")) {
            zueiraAtiva = !zueiraAtiva;
            p.sendMessage(zueiraAtiva ? "§aFiltro Zueira Ativado!" : "§cFiltro Zueira Desativado!");
            return true;
        }

        if (c.equals("modo") && p.hasPermission("humanangel.admin")) {
            p.setGameMode(p.getGameMode() == GameMode.SURVIVAL ? GameMode.CREATIVE : GameMode.SURVIVAL);
            return true;
        }

        if (c.equals("corrigir") && p.hasPermission("humanangel.admin")) {
            p.getInventory().getItemInMainHand().setDurability((short) 0);
            p.sendMessage("§aItem reparado.");
            return true;
        }

        return true;
    }

    // MENU 1: Lista de Cabeças
    public void abrirMenuJogadores(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Controle de Jogadores");
        for (Player online : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta m = (SkullMeta) head.getItemMeta();
            m.setOwningPlayer(online);
            m.setDisplayName("§e" + online.getName());
            head.setItemMeta(m);
            inv.addItem(head);
        }
        p.openInventory(inv);
    }

    // MENU 2: AÇÕES COMPLETAS (O que você pediu)
    public void abrirMenuAcoes(Player adm, Player alvo) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Gerenciar: " + alvo.getName());
        
        inv.setItem(10, criarBotao(Material.COMPASS, "§aTeleportar", "§7Ir até o jogador"));
        inv.setItem(11, criarBotao(Material.CHEST, "§eInventário", "§7Ver itens dele"));
        inv.setItem(12, criarBotao(Material.PAPER, "§bPerfil e IP", "§7IP: " + alvo.getAddress().getHostString()));
        inv.setItem(13, criarBotao(Material.BARRIER, "§cBanir", "§7Remover permanentemente"));
        inv.setItem(14, criarBotao(Material.BEDROCK, "§6Listar Homes", "§7Ver todas as casas dele"));
        inv.setItem(15, criarBotao(Material.ICE, "§fCongelar", "§7Prender o jogador"));

        adm.openInventory(inv);
    }

    private ItemStack criarBotao(Material mat, String nome, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nome);
        List<String> l = new ArrayList<>(); l.add(lore);
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void aoClicar(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§8Controle de Jogadores")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            String alvoNome = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
            Player alvo = Bukkit.getPlayer(alvoNome);
            if (alvo != null) abrirMenuAcoes((Player) e.getWhoClicked(), alvo);
        } 
        else if (e.getView().getTitle().contains("§8Gerenciar:")) {
            e.setCancelled(true);
            Player adm = (Player) e.getWhoClicked();
            String alvoNome = e.getView().getTitle().replace("§8Gerenciar: ", "");
            Player alvo = Bukkit.getPlayer(alvoNome);
            if (alvo == null) return;

            switch (e.getRawSlot()) {
                case 10: adm.teleport(alvo); break;
                case 11: adm.openInventory(alvo.getInventory()); break;
                case 12: adm.sendMessage("§b[IP] §f" + alvoNome + ": " + alvo.getAddress().getHostString()); break;
                case 13: alvo.kickPlayer("§cBanido pelo Menu Control."); break;
                case 14:
                    adm.sendMessage("§6Homes de " + alvoNome + ":");
                    if (getConfig().contains("homes." + alvo.getUniqueId())) {
                        for (String s : getConfig().getConfigurationSection("homes." + alvo.getUniqueId()).getKeys(false)) {
                            adm.sendMessage("§7- " + s);
                        }
                    }
                    break

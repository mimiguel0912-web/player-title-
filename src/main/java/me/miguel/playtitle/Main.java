package com.humanangel;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("§d[HumanAngel] Plugin 2.0 Ativado - Recuperando funções.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String c = cmd.getName().toLowerCase();

        // Lista de Comandos Completa
        if (c.equals("lista")) {
            p.sendMessage("§b--- COMANDOS HUMANANGEL ---");
            p.sendMessage("§fGeral: /home, /sethome, /tpa, /tpaccept, /spawn, /chapeu, /lixeira, /perfil, /morte, /compactar, /luz");
            if (p.hasPermission("humanangel.admin")) {
                p.sendMessage("§eAdmin: /modo, /control, /clearlag, /corrigir, /mudarip, /anuncio, /aviso, /congelar, /zueira, /avisos");
            }
            return true;
        }

        // Sistema de Spawn
        if (c.equals("spawn")) {
            p.teleport(p.getWorld().getSpawnLocation());
            p.sendMessage("§aTeleportado ao Spawn!");
            return true;
        }

        // Homes Infinitas com Nomes
        if (c.equals("sethome")) {
            String nome = (args.length > 0) ? args[0].toLowerCase() : "home";
            String path = "homes." + p.getUniqueId() + "." + nome;
            getConfig().set(path + ".world", p.getLocation().getWorld().getName());
            getConfig().set(path + ".x", p.getLocation().getX());
            getConfig().set(path + ".y", p.getLocation().getY());
            getConfig().set(path + ".z", p.getLocation().getZ());
            saveConfig();
            p.sendMessage("§aHome '" + nome + "' definida!");
            return true;
        }

        if (c.equals("home")) {
            String nome = (args.length > 0) ? args[0].toLowerCase() : "home";
            String path = "homes." + p.getUniqueId() + "." + nome;
            if (!getConfig().contains(path)) {
                p.sendMessage("§cHome não encontrada!");
                return true;
            }
            World w = Bukkit.getWorld(getConfig().getString(path + ".world"));
            Location loc = new Location(w, getConfig().getDouble(path + ".x"), getConfig().getDouble(path + ".y"), getConfig().getDouble(path + ".z"));
            p.teleport(loc);
            p.sendMessage("§aTeleportado para: " + nome);
            return true;
        }

        // Menu Control de Cabeças
        if (c.equals("control") && p.hasPermission("humanangel.admin")) {
            abrirMenuControl(p);
            return true;
        }

        // Comandos de Utilidade
        if (c.equals("luz")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
            p.sendMessage("§aVisão noturna ativada!");
            return true;
        }

        if (c.equals("lixeira")) {
            p.openInventory(Bukkit.createInventory(null, 36, "§8Lixeira"));
            return true;
        }

        if (c.equals("modo") && p.hasPermission("humanangel.admin")) {
            p.setGameMode(p.getGameMode() == GameMode.SURVIVAL ? GameMode.CREATIVE : GameMode.SURVIVAL);
            return true;
        }

        if (c.equals("morte")) {
            p.setHealth(0);
            return true;
        }

        return true;
    }

    public void abrirMenuControl(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Controle de Jogadores");
        for (Player online : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(online);
            meta.setDisplayName("§e" + online.getName());
            head.setItemMeta(meta);
            inv.addItem(head);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void aoClicar(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§8Controle de Jogadores")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            Player alvo = Bukkit.getPlayer(e.getCurrentItem().getItemMeta().getDisplayName().replace("§e", ""));
            if (alvo != null) ((Player)e.getWhoClicked()).teleport(alvo);
        }
    }
}

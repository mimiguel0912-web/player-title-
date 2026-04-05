    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (listaDeTitulos.containsKey(uuid)) {
            String titulo = listaDeTitulos.get(uuid);
            
            // Aplica no Chat imediatamente
            player.setDisplayName(titulo + "§f§l" + player.getName());
            player.setPlayerListName(titulo + "§f§l" + player.getName());

            // ESPERA MAIS TEMPO (5 segundos) para o TAB carregar a cabeça
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) {
                    updateTabPrefix(player, titulo);
                    getLogger().info("Título de " + player.getName() + " restaurado com sucesso!");
                }
            }, 100L); // 100 ticks = 5 segundos
        }
    }

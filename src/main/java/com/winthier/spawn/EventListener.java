package com.winthier.spawn;

import com.cavetale.core.bungee.Bungee;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

@RequiredArgsConstructor
public final class EventListener implements Listener {
    private final SpawnPlugin plugin;
    private final Set<UUID> spawnLocatedPlayers = new HashSet<>();

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        for (var flag : event.getRespawnFlags()) {
            switch (flag) {
            case ANCHOR_SPAWN:
            case BED_SPAWN:
                return;
            default: break;
            }
        }
        event.setRespawnLocation(plugin.getSpawnLocation());
        if (plugin.remote != null && !plugin.remote.isEmpty()) {
            Bungee.send(event.getPlayer(), plugin.remote);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        if (plugin.teleportOnJoin) {
            spawnLocatedPlayers.add(player.getUniqueId());
            event.setSpawnLocation(plugin.getSpawnLocation());
            plugin.getLogger().info("Setting spawn location of " + player.getName()
                                    + " due to teleportOnJoin");
            return;
        }
        if (!player.hasPlayedBefore() && plugin.initialSpawn) {
            spawnLocatedPlayers.add(event.getPlayer().getUniqueId());
            event.setSpawnLocation(plugin.getSpawnLocation());
            plugin.getLogger().info("Setting spawn location of " + event.getPlayer().getName()
                                    + " due to PlayerInitialSpawnEvent");
            return;
        }
        if (plugin.omitDefaultWorld) {
            World defaultWorld = Bukkit.getWorlds().get(0);
            World playerWorld = event.getSpawnLocation().getWorld();
            if (playerWorld.equals(defaultWorld)) {
                spawnLocatedPlayers.add(event.getPlayer().getUniqueId());
                event.setSpawnLocation(plugin.getSpawnLocation());
                plugin.getLogger().info("Setting spawn location of " + player.getName()
                                        + " due to omitDefaultWorld: "
                                        + playerWorld.getName());
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!spawnLocatedPlayers.remove(player.getUniqueId())) return;
        if (player.isOp()) return;
        GameMode defaultGameMode = Bukkit.getDefaultGameMode();
        GameMode playerGameMode = player.getGameMode();
        if (playerGameMode != defaultGameMode) {
            player.setGameMode(defaultGameMode);
            plugin.getLogger().info("Setting game mode of " + player.getName()
                                    + ": " + playerGameMode + " => " + defaultGameMode);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onEntityVoidDamage(EntityDamageEvent event) {
        if (!plugin.onVoidDamage) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (!(event.getEntity() instanceof Player player)) return;
        player.teleport(plugin.getSpawnLocation(), TeleportCause.PLUGIN);
    }
}

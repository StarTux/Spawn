package com.winthier.spawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public final class Spawn {
    private Spawn() { }

    public static Location get() {
        return SpawnPlugin.instance.getSpawnLocation();
    }

    public static void warp(Player player, TeleportCause cause) {
        player.teleport(get(), cause);
    }

    public static void warp(Player player) {
        warp(player, TeleportCause.PLUGIN);
    }
}

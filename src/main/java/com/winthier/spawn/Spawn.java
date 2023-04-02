package com.winthier.spawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public final class Spawn {
    private Spawn() { }

    public static Location get() {
        return SpawnPlugin.instance.getSpawnLocation();
    }

    public static boolean warp(Player player, TeleportCause cause) {
        player.leaveVehicle();
        player.eject();
        return player.teleport(get(), cause);
    }

    public static boolean warp(Player player) {
        return warp(player, TeleportCause.PLUGIN);
    }
}

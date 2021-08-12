package com.winthier.spawn;

import com.cavetale.core.event.player.PluginPlayerEvent;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@RequiredArgsConstructor
public final class SpawnCommand implements TabExecutor {
    private final SpawnPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        // If there are arguments, attempt to copy named players to spawn.
        if (args.length > 0) {
            // Check permission.
            if (!sender.hasPermission("spawn.other")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            // Find and teleport.
            for (String arg : args) {
                if ("*".equals(arg)) {
                    int count = 0;
                    for (Player other : Bukkit.getOnlinePlayers()) {
                        if (!other.equals(player)) {
                            other.teleport(plugin.getSpawnLocation(), TeleportCause.COMMAND);
                            sender.sendMessage(ChatColor.YELLOW + "Teleported " + other.getName() + " to spawn.");
                            other.sendMessage(plugin.message);
                            count += 1;
                        }
                    }
                } else {
                    Player other = Bukkit.getPlayerExact(arg);
                    if (other == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found: " + arg + ".");
                    } else {
                        other.eject();
                        other.teleport(plugin.getSpawnLocation(), TeleportCause.COMMAND);
                        sender.sendMessage(ChatColor.YELLOW + "Teleported " + other.getName() + " to spawn.");
                        other.sendMessage(plugin.message);
                        plugin.getLogger().info(sender.getName() + " teleported " + other.getName() + " to spawn.");
                    }
                }
            }
            return true;
        }
        // No argument means teleport the caller.
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player expected");
            return true;
        }
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
        Location location = plugin.getSpawnLocation();
        if (!PluginPlayerEvent.Name.USE_SPAWN.cancellable(plugin, player).detail("location", location).call()) {
            return true;
        }
        player.teleport(location, TeleportCause.COMMAND);
        player.sendMessage(plugin.message);
        plugin.getLogger().info("Teleported " + player.getName() + " to spawn.");
        player.sendTitle("", ChatColor.GREEN + "Welcome to Spawn");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}

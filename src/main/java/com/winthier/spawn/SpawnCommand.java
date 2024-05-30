package com.winthier.spawn;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.command.RemotePlayer;
import com.cavetale.core.command.RemotePlayerWrapper;
import com.cavetale.core.connect.Connect;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.title.Title.title;

public final class SpawnCommand extends AbstractCommand<SpawnPlugin> {
    protected SpawnCommand(final SpawnPlugin plugin) {
        super(plugin, "spawn");
    }

    @Override
    protected void onEnable() {
        rootNode.denyTabCompletion()
            .description("Teleport to spawn")
            .senderCaller(this::command);
    }

    private boolean command(CommandSender sender, String[] args) {
        if (args.length == 0 && plugin.remote != null && !plugin.remote.isEmpty() && sender instanceof Player player) {
            Connect.get().dispatchRemoteCommand(player, "spawn", plugin.remote);
            return true;
        } else if (sender instanceof RemotePlayer player) {
            if (args.length != 0) return false;
            spawn(player);
            return true;
        } else if (args.length > 0) {
            return sender.hasPermission("spawn.other")
                ? other(sender, args)
                : false;
        } else if (sender instanceof Player player) {
            spawn(new RemotePlayerWrapper(player));
            return true;
        } else {
            throw new CommandWarn("Player expected");
        }
    }

    private void spawn(RemotePlayer player) {
        if (player.isPlayer() && player.getPlayer().isInsideVehicle()) {
            player.getPlayer().leaveVehicle();
        }
        Location location = plugin.getSpawnLocation();
        player.bring(plugin, location, player2 -> {
                if (player2 == null) return;
                plugin.getLogger().info("Teleported " + player2.getName() + " to spawn");
                player.showTitle(title(Component.empty(),
                                       Component.text("Welcome to Spawn", NamedTextColor.GREEN)));
                PluginPlayerEvent.Name.USE_SPAWN.make(plugin, player2)
                    .detail(Detail.LOCATION, location)
                    .callEvent();
            });
    }

    private boolean other(CommandSender sender, String[] args) {
        for (String arg : args) {
            if ("*".equals(arg)) {
                int count = 0;
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!other.equals(sender)) {
                        other.teleport(plugin.getSpawnLocation(), TeleportCause.COMMAND);
                        sender.sendMessage(text("Teleported " + other.getName() + " to spawn", YELLOW));
                        count += 1;
                    }
                }
            } else {
                Player other = Bukkit.getPlayerExact(arg);
                if (other == null) {
                    throw new CommandWarn("Player not found: " + arg);
                } else {
                    other.eject();
                    other.teleport(plugin.getSpawnLocation(), TeleportCause.COMMAND);
                    sender.sendMessage(text("Teleported " + other.getName() + " to spawn", YELLOW));
                    plugin.getLogger().info(sender.getName() + " teleported " + other.getName() + " to spawn.");
                }
            }
        }
        return true;
    }
}

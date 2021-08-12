package com.winthier.spawn;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class SetSpawnCommand implements TabExecutor {
    private final SpawnPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("[spawn:setspawn] Player expected");
            return true;
        }
        final Location loc = player.getLocation();
        plugin.setSpawnLocation(loc);
        player.sendMessage(ChatColor.YELLOW + "Spawn location set to "
                           + loc.getBlockX() + " "
                           + loc.getBlockY() + " "
                           + loc.getBlockZ());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}

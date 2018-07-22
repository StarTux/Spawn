package com.winthier.spawn;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class SpawnPlugin extends JavaPlugin implements Listener {
    private Location spawnLocation;
    private boolean teleportOnJoin;
    private String message;
    private boolean particleEffects;
    private final Set<UUID> spawnLocatedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        teleportOnJoin = getConfig().getBoolean("TeleportOnJoin");
        message = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message"));
        getConfig().options().copyDefaults(true);
        particleEffects = getConfig().getBoolean("ParticleEffects");
    }

    private void loadSpawnLocation() {
        spawnLocation = null;
        ConfigurationSection config = getConfig().getConfigurationSection("spawn");
        if (config == null) return;
        String worldName = config.getString("World");
        World world = getServer().getWorld(worldName);
        if (world == null) {
            getLogger().warning("Spawn world " + worldName + " not found. Using default instead.");
            return;
        }
        double x = config.getDouble("X");
        double y = config.getDouble("Y");
        double z = config.getDouble("Z");
        double pitch = config.getDouble("Pitch");
        double yaw = config.getDouble("Yaw");
        spawnLocation = new Location(world, x, y, z, (float)yaw, (float)pitch);
    }

    private void saveSpawnLocation() {
        ConfigurationSection config = getConfig().createSection("spawn");
        if (spawnLocation != null) {
            config.set("World", spawnLocation.getWorld().getName());
            config.set("X", spawnLocation.getX());
            config.set("Y", spawnLocation.getY());
            config.set("Z", spawnLocation.getZ());
            config.set("Yaw", spawnLocation.getYaw());
            config.set("Pitch", spawnLocation.getPitch());
        }
        saveConfig();
    }

    public Location getSpawnLocation() {
        if (spawnLocation == null) {
            loadSpawnLocation();
            if (spawnLocation == null) {
                spawnLocation = getServer().getWorlds().get(0).getSpawnLocation();
            }
        }
        return spawnLocation;
    }

    public void setSpawnLocation(Location location) {
        if (location != null) {
            location.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
        spawnLocation = location;
        saveSpawnLocation();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) player = (Player)sender;
        if (label.equalsIgnoreCase("spawn")) {
            // If there are arguments, attempt to copy named players to spawn.
            if (args.length > 0) {
                // Check permission.
                if (!sender.hasPermission("spawn.other")) {
                    sender.sendMessage("" + ChatColor.RED + "You don't have permission.");
                    return true;
                }
                // Find and teleport.
                for (String arg : args) {
                    if ("*".equals(arg)) {
                        int count = 0;
                        for (Player other: getServer().getOnlinePlayers()) {
                            if (!other.equals(player)) {
                                other.teleport(getSpawnLocation(), TeleportCause.COMMAND);
                                sender.sendMessage("" + ChatColor.YELLOW + "Teleported " + other.getName() + " to spawn.");
                                other.sendMessage(message);
                                count += 1;
                            }
                        }
                    } else {
                        Player other = getServer().getPlayer(arg);
                        if (other == null) {
                            sender.sendMessage("" + ChatColor.RED + "Player not found: " + arg + ".");
                        } else {
                            other.eject();
                            other.teleport(getSpawnLocation(), TeleportCause.COMMAND);
                            sender.sendMessage("" + ChatColor.YELLOW + "Teleported " + other.getName() + " to spawn.");
                            other.sendMessage(message);
                            getLogger().info(sender.getName() + " teleported " + other.getName() + " to spawn.");
                        }
                    }
                }
                return true;
            }

            // No argument means teleport the caller.
            if (player == null) {
                sender.sendMessage("" + ChatColor.RED + "Player expected");
                return true;
            }
            //if (particleEffects) spawnLocation.getWorld().spigot().playEffect(spawnLocation.clone().add(0.0, 0.5, 0.0), Effect.PORTAL, 0, 0, 0.2f, 0.5f, 0.2f, 0.1f, 256, 32);
            player.eject();
            player.teleport(getSpawnLocation(), TeleportCause.COMMAND);
            //if (particleEffects) player.getLocation().getWorld().spigot().playEffect(player.getLocation().add(0.0, 0.5, 0.0), Effect.SMOKE, 0, 0, 0.2f, 0.5f, 0.2f, 0.001f, 256, 32);
            player.sendMessage(message);
            getLogger().info("Teleported " + player.getName() + " to spawn.");
            String cc;
            CommandSender console = getServer().getConsoleSender();
            player.sendTitle("", ChatColor.GREEN + "Welcome to Spawn");
            return true;
        } else if (label.equalsIgnoreCase("setspawn")) {
            if (player == null) {
                sender.sendMessage("" + ChatColor.RED + "Player expected");
                return true;
            }
            final Location loc = player.getLocation();
            setSpawnLocation(loc);
            player.sendMessage(String.format("" + ChatColor.YELLOW + "Spawn location set to (%d,%d,%d).", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn()) return;
        event.setRespawnLocation(getSpawnLocation());
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        if (teleportOnJoin || !event.getPlayer().hasPlayedBefore()) {
            spawnLocatedPlayers.add(event.getPlayer().getUniqueId());
            event.setSpawnLocation(getSpawnLocation());
            getLogger().info("Did set spawn location of " + event.getPlayer().getName() + ".");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (spawnLocatedPlayers.remove(event.getPlayer().getUniqueId())) {
            event.getPlayer().setGameMode(getServer().getDefaultGameMode());
            getLogger().info("Did force game mode of " + event.getPlayer().getName() + ".");
        }
    }
}

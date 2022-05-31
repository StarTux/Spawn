package com.winthier.spawn;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpawnPlugin extends JavaPlugin {
    protected static SpawnPlugin instance;
    private Location spawnLocation;
    private int spawnRadius;
    protected boolean teleportOnJoin;
    protected boolean omitDefaultWorld;
    protected boolean initialSpawn;
    protected Random random;
    protected String remote;

    @Override
    public void onEnable() {
        instance = this;
        random = ThreadLocalRandom.current();
        new SpawnCommand(this).enable();
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        loadConfiguration();
    }

    public void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
        teleportOnJoin = getConfig().getBoolean("TeleportOnJoin");
        omitDefaultWorld = getConfig().getBoolean("OmitDefaultWorld");
        initialSpawn = getConfig().getBoolean("InitialSpawn");
        spawnRadius = getConfig().getInt("SpawnRadius");
        loadSpawnLocation();
        remote = getConfig().getString("Remote");
    }

    private void loadSpawnLocation() {
        spawnLocation = null;
        ConfigurationSection config = getConfig().getConfigurationSection("spawn");
        if (config == null) {
            getLogger().warning("Spawn not set. Using default instead.");
            spawnLocation = getServer().getWorlds().get(0).getSpawnLocation();
            return;
        }
        String worldName = config.getString("World");
        World world = getServer().getWorld(worldName);
        if (world == null) {
            getLogger().warning("Spawn world " + worldName + " not found. Using default instead.");
            spawnLocation = getServer().getWorlds().get(0).getSpawnLocation();
            return;
        }
        double x = config.getDouble("X");
        double y = config.getDouble("Y");
        double z = config.getDouble("Z");
        double pitch = config.getDouble("Pitch");
        double yaw = config.getDouble("Yaw");
        spawnLocation = new Location(world, x, y, z, (float) yaw, (float) pitch);
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
        if (spawnRadius > 0) {
            double radius = random.nextDouble() * (double) spawnRadius;
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            return spawnLocation.clone().add(dx, 0, dz);
        }
        return spawnLocation.clone();
    }

    public void setSpawnLocation(Location location) {
        if (location != null) {
            location.getWorld().setSpawnLocation(location.getBlockX(),
                                                 location.getBlockY(),
                                                 location.getBlockZ());
        }
        spawnLocation = location;
        saveSpawnLocation();
    }
}

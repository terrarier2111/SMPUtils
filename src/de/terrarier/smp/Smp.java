package de.terrarier.smp;

import de.terrarier.smp.commands.CommandLocation;
import de.terrarier.smp.listeners.ListenerDeath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public final class Smp extends JavaPlugin {

    public HashMap<UUID, HashMap<String, Location>> ownedLocs = new HashMap<>();
    public HashMap<String, Location> globalLocs = new HashMap<>();

    @Override
    public void onEnable() {
        // initialize locations
        File globalLocs = new File("./smp/locs/global/");
        globalLocs.mkdirs();
        for (File loc : globalLocs.listFiles()) {
            this.globalLocs.put(loc.getName().split(".")[0], getLocation(loc));
        }
        File playersDirs = new File("./smp/locs/own/");
        for (File player : playersDirs.listFiles()) {
            HashMap<String, Location> locs = new HashMap<>();
            for (File loc : player.listFiles()) {
                locs.put(loc.getName().split(".")[0], getLocation(loc));
            }
            ownedLocs.put(UUID.fromString(player.getName()), locs);
        }

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ListenerDeath(), this);
        getCommand("location").setExecutor(new CommandLocation(this));
    }

    public static void setLocation(File file, Location location) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("world", location.getWorld().getName());
        cfg.set("x", location.getX());
        cfg.set("y", location.getY());
        cfg.set("z", location.getZ());
        cfg.set("yaw", location.getYaw());
        cfg.set("pitch", location.getPitch());
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Location getLocation(File file) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String worldName;
        double x;
        double y;
        double z;
        float yaw = 0;
        float pitch = 0;
        worldName = cfg.getString("world");
        x = cfg.getDouble("x");
        y = cfg.getDouble("y");
        z = cfg.getDouble("z");
        if (cfg.contains("yaw")) {
            yaw = (float) cfg.getDouble("yaw");
        }
        if (cfg.contains("pitch")) {
            pitch = (float) cfg.getDouble("pitch");
        }
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

}

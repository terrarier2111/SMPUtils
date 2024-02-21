package de.terrarier.smp;

import de.terrarier.smp.commands.CommandLocation;
import de.terrarier.smp.listeners.ListenerCrops;
import de.terrarier.smp.listeners.ListenerDeath;
import de.terrarier.smp.listeners.ListenerMobGrief;
import de.terrarier.smp.listeners.ListenerSit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Smp extends JavaPlugin {

    public HashMap<UUID, HashMap<String, Location>> ownedLocs = new HashMap<>();
    public HashMap<String, Location> globalLocs = new HashMap<>();
    public HashMap<UUID, Location> follow = new HashMap<>();

    // FIXME: implement combat logging system and afk system (automatic protection when player goes afk)


    @Override
    public void onLoad() {
        String ver = Bukkit.getServer().getClass().getPackage().getName();
        ver = ver.substring(ver.lastIndexOf(".") + 1);
        VERSION = ver;
    }

    @Override
    public void onEnable() {
        // initialize locations
        File globalLocs = new File("./plugins/smp/locs/global/");
        globalLocs.mkdirs();
        for (File loc : globalLocs.listFiles()) {
            this.globalLocs.put(loc.getName().split("\\.")[0], getLocation(loc));
        }
        File playersDirs = new File("./plugins/smp/locs/own/");
        if (playersDirs.exists()) {
            for (File player : playersDirs.listFiles()) {
                HashMap<String, Location> locs = new HashMap<>();
                for (File loc : player.listFiles()) {
                    locs.put(loc.getName().split("\\.")[0], getLocation(loc));
                }
                ownedLocs.put(UUID.fromString(player.getName()), locs);
            }
        }

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ListenerDeath(), this);
        pm.registerEvents(new ListenerCrops(this), this);
        pm.registerEvents(new ListenerSit(this), this);
        // this module disables the most annoying parts about mob griefing while still allowing
        // for things like villager breeding which the game rule for mob griefing disables as well
        pm.registerEvents(new ListenerMobGrief(), this);
        getCommand("location").setExecutor(new CommandLocation(this));

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Map.Entry<UUID, Location> entry : follow.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    sendActionBar(player, "§7" + entry.getValue().getBlockX() + " | " + entry.getValue().getBlockY() + " | " + entry.getValue().getBlockZ());
                }
            }
        }, 1L, 1L);
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

    private static String VERSION;

    public void sendActionBar(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (VERSION.startsWith("v1_20")) {
            try {
                Method method = player.spigot().getClass().getDeclaredMethod("sendMessage", ChatMessageType.class, BaseComponent.class);
                method.setAccessible(true);
                method.invoke(player.spigot(), ChatMessageType.ACTION_BAR, new TextComponent(message));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object packet;
            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + VERSION + ".PacketPlayOutChat");
            Class<?> packetClass = Class.forName("net.minecraft.server." + VERSION + ".Packet");
            if (VERSION.equalsIgnoreCase("v1_8_R1") || VERSION.startsWith("v1_7_")) {
                Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + VERSION + ".ChatSerializer");
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + VERSION + ".IChatBaseComponent");
                Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
                Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
                packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(cbc, (byte) 2);
            } else {
                Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + VERSION + ".ChatComponentText");
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + VERSION + ".IChatBaseComponent");
                try {
                    Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + VERSION + ".ChatMessageType");
                    Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
                    Object chatMessageType = null;
                    for (Object obj : chatMessageTypes) {
                        if (obj.toString().equals("GAME_INFO")) {
                            chatMessageType = obj;
                        }
                    }
                    Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
                    packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass}).newInstance(chatCompontentText, chatMessageType);
                } catch (ClassNotFoundException cnfe) {
                    Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
                    packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(chatCompontentText, (byte) 2);
                }
            }
            Method craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
            Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
            Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(craftPlayerHandle);
            Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

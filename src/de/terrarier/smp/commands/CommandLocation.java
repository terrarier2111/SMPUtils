package de.terrarier.smp.commands;

import de.terrarier.smp.Smp;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class CommandLocation implements CommandExecutor {

    private static final String OWN_PREFIX = "§7[Own] ";
    private static final String GLOBAL_PREFIX = "§7[Global] ";

    private final Smp instance;

    public CommandLocation(Smp instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only supported for players");
            return false;
        }
        String rawCmd = label.split(" ")[0];
        if (args.length == 0 || args.length > 3) {
            sendUsage(sender, rawCmd);
            return false;
        }
        Player player = (Player) sender;
        switch (args[0].toLowerCase()) {
            case "list":
                if (args.length > 2) {
                    sendUsage(sender, rawCmd);
                    return false;
                }
                if (args.length == 1) {
                    HashMap<String, Location> ownLocs = instance.ownedLocs.get(player.getUniqueId());
                    int locations = instance.globalLocs.size();
                    if (ownLocs != null) {
                        locations += ownLocs.size();
                    }
                    if (locations == 0) {
                        player.sendMessage("§cThere are no locations set yet");
                        return true;
                    }
                    player.sendMessage("§7Locations (" + locations + "):");
                    for (Map.Entry<String, Location> loc : instance.globalLocs.entrySet()) {
                        player.sendMessage(GLOBAL_PREFIX + "\"§6" + loc.getKey() + "§7\": §6" + loc.getValue().getBlockX() + "§7, §6" + loc.getValue().getBlockY() + "§7, §6" + loc.getValue().getBlockZ() + " §7in \"§6" + loc.getValue().getWorld().getName() + "§7\"");
                    }
                    if (ownLocs != null) {
                        for (Map.Entry<String, Location> loc : ownLocs.entrySet()) {
                            player.sendMessage(OWN_PREFIX + "\"§6" + loc.getKey() + "§7\": §6" + loc.getValue().getBlockX() + "§7, §6" + loc.getValue().getBlockY() + "§7, §6" + loc.getValue().getBlockZ() + " §7in \"§6" + loc.getValue().getWorld().getName() + "§7\"");
                        }
                    }
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "global":
                        if (instance.globalLocs.isEmpty()) {
                            player.sendMessage(GLOBAL_PREFIX + "§cThere are no global locations yet");
                            return true;
                        }
                        player.sendMessage(GLOBAL_PREFIX + "§7Locations (" + instance.globalLocs.size() + "):");
                        for (Map.Entry<String, Location> loc : instance.globalLocs.entrySet()) {
                            player.sendMessage(GLOBAL_PREFIX + "\"§6" + loc.getKey() + "§7\": §6" + loc.getValue().getBlockX() + "§7, §6" + loc.getValue().getBlockY() + "§7, §6" + loc.getValue().getBlockZ() + " §7in \"§6" + loc.getValue().getWorld().getName() + "§7\"");
                        }
                        break;
                    case "own":
                        HashMap<String, Location> ownLocs = instance.ownedLocs.get(player.getUniqueId());
                        if (ownLocs == null) {
                            player.sendMessage(OWN_PREFIX + "§cYou have not yet set any own locations");
                            return true;
                        }
                        player.sendMessage(OWN_PREFIX + "§7Locations (" + ownLocs.size() + "):");
                        for (Map.Entry<String, Location> loc : ownLocs.entrySet()) {
                            player.sendMessage(OWN_PREFIX + "\"§6" + loc.getKey() + "§7\": §6" + loc.getValue().getBlockX() + "§7, §6" + loc.getValue().getBlockY() + "§7, §6" + loc.getValue().getBlockZ() + " §7in \"§6" + loc.getValue().getWorld().getName() + "§7\"");
                        }
                        break;
                    default:
                        sendUsage(sender, rawCmd);
                        break;
                }
                break;
            case "set":
                if (args.length != 3) {
                    sendUsage(sender, rawCmd);
                    return false;
                }
                String locName = args[1].toLowerCase();
                switch (args[2].toLowerCase()) {
                    case "global":
                        try {
                            File file = new File("./plugins/smp/locs/global/" + locName + ".yml");
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            Smp.setLocation(file, player.getLocation());
                            instance.globalLocs.put(locName, player.getLocation());
                            player.sendMessage(GLOBAL_PREFIX + "§aSuccessfully set \"" + args[1] + "\" to your current location");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case "own":
                        new File("./plugins/smp/locs/own/" + player.getUniqueId().toString()).mkdirs();
                        try {
                            File file = new File("./plugins/smp/locs/own/" + player.getUniqueId().toString() + "/" + locName + ".yml");
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            Smp.setLocation(file, player.getLocation());
                            instance.ownedLocs.getOrDefault(player.getUniqueId(), new HashMap<>()).put(locName, player.getLocation());
                            player.sendMessage(OWN_PREFIX + "§aSuccessfully set \"" + args[1] + "\" to your current location");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    default:
                        sendUsage(sender, rawCmd);
                        break;
                }
                break;
            case "remove":
                if (args.length != 3) {
                    sendUsage(sender, rawCmd);
                    return false;
                }

                String locName2 = args[1].toLowerCase();

                switch (args[2].toLowerCase()) {
                    case "global":
                        if (instance.globalLocs.remove(locName2) == null) {
                            player.sendMessage(GLOBAL_PREFIX + "§cThere is no global location called \"§7" + args[1] + "§c\"");
                            return false;
                        }
                        new File("./plugins/smp/locs/global/" + locName2 + ".yml").delete();
                        player.sendMessage(GLOBAL_PREFIX + "§aSuccessfully deleted location \"" + locName2 + "\"");
                        break;
                    case "own":
                        HashMap<String, Location> ownLocs = instance.ownedLocs.get(player.getUniqueId());
                        if (ownLocs == null) {
                            player.sendMessage(OWN_PREFIX + "§cYou don't have any own locations");
                            return true;
                        }
                        if (ownLocs.remove(locName2) == null) {
                            player.sendMessage(OWN_PREFIX + "§cYou don't have a location called \"§7" + args[1] + "§c\"");
                            return true;
                        }
                        new File("./plugins/smp/locs/own/" + player.getUniqueId() + "/" + locName2 + ".yml").delete();
                        player.sendMessage(OWN_PREFIX + "§aSuccessfully deleted location \"" + args[1] + "\"");
                        break;
                    default:
                        sendUsage(sender, rawCmd);
                        break;
                }

                break;
            case "follow":
                if (args.length != 3) {
                    sendUsage(sender, rawCmd);
                    return false;
                }

                String locName3 = args[1].toLowerCase();

                switch (args[2].toLowerCase()) {
                    case "global":
                        Location loc1 = instance.globalLocs.get(locName3);
                        if (loc1 == null) {
                            player.sendMessage(GLOBAL_PREFIX + "§cThere is no global location called \"§7" + args[1] + "§c\"");
                            return false;
                        }
                        instance.follow.put(player.getUniqueId(), loc1);
                        player.sendMessage("§aFollowing " + locName3);
                        break;
                    case "own":
                        HashMap<String, Location> ownLocs = instance.ownedLocs.get(player.getUniqueId());
                        if (ownLocs == null) {
                            player.sendMessage(OWN_PREFIX + "§cYou don't have any own locations");
                            return true;
                        }
                        Location loc2 = ownLocs.get(locName3);
                        if (loc2 == null) {
                            player.sendMessage(OWN_PREFIX + "§cYou don't have a location called \"§7" + args[1] + "§c\"");
                            return true;
                        }
                        instance.follow.put(player.getUniqueId(), loc2);
                        player.sendMessage("§aFollowing " + locName3);
                        break;
                    default:
                        sendUsage(sender, rawCmd);
                        break;
                }
            case "unfollow":
                if (args.length != 1) {
                    sendUsage(sender, rawCmd);
                    return false;
                }
                boolean hadLoc = instance.follow.remove(player.getUniqueId()) != null;
                if (hadLoc) {
                    player.sendMessage("§aUnfollowed location");
                } else {
                    player.sendMessage("§cThere is no location to unfollow");
                }
                break;
            default:

                break;
        }
        return true;
    }

    void sendUsage(CommandSender sender, String cmd) {
        sender.sendMessage("§cUsage:");
        sender.sendMessage("§c/" + cmd + " list <own, global> (optional)");
        sender.sendMessage("§c/" + cmd + " set [name] <own, global> | set a specific location");
        sender.sendMessage("§c/" + cmd + " remove [name] <own, global> | delete the specific location");
        sender.sendMessage("§c/" + cmd + " [name] <own, global> | get the coords of the specific location");
        sender.sendMessage("§c/" + cmd + " follow [name] <own, global> | follow a specific location");
        sender.sendMessage("§c/" + cmd + " unfollow | stop following a location");
    }

}

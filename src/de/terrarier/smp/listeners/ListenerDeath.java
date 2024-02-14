package de.terrarier.smp.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class ListenerDeath implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent ev) {
        Location location = ev.getEntity().getLocation();
        ev.getEntity().sendMessage("§6cYou died at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + " in the world \"" + location.getWorld().getName() + "\"");
    }

}

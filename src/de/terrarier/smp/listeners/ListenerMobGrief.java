package de.terrarier.smp.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class ListenerMobGrief implements Listener {

    @EventHandler
    public void onExplosion(EntityExplodeEvent ev) {
        if (ev.getEntityType() == EntityType.CREEPER || ev.getEntityType() == EntityType.GHAST) {
            ev.blockList().clear();
        }
    }

    @EventHandler
    public void onPickup(EntityChangeBlockEvent ev) {
        if (ev.getEntityType() == EntityType.ENDERMAN) {
            ev.setCancelled(true);
        }
    }

}

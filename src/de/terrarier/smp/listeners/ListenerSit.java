package de.terrarier.smp.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ListenerSit implements Listener {

    private final HashMap<UUID, Arrow> arrows = new HashMap<>();
    private final JavaPlugin instance;

    public ListenerSit(JavaPlugin instance) {
        this.instance = instance;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {
            for (Map.Entry<UUID, Arrow> arrow : arrows.entrySet()) {
                arrow.getValue().setTicksLived(1);
            }
        }, 200L, 200L);
    }

    @EventHandler
    public void onSit(PlayerInteractEvent ev) {
        if ((ev.getPlayer().getItemInHand() == null || ev.getPlayer().getItemInHand().getType() == Material.AIR)
                && ev.getClickedBlock() != null && ev.getClickedBlock().getType().name().toLowerCase().contains("stair")) {
            ev.setCancelled(true);
            Location loc = ev.getClickedBlock().getLocation().clone().add(0.5, 0.25, 0.5);
            Arrow arrow = ev.getClickedBlock().getWorld().spawnArrow(loc, new Vector(0, 0, 0), (float) 0.0, (float) 0.0);
            arrow.setPassenger(ev.getPlayer());
            arrow.spigot().setDamage(0.0);
            arrows.put(arrow.getUniqueId(), arrow);
        }
    }

    @EventHandler
    public void onUnsit(EntityDismountEvent ev) {
        if (ev.getEntity().getType() == EntityType.ARROW && ev.getDismounted().getType() == EntityType.PLAYER) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> ev.getEntity().remove(), 1L);
            arrows.remove(ev.getEntity().getUniqueId());
        }
    }

}

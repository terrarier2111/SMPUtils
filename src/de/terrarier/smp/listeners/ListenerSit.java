package de.terrarier.smp.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Stairs;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ListenerSit implements Listener {

    private final HashMap<UUID, Arrow> arrows = new HashMap<>();
    private final HashMap<Location, UUID> locToArrow = new HashMap<>();
    private final HashMap<UUID, Location> arrowToLoc = new HashMap<>();
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
                && ev.getClickedBlock() != null && ev.getClickedBlock().getType().name().toLowerCase().contains("stair")
                && ev.getAction() == Action.RIGHT_CLICK_BLOCK
                && !ev.getPlayer().isInsideVehicle()) {
            // there may only ever be one player sitting at a certain location
            if (locToArrow.containsKey(ev.getClickedBlock().getLocation())) {
                return;
            }
            boolean isEmpty = ev.getClickedBlock().getLocation().clone().add(0, 1, 0).getBlock().isEmpty();
            Stairs stairs = (Stairs) ev.getClickedBlock().getState().getData();
            if (!stairs.isInverted() && isEmpty && (ev.getClickedBlock().getLocation().getBlockY() - ev.getPlayer().getLocation().getY()) < 0.51) {
                ev.setCancelled(true);

                Location loc = ev.getClickedBlock().getLocation().clone().add(0.5, 0.25, 0.5);
                Arrow arrow = ev.getClickedBlock().getWorld().spawnArrow(loc, new Vector(0, 0, 0), (float) 0.0, (float) 0.0);
                arrow.setPassenger(ev.getPlayer());
                arrow.setKnockbackStrength(0);
                arrows.put(arrow.getUniqueId(), arrow);
                locToArrow.put(ev.getClickedBlock().getLocation(), arrow.getUniqueId());
                arrowToLoc.put(arrow.getUniqueId(), ev.getClickedBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onUnsit(EntityDismountEvent ev) {
        if (ev.getDismounted().getType() == EntityType.ARROW && ev.getEntity().getType() == EntityType.PLAYER) {
            removeArrow(ev.getDismounted().getUniqueId());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent ev) {
        UUID arrow = locToArrow.remove(ev.getBlock().getLocation());
        if (arrow != null) {
            removeArrow(arrow);
        }
    }

    private void removeArrow(UUID arrow) {
        Arrow arrowEnt = arrows.remove(arrow);
        // we have to check this as we are calling eject inside this body which will in turn cause an EntityDismountEvent in which we would
        // call removeArrow again but the maps would already have been modified
        if (arrowEnt != null) {
            Location loc = arrowToLoc.remove(arrow);
            locToArrow.remove(loc);

            Entity passenger = arrowEnt.getPassenger();
            passenger.eject();
            passenger.teleport(passenger.getLocation().clone().add(0, 0.25, 0));

            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, arrowEnt::remove, 1L);
        }
    }

}

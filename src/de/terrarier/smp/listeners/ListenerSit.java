package de.terrarier.smp.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
            Material upper = ev.getClickedBlock().getLocation().clone().add(0, 1, 0).getBlock().getType();
            // BlockFace bFace = ev.getClickedBlock().getFace(ev.getClickedBlock().getLocation().clone().add(0, -1, 0).getBlock());
            if (/*((bFace != BlockFace.DOWN && bFace != BlockFace.UP) || (ev.getBlockFace() != BlockFace.DOWN && ev.getBlockFace() != BlockFace.UP)) && */(ev.getClickedBlock().getLocation().getBlockY() - ev.getPlayer().getLocation().getY()) < 0.51) {
                ev.setCancelled(true);

                Location loc = ev.getClickedBlock().getLocation().clone().add(0.5, 0.25, 0.5);
                Arrow arrow = ev.getClickedBlock().getWorld().spawnArrow(loc, new Vector(0, 0, 0), (float) 0.0, (float) 0.0);
                arrow.setPassenger(ev.getPlayer());
                arrow.setKnockbackStrength(0);
                arrows.put(arrow.getUniqueId(), arrow);
            }
        }
    }

    @EventHandler
    public void onUnsit(EntityDismountEvent ev) {
        if (ev.getDismounted().getType() == EntityType.ARROW && ev.getEntity().getType() == EntityType.PLAYER) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> ev.getDismounted().remove(), 1L);
            arrows.remove(ev.getDismounted().getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> ev.getEntity().teleport(ev.getEntity().getLocation().clone().add(0, 0.25, 0)), 1L);
        }
    }

}

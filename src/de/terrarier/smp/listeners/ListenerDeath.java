package de.terrarier.smp.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collections;

public final class ListenerDeath implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent ev) {
        // send death coords
        Location location = ev.getEntity().getLocation();
        ev.getEntity().sendMessage("§6cYou died at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + " in the world \"" + location.getWorld().getName() + "\"");

        // drop player head
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(ev.getEntity().getName());
        meta.setDisplayName("§6" + ev.getEntity().getDisplayName());
        meta.setLore(Collections.singletonList(ev.getEntity().getName()));
        head.setItemMeta(meta);
        ev.getDrops().add(head);
    }

}

package de.terrarier.smp.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ListenerCrops implements Listener {

    private final JavaPlugin instance;

    public ListenerCrops(JavaPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onCrop(BlockBreakEvent ev) {
        if ((ev.getBlock().getType() == Material.CROPS || ev.getBlock().getType() == Material.CARROT || ev.getBlock().getType() == Material.POTATO)
                && ev.getPlayer().getItemInHand() != null
                && ev.getPlayer().getItemInHand().getType().name().contains("HOE")) {
            for (ItemStack item : ev.getBlock().getDrops()) {
                if (item.getType() == Material.WHEAT || item.getType() == Material.CARROT_ITEM || item.getType() == Material.POTATO_ITEM) {
                    // ev.getBlock().setData((byte) 0);
                    Material prev = ev.getBlock().getType();
                    ev.getBlock().setType(Material.AIR);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> ev.getBlock().setType(prev), 1L);
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() == 0) {
                        item.setType(Material.AIR);
                    }
                    return;
                }
            }
        }
    }

}

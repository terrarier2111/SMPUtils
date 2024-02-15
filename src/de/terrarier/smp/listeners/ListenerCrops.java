package de.terrarier.smp.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ListenerCrops implements Listener {

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
                    ev.getBlock().setType(prev);
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

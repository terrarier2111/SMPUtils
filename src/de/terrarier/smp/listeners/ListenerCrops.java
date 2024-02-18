package de.terrarier.smp.listeners;

import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class ListenerCrops implements Listener {

    private final JavaPlugin instance;

    public ListenerCrops(JavaPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onCrop(PlayerInteractEvent ev) {
        ItemStack hand = ev.getPlayer().getItemInHand();
        if (ev.getAction() == Action.RIGHT_CLICK_BLOCK && ev.getClickedBlock() != null &&
                (ev.getClickedBlock().getType() == Material.POTATO || ev.getClickedBlock().getType() == Material.CARROT || ev.getClickedBlock().getType() == Material.CROPS || ev.getClickedBlock().getType().name().equals("BEETROOT_BLOCK"))
                && (hand == null || hand.getType() == Material.AIR || hand.getType() == Material.SEEDS || hand.getType() == Material.WHEAT || hand.getType() == Material.CARROT_ITEM || hand.getType() == Material.POTATO_ITEM || hand.getType() == Material.POISONOUS_POTATO)
                && ((Crops) ev.getClickedBlock().getState().getData()).getState() == CropState.RIPE) {
            for (ItemStack item : ev.getClickedBlock().getDrops()) {
                if (item.getType() == Material.SEEDS || item.getType() == Material.CARROT_ITEM || item.getType() == Material.POTATO_ITEM) {
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() == 0) {
                        item.setType(Material.AIR);
                    }
                }
            }
            Location loc = ev.getClickedBlock().getLocation().clone().add(0.5, 0, 0.5);
            for (ItemStack drop : ev.getClickedBlock().getDrops()) {
                loc.getWorld().dropItem(loc, drop);
            }
            Material prev = ev.getClickedBlock().getType();
            ev.getClickedBlock().setType(Material.AIR);
            ev.getClickedBlock().setType(prev);
        }
    }

}

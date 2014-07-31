package com.noyhillel.networkhub.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

/**
 * Created by Noy on 26/05/2014.
 */
public final class BouncyPads extends ModuleListener {

    public BouncyPads() {
        super("bouncy-pads");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (!(event.getAction() == Action.PHYSICAL)) return;
        if (!(block.getType().equals(Material.STONE_PLATE) || block.getType().equals(Material.WOOD_PLATE))) return;
        if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.WOOL)) return;
        player.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.0).add(new Vector(0, 2, 0)));
        player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
        event.setCancelled(true);
    }
}
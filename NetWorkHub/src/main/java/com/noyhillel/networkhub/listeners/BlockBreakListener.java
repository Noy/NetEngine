package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Created by Belfort on 4/12/2016.
 */
public final class BlockBreakListener extends ModuleListener {

    public BlockBreakListener() {
        super("block-break");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (player.isOp()) return;
        event.setCancelled(true);
    }

}

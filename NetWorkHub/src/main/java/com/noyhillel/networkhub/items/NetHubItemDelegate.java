package com.noyhillel.networkhub.items;

import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkhub.NetHub;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Noy on 25/05/2014.
 */
public abstract class NetHubItemDelegate implements Listener {

    /**
     * The Item Which the player wishes to right click.
     * @return The Item.
     */
    protected abstract ItemStack getItem();

    /**
     * The Item Slot you wish to have the item in.
     * @return The Item Slot
     */
    protected abstract Integer getItemSlot();

    /**
     * When the Player Right clicks.
     * @param player The Player
     */
    protected abstract void onRightClick(NetPlayer player); // May want to change this to onRightClickAir and onRightClickBlock in the future

    /**
     * When the Player Left clicks.
     * @param player The Player
     */
    protected abstract void onLeftClick(NetPlayer player); // May want to change this to onLeftClickAir and onLeftClickBlock in the future

    /**
     * Creating our constructor.
     * @param shouldRegisterEvents Boolean to verify registering our events.
     */
    public NetHubItemDelegate(boolean shouldRegisterEvents) {
        if (shouldRegisterEvents) NetHub.getInstance().registerListener(this);
        // super(true) to register event below.
    }

    /* Interact Event */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Getting the player
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        // Getting the action
        Action action = event.getAction();
        // Getting the Item in hand
        ItemStack itemInHand = player.getItemInHand();
        // If it's not a right click, don't do anything.
        if (!(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) return;
        // If NULL, don't do anything
        if (itemInHand == null) return;
        // If the Item does not have anything unique to it, don't do anything
        if (!(itemInHand.hasItemMeta())) return;
        // If the Item is not the Item from our getItem method, don't do anything
        if (!(itemInHand.getItemMeta().getDisplayName().equals(getItem().getItemMeta().getDisplayName()))) return;
        // Switch through the actions.
        switch (action) {
            // When they right click, call our abstract right click method.
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                onRightClick(player);
                break;
            // When they left click, call our abstract left click method.
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                onLeftClick(player);
        }
        // Cancel the event, For example, if it's a hoe, it would not be used for farming.
        event.setCancelled(true); // Obviously.
    }
}
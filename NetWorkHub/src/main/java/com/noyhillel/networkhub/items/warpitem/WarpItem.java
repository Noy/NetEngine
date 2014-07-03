package com.noyhillel.networkhub.items.warpitem;

import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.InventoryGUI;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.NetHub;
import com.noyhillel.networkhub.items.NetHubItemDelegate;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Noy on 26/05/2014.
 */
public final class WarpItem extends NetHubItemDelegate {

    /**
     * Private field,, the InventoryGUI
     */
    private InventoryGUI inventoryGUI;

    /**
     * Creating our constructor.
     * @param config The ConfigManager
     */
    public WarpItem(final ConfigManager config) {
        super(true); // Enabling / Registering
        this.inventoryGUI = new InventoryGUI(config.getInventoryItems(), MessageManager.getFormat("warp-item.menu-name"), new InventoryGUI.InventoryGUIDelegate() {
            /* Delegated methods. */
            /**
             * On Click method
             * @param gui Inventory GUI
             * @param item The Item
             * @param player The Player
             */
            @Override
            public void onClickItem(InventoryGUI gui, InventoryGUI.InventoryItem item, NetPlayer player) {
                inventoryGUI.closeInventory(player);
                try {
                    player.teleport(config.getLocation(item.getName()));
                }catch (Exception e) {
                    e.printStackTrace();
                }
                player.sendMessage(MessageManager.getFormat("warp-item.warp-to", true, new String[]{"<warp>", item.getName()}));
            }

            /**
             * onOpen Method - Unused
             * @param gui Inventory GUI
             * @param player The Player
             */
            @Override
            public void onOpen(InventoryGUI gui, NetPlayer player) {
            }

            /**
             * onClose Method - Unused.
             * @param gui Inventory GUI
             * @param player The Player
             */
            @Override
            public void onClose(InventoryGUI gui, NetPlayer player) {
            }
        });
    }

    /**
     * The Item Which the player wishes to right click, Overridden method.
     * @return The Item.
     */
    @Override
    protected ItemStack getItem() {
        ItemStack item = new ItemStack(Material.valueOf(NetHub.getInstance().getConfig().getString("warp-item.item").toUpperCase()));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageManager.getFormat("warp-item.item-name", false));
        List<String> lore = new ArrayList<>();
        lore.add(MessageManager.getFormat("warp-item.item-lore", false));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return new ItemStack(item);
    }

    /**
     * The Item Slot you wish to have the item in.
     * @return The Item Slot
     */
    @Override
    protected Integer getItemSlot() {
        return 0;
    }

    /**
     * When the Player Right clicks, overridden method.
     * @param player The Player
     */
    @Override
    protected void onRightClick(NetPlayer player) {
        inventoryGUI.openInventory(player);
        player.playSound(Sound.ARROW_HIT, 0.5F);
    }

    @Override
    protected void onLeftClick(NetPlayer player) {
        //nothing
    }
}
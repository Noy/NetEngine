package com.noyhillel.netkitpvp.commands;

import com.noyhillel.netkitpvp.game.ConfigManager;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.InventoryGUI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by noyhillel1 on 01/07/2014.
 */
public class KitCommandHandler extends NetAbstractCommandHandler {


    private InventoryGUI inventoryGUI;

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length != 0) throw new NewNetCommandException("Too few Arguments.", NewNetCommandException.ErrorType.FewArguments);
        ConfigManager configManager = new ConfigManager();
        inventoryGUI = new InventoryGUI(configManager.getInventoryItems(), "Test", new InventoryGUI.InventoryGUIDelegate() {
            @Override
            public void onOpen(InventoryGUI gui, NetPlayer player) {
            }

            @Override
            public void onClose(InventoryGUI gui, NetPlayer player) {

            }

            @Override
            public void onClickItem(InventoryGUI gui, InventoryGUI.InventoryItem item, NetPlayer player) {
                givePlayerKit(player);
                player.playSound(Sound.CHEST_OPEN);
                player.closeInventory();
            }
        });
    }


    // Testing
    private void givePlayerKit(NetPlayer player) {
        player.giveItem(Material.ACACIA_STAIRS);
        player.giveItem(Material.ACTIVATOR_RAIL);
        player.giveItem(Material.APPLE);
        player.giveItem(Material.WORKBENCH);
        player.giveItemAStackOf(Material.WOOD_SPADE);
    }
}

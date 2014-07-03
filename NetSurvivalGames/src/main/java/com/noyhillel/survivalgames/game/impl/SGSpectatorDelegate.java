package com.noyhillel.survivalgames.game.impl;

import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUI;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUIDelegate;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUIItem;
import lombok.Data;

@Data
public final class SGSpectatorDelegate implements InventoryGUIDelegate {
    private final SGGame game;
    @Override
    public void playerClickedItem(GPlayer player, InventoryGUIItem item, InventoryGUI gui) {
        if (!(item instanceof SGSpectatorHeadItem)) return;
        GPlayer spectator = ((SGSpectatorHeadItem) item).getSpectator();
        game.attemptSpectatorTeleport(player, spectator);
        gui.close(player);
    }
}

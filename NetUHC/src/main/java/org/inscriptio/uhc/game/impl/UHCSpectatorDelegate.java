package org.inscriptio.uhc.game.impl;

import lombok.Data;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.utils.inventory.InventoryGUI;
import org.inscriptio.uhc.utils.inventory.InventoryGUIDelegate;
import org.inscriptio.uhc.utils.inventory.InventoryGUIItem;

@Data
public final class UHCSpectatorDelegate implements InventoryGUIDelegate {
    private final UHCGame game;
    @Override
    public void playerClickedItem(UHCPlayer player, InventoryGUIItem item, InventoryGUI gui) {
        if (!(item instanceof UHCSpectatorHeadItem)) return;
        UHCPlayer spectator = ((UHCSpectatorHeadItem) item).getSpectator();
        game.attemptSpectatorTeleport(player, spectator);
        gui.close(player);
    }
}

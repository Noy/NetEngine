package org.inscriptio.uhc.utils.inventory;

import org.inscriptio.uhc.player.UHCPlayer;

public interface InventoryGUIDelegate {
    void playerClickedItem(UHCPlayer player, InventoryGUIItem item, InventoryGUI gui);
}

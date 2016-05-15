package com.noyhillel.survivalgames.utils.inventory;

import com.noyhillel.survivalgames.player.SGPlayer;

public interface InventoryGUIDelegate {
    void playerClickedItem(SGPlayer player, InventoryGUIItem item, InventoryGUI gui);
}

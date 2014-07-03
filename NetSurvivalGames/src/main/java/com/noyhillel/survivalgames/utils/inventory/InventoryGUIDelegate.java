package com.noyhillel.survivalgames.utils.inventory;

import com.noyhillel.survivalgames.player.GPlayer;

public interface InventoryGUIDelegate {
    void playerClickedItem(GPlayer player, InventoryGUIItem item, InventoryGUI gui);
}

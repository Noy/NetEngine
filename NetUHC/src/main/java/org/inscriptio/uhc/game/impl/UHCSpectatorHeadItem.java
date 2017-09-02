package org.inscriptio.uhc.game.impl;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.utils.inventory.InventoryGUIItem;

final class UHCSpectatorHeadItem extends InventoryGUIItem {
    @Getter private final UHCPlayer spectator;

    UHCSpectatorHeadItem(ItemStack representationItem, UHCPlayer player) {
        super(representationItem);
        this.spectator = player;
    }
}

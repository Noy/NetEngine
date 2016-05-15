package com.noyhillel.survivalgames.game.impl;

import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUIItem;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public final class SGSpectatorHeadItem extends InventoryGUIItem {
    @Getter private final SGPlayer spectator;

    public SGSpectatorHeadItem(ItemStack representationItem, SGPlayer player) {
        super(representationItem);
        this.spectator = player;
    }
}

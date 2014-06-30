package com.noyhillel.survivalgames.game.impl;

import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUIItem;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public final class SGSpectatorHeadItem extends InventoryGUIItem {
    @Getter private final GPlayer spectator;

    public SGSpectatorHeadItem(ItemStack representationItem, GPlayer player) {
        super(representationItem);
        this.spectator = player;
    }
}

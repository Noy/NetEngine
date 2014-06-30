package com.noyhillel.survivalgames.utils.inventory;

import lombok.Data;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

@Data
public class InventoryGUIItem {
    @NonNull
    private final ItemStack representationItem;

    private Integer slot;
}

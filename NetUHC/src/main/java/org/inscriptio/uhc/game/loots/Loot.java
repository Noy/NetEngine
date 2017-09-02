package org.inscriptio.uhc.game.loots;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.inscriptio.uhc.NetUHC;

import java.util.List;
import java.util.Random;

@SuppressWarnings("ALL")
@Data
final class Loot {
    private final Tier tier;
    private final Location chestLocation;

    void fillChest() {
        Block block = chestLocation.getBlock();
        block.setType(Material.CHEST);
        BlockState state = block.getState();
        if (!(state instanceof Chest)) return;
        Chest chest = (Chest)state;
        Inventory inventory = chest.getInventory();
        inventory.clear();
        Integer size = inventory.getSize();
        List<ItemStack> randomItems = getTier().getRandomItems(NetUHC.getRandom().nextInt(3)+4);
        Random random = NetUHC.getRandom();
        for (ItemStack randomItem : randomItems) {
            Integer slot;
            do {
                slot = random.nextInt(size);
            } while (inventory.getItem(slot) != null);
            inventory.setItem(slot, randomItem);
        }
    }
}
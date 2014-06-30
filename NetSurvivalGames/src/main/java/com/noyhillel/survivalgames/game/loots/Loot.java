package com.noyhillel.survivalgames.game.loots;

import com.noyhillel.survivalgames.SurvivalGames;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

@Data
public final class Loot {
    private final Tier tier;
    private final Location chestLocation;

    public void fillChest() {
        Block block = chestLocation.getBlock();
        block.setType(Material.CHEST);
        BlockState state = block.getState();
        if (!(state instanceof Chest)) return;
        Chest chest = (Chest)state;
        Inventory inventory = chest.getInventory();
        Integer size = inventory.getSize();
        List<ItemStack> randomItems = getTier().getRandomItems(SurvivalGames.getRandom().nextInt(3)+4);
        Random random = SurvivalGames.getRandom();
        for (ItemStack randomItem : randomItems) {
            Integer slot;
            do {
                slot = random.nextInt(size);
            } while (inventory.getItem(slot) != null);
            inventory.setItem(slot, randomItem);
        }
    }
}

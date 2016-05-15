package com.noyhillel.survivalgames.game.lobby;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.player.SGPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public abstract class LobbyItemDefinition {
    abstract Integer getSlot();
    abstract Material getType();
    abstract String getTitle();

    /* delegation methods */
    void rightClick(SGPlayer player, GameManager manager) {}
    void leftClick(SGPlayer player, GameManager manager) {}

    protected String[] getLore(SGPlayer player, GameManager manager) {return new String[0];}
    protected Integer getQuantity(SGPlayer player, GameManager manager) {return 1;}
    protected LobbyItemEnchantment[] getEnchantments(SGPlayer player, GameManager manager) {return null;}

    final void givePlayerItem(SGPlayer player, GameManager gameManager) {
        Integer slot = getSlot();
        if (slot < 1 || slot > 9) throw new IllegalArgumentException("The slot is out of range!");
        player.getPlayer().getInventory().setItem(slot-1, getItem(player, gameManager));
    }

    protected ItemStack getItem(SGPlayer player, GameManager gameManager) {
        ItemStack itemStack = new ItemStack(getType(), getQuantity(player, gameManager));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(getTitle());
        String[] lore = getLore(player, gameManager);
        if (lore.length != 0) itemMeta.setLore(Arrays.asList(lore));
        LobbyItemEnchantment[] enchantments = getEnchantments(player, gameManager);
        if (enchantments != null) {
            for (LobbyItemEnchantment enchantment : enchantments) {
                itemStack.addUnsafeEnchantment(enchantment.getEnchantmentType(), enchantment.getLevel());
            }
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /* Util method */
    final protected SurvivalGames getPlugin() {
        return SurvivalGames.getInstance();
    }
}

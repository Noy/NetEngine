package org.inscriptio.uhc.game.lobby;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.player.UHCPlayer;

import java.util.Arrays;

public abstract class LobbyItemDefinition {
    abstract Integer getSlot();
    abstract Material getType();
    abstract String getTitle();

    /* delegation methods */
    void rightClick(UHCPlayer player, GameManager manager) {}
    void leftClick(UHCPlayer player, GameManager manager) {}

    protected String[] getLore(UHCPlayer player, GameManager manager) {return new String[0];}
    protected Integer getQuantity(UHCPlayer player, GameManager manager) {return 1;}
    protected LobbyItemEnchantment[] getEnchantments(UHCPlayer player, GameManager manager) {return null;}

    final void givePlayerItem(UHCPlayer player, GameManager gameManager) {
        Integer slot = getSlot();
        if (slot < 1 || slot > 9) throw new IllegalArgumentException("The slot is out of range!");
        player.getPlayer().getInventory().setItem(slot-1, getItem(player, gameManager));
    }

    protected ItemStack getItem(UHCPlayer player, GameManager gameManager) {
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
    final protected NetUHC getPlugin() {
        return NetUHC.getInstance();
    }
}

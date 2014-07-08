package com.noyhillel.networkhub.items;

import com.noyhillel.networkengine.exceptions.CooldownUnexpiredException;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.NetHub;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by Noy on 29/05/2014.
 */
public final class HidePlayersItem extends NetHubItemDelegate {

    public static Set<UUID> hidingPlayers = new HashSet<>();

    public HidePlayersItem() {
        super(true);
    }

    /**
     * The Item Which the player wishes to right click, Overridden method.
     * @return The Item.
     */
    @Override
    protected ItemStack getItem() {
        ItemStack item = new ItemStack(Material.valueOf(NetHub.getInstance().getConfig().getString("hide-item.item").toUpperCase()));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageManager.getFormat("hide-item.item-name", false));
        List<String> lore = new ArrayList<>();
        lore.add(MessageManager.getFormat("hide-item.item-lore", false));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * The Item Slot you wish to have the item in.
     * @return The Item Slot
     */
    @Override
    protected Integer getItemSlot() {
        return 1;
    }

    /**
     * When the Player Right clicks, overridden method.
     * @param player The Player
     */
    @Override
    protected void onRightClick(NetPlayer player) {
        try {
            if (!player.isOp()) NetHub.getCooldown().testCooldown(player.getName(), NetHub.getInstance().getConfig().getLong("cooldown.cooldown-time"));
        } catch (CooldownUnexpiredException e) {
            player.sendMessage(MessageManager.getFormat("cooldown.cooldown-item", true, new String[]{"<time>", String.valueOf(NetHub.getInstance().getConfig().getLong("cooldown.cooldown-time"))}));
            return;
        }
        if (!hidingPlayers.contains(player.getUuid())) {
            hidingPlayers.add(player.getUuid());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("hub.staff")) {
                    player.getPlayer().hidePlayer(online);
                }
            }
            player.sendMessage(MessageManager.getFormats("hide-item.hide"));
            player.playSound(Sound.CLICK, 0.5F);
        }
        else if (hidingPlayers.contains(player.getUuid())) {
            hidingPlayers.remove(player.getUuid());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!player.getPlayer().canSee(online)) {
                    player.getPlayer().showPlayer(online);
                }
            }
            player.sendMessage(MessageManager.getFormats("hide-item.unhide"));
            player.playSound(Sound.CLICK, 1F);
        }
    }

    @Override
    protected void onLeftClick(NetPlayer player) {
        // nothing
    }
}

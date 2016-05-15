package com.noyhillel.networkhub.items;


import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.commands.SpawnCommand;
import com.noyhillel.networkhub.items.warpitem.ConfigManager;
import com.noyhillel.networkhub.items.warpitem.WarpItem;
import com.noyhillel.networkhub.listeners.ModuleListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Noy on 26/05/2014.
 */
public final class HubItemJoinListener extends ModuleListener {

    private ArrayList<NetHubItemDelegate> items;

    public HubItemJoinListener() {
        super("hub-items");
        items = new ArrayList<>();
        ConfigManager configManager = new ConfigManager();
        items.add(new WarpItem(configManager));
        items.add(new HidePlayersItem());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        player.resetPlayer();
        items.stream().filter(item -> shouldAdd(player, item.getItem())).forEach(item -> player.getPlayer().getInventory().setItem(item.getItemSlot(), item.getItem()));
        if (!HidePlayersItem.hidingPlayers.isEmpty()) {
            if (player.getPlayer().hasPermission("hub.staff")) return;
            for (UUID uuid : HidePlayersItem.hidingPlayers) {
                Player hidingPlayer = Bukkit.getPlayer(uuid);
                if (hidingPlayer != null) {
                    hidingPlayer.hidePlayer(player.getPlayer());
                } else {
                    HidePlayersItem.hidingPlayers.remove(uuid);
                }
            }
        }
        event.setJoinMessage(MessageManager.getFormat("formats.join-message", false));
        if (SpawnCommand.getLocation("spawn") == null) return;
        if (!player.isOp()) player.teleport(SpawnCommand.getLocation("spawn"));
    }

    private boolean shouldAdd(NetPlayer player, ItemStack item) {
        return !player.getPlayer().getInventory().contains(item);
    }
}
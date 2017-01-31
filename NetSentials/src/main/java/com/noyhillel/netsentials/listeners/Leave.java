package com.noyhillel.netsentials.listeners;

import com.noyhillel.netsentials.commands.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static com.noyhillel.netsentials.listeners.Death.back;

public final class Leave extends ModuleListener {

    public Leave() {
        super("leave");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        if (back.containsKey(player.getUniqueId())) {
            back.remove(player.getUniqueId());
        }
        if (Message.receivedMessage.containsKey(uniqueId)) {
            Message.receivedMessage.remove(uniqueId);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerKickEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        if (back.containsKey(player.getUniqueId())) {
            back.remove(player.getUniqueId());
        }
        if (Message.receivedMessage.containsKey(uniqueId)) {
            Message.receivedMessage.remove(uniqueId);
        }
    }
}

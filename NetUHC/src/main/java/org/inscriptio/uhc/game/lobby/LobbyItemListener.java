package org.inscriptio.uhc.game.lobby;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.player.UHCPlayer;


@Data
public class LobbyItemListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;
        ItemStack itemInHand = event.getPlayer().getItemInHand();
        if (itemInHand == null) return;
        if (!(itemInHand.hasItemMeta())) return;
        LobbyItem lobbyItem1 = resolveLobbyItem(itemInHand, event.getPlayer().getInventory().getHeldItemSlot());
        if (lobbyItem1 == null) return;
        LobbyItemDefinition lobbyItem = lobbyItem1.getLobbyItemDefinition();
        UHCPlayer uhcPlayer = resolveGPlayer(event.getPlayer());
        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                lobbyItem.leftClick(uhcPlayer, gameManager);
                break;
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                lobbyItem.rightClick(uhcPlayer, gameManager);
                break;
        }
        event.setCancelled(true);
    }

    private LobbyItem resolveLobbyItem(ItemStack stack, Integer slot) {
        for (LobbyItem lobbyItem : gameManager.getLobbyState().getItems()) {
            LobbyItemDefinition lobbyItemDefinition = lobbyItem.getLobbyItemDefinition();
            if (!lobbyItemDefinition.getSlot().equals(slot + 1)) continue;
            if (compareItemToLobbyItem(stack, lobbyItemDefinition)) return lobbyItem;
        }
        return null;
    }

    private boolean compareItemToLobbyItem(ItemStack itemStack, LobbyItemDefinition lobbyItemDefinition) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta != null && lobbyItemDefinition.getType().equals(itemStack.getType()) && lobbyItemDefinition.getTitle().equalsIgnoreCase(lobbyItemDefinition.getTitle());
    }

    private UHCPlayer resolveGPlayer(Player player) {
        return NetUHC.getInstance().getUhcPlayerManager().getOnlinePlayer(player);
    }
}
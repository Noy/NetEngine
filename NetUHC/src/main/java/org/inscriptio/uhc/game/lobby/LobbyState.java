package org.inscriptio.uhc.game.lobby;

import lombok.AccessLevel;
import lombok.Getter;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.player.UHCPlayer;

public enum LobbyState {
    SPECTATING(LobbyItem.SPECTATOR_COMPASS/*, LobbyItem.MUTATION_INTERFACE*/),
    PRE_GAME(LobbyItem.PREGAME_BOOK),
    POST_GAME();

    @Getter(AccessLevel.PACKAGE) private LobbyItem[] items;
    LobbyState(LobbyItem... items) {
        this.items = items;
    }

    public void giveItems(UHCPlayer player, GameManager manager) {
        for (LobbyItem item : items) {
            item.getLobbyItemDefinition().givePlayerItem(player, manager);
        }
    }
}
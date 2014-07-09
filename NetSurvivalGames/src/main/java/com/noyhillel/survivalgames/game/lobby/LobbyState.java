package com.noyhillel.survivalgames.game.lobby;

import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.player.GPlayer;
import lombok.AccessLevel;
import lombok.Getter;

public enum LobbyState {
    SPECTATING(LobbyItem.SPECTATOR_COMPASS/*LobbyItem.MUTATION_INTERFACE*/),
    PRE_GAME(LobbyItem.PREGAME_BOOK),
    POST_GAME();

    @Getter(AccessLevel.PACKAGE) private LobbyItem[] items;
    LobbyState(LobbyItem... items) {
        this.items = items;
    }

    public void giveItems(GPlayer player, GameManager manager) {
        for (LobbyItem item : items) {
            item.getLobbyItemDefinition().givePlayerItem(player, manager);
        }
    }
}
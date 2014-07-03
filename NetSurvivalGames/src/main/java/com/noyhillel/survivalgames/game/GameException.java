package com.noyhillel.survivalgames.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public final class GameException extends Exception {
    private final Exception cause;
    private final com.noyhillel.survivalgames.game.impl.SGGame SGGame;
    private final String message;
}

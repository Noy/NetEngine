package com.noyhillel.survivalgames.game;

import com.noyhillel.survivalgames.game.impl.SGGame;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public final class GameException extends Exception {
    private final Exception cause;
    private final SGGame sgGame;
    private final String message;
}

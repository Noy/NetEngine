package com.noyhillel.paintball.game;

import com.noyhillel.paintball.game.impl.PaintballGame;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public final class GameException extends Exception {
    private final Exception cause;
    private final PaintballGame pbGame;
    private final String message;
}

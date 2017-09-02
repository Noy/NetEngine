package org.inscriptio.uhc.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.inscriptio.uhc.game.impl.UHCGame;

@EqualsAndHashCode(callSuper = false)
@Data
public final class GameException extends Exception {
    private final Exception cause;
    private final UHCGame uhcGame;
    private final String message;
}

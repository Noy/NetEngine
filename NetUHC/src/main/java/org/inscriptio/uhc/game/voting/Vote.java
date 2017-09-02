package org.inscriptio.uhc.game.voting;

import lombok.Data;
import org.inscriptio.uhc.arena.Arena;
import org.inscriptio.uhc.player.UHCPlayer;

@Data
final class Vote {
    private final Arena arena;
    private final UHCPlayer player;
    private final Integer multiplier;
}

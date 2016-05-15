package com.noyhillel.survivalgames.game.voting;

import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.player.SGPlayer;
import lombok.Data;

@Data
final class Vote {
    private final Arena arena;
    private final SGPlayer player;
    private final Integer multiplier;
}

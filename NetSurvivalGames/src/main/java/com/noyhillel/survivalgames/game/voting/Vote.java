package com.noyhillel.survivalgames.game.voting;

import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.player.GPlayer;
import lombok.Data;

@Data
public final class Vote {
    private final Arena arena;
    private final GPlayer player;
    private final Integer multiplier;
}

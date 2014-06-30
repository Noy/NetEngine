package com.noyhillel.survivalgames.game.voting;

import com.noyhillel.survivalgames.arena.Arena;

public interface VotingSessionDisplay {
    void votingStarted();
    void votingEnded(Arena arena) throws VotingRestartException;
    void votesUpdated(Arena arena, Integer votes);
    void clockUpdated(Integer secondsRemain);
    void votingFailedStart(VotingRestartReason reason);
}

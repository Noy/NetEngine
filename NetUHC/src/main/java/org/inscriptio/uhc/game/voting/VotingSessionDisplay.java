package org.inscriptio.uhc.game.voting;

import org.inscriptio.uhc.arena.Arena;

public interface VotingSessionDisplay {
    void votingStarted();
    void votingEnded(Arena arena) throws VotingRestartException;
    void votesUpdated(Arena arena, Integer votes);
    void clockUpdated(Integer secondsRemain);
    void votingFailedStart(VotingRestartReason reason);
}

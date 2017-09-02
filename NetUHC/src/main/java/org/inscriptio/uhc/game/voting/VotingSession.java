package org.inscriptio.uhc.game.voting;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.inscriptio.uhc.arena.Arena;
import org.inscriptio.uhc.game.countdown.CountdownDelegate;
import org.inscriptio.uhc.game.countdown.GameCountdown;
import org.inscriptio.uhc.player.UHCPlayer;

import java.util.*;

@Data
public final class VotingSession {
    @Getter(AccessLevel.NONE) private final VotingSessionDisplay display;
    @Getter(AccessLevel.NONE) private final List<Arena> arenas;
    @Getter(AccessLevel.NONE) private final Integer countdownStartingLength;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private Map<String, Vote> votes = new HashMap<>();
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private GameCountdown countdown;

    @Data
    private static final class VotingSessionCountdownBridge implements CountdownDelegate {
        private final VotingSession votingSession;

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {
        }

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            votingSession.updateCountdown(secondsRemaining);
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) {
            votingSession.countdownDone();
        }
    }

    private void countdownDone() {
        try {
            display.votingEnded(getSortedArenas().get(0));
        } catch (VotingRestartException e) {
            resetTimer();
            display.votingFailedStart(e.getRestartReason());
        }
    }

    private void updateCountdown(Integer seconds) {
        this.display.clockUpdated(seconds);
    }

    public void handleVote(Arena arena, UHCPlayer player) {
        removeVote(player);
        this.votes.put(player.getUsername(), new Vote(arena, player, 1));
        this.display.votesUpdated(arena, getVotesFor(arena));
    }

    public void removeVote(UHCPlayer player) {
        Vote vote = this.votes.get(player.getUsername());
        if (vote != null) {
            this.votes.remove(player.getUsername());
            this.display.votesUpdated(vote.getArena(), getVotesFor(vote.getArena()));
        }
    }

    public List<Arena> getSortedArenas() {
        ArrayList<Arena> arenas1 = new ArrayList<>(this.arenas);
        Collections.sort(arenas1, (o1, o2) -> {
            //positive o2,o1
            //negative o1,o2
            return getVotesFor(o2)- getVotesFor(o1);

        });
        return arenas1;
    }

    public void start() {
        resetTimer();
    }

    public void resetTimer() {
        this.countdown = new GameCountdown(new VotingSessionCountdownBridge(this), countdownStartingLength);
        this.countdown.start();
        display.votingStarted();
    }

    public Integer getVotesFor(Arena arena) {
        Integer count = 0;
        for (Vote vote : votes.values()) {
            if (!vote.getArena().equals(arena)) continue;
            count += vote.getMultiplier();
        }
        return count;
    }
}

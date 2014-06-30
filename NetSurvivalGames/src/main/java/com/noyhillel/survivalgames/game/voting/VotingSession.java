package com.noyhillel.survivalgames.game.voting;

import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.game.countdown.CountdownDelegate;
import com.noyhillel.survivalgames.game.countdown.GameCountdown;
import com.noyhillel.survivalgames.player.GPlayer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

    public void handleVote(Arena arena, GPlayer player) {
        removeVote(player);
        this.votes.put(player.getUsername(), new Vote(arena, player, 1));
        this.display.votesUpdated(arena, getVotesFor(arena));
    }

    public void removeVote(GPlayer player) {
        Vote vote = this.votes.get(player.getUsername());
        if (vote != null) {
            this.votes.remove(player.getUsername());
            this.display.votesUpdated(vote.getArena(), getVotesFor(vote.getArena()));
        }
    }

    public List<Arena> getSortedArenas() {
        ArrayList<Arena> arenas1 = new ArrayList<>(this.arenas);
        Collections.sort(arenas1, new Comparator<Arena>() {
            @Override
            public int compare(Arena o1, Arena o2) {
                //positive o1,o2
                //negative o2,o1
                return getVotesFor(o1)- getVotesFor(o2);

            }
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

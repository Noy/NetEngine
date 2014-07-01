package com.noyhillel.survivalgames.game;

import com.noyhillel.networkengine.util.RandomUtils;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.arena.ArenaException;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.game.lobby.GameLobby;
import com.noyhillel.survivalgames.game.lobby.LobbyItemListener;
import com.noyhillel.survivalgames.game.lobby.LobbyState;
import com.noyhillel.survivalgames.game.voting.VotingRestartException;
import com.noyhillel.survivalgames.game.voting.VotingRestartReason;
import com.noyhillel.survivalgames.game.voting.VotingSession;
import com.noyhillel.survivalgames.game.voting.VotingSessionDisplay;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.player.PlayerNotFoundException;
import com.noyhillel.survivalgames.player.StorageError;
import com.noyhillel.survivalgames.utils.MessageManager;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GameManager implements VotingSessionDisplay {
    @Getter private SGGame runningSGGame = null;

    @Getter private GameLobby lobby;
    @Getter private List<Arena> allAreans;

    private SurvivalGames plugin;
    @Getter(AccessLevel.PACKAGE) private VotingSession votingSession;

    @Getter private Integer maxPlayers;
    @Getter private Integer minPlayers;

    @Getter private LobbyState lobbyState = LobbyState.PRE_GAME;

    /* constants */
    private static final Integer[] BROADCAST_TIMES = new Integer[] {60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

    public GameManager() throws GameException {
        plugin = SurvivalGames.getInstance();

        //Setup the game lobby and arena list
        try {
            plugin.getArenaManager().reloadArenas();
        } catch (ArenaException e) {
            throw new GameException(e, null, "Could not load arenas!");
        }
        //noinspection unchecked
        this.allAreans = plugin.getArenaManager().getArenas();
        this.lobby = plugin.getArenaManager().getGameLobby();

        try {
            this.lobby.loadWorld();
        } catch (ArenaException e) {
            throw new GameException(e, null, "Could not load game lobby!");
        }

        //Setup voting session
        List<Arena> arenasToVote = new ArrayList<>();
        while (arenasToVote.size() < Math.min(5, allAreans.size())) {
            Arena arenaToAdd;
            do {
                arenaToAdd = allAreans.get(SurvivalGames.getRandom().nextInt(allAreans.size()));
            } while (arenasToVote.contains(arenaToAdd));
            arenasToVote.add(arenaToAdd);
        }
        this.votingSession = new VotingSession(this, arenasToVote, plugin.getConfig().getInt("voting-length", 30));

        //Get data
        this.maxPlayers = plugin.getConfig().getInt("maximum-players", 24);
        this.minPlayers = plugin.getConfig().getInt("minimum-players", 6);

        plugin.registerListener(new GameManagerListener(plugin, this));
        plugin.registerListener(new LobbyItemListener(this));

        //Start
        this.votingSession.start();
    }

    public void startGame(Arena arena) throws GameStartException {
        //Creates a list of GPlayers from the players
        Set<GPlayer> players = getPlayers();

        //Loads the arena
        try {
            arena.loadWorld();
        } catch (ArenaException e) {
            e.printStackTrace();
            throw new GameStartException("The arena failed to load!");
        }

        //Starts the game
        this.runningSGGame = new SGGame(arena, players, this, plugin);
        try {
            this.runningSGGame.start();
        } catch (GameException e) {
            e.printStackTrace();
            throw new GameStartException("Could not start game due to unknown error!");
        }
        this.lobbyState = LobbyState.SPECTATING;
    }

    public void gameEnded() {
        this.lobbyState = LobbyState.POST_GAME;
        updateItemStates();
        Set<GPlayer> players = getPlayers();
        for (GPlayer player : players) {
            player.resetPlayer();
        }
        final GPlayer victor = this.runningSGGame.getVictor();
        victor.setWins(victor.getWins() + 1);
        if (victor.getPlayer().hasPermission("survivalgames.extra-mutation")) {
            victor.setMutationCredits(victor.getMutationCredits() + 2);
            return;
        }
        victor.setMutationCredits(victor.getMutationCredits() + 1);
        if (victor.getPlayer().hasPermission("survivalgames.double-points")) {
            victor.setPoints(victor.getPoints() + 200);
            return;
        }
        victor.setPoints(victor.getPoints() + 100);
        int shutdownCountdownLength = SurvivalGames.getInstance().getConfig().getInt("countdowns.server-shutdown");
        broadcast(MessageManager.getFormat("formats.shutdown", new String[]{"<seconds>", String.valueOf(shutdownCountdownLength)}));
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                String format = MessageManager.getFormat("formats.kick-game-over", false, new String[]{"<victor>", victor.getDisplayableName()});
                for (GPlayer gPlayer : getPlayers()) {
                    try {
                        gPlayer.save();
                    } catch (StorageError | PlayerNotFoundException storageError) {
                        storageError.printStackTrace();
                        gPlayer.sendMessage(ChatColor.RED + "Unable to save your player data!");
                    }
                    gPlayer.getPlayer().kickPlayer(format);
                }
                try {
                    runningSGGame.getArena().unloadWorld();
                } catch (ArenaException e) {
                    e.printStackTrace();
                }
                Bukkit.shutdown();
            }
        }, shutdownCountdownLength*20L);
    }

    public boolean isPlayingGame() {
        return this.runningSGGame != null;
    }

    Set<GPlayer> getPlayers() {
        Set<GPlayer> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(plugin.getPlayerManager().getOnlinePlayer(player));
        }
        return players;
    }

    @Override
    public void votingStarted() {
        for (GPlayer gPlayer : getPlayers()) {
            setupScoreboard(gPlayer);
        }
    }

    @Override
    public void votingEnded(Arena arena) throws VotingRestartException {
        Set<GPlayer> players = getPlayers();
        if (players.size() > this.maxPlayers) throw new VotingRestartException(VotingRestartReason.MANY_PLAYERS);
        if (players.size() < this.minPlayers) throw new VotingRestartException(VotingRestartReason.FEW_PLAYERS);
        if (arena.getCornicopiaSpawns().getPoints().size() < players.size()) throw new VotingRestartException(VotingRestartReason.INVALID_ARENA);
        try {
            startGame(arena);
        } catch (GameStartException e) {
            e.printStackTrace();
            throw new VotingRestartException(VotingRestartReason.FAILURE);
        }
    }

    @Override
    public void votesUpdated(Arena arena, Integer votes) {
        String name = arena.getMeta().getName();
        for (GPlayer gPlayer : getPlayers()) {
            gPlayer.setScoreboardSide(name, votes);
        }

    }

    @Override
    public void clockUpdated(Integer secondsRemain) {
        if (RandomUtils.contains(secondsRemain, BROADCAST_TIMES)) {
            broadcast(MessageManager.getFormat("formats.time-remaining-lobby", new String[]{"<time>", String.valueOf(secondsRemain)}));
            broadcastSound(Sound.CLICK);
        }
    }

    @Override
    public void votingFailedStart(VotingRestartReason reason) {
        switch (reason) {
            case FAILURE:
                broadcast(ChatColor.RED + "Could not start game! Reason: The game has failed to start, restarting countdown!!");
                break;
            case FEW_PLAYERS:
                broadcast(ChatColor.RED + "Could not start game! Reason: There are not enough players to start the game, restarting countdown!");
                break;
            case MANY_PLAYERS:
                broadcast(ChatColor.RED + "Could not start game! Reason: There are too many players to start the game, restarting countdown!");
                break;
            case INVALID_ARENA:
                broadcast(ChatColor.RED + "Could not start game! Reason: The specific Arena is invalid!");
                break;
            default:
                break;
        }
    }

    public void voteFor(Player player, Arena arena) {
        this.votingSession.handleVote(arena, plugin.getPlayerManager().getOnlinePlayer(player));
    }

    public void setupScoreboard(GPlayer player) {
        player.setScoreboardTitle(MessageManager.getFormat("formats.voting-scoreboard-title"));
        for (Arena arena : votingSession.getSortedArenas()) {
            player.setScoreboardSide(arena.getMeta().getName(), votingSession.getVotesFor(arena));
        }
    }

    void broadcast(String message) {
        for (GPlayer gPlayer : getPlayers()) {
            gPlayer.sendMessage(message);
        }
    }

    void broadcastSound(Sound sound) {
        for (GPlayer p : getPlayers()) {
            p.playSound(sound);
        }
    }

    /* Game listener methods */
    void playerJoined(GPlayer player) {
        player.resetPlayer();
        player.teleport(lobby.getSpawnPoints().next().toLocation(lobby.getLoadedWorld()));
        if (isPlayingGame()) this.runningSGGame.makePlayerSpectator(player);
        else setupScoreboard(player);
        this.lobbyState.giveItems(player, this);
    }

    void updateItemStates() {
        switch (this.lobbyState) {
            case PRE_GAME:
            case POST_GAME:
                for (GPlayer gPlayer : this.getPlayers()) {
                    gPlayer.resetPlayer();
                    this.lobbyState.giveItems(gPlayer, this);
                }
                break;
        }
    }

    void spawnPlayer(Player player) {
        player.teleport(getSpawnPoint());
    }

    private Location getSpawnPoint() {
        return lobby.getSpawnPoints().random().toLocation(lobby.getLoadedWorld());
    }

    void playerLeft(GPlayer player) {
        try {
            player.save();
        } catch (StorageError | PlayerNotFoundException storageError) {
            storageError.printStackTrace();
        }
        if (isPlayingGame()) {
            getRunningSGGame().playerLeftServer(player);
        }
        else {
            this.votingSession.removeVote(player);
        }
    }

    public void disable() throws ArenaException {
        lobby.unloadWorld();
    }
}

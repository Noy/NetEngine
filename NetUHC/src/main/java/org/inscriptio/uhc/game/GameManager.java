package org.inscriptio.uhc.game;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.game.arena.lobby.GameLobby;
import com.noyhillel.networkengine.util.effects.NetEnderBar;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.RandomUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.arena.Arena;
import org.inscriptio.uhc.game.impl.UHCGame;
import org.inscriptio.uhc.game.lobby.LobbyItemListener;
import org.inscriptio.uhc.game.lobby.LobbyState;
import org.inscriptio.uhc.game.voting.VotingRestartException;
import org.inscriptio.uhc.game.voting.VotingRestartReason;
import org.inscriptio.uhc.game.voting.VotingSession;
import org.inscriptio.uhc.game.voting.VotingSessionDisplay;
import org.inscriptio.uhc.player.PlayerNotFoundException;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.player.StorageError;
import org.inscriptio.uhc.utils.MessageManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GameManager implements VotingSessionDisplay {
    @Getter private UHCGame runningUHCGame = null;

    @Getter private GameLobby lobby;
    @Getter private List<Arena> allAreans;

    private NetUHC plugin;
    @Getter private VotingSession votingSession;

    @Getter private Integer maxPlayers;
    @Getter private Integer minPlayers;

    @Getter private LobbyState lobbyState = LobbyState.PRE_GAME;

    /* constants */
    private static final Integer[] BROADCAST_TIMES = {60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

    public GameManager() throws GameException {
        plugin = NetUHC.getInstance();

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
                arenaToAdd = allAreans.get(NetUHC.getRandom().nextInt(allAreans.size()));
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
        //Creates a list of SGPlayers from the players
        Set<UHCPlayer> players = getPlayers();

        //Loads the arena
        try {
            arena.loadWorld();
        } catch (ArenaException e) {
            e.printStackTrace();
            throw new GameStartException("The arena failed to load!");
        }

        //Starts the game
        this.runningUHCGame = new UHCGame(arena, players, this, plugin);
        try {
            this.runningUHCGame.start();
        } catch (GameException e) {
            e.printStackTrace();
            throw new GameStartException("Could not start game due to unknown error!");
        }
        this.lobbyState = LobbyState.SPECTATING;
    }

    public void gameEnded() {
        this.lobbyState = LobbyState.POST_GAME;
        updateItemStates();
        final UHCPlayer victor = this.runningUHCGame.getVictor();
        victor.setPoints(victor.getPoints() + 100);
        int shutdownCountdownLength = NetUHC.getInstance().getConfig().getInt("countdowns.server-shutdown", 12);
        broadcast(MessageManager.getFormat("formats.shutdown", new String[]{"<seconds>", String.valueOf(shutdownCountdownLength)}));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String format = MessageManager.getFormat("formats.kick-game-over", false, new String[]{"<victor>", victor.getDisplayableName()});
            for (UHCPlayer uhcPlayer : getPlayers()) {
                try {
                    uhcPlayer.save();
                } catch (StorageError | PlayerNotFoundException storageError) {
                    storageError.printStackTrace();
                    uhcPlayer.sendMessage(ChatColor.RED + "Unable to save your player data!");
                }
                uhcPlayer.sendMessage(format);
                //SurvivalGames.getInstance().sendToServer("hub", UHCPlayer.getPlayer());
            }
            try {
                runningUHCGame.getArena().unloadWorld();
                NetUHC.logInfo("Unloaded world " + runningUHCGame.getArena().getLoadedWorld());
            } catch (ArenaException e) {
                e.printStackTrace();
            }
            Bukkit.shutdown();
        }, shutdownCountdownLength*60L);
    }

    public boolean isPlayingGame() {
        return this.runningUHCGame != null;
    }

    public Set<UHCPlayer> getPlayers() {
        Set<UHCPlayer> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(plugin.getUhcPlayerManager().getOnlinePlayer(player));
        }
        return players;
    }

    @Override
    public void votingStarted() {
//        for (UHCPlayer sgPlayer : getPlayers()) {
//            setupScoreboard(UHCPlayer);
//        }
        // Java 8
        getPlayers().forEach(this::setupScoreboard);
    }

    @Override
    public void votingEnded(Arena arena) throws VotingRestartException {
        Set<UHCPlayer> players = getPlayers();
        if (players.size() > this.maxPlayers) throw new VotingRestartException(VotingRestartReason.MANY_PLAYERS);
        if (players.size() < this.minPlayers) throw new VotingRestartException(VotingRestartReason.FEW_PLAYERS);
        if (arena.getRandomPointSpawns().getPoints().size() < players.size()) throw new VotingRestartException(VotingRestartReason.INVALID_ARENA);
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
        for (UHCPlayer uhcPlayer : getPlayers()) {
            uhcPlayer.setScoreBoardSide(ChatColor.GOLD + name, votes);
        }
    }

    @Override
    public void clockUpdated(Integer secondsRemain) {
        if (RandomUtils.contains(secondsRemain, BROADCAST_TIMES)) {
            broadcast(MessageManager.getFormat("formats.time-remaining-lobby", true, new String[]{"<time>", secondsRemain.toString()}));
            broadcastSound(Sound.UI_BUTTON_CLICK);
        }
        if (secondsRemain <= 60 && secondsRemain >= 1) {
            for (UHCPlayer uhcPlayer : getPlayers()) {
                NetPlayer playerFromNetPlayer = uhcPlayer.getPlayerFromNetPlayer();
                playerFromNetPlayer.setExperience(secondsRemain.floatValue()/60);
                playerFromNetPlayer.getPlayer().setLevel(secondsRemain);
                NetEnderBar.setTextFor(playerFromNetPlayer, MessageManager.getFormat("enderbar.lobby-time", false, new String[]{"<player>", uhcPlayer.getDisplayableName()}));
                NetEnderBar.setHealthPercent(playerFromNetPlayer, secondsRemain.doubleValue()/60);
            }
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
        this.votingSession.handleVote(arena, plugin.getUhcPlayerManager().getOnlinePlayer(player));
    }

    public void setupScoreboard(UHCPlayer player) {
        player.setScoreboardSideTitle(MessageManager.getFormat("formats.voting-scoreboard-title"));
        for (Arena arena : votingSession.getSortedArenas()) {
            player.setScoreBoardSide(ChatColor.GOLD + arena.getMeta().getName(), votingSession.getVotesFor(arena));
        }
    }

    private void broadcast(String message) {
        for (UHCPlayer uhcPlayer : getPlayers()) {
            uhcPlayer.sendMessage(message);
        }
        NetUHC.getInstance().getServer().getConsoleSender().sendMessage(message);
    }

    void broadcastSound(Sound sound) {
        for (UHCPlayer p : getPlayers()) {
            p.playSound(sound);
        }
    }

    /* Game listener methods */
    void playerJoined(UHCPlayer player) {
        if (player == null) return;
        player.resetPlayer();
        player.teleport(lobby.getSpawnPoints().next().toLocation(lobby.getLoadedWorld()));
        setupScoreboard(player);
        this.lobbyState.giveItems(player, this);
    }

    void updateItemStates() {
        switch (this.lobbyState) {
            case PRE_GAME:
            case POST_GAME:
                for (UHCPlayer uhcPlayer : this.getPlayers()) {
                    uhcPlayer.resetPlayer();
                    this.lobbyState.giveItems(uhcPlayer, this);
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

    void playerLeft(UHCPlayer player) {
        try {
            player.save();
        } catch (StorageError | PlayerNotFoundException storageError) {
            storageError.printStackTrace();
        }
        if (isPlayingGame()) {
            this.getRunningUHCGame().playerLeftServer(player);
        }
        else {
            this.votingSession.removeVote(player);
        }
    }

    public void disable() throws ArenaException {
        lobby.unloadWorld();
    }
}

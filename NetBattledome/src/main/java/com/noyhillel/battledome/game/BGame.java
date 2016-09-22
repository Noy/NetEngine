package com.noyhillel.battledome.game;

import com.noyhillel.battledome.MessageManager;
import com.noyhillel.battledome.NetBD;
import com.noyhillel.battledome.arena.Point;
import com.noyhillel.networkengine.util.effects.NetEnderBar;
import com.noyhillel.networkengine.util.effects.NetFireworkEffect;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.RandomUtils;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Noy on 6/23/2014.
 */
public final class BGame implements Listener, GameCountdownHandler {

    private final Set<NetPlayer> playerSet;
    private final Set<NetPlayer> spectators = new HashSet<>();
    private final Set<NetPlayer> pendingSpectators = new HashSet<>();
    @Getter private final Point centerPoint;
    private final World world;
    @Getter private Phase phase;
    private Map<NetPlayer, Team> teams;

    @Getter private Map<Team, Location> obsidianLocations = new HashMap<>();
    @Getter private Map<Team, NetPlayer> obsidianHolders = new HashMap<>();

    private GameCountdown gameCountdown;

    BGame(Collection<? extends Player> players, Point centerPoint, World world) {
        this.playerSet = new HashSet<>();
        this.centerPoint = centerPoint;
        this.world = world;
        this.phase = Phase.BUILD;
        for (Player player : players) {
            this.playerSet.add(NetPlayer.getPlayerFromPlayer(player));
        }
    }

    private boolean isSpectating(NetPlayer player) {
        return this.spectators.contains(player);
    }

    void startGame() {
        if (playerSet.size() <= 1) {
            broadcastMessage(ChatColor.RED + "Cannot start game with only one player!");
            return;
        }
        NetBD.getInstance().registerListener(this);
        randomisePlayersIntoTeams();
        for (NetPlayer player : playerSet) {
            playerStartsGame(player);
            player.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
        for (Team team : Team.values()) {
            giveTeamObsiRandomly(team);
        }
        this.centerPoint.toLocation(world).getBlock().getRelative(BlockFace.DOWN).setType(Material.ENCHANTMENT_TABLE);
        broadcastMessage(MessageManager.getFormat("formats.start-game", true));
        performPhaseUpdate();
    }

    public Team getTeamForPlayer(NetPlayer player) {
        return this.teams.get(player);
    }

    private List<NetPlayer> getPlayersForTeam(Team team) {
        List<NetPlayer> playerList = new ArrayList<>();
        for (Map.Entry<NetPlayer, Team> netPlayerTeamEntry : teams.entrySet()) {
            if (netPlayerTeamEntry.getValue().equals(team)) playerList.add(netPlayerTeamEntry.getKey());
        }
        return playerList;
    }

    private void broadcastMessage(String... messages) {
        for (String message : messages) {
            for (NetPlayer player : playerSet) {
                player.sendMessage(message.replaceAll("<team>", getTeamForPlayer(player).toString()));
            }
        }
    }

    private void broadcastMessage(Team team, String... messages) {
        List<NetPlayer> playersForTeam = getPlayersForTeam(team);
        for (String message : messages) {
            for (NetPlayer player : playersForTeam) {
                player.sendMessage(message.replaceAll("<team>", team.toString()));
            }
        }
    }

    private void broadcastSound(Sound sound, Float pitch) {
        for (NetPlayer player : playerSet) {
            player.playSound(sound, pitch);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private boolean isCrossingOutsideBounds(NetPlayer player, Location goingTo) {
        return (goingTo.distance(centerPoint.toLocation(world)) >= 200);
    }

    private void performPhaseUpdate() {
        if (this.phase.getLength() == -1) this.gameCountdown = null;
        else (this.gameCountdown = new GameCountdown(phase.getLength(), this, NetBD.getInstance())).start();
        broadcastSound(Sound.ENTITY_WITHER_DEATH, 0.5F);
        broadcastMessage(MessageManager.getFormat("formats.phase-start", true, new String[]{"<phase>", this.phase.getName()}));
        updateInterfaces();
    }

    private void updateInterfaces() {
        if (this.teams == null) return;
        if (playerSet == null) return;
        String enderBarText = MessageManager.getFormat("formats.ender-bar-ingame", false, new String[]{"<phase>", this.phase.getName()});
        if (this.gameCountdown != null) enderBarText = enderBarText + MessageManager.getFormat("formats.ender-bar-time-ingame", false, new String[]{"<time>", RandomUtils.formatTime(this.gameCountdown.getSeconds() - this.gameCountdown.getPassed())});
        Float percentEnderBar = this.gameCountdown == null ? 1 : ((float)this.gameCountdown.getSeconds()-(float)this.gameCountdown.getPassed())/(float)this.gameCountdown.getSeconds();
        for (NetPlayer player : playerSet) {
            NetEnderBar.setTextFor(player, enderBarText.replaceAll("<team>", getTeamForPlayer(player).toString()));
            NetEnderBar.setHealthPercent(player, (double)percentEnderBar);
        }
    }

    private void checkForWins() {
        List<Team> teamsRemaining = new ArrayList<>();
        Collections.addAll(teamsRemaining, Team.values());
        for (Team team : Team.values()) {
            if (getPlayersForTeam(team).size() == 0) teamsRemaining.remove(team);
        }
        if (teamsRemaining.size() == 1) teamWon(teamsRemaining.get(0));
        else if (teamsRemaining.size() > 1) return;
        willFinishGame();
    }

    public String getStatusString() {
        StringBuilder builder = new StringBuilder();
        for (Team team : Team.values()) {
            builder.append(team.toString()).append(ChatColor.DARK_AQUA).append(": ").append(ChatColor.GREEN).append(getPlayersForTeam(team).size()).append(" ");
        }
        return builder.toString().trim();
    }

    private void randomisePlayersIntoTeams() {
        this.teams = new HashMap<>();
        ArrayList<NetPlayer> netPlayers = new ArrayList<>(playerSet);
        Collections.shuffle(netPlayers);
        for (NetPlayer player : netPlayers) {
            teams.put(player, getLeastFilledTeamCurrently());
        }
    }

    private Team getLeastFilledTeamCurrently() {
        Team t = null;
        for (Team team : Team.values()) {
            if (t == null || getPlayersForTeam(team).size() < getPlayersForTeam(t).size()) t = team;
        }
        return t;
    }

    private void giveTeamObsiRandomly(Team team) {
        List<NetPlayer> playersForTeam = getPlayersForTeam(team);
        NetPlayer player = playersForTeam.get(NetBD.getRandom().nextInt(playersForTeam.size()));
        player.giveItem(Material.OBSIDIAN, 1, (short)0, MessageManager.getFormat("formats.team-obsidian", false, new String[]{"<team>", team.toString()}));
        player.sendMessage(MessageManager.getFormat("formats.holding-team-obsidian"));
        this.obsidianHolders.put(team, player);
    }

    private Phase getNextPhase() {
        Phase[] values = Phase.values();
        Integer indexOfNext = getIndexOf(this.phase, values)+1;
        if (indexOfNext >= values.length) return null;
        return values[indexOfNext];
    }

    private void changeToNextPhase() {
        this.phase = getNextPhase();
        performPhaseUpdate();
    }

    private void playerLeft(NetPlayer player) {
        this.playerSet.remove(player);
        this.spectators.remove(player);
        this.teams.remove(player);
        for (Map.Entry<Team, NetPlayer> teamNetPlayerEntry : this.obsidianHolders.entrySet()) {
            if (teamNetPlayerEntry.getValue().equals(player)) {
                giveTeamObsiRandomly(teamNetPlayerEntry.getKey());
                break;
            }
        }
        updateInterfaces();
        checkForWins();
    }

    private void playerStartsGame(NetPlayer player) {
        player.resetPlayer();
        player.teleport(centerPoint.toLocation(world));
        player.getPlayer().setPlayerListName(getTeamForPlayer(player).getColor() + player.getName());
        NetFireworkEffect.shootFireWorks(player, player.getPlayer().getLocation());
    }

    private void teamWon(Team team) {
        broadcastMessage(team, MessageManager.getFormat("formats.game-won"));
        broadcastSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F);
    }

    void placeObsidian(Team team, Location location) {
        this.obsidianLocations.put(team, location);
        this.obsidianHolders.remove(team);
        broadcastMessage(team, MessageManager.getFormat("formats.obsidian-placed"));
    }

    private static <T> Integer getIndexOf(T t, T[] ts) {
        for (Integer x = 0; x < ts.length; x++) {
            if (ts[x].equals(t)) return x;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    private void makePlayerSpectator(NetPlayer player) {
        if (this.spectators.contains(player)) throw new IllegalStateException("You cannot make a spectator twice.");
        hideFromAllPlayers(player);
        this.spectators.add(player);
    }

    private void hideFromAllPlayers(NetPlayer player) {
        for (NetPlayer allPlayers : getAllPlayers()) {
            allPlayers.getPlayer().hidePlayer(player.getPlayer());
        }
    }

    private Set<NetPlayer> getAllPlayers() {
        Set<NetPlayer> players = new HashSet<>();
        for (NetPlayer spectator : spectators) {
            players.add(spectator);
        }
        for (NetPlayer player : playerSet) {
            players.add(player);
        }
        for (NetPlayer pendingSpecs : pendingSpectators) {
            players.add(pendingSpecs);
        }
        return players;
    }

    private void endGameForPlayerForce(NetPlayer player) {
        player.getPlayer().kickPlayer(MessageManager.getFormat("formats.battledome-over", false));
    }

    /* Events */

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.OBSIDIAN)) {
            NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
            if (isSpectating(player)) event.setCancelled(true);
            Team team = getTeamForPlayer(player);
            if (!obsidianHolders.get(team).equals(player)) {
                event.setCancelled(true);
                player.sendMessage(MessageManager.getFormat("formats.no-place-obsidian"));
            } else {
                player.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F);
                placeObsidian(team, player.getPlayer().getLocation());
            }
        }
    }

    private void willFinishGame() {
        if (playerSet.size() <= 1) {
            broadcastMessage(ChatColor.RED + "Game has ended! The server will shut down in 5 seconds!");
            Bukkit.getScheduler().runTaskLater(NetBD.getInstance(), () -> {
                for (NetPlayer player : playerSet) {
                    Team team = getTeamForPlayer(player);
                    broadcastMessage(MessageManager.getFormat("formats.team-won", new String[]{"<team>", team.toString()}));
                }
                broadcastMessage(MessageManager.getFormat("formats.game-ending"));
                Bukkit.getScheduler().runTaskLater(NetBD.getInstance(), Bukkit::shutdown, 200);
                broadcastSound(Sound.ENTITY_PLAYER_LEVELUP, 1F);
            }, 100L);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (isSpectating(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (isSpectating(player)) event.setCancelled(true);
        if (event.getBlock().getType().equals(Material.ENCHANTMENT_TABLE)) event.setCancelled(true);
        if (event.getBlock().getType().equals(Material.GLASS)) event.setCancelled(true);
        ItemStack obs = new ItemStack(Material.OBSIDIAN);
        if (event.getBlock().getType().equals(obs.getType())) {
            if (!(obs.hasItemMeta())) return;
            if (phase == Phase.BUILD) {
                if (!this.phase.getGameListenerDelegate().canBreakObsi()) {
                    event.setCancelled(true);
                    player.sendMessage(MessageManager.getFormat("formats.cannot-break-now"));
                }
            }
            if (phase == Phase.BATTLE) {
                Bukkit.getScheduler().runTaskLater(NetBD.getInstance(), () -> {
                    for (NetPlayer allPlayers : getAllPlayers()) {
                        allPlayers.kick(MessageManager.getFormat("formats.battledome-end"));
                    }
                }, 200L);
                broadcastMessage(MessageManager.getFormat("formats.team-eliminated", true,
                        new String[]{"<player>", player.getName()}));
                broadcastSound(Sound.ENTITY_ENDERDRAGON_GROWL, 1F);
                Team losingTeam = null;
                for (Map.Entry<Team, Location> teamLocationEntry : this.obsidianLocations.entrySet()) {
                    if (teamLocationEntry.getValue().equals(event.getBlock().getLocation())) {
                        losingTeam = teamLocationEntry.getKey();
                        break;
                    }
                }
                if (losingTeam == null) return;
                getPlayersForTeam(losingTeam).forEach(this::endGameForPlayerForce);
            }
        }
    }

    @SuppressWarnings({"ConstantConditions", "Duplicates"})
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getEntity());
        for (int x = 0; x <= 20; x++) {
            if (!player.getPlayer().getName().equalsIgnoreCase("NoyHillel1")) continue;
            player.playSound(Sound.ENTITY_COW_HURT);
            player.sendMessage(ChatColor.RED + "ha u bad, ha you dead");
        }
        EntityDamageEvent lastDamageCause = event.getEntity().getLastDamageCause();
        EntityDamageEvent.DamageCause cause = lastDamageCause.getCause();
        switch (cause) {
            case CONTACT:
                break;
            case ENTITY_ATTACK:
                break;
            case PROJECTILE:
                break;
            case SUFFOCATION:
                break;
            case FALL:
                break;
            case FIRE:
                break;
            case FIRE_TICK:
                break;
            case MELTING:
                break;
            case LAVA:
                break;
            case DROWNING:
                break;
            case BLOCK_EXPLOSION:
                break;
            case ENTITY_EXPLOSION:
                break;
            case VOID:
                break;
            case LIGHTNING:
                break;
            case SUICIDE:
                break;
            case STARVATION:
                break;
            case POISON:
                break;
            case MAGIC:
                break;
            case WITHER:
                break;
            case FALLING_BLOCK:
                break;
            case THORNS:
                break;
            case CUSTOM:
                break;
        }
        StringBuilder sb = new StringBuilder();
        for (String s1 : Arrays.asList(cause.toString())) {
            char[] chars = s1.toLowerCase().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                sb.append(i == 0 ? Character.toUpperCase(chars[i]) : chars[i]);
            }
            sb.append(" ");
        }
        if (event.getEntity().getKiller() == null) {
            event.setDeathMessage(MessageManager.getFormat("formats.player-fallen", true, new String[]{"<killer>", event.getEntity().getKiller() == null ? sb.toString().trim() : event.getEntity().getKiller().getPlayerListName()}, new String[]{"<fallen>", player.getDisplayName()}));
            tributeFallen(player);
            pendingSpectators.add(NetPlayer.getPlayerFromPlayer(event.getEntity()));
            return;
        }
        checkForWins();
        NetPlayer killer = NetPlayer.getPlayerFromPlayer(event.getEntity().getKiller());
        event.setDeathMessage(MessageManager.getFormat("formats.player-fallen", true, new String[]{"<killer>", killer.getDisplayName() == null ? "The Environment" : killer.getDisplayName()}, new String[]{"<fallen>", player.getDisplayName()}));
        tributeFallen(player);
        pendingSpectators.add(NetPlayer.getPlayerFromPlayer(event.getEntity()));
    }

    private void tributeFallen(NetPlayer player) {
        if (phase != Phase.BATTLE) return;
        if (!playerSet.contains(player)) return;
        playerSet.remove(player);
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) world.strikeLightningEffect(bukkitPlayer.getLocation());
        updateInterfaces();
        if (getAllPlayers().size() <= 1) {
            willFinishGame();
        }
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!pendingSpectators.contains(netPlayer)) return;
        pendingSpectators.remove(netPlayer);
        if (!phase.getGameListenerDelegate().makeSpectatorOnDeath()) {
            Bukkit.getScheduler().runTaskLater(NetBD.getInstance(), () -> event.setRespawnLocation(centerPoint.toLocation(world)), 1L);
        }
        else {
            Bukkit.getScheduler().runTask(NetBD.getInstance(), () -> makePlayerSpectator(netPlayer));
            event.setRespawnLocation(centerPoint.toLocation(world));
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        NetPlayer playerFromPlayer = NetPlayer.getPlayerFromPlayer((Player) event.getEntity());
        if (isSpectating(playerFromPlayer)) event.setCancelled(true);
        if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player)) return;
        if (!phase.getGameListenerDelegate().canPvp()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage2(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        NetPlayer playerFromPlayer = NetPlayer.getPlayerFromPlayer((Player) event.getEntity());
        if (playerFromPlayer == null) return;
        if (isSpectating(playerFromPlayer))event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (isCrossingOutsideBounds(player, event.getTo())) {
            player.damage(1);
            player.strikeLightning(player.getLocation());
            player.sendMessage(MessageManager.getFormat("formats.do-not-cross"));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage(MessageManager.getFormat("formats.quit-message", true, new String[]{"<name>", event.getPlayer().getName()}));
        playerLeft(NetPlayer.getPlayerFromPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        NetPlayer playerFromPlayer = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (phase.getGameListenerDelegate().joinAsSpectator()) {
            makePlayerSpectator(playerFromPlayer);
        }
        event.setJoinMessage(MessageManager.getFormat("formats.join-message", true, new String[]{"<name>", event.getPlayer().getName()}));
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        playerLeft(NetPlayer.getPlayerFromPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!isSpectating(player)) {
            event.setFormat(getTeamForPlayer(player).getColor() + player.getName() + ChatColor.WHITE + ": " + event.getMessage());
            return;
        }
        if (isSpectating(player)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageManager.getFormat("formats.spectate-chat"));
        }
    }

    /* Delegated methods */
    @Override
    public void onCountdownStart(Integer max, GameCountdown countdown) {}

    @Override
    public void onCountdownChange(Integer seconds, Integer max, GameCountdown countdown) {
        if (seconds <= 10 && seconds >= 6) {
            for (NetPlayer player : playerSet) {
                player.sendMessage(MessageManager.getFormat("formats.seconds-left", true, new String[]{"<seconds>", seconds.toString()}));
                player.playSound(Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1F);
            }
        }
        if (seconds <= 5) {
            for (NetPlayer player : playerSet) {
                player.sendMessage(MessageManager.getFormat("formats.seconds-left", true, new String[]{"<seconds>", seconds.toString()}));
                player.playSound(Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 0.5F);
            }
        }
        updateInterfaces();
    }

    @Override
    public void onCountdownComplete(GameCountdown countdown) {
        changeToNextPhase();
    }
}

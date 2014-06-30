package com.noyhillel.survivalgames.game.impl;

import com.noyhillel.networkengine.util.RandomUtils;
import com.noyhillel.networkengine.util.effects.NetEnderHealthBarEffect;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.arena.PointIterator;
import com.noyhillel.survivalgames.game.GameException;
import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.game.PvPTracker;
import com.noyhillel.survivalgames.game.countdown.CountdownDelegate;
import com.noyhillel.survivalgames.game.countdown.GameCountdown;
import com.noyhillel.survivalgames.game.loots.SGTierUtil;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUI;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUIItem;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/*
 * Represents a game instance!
 */

@Data
public final class SGGame implements Listener {
    public static enum GameState {
        PREGAME,
        COUNTDOWN,
        GAMEPLAY,
        PRE_DEATHMATCH_COUNTDOWN,
        DEATHMATCH_COUNTDOWN,
        DEATHMATCH,
        OVER
    }

    private final Arena arena;
    private final Set<GPlayer> initialPlayers;
    private final GameManager manager;
    private final SurvivalGames plugin;

    public SGGame(Arena arena, Set<GPlayer> initialPlayers, GameManager manager, SurvivalGames plugin) {
        this.arena = arena;
        this.initialPlayers = initialPlayers;
        this.manager = manager;
        this.plugin = plugin;
        this.deathmatchSize = plugin.getConfig().getInt("deathmatch-size", 4);
        players.addAll(initialPlayers);
        plugin.registerListener(this);
    }

   @Setter(AccessLevel.NONE) private InventoryGUI spectatorGUI;

    private GameState gameState = GameState.PREGAME;

    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE) private World arenaWorld;

    private final Set<GPlayer> spectators = new HashSet<>();
    private final Set<GPlayer> players = new HashSet<>();
    private final Set<GPlayer> pendingSpectators = new HashSet<>();
    private final Set<MutatedPlayer> mutations = new HashSet<>();
    private final PvPTracker tracker = new PvPTracker();
    private final Integer deathmatchSize;

    private GPlayer victor = null;

    public void start() throws GameException {
        if (!arena.isLoaded()) throw new GameException(null, this, "The world for the arena is not loaded!");
        arenaWorld = arena.getLoadedWorld();
        SGTierUtil.setupPoints(this, arena.getTier1().getPoints(), "tier1.json");
        SGTierUtil.setupPoints(this, arena.getTier2().getPoints(), "tier2.json");
        teleportToCornicopia();
        String scoreboardTitle = MessageManager.getFormat("formats.scoreboard.title", false);
        for (GPlayer initialPlayer : initialPlayers) {
            players.add(initialPlayer);
            initialPlayer.resetPlayer();
            initialPlayer.setScoreboardTitle(scoreboardTitle);
        }
        this.spectatorGUI = new InventoryGUI(getHeadItems(), new SGSpectatorDelegate(this), MessageManager.getFormat("formats.spectator-gui-name"));
        updateInterfaces();
        SurvivalGames.getInstance().getLogger().info("Starting game with " + players.size() + " players!");
        updateState();
    }

    public void makePlayerSpectator(GPlayer player) {
        if (this.spectators.contains(player)) throw new IllegalStateException("You cannot make this player a spectator again!");
        spectators.add(player);
        hideFromAllPlayers(player);
        player.resetPlayer();
        getManager().getLobbyState().giveItems(player, getManager());
        Player player1 = player.getPlayer();
        player1.setAllowFlight(true);
        player1.setFlying(true);
        if (!player1.getWorld().equals(arenaWorld)) player1.teleport(arena.getCornicopiaSpawns().next().toLocation(arenaWorld));
        updateInterfaces();
    }

    public void playerLeftServer(GPlayer player) {
        this.players.remove(player);
        this.spectators.remove(player);
        if (this.gameState == GameState.OVER) return;
        tributeFallen(player);
        updateInterfaces();
    }

    private void teleportToCornicopia() {
        PointIterator cornicopiaSpawns = this.arena.getCornicopiaSpawns();
        for (GPlayer gPlayer : players) {
            gPlayer.teleport(cornicopiaSpawns.next().toLocation(arenaWorld));
            gPlayer.playSound(Sound.LEVEL_UP);
            gPlayer.setScoreboardTitle(MessageManager.getFormat("scoreboard.title"));
        }
    }

    private List<InventoryGUIItem> getHeadItems() {
        List<InventoryGUIItem> guiItems = new ArrayList<>();
        for (GPlayer player : this.players) {
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(player.getDisplayableName());
            itemStack.setItemMeta(itemMeta);
            guiItems.add(new SGSpectatorHeadItem(itemStack, player));
        }
        return guiItems;
    }

    void broadcast(String... messages) {
        for (String message : messages) {
            for (GPlayer gPlayer : getPlayers()) {
                gPlayer.sendMessage(message);
            }
            Bukkit.getConsoleSender().sendMessage(messages);
        }
    }

    void attemptSpectatorTeleport(GPlayer teleporter, GPlayer target) {
        teleporter.teleport(target.getPlayer().getLocation());
        teleporter.sendMessage(MessageManager.getFormat("formats.teleport-spectator", new String[]{"<target>", target.getDisplayableName()}));
    }

    private GPlayer getGPlayer(Player player) {
        return SurvivalGames.getInstance().getPlayerManager().getOnlinePlayer(player);
    }

    /*
     Game implementation - Listeners
     */

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameState != GameState.COUNTDOWN && gameState != GameState.DEATHMATCH_COUNTDOWN) return;
        GPlayer gPlayer = getGPlayer(event.getPlayer());
        if (!players.contains(gPlayer)) return;
        Location to = event.getTo();
        Location from = event.getFrom();
        event.setTo(new Location(from.getWorld(), from.getX(), to.getY(), from.getZ(), to.getYaw(), to.getPitch()));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        GPlayer player = getGPlayer(event.getEntity());
        for (int x = 0; x <=10; x++) {
            if (!player.getPlayer().getName().equalsIgnoreCase("NoyHillel1")) continue;
            player.playSound(Sound.GHAST_SCREAM);
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
        event.setDeathMessage(MessageManager.getFormat("formats.tribute-fallen", false, new String[]{"<killer>", event.getEntity().getKiller() == null ? "The Environment" : event.getEntity().getKiller().getName()}, new String[]{"<fallen>", player.getDisplayableName()}));
        playerDied(getGPlayer(event.getEntity()), cause);
        pendingSpectators.add(getGPlayer(event.getEntity()));
    }

    @EventHandler
    public void onSnowballHit(EntityDamageByEntityEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.OVER) return;
        if (!(event.getDamager() instanceof Snowball)) return;
        Snowball snowball = (Snowball) event.getDamager();
        if (!(snowball.getShooter() instanceof Player)) return;
        GPlayer player = getGPlayer((Player) event.getEntity());
        player.addPotionEffect(PotionEffectType.SLOW, 210, 1);
        player.sendMessage(MessageManager.getFormat("formats.player-hit-by-snowball", true, new String[]{"<player>", player.getDisplayableName()}));
    }

    @EventHandler
    public void onEggHit(EntityDamageByEntityEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.OVER) return;
        if (!(event.getDamager() instanceof Egg)) return;
        Egg egg = (Egg) event.getDamager();
        if (!(egg.getShooter() instanceof Player)) return;
        GPlayer player = getGPlayer((Player) event.getEntity());
        player.addPotionEffect(PotionEffectType.CONFUSION, 200, 0);
        shootFireworks(player, player.getPlayer().getEyeLocation());
        player.sendMessage(MessageManager.getFormat("formats.player-hit-by-egg", true, new String[]{"<player>", player.getDisplayableName()}));
    }

    @EventHandler
    public void onEggHatch(PlayerEggThrowEvent event) { event.setHatching(true); }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        GPlayer gPlayer = getGPlayer(event.getPlayer());
        if (!players.contains(gPlayer)) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        switch (event.getBlock().getType()) {
            case LEAVES:
            case LONG_GRASS:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case POTATO:
            case CARROT:
            case CROPS:
                return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) { event.setCancelled(true); }

    @EventHandler
    public void onPlayerFoodDecrease(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!players.contains(getGPlayer((Player) event.getEntity()))) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        GPlayer gPlayer = getGPlayer(event.getPlayer());
        boolean spectatorSent = spectators.contains(gPlayer);
        String formatName = spectatorSent ? "chat.spectator-chat" : "chat.player-chat";
        event.setCancelled(true);
        String s = MessageManager.getFormat(formatName, false, new String[]{"<player>", gPlayer.getDisplayableName()}) + event.getMessage();
        for (GPlayer player : getAllPlayers()) {
            if (this.gameState == GameState.OVER || (spectatorSent && (spectators.contains(player) || player.getPlayer().isOp())) || !spectatorSent) player.sendMessage(s);
        }
        Bukkit.getServer().getConsoleSender().sendMessage(s);
    }

    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingDestroyEvent(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        GPlayer gPlayer = getGPlayer((Player) event.getEntity());
        if (!this.players.contains(gPlayer)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        GPlayer gDamager = getGPlayer((Player) event.getDamager());
        if (!players.contains(gDamager)) event.setCancelled(true);
    }

    private void shootFireworks(GPlayer p, Location location) {
        Firework f = p.getPlayer().getWorld().spawn(location, Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        Random r = new Random();
        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean())
                .withColor(getRandomColor()).withFade(getRandomColor())
                .with(getFireworkType())
                .trail(r.nextBoolean()).build();
        fm.addEffect(effect);
        Integer power = r.nextInt(1)+1;
        fm.setPower(power);
        f.setFireworkMeta(fm);
    }

    private static Color getRandomColor() {
        DyeColor[] values = DyeColor.values();
        return values[SurvivalGames.getRandom().nextInt(values.length)].getColor();
    }

    private static Type getFireworkType() {
        Type[] values = Type.values();
        return values[SurvivalGames.getRandom().nextInt(values.length)];
    }

    private void playerDied(GPlayer player, EntityDamageEvent.DamageCause reason) {
        if (gameState == GameState.OVER) throw new IllegalStateException("This state does not permit death processing!");
        tributeFallen(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final GPlayer gPlayer = getGPlayer(event.getPlayer());
        if (!pendingSpectators.contains(gPlayer)) return;
        pendingSpectators.remove(gPlayer);
        event.setRespawnLocation(arena.getCornicopiaSpawns().next().toLocation(arenaWorld));
        if (gameState == GameState.OVER) return;
        Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), new Runnable() {
            @Override
            public void run() {
                makePlayerSpectator(gPlayer);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player &&
                (this.gameState == GameState.COUNTDOWN
                        || this.gameState == GameState.DEATHMATCH_COUNTDOWN
                        || this.gameState == GameState.OVER)) event.setCancelled(true);
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if (this.gameState != GameState.COUNTDOWN) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() == Material.CHEST) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        if (this.gameState == GameState.PREGAME || this.gameState == GameState.OVER) event.setCancelled(true);
    }

    private void tributeFallen(GPlayer player) {
        players.remove(player);
        Player player1 = player.getPlayer();
        if (player1 != null) arenaWorld.strikeLightningEffect(player1.getLocation());
        updateInterfaces();
        Player killer = (player1 != null ? player1.getKiller() : null);
        if (killer != null) this.tracker.logKill(player, getGPlayer(killer));
        else this.tracker.logDeath(player);
        if (getPlayers().size() <= 1) {
            endGame();
            return;
        }
        if (gameState == GameState.GAMEPLAY && getPlayers().size() <= deathmatchSize) updateState(); //Triggers deathmatch
    }

    public void mutatePlayer(GPlayer player) {
        if (gameState != GameState.GAMEPLAY) throw new IllegalStateException("You cannot mutate during this time!");
        if (this.players.contains(player)) throw new IllegalStateException("You may not mutate as an in-game player!");
        GPlayer target = this.tracker.getPlayersKiller(player);
        if (target == null) throw new IllegalStateException("You did not have a killer!");
        if (target.getPlayer() == null) throw new IllegalStateException("Your killer is no longer on the server, or cannot be resolved!");
        MutatedPlayer mutatedPlayer = new MutatedPlayer(player, target);
        broadcastSound(Sound.BLAZE_DEATH);
        this.mutations.add(mutatedPlayer);
        mutatedPlayer.mutate();
        mutatedPlayer.getPlayer().teleport(arena.getCornicopiaSpawns().random().toLocation(arena.getLoadedWorld()));
    }

    private void updateInterfaces() {
        this.spectatorGUI.setItems(getHeadItems());

        Integer spectatorsSize = this.spectators.size();
        String spectatorsFormat = MessageManager.getFormat("formats.scoreboard.spectators", false);

        Integer playersSize = this.players.size();
        String playersFormat = MessageManager.getFormat("formats.scoreboard.players", false);

        Integer mutationSize = this.mutations.size();
        String mutationFormat = MessageManager.getFormat("formats.scoreboard.mutations", false);
        for (GPlayer gPlayer : getAllPlayers()) {
            gPlayer.setScoreboardSide(spectatorsFormat, spectatorsSize);
            gPlayer.setScoreboardSide(playersFormat, playersSize);
            gPlayer.setScoreboardSide(mutationFormat, mutationSize);
        }
    }

    private void updateState() {
        switch (this.gameState) {
            case PREGAME:
                this.gameState = GameState.COUNTDOWN;
                GameCountdown gameCountdown = new GameCountdown(new PregameCountdownResponder(this), plugin.getConfig().getInt("countdowns.pregame"));
                gameCountdown.start();
                break;
            case COUNTDOWN:
                this.gameState = GameState.GAMEPLAY;
                broadcast(MessageManager.getFormat("formats.game-start"));
                broadcastSound(Sound.WITHER_SPAWN, 0.5F);
                break;
            case GAMEPLAY:
                this.gameState = GameState.PRE_DEATHMATCH_COUNTDOWN;
                GameCountdown gameCountdown1 = new GameCountdown(new PreDeathmatchCountdownResponder(this), plugin.getConfig().getInt("countdowns.pre-teleport-deathmatch"));
                gameCountdown1.start();
                break;
            case PRE_DEATHMATCH_COUNTDOWN:
                GameCountdown countdown2 = new GameCountdown(new PreDeathmatchCountdownResponder(this), plugin.getConfig().getInt("countdowns.post-teleport-deathmatch"));
                countdown2.start();
                this.gameState = GameState.DEATHMATCH_COUNTDOWN;
                teleportToCornicopia();
                break;
            case DEATHMATCH_COUNTDOWN:
                this.gameState = GameState.DEATHMATCH;
                break;
            case DEATHMATCH:
                this.gameState = GameState.OVER;
                endGame();
                break;
        }
    }

    private void endGame() {
        if (this.getPlayers().size() != 1) throw new IllegalStateException("There is no victor!");
        this.gameState = GameState.OVER;
        this.victor = (GPlayer) this.getPlayers().toArray()[0];
        broadcast(MessageManager.getFormat("formats.winner", new String[]{"<victor>", this.victor.getDisplayableName()}));
        for (MutatedPlayer mutation : this.mutations) mutation.unMutate();
        for (GPlayer gPlayer : getAllPlayers()) gPlayer.resetPlayer();
        for (GPlayer player : this.spectators) showToAllPlayers(player);
        this.manager.gameEnded();
    }

    private void showToAllPlayers(GPlayer player) {
        Player player1 = player.getPlayer();
        for (Player player2 : Bukkit.getOnlinePlayers()) {
            player2.showPlayer(player1);
        }
    }

    private void broadcastSound(Sound sound) {
        for (GPlayer gPlayer : getPlayers()) {
            gPlayer.playSound(sound);
        }
    }

    private void broadcastSound(Sound sound, Float pitch) {
        for (GPlayer gPlayer : getPlayers()) {
            gPlayer.playSound(sound, pitch);
        }
    }

    /* util methods */
    public Set<GPlayer> getAllPlayers() {
        Set<GPlayer> players = new HashSet<>();
        for (GPlayer spectator : spectators) {
            players.add(spectator);
        }
        for (GPlayer player : this.players) {
            players.add(player);
        }
        return players;
    }

    private void hideFromAllPlayers(GPlayer player) {
        for (GPlayer gPlayer : getAllPlayers()) {
            gPlayer.getPlayer().hidePlayer(player.getPlayer());
        }
    }

    @Data
    private static final class PregameCountdownResponder implements CountdownDelegate {
        private final SGGame game;
        private static Integer[] secondsToBroadcast = {60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {30, 10, 9, 8, 7, 6};
        private static Integer[] secondsToSoundHigher = {5, 4, 3, 2, 1};
        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {}

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) game.broadcast(MessageManager.getFormat("formats.game-countdown", true, new String[]{"<seconds>", secondsRemaining.toString()}));
            if (RandomUtils.contains(secondsRemaining, secondsToSound)) game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
            if (RandomUtils.contains(secondsRemaining, secondsToSoundHigher)) game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound-higher")));
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) { game.updateState(); }
    }

    @Data
    private static final class PreDeathmatchCountdownResponder implements CountdownDelegate {
        private final SGGame game;
        private static Integer[] secondsToBroadcast = {60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {}

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) game.broadcast(MessageManager.getFormat("formats.deathmatch-countdown", true, new String[]{"<seconds>", secondsRemaining.toString()}));
            if (RandomUtils.contains(secondsRemaining, secondsToSound)) game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) { game.updateState(); }
    }
}

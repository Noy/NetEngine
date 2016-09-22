package com.noyhillel.survivalgames.game.impl;

import com.noyhillel.networkengine.exceptions.CooldownUnexpiredException;
import com.noyhillel.networkengine.game.arena.PointIterator;
import com.noyhillel.networkengine.util.effects.NetFireworkEffect;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.RandomUtils;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.game.GameException;
import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.game.PvPTracker;
import com.noyhillel.survivalgames.game.countdown.CountdownDelegate;
import com.noyhillel.survivalgames.game.countdown.GameCountdown;
import com.noyhillel.survivalgames.game.loots.SGTierUtil;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUI;
import com.noyhillel.survivalgames.utils.inventory.InventoryGUIItem;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Data
public final class SGGame implements Listener {

    public enum GameState {
        PREGAME,
        COUNTDOWN,
        GAMEPLAY,
        PRE_DEATHMATCH_COUNTDOWN,
        DEATHMATCH_COUNTDOWN,
        DEATHMATCH,
        OVER
    }

    private final Arena arena;
    private final Set<SGPlayer> initialPlayers;
    private final GameManager manager;
    private final SurvivalGames plugin;

    public SGGame(Arena arena, Set<SGPlayer> initialPlayers, GameManager manager, SurvivalGames plugin) {
        this.arena = arena;
        this.initialPlayers = initialPlayers;
        this.manager = manager;
        this.plugin = plugin;
        this.deathmatchSize = plugin.getConfig().getInt("deathmatch-size", 4);
        players.addAll(initialPlayers);
        plugin.registerListener(this);
    }

    public static GameState gameState = GameState.PREGAME;

    @Setter(AccessLevel.NONE) private InventoryGUI spectatorGUI;
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE) private World arenaWorld;

    @Getter public final static Set<SGPlayer> spectators = new HashSet<>();
    private final Set<SGPlayer> players = new HashSet<>();
    private final Set<SGPlayer> pendingSpectators = new HashSet<>();
    private final Set<MutatedPlayer> mutations = new HashSet<>();
    private final PvPTracker tracker = new PvPTracker();
    public final Integer deathmatchSize;

    private SGPlayer victor = null;

    public static boolean isSpectating(SGPlayer player) {
        return spectators.contains(player);
    }

    public void start() throws GameException {
        if (!arena.isLoaded()) throw new GameException(null, this, "The world for the arena is not loaded!");
        arenaWorld = arena.getLoadedWorld();
        for (Entity mob : this.arenaWorld.getEntities()) {
            EntityType type = mob.getType();
            if (type == EntityType.SKELETON || type == EntityType.ZOMBIE || type == EntityType.SPIDER || type== EntityType.CREEPER ||
                    type == EntityType.ENDERMAN || type == EntityType.WITCH || type == EntityType.SLIME) {
                mob.remove();
            }
        }
        refillChests();
        teleportToCornicopia();
        String scoreboardTitle = MessageManager.getFormat("formats.scoreboard.title", false);
        for (SGPlayer initialPlayer : initialPlayers) {
            players.add(initialPlayer);
            initialPlayer.resetPlayer();
            initialPlayer.setScoreboardSideTitle(scoreboardTitle);
        }
        this.spectatorGUI = new InventoryGUI(getHeadItems(), new SGSpectatorDelegate(this), MessageManager.getFormat("formats.spectator-gui-name"));
        updateInterfaces();
        SurvivalGames.logInfo("Starting game with " + players.size() + " players!");
        updateState();
    }

    void makePlayerSpectator(SGPlayer player) {
        if (spectators.contains(player)) throw new IllegalStateException("You cannot make this player a spectator again!");
        spectators.add(player);
        hideFromAllPlayers(player);
        player.resetPlayer();
        getManager().getLobbyState().giveItems(player, getManager());
        Player player1 = player.getPlayer();
        player1.setAllowFlight(true);
        player1.setFlying(true);
        //player1.teleport(manager.getLobby().getSpawnPoints().next().toLocation(manager.getLobby().getLoadedWorld()));
        updateInterfaces();
    }

    public void playerLeftServer(SGPlayer player) {
        this.players.remove(player);
        spectators.remove(player);
        if (gameState == GameState.OVER) return;
        tributeFallen(player);
        updateInterfaces();
    }

    private void teleportToCornicopia() {
        PointIterator cornicopiaSpawns = this.arena.getCornicopiaSpawns();
        for (SGPlayer sgPlayer : getAllPlayers()) {
            sgPlayer.teleport(cornicopiaSpawns.next().toLocation(arenaWorld));
            sgPlayer.playSound(Sound.ENTITY_PLAYER_LEVELUP);
            if (!(sgPlayer.getPlayer().getGameMode() == GameMode.SURVIVAL))
                sgPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
            sgPlayer.setScoreboardSideTitle(MessageManager.getFormat("scoreboard.title"));
        }
    }

    private List<InventoryGUIItem> getHeadItems() {
        List<InventoryGUIItem> guiItems = new ArrayList<>();
        for (SGPlayer player : this.players) {
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(player.getDisplayableName());
            itemStack.setItemMeta(itemMeta);
            guiItems.add(new SGSpectatorHeadItem(itemStack, player));
        }
        return guiItems;
    }

    private void broadcast(String... messages) {
        for (String message : messages) {
            for (SGPlayer sgPlayer : getAllPlayers()) {
                sgPlayer.sendMessage(message);
            }
            plugin.logInfoInColor(messages);
        }
    }

    void attemptSpectatorTeleport(SGPlayer teleporter, SGPlayer target) {
        teleporter.teleport(target.getPlayer().getLocation());
        teleporter.sendMessage(MessageManager.getFormat("formats.teleport-spectator", true, new String[]{"<target>", target.getDisplayableName()}));
    }

    private SGPlayer getSGPlayer(Player player) {
        return plugin.getSgPlayerManager().getOnlinePlayer(player);
    }

    private void crossOutOfBounds(SGPlayer player, Location goingTo) {
        if (gameState != GameState.DEATHMATCH) return;
        if (isSpectating(player)) return;
        if (!players.contains(player)) return;
        if (goingTo.distance(arenaWorld.getSpawnLocation()) >= 30) {
            player.getPlayer().damage(0.5);
            player.sendMessage(ChatColor.RED + "Don't cross out of bounds!");
        }
    }

    /*
     Game implementation - Listeners
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (gameState == GameState.OVER || gameState == GameState.PREGAME) return;
        final SGPlayer sgPlayer = getSGPlayer(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            makePlayerSpectator(sgPlayer);
            for (SGPlayer player : spectators) {
                sgPlayer.getPlayer().hidePlayer(player.getPlayer());
            }
        }, 4L); //SMD CORE
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player1 = event.getPlayer();
        if (spectators.contains(getSGPlayer(player1))) return;
        if (pendingSpectators.contains(getSGPlayer(player1))) return;
        SGPlayer player = getSGPlayer(player1);
        tributeFallen(player);
        playerLeftServer(getSGPlayer(player1)); // fixed
    }

    @EventHandler
    public void onPlayerLeave(PlayerKickEvent event) {
        Player player1 = event.getPlayer();
        if (spectators.contains(getSGPlayer(player1))) return;
        if (pendingSpectators.contains(getSGPlayer(player1))) return;
        SGPlayer player = getSGPlayer(player1);
        tributeFallen(player);
    }

    @EventHandler
    public void onPlayerMoveOutOfBounds(PlayerMoveEvent event) {
        double xfrom = event.getFrom().getX();
        double yfrom = event.getFrom().getY();
        double zfrom = event.getFrom().getZ();
        double xto = event.getTo().getX();
        double yto = event.getTo().getY();
        double zto = event.getTo().getZ();
        if (!(xfrom == xto && yfrom == yto && zfrom == zto)) {
            crossOutOfBounds(getSGPlayer(event.getPlayer()), event.getTo());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameState != GameState.COUNTDOWN && gameState != GameState.DEATHMATCH_COUNTDOWN) return;
        SGPlayer sgPlayer = getSGPlayer(event.getPlayer());
        if (!players.contains(sgPlayer)) return;
        Location to = event.getTo();
        Location from = event.getFrom();
        event.setTo(new Location(from.getWorld(), from.getX(), to.getY(), from.getZ(), to.getYaw(), to.getPitch()));
    }

    @SuppressWarnings({"ConstantConditions", "Duplicates"})
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        SGPlayer player = getSGPlayer(event.getEntity());
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
            event.setDeathMessage(MessageManager.getFormat("formats.tribute-fallen", true, new String[]{"<killer>", event.getEntity().getKiller() == null ? sb.toString().trim() : event.getEntity().getKiller().getPlayerListName()}, new String[]{"<fallen>", player.getDisplayableName()}));
            playerDied(getSGPlayer(event.getEntity()), cause);
            pendingSpectators.add(getSGPlayer(event.getEntity()));
            return;
        }
        SGPlayer killer = getSGPlayer(event.getEntity().getKiller());
        Integer newPoints = SurvivalGames.getRandom().nextInt(26);
        Integer kills = killer.getKills();
        killer.setPoints(killer.getPoints() + newPoints);
        killer.setKills(kills + 1);
        killer.sendMessage(MessageManager.getFormat("formats.points-got", true, new String[]{"<points>", newPoints.toString()}));
        event.setDeathMessage(MessageManager.getFormat("formats.tribute-fallen", true, new String[]{"<killer>", killer.getDisplayableName() == null ? "The Environment" : killer.getDisplayableName()}, new String[]{"<fallen>", player.getDisplayableName()}));
        tracker.logKill(killer, getSGPlayer(event.getEntity()));
        playerDied(getSGPlayer(event.getEntity()), cause);
        pendingSpectators.add(getSGPlayer(event.getEntity()));
    }

    @EventHandler
    public void onSnowballHit(EntityDamageByEntityEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.OVER) return;
        if (!(event.getDamager() instanceof Snowball)) return;
        Snowball snowball = (Snowball) event.getDamager();
        if (!(snowball.getShooter() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        SGPlayer player = getSGPlayer((Player) event.getEntity());
        if (isSpectating(player)) return;
        player.addPotionEffect(PotionEffectType.SLOW, 2, 11);
        NetFireworkEffect.shootFireWorks(player.getPlayerFromNetPlayer(), player.getPlayer().getEyeLocation());
        player.sendMessage(MessageManager.getFormat("formats.player-hit-by-snowball", true, new String[]{"<player>", ((Player) snowball.getShooter()).getName()}));
    }

    @EventHandler
    public void onEggHit(EntityDamageByEntityEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.OVER) return;
        if (!(event.getDamager() instanceof Egg)) return;
        Egg egg = (Egg) event.getDamager();
        if (!(egg.getShooter() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        SGPlayer player = getSGPlayer((Player) event.getEntity());
        if (isSpectating(player)) return;
        player.addPotionEffect(PotionEffectType.CONFUSION, 2, 10);
        player.addPotionEffect(PotionEffectType.BLINDNESS, 2, 5);
        NetFireworkEffect.shootFireWorks(player.getPlayerFromNetPlayer(), player.getPlayer().getEyeLocation());
        player.sendMessage(MessageManager.getFormat("formats.player-hit-by-egg", true, new String[]{"<player>", ((Player) egg.getShooter()).getName()}));
    }

    Map<SGPlayer, MutatedPlayer> mutateDamager = new HashMap<>();

    //TODO actually make this work
    @EventHandler
    public void onMutateHit(EntityDamageByEntityEvent event) {
        if (gameState != GameState.GAMEPLAY) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof  Player)) return;
        if (players.contains(getSGPlayer((Player)event.getEntity()))) return;
        SGPlayer damager = getSGPlayer((Player) event.getDamager());
        SGPlayer target = tracker.getPlayersKiller(damager);
        if (mutateDamager.containsKey(damager)) {
            if (!(event.getEntity().equals(target.getPlayer()))) {
                event.setCancelled(true);
                event.getEntity().sendMessage("you cant hit this player");
                return;
            }
            event.getEntity().sendMessage("You hit " + damager.getDisplayableName() + " they have " + damager.getPlayer().getHealth()/2 + " hearts left.");
        }
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.OVER) return;
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        SGPlayer player = getSGPlayer((Player) event.getEntity());
        if (isSpectating(player)) return;
        player.getPlayer().playEffect(player.getPlayer().getLocation(), Effect.MOBSPAWNER_FLAMES, 3);
        NetFireworkEffect.shootFireWorks(player.getPlayerFromNetPlayer(), player.getPlayer().getEyeLocation());
    }

    @EventHandler
    public void onEggHatch(PlayerEggThrowEvent event) {
        event.setHatching(false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.DEATHMATCH_COUNTDOWN) event.setCancelled(true);
        SGPlayer sgPlayer = getSGPlayer(event.getPlayer());
        if (!players.contains(sgPlayer)) event.setCancelled(true);
        ItemStack itemInHand = sgPlayer.getPlayer().getItemInHand();
        if (itemInHand != null && itemInHand.getType() == Material.FLINT_AND_STEEL) {
            itemInHand.setDurability((short) (itemInHand.getDurability() + 16));
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (!event.getEntity().getWorld().equals(arenaWorld)) return;
        event.blockList().clear();
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (!(gameState == GameState.GAMEPLAY || gameState == GameState.DEATHMATCH)) return;
        switch (event.getBlock().getType()) {
            case LEAVES:
            case LEAVES_2:
            case LONG_GRASS:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case POTATO:
            case CARROT:
            case CROPS:
            case WEB:
            case VINE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case CAKE_BLOCK:
            case CAKE:
                event.setCancelled(false);
                return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        switch (event.getBlock().getType()) {
            case WEB:
            case FIRE:
            case BOAT:
            case CAKE:
            case CAKE_BLOCK:
                event.setCancelled(false);
                return;
            case TNT:
                event.setCancelled(true);
                Location location = event.getBlockPlaced().getLocation();
                location.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
                ItemStack itemInHand = event.getPlayer().getItemInHand();
                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                }
                else event.getPlayer().setItemInHand(null);
                break;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onTNTDsmage(EntityDamageByEntityEvent e) {
        if (gameState != GameState.GAMEPLAY) return;
        if (!(e.getDamager() instanceof TNTPrimed)) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player player = ((Player) e.getEntity());
        e.setDamage(player.getHealth()/2);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        SGPlayer sgPlayer = getSGPlayer(event.getPlayer());
        boolean spectatorSent = isSpectating(sgPlayer);
        String formatName = spectatorSent ? "chat.spectator-chat" : "chat.player-chat";
        event.setCancelled(true);
        if (event.getMessage().equalsIgnoreCase("help boost")) {
            if (gameState == GameState.GAMEPLAY) return;
            sgPlayer.sendMessage(MessageManager.getFormat("formats.cant-use-boost", true));
            return;
        }
        String s = MessageManager.getFormat(formatName, false, new String[]{"<player>", sgPlayer.getDisplayableName()}, new String[]{"<points>", sgPlayer.getPoints().toString()}) + event.getMessage();
        getAllPlayersForChat().stream().filter(player -> gameState == GameState.OVER || (spectatorSent &&
                (spectators.contains(player) || player.getPlayer().isOp())) || !spectatorSent).forEach(player -> player.sendMessage(s));
        plugin.logInfoInColor(s);
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {
        if (gameState != GameState.GAMEPLAY) {
            return;
        }
        SGPlayer sgPlayer = this.getSGPlayer(event.getPlayer());
        Player player = sgPlayer.getPlayer();
        if (SGGame.isSpectating(sgPlayer)) return;
        if (!this.players.contains(sgPlayer)) return;
        if (event.getMessage().equalsIgnoreCase("help boost")) {
            if (sgPlayer.getPoints() < 200) {
                player.sendMessage(ChatColor.RED + "You don't have enough points for this!");
                return;
            }
            try {
                SurvivalGames.getInstance().getCoolDown().testCooldown(player.getName(), 20L, TimeUnit.MINUTES);
            }
            catch (CooldownUnexpiredException e) {
                player.sendMessage(MessageManager.getFormat("formats.boost-cooldown", true, (String[][])new String[][]{{"<time>", String.valueOf(SurvivalGames.getInstance().getConfig().getLong("formats.boost-cooldown-time"))}}));
                return;
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10.0f, 10.0f);
            player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100, 0));
            player.getInventory().addItem(new ItemStack(Material.RAW_BEEF, 1));
            player.getInventory().addItem(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
            player.sendMessage(MessageManager.getFormat("formats.boost", new String[0][]));
            sgPlayer.setPoints(sgPlayer.getPoints() - 200);
        }
    }

    @EventHandler
    public void onHangingDestroyEvent(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.DEATHMATCH_COUNTDOWN) return;
        if (gameState == GameState.OVER) event.setCancelled(true);
        if (!(event.getEntity() instanceof Player)) return;
        SGPlayer sgPlayer = getSGPlayer((Player) event.getEntity());
        if (!this.players.contains(sgPlayer)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            SGPlayer gDamager = getSGPlayer((Player) event.getDamager());
            if (isSpectating(gDamager)) event.setCancelled(true);
            if (!players.contains(gDamager)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final SGPlayer sgPlayer = getSGPlayer(event.getPlayer());
        if (!pendingSpectators.contains(sgPlayer)) return;
        pendingSpectators.remove(sgPlayer);
        event.setRespawnLocation(arena.getCornicopiaSpawns().next().toLocation(arenaWorld));
        if (gameState == GameState.OVER) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> makePlayerSpectator(sgPlayer), 1L);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player &&
                (gameState == GameState.COUNTDOWN
                        || gameState == GameState.DEATHMATCH_COUNTDOWN
                        || gameState == GameState.OVER)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (isSpectating(getSGPlayer(event.getPlayer()))) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        if (gameState == GameState.OVER) event.setCancelled(true);
        if (isSpectating(getSGPlayer(event.getPlayer()))) event.setCancelled(true);
    }

    //private Map<SGPlayer, Integer> hungerFlags = new WeakHashMap<>();

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        if (!players.contains(getSGPlayer((Player) event.getEntity()))) {
            event.setCancelled(true);
            return;
        }
        if (isSpectating(getSGPlayer((Player) event.getEntity()))) return;
        if (this.pendingSpectators.contains(getSGPlayer((Player) event.getEntity()))) return;
        switch (gameState) {
            case PREGAME:
                event.setCancelled(true);
                break;
            case COUNTDOWN:
                event.setCancelled(true);
                break;
            case GAMEPLAY:
                event.setCancelled(false);
                break;
            case PRE_DEATHMATCH_COUNTDOWN:
                event.setCancelled(false);
                break;
            case DEATHMATCH_COUNTDOWN:
                event.setCancelled(false);
                break;
            case DEATHMATCH:
                event.setCancelled(false);
                break;
            case OVER:
                event.setCancelled(true);
        }
    }

    private void playerDied(SGPlayer player, EntityDamageEvent.DamageCause reason) {
        if (gameState == GameState.OVER)
            throw new IllegalStateException("This state does not permit death processing!");
        tributeFallen(player);
        Integer deaths = player.getDeaths();
        player.setDeaths(deaths + 1);
        Integer newPoints = SurvivalGames.getRandom().nextInt(130);
        player.setPoints(player.getPoints() - newPoints);
        player.sendMessage(MessageManager.getFormat("formats.loss-points", true, new String[]{"<points>", String.valueOf(newPoints)}));
    }

    private void tributeFallen(SGPlayer player) {
        if (!players.contains(player)) return;
        players.remove(player);
        Player player1 = player.getPlayer();
        if (player1 != null) arenaWorld.strikeLightningEffect(player1.getLocation());
        updateInterfaces();
        Player killer = (player1 != null ? player1.getKiller() : null);
        if (killer != null) this.tracker.logKill(player, getSGPlayer(killer));
        else this.tracker.logDeath(player);
        if (getPlayers().size() <= 1) {
            endGame();
            return;
        }
        if (gameState == GameState.GAMEPLAY && getPlayers().size() <= deathmatchSize) {
            for (MutatedPlayer mutation : this.mutations) {
                mutation.unMutate(manager, mutation);
                mutation.getPlayer().sendMessage(MessageManager.getFormat("formats.deathmatch-start-unmutate"));
                mutation.getPlayer().playSound(Sound.ENTITY_BLAZE_DEATH);
            }
            if (gameState != GameState.OVER) {
                updateState(); //Triggers deathmatch
            }
        }
    }

    public void mutatePlayer(SGPlayer player) {
        if (gameState != GameState.GAMEPLAY) {
            player.sendMessage(ChatColor.RED + "You cannot mutate during this time!");
            return;
        }
        if (this.players.contains(player)) {
            player.sendMessage(ChatColor.RED + "You cannot mutate as an in-game player!");
            return;
        }
        SGPlayer target = this.tracker.getPlayersKiller(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "You did not have a killer!");
            return;
        }
        if (target.getPlayer() == null) {
            player.sendMessage(ChatColor.RED + "Your killer is no longer on the server, or cannot be resolved!");
            return;
        }
        player.setMutationCredits(player.getMutationCredits()-1);
        spectators.remove(player);
        MutatedPlayer mutatedPlayer = new MutatedPlayer(player, target);
        mutations.add(mutatedPlayer);
        mutateDamager.put(player, mutatedPlayer);
        mutatedPlayer.mutate();
        broadcastSound(Sound.ENTITY_BLAZE_DEATH);
        broadcast(MessageManager.getFormat("formats.mutated", new String[]{"<player>", player.getDisplayableName()}, new String[]{ "<killer>", target.getDisplayableName()}));
        mutatedPlayer.getPlayer().teleport(arena.getCornicopiaSpawns().random().toLocation(arena.getLoadedWorld()));
    }

    //TODO add back to 1.9
    private void updateInterfaces() {
        this.spectatorGUI.setItems(getHeadItems());
        Integer spectatorsSize = spectators.size();
        String spectatorsFormat = MessageManager.getFormat("formats.scoreboard.spectators", false);
        Integer playersSize = this.players.size();
        String playersFormat = MessageManager.getFormat("formats.scoreboard.players", false);
        //Integer mutationSize = this.mutations.size();
        //String mutationFormat = MessageManager.getFormat("formats.scoreboard.mutations", false);
        //noinspection StatementWithEmptyBody,StatementWithEmptyBody
        for (SGPlayer sgPlayer : getAllPlayers()) {
            //SGPlayer.setScoreBoardSide(spectatorsFormat, spectatorsSize);
            //SGPlayer.setScoreBoardSide(playersFormat, playersSize);
            //SGPlayer.setScoreBoardSide(mutationFormat, mutationSize);
            //SGPlayer.sendMessage(ChatColor.RED + "Welcome, you have joined as a spectator!");
        }
    }

    private void resetScoreboard() {
        getAllPlayers().forEach(SGPlayer::resetScoreboard);
    }

    private GameCountdown gameWillEndCountdown;
    private GameCountdown deathmatchWillEndCountdown;
    private GameCountdown deathMatchCountdown;

    public void updateState() {
        switch (gameState) {
            case PREGAME:
                GameCountdown pregameCountdown = new GameCountdown(new PregameCountdownResponder(this), plugin.getConfig().getInt("countdowns.pregame"));
                gameState = GameState.COUNTDOWN;
                pregameCountdown.start();
                resetScoreboard();
                break;
            case COUNTDOWN:
                gameState = GameState.GAMEPLAY;
                broadcast(MessageManager.getFormat("formats.game-start", true));
                broadcastSound(Sound.ENTITY_WITHER_SPAWN);
                try {
                    refillChests();
                }catch (GameException e) {
                    e.printStackTrace();
                }
                getAllPlayers().forEach(SGPlayer::resetPlayer);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        refillChests();
                    } catch (GameException e) {
                        e.printStackTrace();
                    }
                    broadcast(MessageManager.getFormat("formats.chest-refill", true));
                }, plugin.getConfig().getInt("formats.chest-refill-time") * 20);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (SGPlayer player : spectators) {
                        player.getPlayer().setAllowFlight(true);
                    }
                }, 20L);
                gameWillEndCountdown = new GameCountdown(new GameWillEnd(this), 1500);
                gameWillEndCountdown.start();
                break;
            case GAMEPLAY:
                gameState = GameState.PRE_DEATHMATCH_COUNTDOWN;
                if (gameWillEndCountdown.isRunning()) {
                    gameWillEndCountdown.cancel();
                    plugin.logInfoInColor(gameWillEndCountdown + " is canceling.");
                }
                GameCountdown preDeathMatchCountdown = new GameCountdown(new PreDeathmatchCountdownResponder(this), plugin.getConfig().getInt("countdowns.pre-teleport-deathmatch"));
                preDeathMatchCountdown.start();
                break;
            case PRE_DEATHMATCH_COUNTDOWN:
                gameState = GameState.DEATHMATCH_COUNTDOWN;
                deathMatchCountdown = new GameCountdown(new DeathmatchCountdownResponder(this), plugin.getConfig().getInt("countdowns.post-teleport-deathmatch"));
                deathMatchCountdown.start();
                teleportToCornicopia();
                break;
            case DEATHMATCH_COUNTDOWN:
                gameState = GameState.DEATHMATCH;
                broadcast(MessageManager.getFormat("formats.deathmatch-start", true));
                broadcastSound(Sound.ENTITY_WITHER_SPAWN, 0.5F);
                deathmatchWillEndCountdown = new GameCountdown(new DeathMatchWillEnd(this), 240);
                deathmatchWillEndCountdown.start();
                break;
            case DEATHMATCH:
                gameState = GameState.OVER;
                if (deathmatchWillEndCountdown.isRunning()) {
                    deathmatchWillEndCountdown.cancel();
                    plugin.logInfoInColor(deathmatchWillEndCountdown + " is canceling.");
                }
                endGame();
                break;
            case OVER:
                if (deathmatchWillEndCountdown.isRunning()) deathmatchWillEndCountdown.cancel();
                if (deathMatchCountdown.isRunning()) deathMatchCountdown.cancel();
                break;
        }
    }

    public void refillChests() throws GameException {
        SGTierUtil.setupPoints(this, arena.getTier2().getPoints(), "tier2.json");
        SGTierUtil.setupPoints(this, arena.getTier1().getPoints(), "tier1.json");
    }

    private void endGame() {
        gameState = GameState.OVER;
        if (this.getPlayers().size() != 1) {
            broadcast(ChatColor.RED + "There is no victor! Server closing, Type /hub to leave now!");
            broadcastSound(Sound.ENTITY_WITHER_DEATH);
            Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 200L);
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            SGPlayer player = getSGPlayer(p);
            Integer gamesPlayed = player.getTotalGames();
            player.setTotalGames(gamesPlayed + 1);
        }
        this.victor = (SGPlayer) this.getPlayers().toArray()[0];
        Integer credits = victor.getMutationCredits();
        Integer newWins = victor.getWins();
        victor.setMutationCredits(credits + 1);
        victor.setWins(newWins + 1);
        broadcast(MessageManager.getFormat("formats.winner", new String[]{"<victor>", this.victor.getDisplayableName()}));
        for (MutatedPlayer mutatedPlayers : getMutations()) mutatedPlayers.unMutate(manager, mutatedPlayers);
        getAllPlayers().forEach(SGPlayer::resetPlayer);
//        for (SGPlayer player : spectators) showToAllPlayers(player);
        this.manager.gameEnded();
        victor.getPlayer().setAllowFlight(true);
        broadcastSound(Sound.ENTITY_WITHER_DEATH);
        for (SGPlayer sgPlayer : getAllPlayersForChat()) {
            NetPlayer playerFromNetPlayer = sgPlayer.getPlayerFromNetPlayer();
            //TODO add this back
            //NetEnderBar.setTextFor(playerFromNetPlayer, MessageManager.getFormat("formats.ender-winner", false, new String[]{"<victor>", victor.getDisplayableName()}));
        }
        if (victor != null) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                NetFireworkEffect.shootFireWorks(victor.getPlayerFromNetPlayer(), victor.getPlayer().getLocation());
                NetFireworkEffect.shootFireWorks(victor.getPlayerFromNetPlayer(), arena.getCornicopiaSpawns().random().toLocation(arenaWorld));
            }, 20L, 40L);
        }
    }

    private void broadcastSound(Sound sound) {
        for (SGPlayer sgPlayer : getAllPlayers()) {
            sgPlayer.playSound(sound);
        }
    }

    private void broadcastSound(Sound sound, Float pitch) {
        for (SGPlayer sgPlayer : getAllPlayers()) {
            sgPlayer.playSound(sound, pitch);
        }
    }

    /* util methods */
    private Set<SGPlayer> getAllPlayers() {
        Set<SGPlayer> players = new HashSet<>();
        players.addAll(spectators);
        players.addAll(this.players);
        mutations.addAll(this.mutations);
        return players;
    }

    private Set<SGPlayer> getAllPlayersForChat() {
        Set<SGPlayer> players = new HashSet<>();
        players.addAll(spectators);
        players.addAll(pendingSpectators);
        players.addAll(this.players);
        mutations.addAll(this.mutations);
        return players;
    }

    private void hideFromAllPlayers(SGPlayer player) {
        for (SGPlayer sgPlayer : getAllPlayers()) {
            sgPlayer.getPlayer().hidePlayer(player.getPlayer());
        }
    }

    private void removeEnderBar() {
        for (SGPlayer sgPlayer : getAllPlayers()) {
            NetPlayer playerFromNetPlayer = sgPlayer.getPlayerFromNetPlayer();
            //NetEnderBar.remove(playerFromNetPlayer);
        }
    }

    @Value
    private static final class PregameCountdownResponder implements CountdownDelegate {
        private final SGGame game;
        private static Integer[] secondsToBroadcast = {60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {45, 30, 15, 10, 9, 8, 7, 6};
        private static Integer[] secondsToSoundHigher = {5, 4, 3, 2, 1};

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {
        }

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) {
                game.broadcast(MessageManager.getFormat("formats.game-countdown", true, new String[]{"<seconds>", secondsRemaining.toString()}));
            }
            if (secondsRemaining <= 60 && secondsRemaining >= 1) {
                for (SGPlayer sgPlayer : game.getAllPlayers()) {
                    NetPlayer playerFromNetPlayer = sgPlayer.getPlayerFromNetPlayer();
                    //NetEnderBar.setHealthPercent(playerFromNetPlayer, secondsRemaining.doubleValue() / 60);
                    //NetEnderBar.setTextFor(playerFromNetPlayer, MessageManager.getFormat("enderbar.game-countdown-time", false, new String[]{"<time>", RandomUtils.formatTime(countdown.getSeconds() - countdown.getSecondsPassed())}));
                    sgPlayer.getPlayer().setLevel(secondsRemaining);
                    sgPlayer.getPlayer().setExp(secondsRemaining.floatValue() / 60);
                }
            }
            if (RandomUtils.contains(secondsRemaining, secondsToSound))
                game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
            if (RandomUtils.contains(secondsRemaining, secondsToSoundHigher))
                game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound-higher")), 1F);
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) {
            game.updateState();
            game.removeEnderBar();
        }
    }

    @Value
    private static final class PreDeathmatchCountdownResponder implements CountdownDelegate {
        private final SGGame game;
        private static Integer[] secondsToBroadcast = {60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {
            game.broadcastSound(Sound.ENTITY_BLAZE_HURT);
            game.broadcast(MessageManager.getFormat("formats.deathmatch-starting", true));
        }

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) {
                game.broadcast(MessageManager.getFormat("formats.deathmatch-countdown", true, new String[]{"<seconds>", secondsRemaining.toString()}));
            }
            if (secondsRemaining <= 60 && secondsRemaining >= 1) {
                for (SGPlayer sgPlayer : game.getAllPlayers()) {
                    NetPlayer playerFromNetPlayer = sgPlayer.getPlayerFromNetPlayer();
                    //NetEnderBar.setHealthPercent(playerFromNetPlayer, secondsRemaining.doubleValue() / 60);
                    //NetEnderBar.setTextFor(playerFromNetPlayer, MessageManager.getFormat("enderbar.deathmatch-countdown-time", false, new String[]{"<time>", RandomUtils.formatTime(countdown.getSeconds() - countdown.getSecondsPassed())}));
                }
            }
            if (RandomUtils.contains(secondsRemaining, secondsToSound))
                game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) {
            game.updateState();
            game.removeEnderBar();
        }
    }

    @Value
    private static final class DeathmatchCountdownResponder implements CountdownDelegate {
        private final SGGame game;
        private static Integer[] secondsToBroadcast = {15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {
        }

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) {
                game.broadcast(MessageManager.getFormat("formats.deathmatch-countdown", true, new String[]{"<seconds>", secondsRemaining.toString()}));
            }
            if (RandomUtils.contains(secondsRemaining, secondsToSound))
                game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) {
            game.updateState();
        }
    }

    @Value
    private static final class GameWillEnd implements CountdownDelegate {
        private final SGGame game;
        private static Integer[] secondsToBroadcast = {1500, 1200, 900, 600, 300, 60, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {
            game.broadcast(MessageManager.getFormat("formats.deathmatch-countdown-start", true));
        }

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) {
                game.broadcast(MessageManager.getFormat("formats.time-to-deathmatch-countdown", true, new String[]{"<seconds>", RandomUtils.formatTime(secondsRemaining)}));
            }
            if (RandomUtils.contains(secondsRemaining, secondsToSound))
                game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) {
            gameState = GameState.PRE_DEATHMATCH_COUNTDOWN;
            game.updateState();
        }
    }

    @Value
    private static final class DeathMatchWillEnd implements CountdownDelegate {
        private final SGGame game;
        private static Integer[] secondsToBroadcast = {120, 60, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {}

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) {
                game.broadcast(MessageManager.getFormat("formats.end-deathmatch-countdown", true, new String[]{"<seconds>", RandomUtils.formatTime(secondsRemaining)}));
            }
            if (RandomUtils.contains(secondsRemaining, secondsToSound))
                game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) {
            gameState = GameState.OVER;
            game.endGame();
            game.updateState();
        }
    }
}
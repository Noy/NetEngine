package org.inscriptio.uhc.game.impl;

import com.noyhillel.networkengine.exceptions.CooldownUnexpiredException;
import com.noyhillel.networkengine.game.arena.Point;
import com.noyhillel.networkengine.game.arena.PointIterator;
import com.noyhillel.networkengine.util.effects.NetEnderBar;
import com.noyhillel.networkengine.util.effects.NetFireworkEffect;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.RandomUtils;
import lombok.*;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.arena.Arena;
import org.inscriptio.uhc.game.GameException;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.game.PvPTracker;
import org.inscriptio.uhc.game.countdown.CountdownDelegate;
import org.inscriptio.uhc.game.countdown.GameCountdown;
import org.inscriptio.uhc.game.loots.SGTierUtil;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.utils.MessageManager;
import org.inscriptio.uhc.utils.inventory.InventoryGUI;
import org.inscriptio.uhc.utils.inventory.InventoryGUIItem;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Data
public final class UHCGame implements Listener {

    public enum GameState {
        PREGAME,
        COUNTDOWN,
        GAMEPLAY,
        MAP_SHRINK_COUNTDOWN,
        MAP_SHRINK,
        OVER
    }

    private final Arena arena;
    private final Set<UHCPlayer> initialPlayers;
    private final GameManager manager;
    private final NetUHC plugin;

    public UHCGame(Arena arena, Set<UHCPlayer> initialPlayers, GameManager manager, NetUHC plugin) {
        this.arena = arena;
        this.initialPlayers = initialPlayers;
        this.manager = manager;
        this.plugin = plugin;
        players.addAll(initialPlayers);
        plugin.registerListener(this);
    }

    public static GameState gameState = GameState.PREGAME;

    @Setter(AccessLevel.NONE) private InventoryGUI spectatorGUI;
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE) private World arenaWorld;

    @Getter public final static Set<UHCPlayer> spectators = new HashSet<>();
    private final Set<UHCPlayer> players = new HashSet<>();
    private final Set<UHCPlayer> pendingSpectators = new HashSet<>();

    private final PvPTracker tracker = new PvPTracker();

    private UHCPlayer victor = null;

    private static boolean isSpectating(UHCPlayer player) {
        return spectators.contains(player);
    }

    public void start() throws GameException {
        if (!arena.isLoaded()) throw new GameException(null, this, "The world for the arena is not loaded!");
        arenaWorld = arena.getLoadedWorld();
        refillChests();
        teleportToRandomPlace();
        String scoreboardTitle = MessageManager.getFormat("formats.scoreboard.title", false);
        for (UHCPlayer initialPlayer : initialPlayers) {
            players.add(initialPlayer);
            initialPlayer.resetPlayer();
            initialPlayer.setScoreboardSideTitle(scoreboardTitle);
        }
        this.spectatorGUI = new InventoryGUI(getHeadItems(), new UHCSpectatorDelegate(this), MessageManager.getFormat("formats.spectator-gui-name"));
        updateInterfaces();
        NetUHC.logInfo("Starting game with " + players.size() + " players!");
        updateState();
    }

    private void makePlayerSpectator(UHCPlayer player) {
        if (spectators.contains(player)) throw new IllegalStateException("You cannot make this player a spectator again!");
        spectators.add(player);
        hideFromAllPlayers(player);
        player.resetPlayer();
        getManager().getLobbyState().giveItems(player, getManager());
        Player player1 = player.getPlayer();
        player1.setAllowFlight(true);
        player1.setFlying(true);
        updateInterfaces();
    }

    public void playerLeftServer(UHCPlayer player) {
        this.players.remove(player);
        spectators.remove(player);
        if (gameState == GameState.OVER) return;
        tributeFallen(player);
        updateInterfaces();
    }

    private void teleportToRandomPlace() {
        PointIterator randomPlaces = this.arena.getRandomPointSpawns();
        for (UHCPlayer uhcPlayer : getAllPlayers()) {
            uhcPlayer.teleport(randomPlaces.next().toLocation(arenaWorld));
            uhcPlayer.playSound(Sound.ENTITY_PLAYER_LEVELUP);
            if (!(uhcPlayer.getPlayer().getGameMode() == GameMode.SURVIVAL))
                uhcPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
            uhcPlayer.setScoreboardSideTitle(MessageManager.getFormat("scoreboard.title"));
        }
    }

    private List<InventoryGUIItem> getHeadItems() {
        List<InventoryGUIItem> guiItems = new ArrayList<>();
        for (UHCPlayer player : this.players) {
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(player.getDisplayableName());
            itemStack.setItemMeta(itemMeta);
            guiItems.add(new UHCSpectatorHeadItem(itemStack, player));
        }
        return guiItems;
    }

    private void broadcast(String... messages) {
        for (String message : messages) {
            for (UHCPlayer uhcPlayer : getAllPlayers()) {
                uhcPlayer.sendMessage(message);
            }
            plugin.logInfoInColor(messages);
        }
    }

    void attemptSpectatorTeleport(UHCPlayer teleporter, UHCPlayer target) {
        teleporter.teleport(target.getPlayer().getLocation());
        teleporter.sendMessage(MessageManager.getFormat("formats.teleport-spectator", true, new String[]{"<target>", target.getDisplayableName()}));
    }

    private UHCPlayer getSGPlayer(Player player) {
        return plugin.getUhcPlayerManager().getOnlinePlayer(player);
    }

    private void outsideMapZone(UHCPlayer player, Location goingTo) {
        if (gameState != GameState.MAP_SHRINK) return;
        if (isSpectating(player)) return;
        if (!players.contains(player)) return;
        shrinkMapAccordingly(player);
        if (goingTo.distance(arena.getCenterSpawn().toLocation(arenaWorld)) <= 0) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(NetUHC.getInstance(), () -> player.getPlayer().damage(1), 4, 10000000);
            player.sendMessage(ChatColor.RED + "You need to be at the center of the map!");
        }
    }

    //TODO this

    private void destroyOldCreateNew(int radius, int newRadius, UHCPlayer player) {
        Location location = arena.getCenterSpawn().toLocation(arenaWorld);
        double distance = location.distance(location);
        Block block = location.getBlock();
        int blockX = block.getX();
        int blockY = block.getY();
        int blockZ = block.getZ();
        if (block.getType() != Material.GLASS || block.getType() != Material.AIR) return;
        for (int x = blockX; x <= blockX + radius; x++) {
            for (int y = blockY; y <= blockY + radius; y++) {
                for (int z = blockZ; z <= blockZ + radius; z++) {
                    location.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(NetUHC.getInstance(), () -> {
            for (int x = blockX; x <= blockX + newRadius; x++) {
                for (int y = blockY; y <= blockY + newRadius; y++) {
                    for (int z = blockZ; z <= blockZ + newRadius; z++) {
                        location.getWorld().getBlockAt(x, y, z).setType(Material.GLASS);
                    }
                }
            }
        }, 4L);
        // most definately wrong - that x,y,z location is the block, not the radius
        if (player.getPlayer().getLocation().distance(location.getWorld().getBlockAt(blockX, blockY, blockZ).getLocation()) <= 0) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(NetUHC.getInstance(), () -> player.getPlayer().damage(1), 120, 4);
            player.sendMessage(ChatColor.RED + "You need to be at the center of the map!");
        } else {
            Bukkit.getScheduler().cancelAllTasks();
        }
    }

    /*
         Here we'll need to find out how to first:
           - Remove current area of blocks around the map (high intensity on the server)
           - Spawn another area around the map with the smaller sphere of blocks (also high intensity)
           - E.g. Get area around the map and make the glass blocks sphere which cover it smaller
     */
    private void shrinkMapAccordingly(UHCPlayer player) {
        Point centerSpawn = arena.getCenterSpawn();
        Location location = centerSpawn.toLocation(arenaWorld);
        double distance = location.distance(location);
        switch (gameWillEndCountdown.getSeconds()) {
            case 1200:
                destroyOldCreateNew(500, 300, player);
                break;
            case 900:
                destroyOldCreateNew(300, 250, player);
                break;
            case 600:
                destroyOldCreateNew(250, 150, player);
                break;
            case 300:
                destroyOldCreateNew(150, 150, player);
                break;
            default:
                break;
        }
    }

    /*
     Game implementation - Listeners
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (gameState == GameState.OVER || gameState == GameState.PREGAME) return;
        final UHCPlayer uhcPlayer = getSGPlayer(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            makePlayerSpectator(uhcPlayer);
            for (UHCPlayer player : spectators) {
                uhcPlayer.getPlayer().hidePlayer(player.getPlayer());
            }
        }, 4L);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player1 = event.getPlayer();
        if (spectators.contains(getSGPlayer(player1))) return;
        if (pendingSpectators.contains(getSGPlayer(player1))) return;
        UHCPlayer player = getSGPlayer(player1);
        tributeFallen(player);
        playerLeftServer(getSGPlayer(player1)); // fixed
    }

    @EventHandler
    public void onPlayerLeave(PlayerKickEvent event) {
        Player player1 = event.getPlayer();
        if (spectators.contains(getSGPlayer(player1))) return;
        if (pendingSpectators.contains(getSGPlayer(player1))) return;
        UHCPlayer player = getSGPlayer(player1);
        tributeFallen(player);
    }

    @EventHandler
    public void onPlayerMoveOutOfBounds(PlayerMoveEvent event) { //TODO THE LOGIC FOR SMALLER
        double xfrom = event.getFrom().getX();
        double yfrom = event.getFrom().getY();
        double zfrom = event.getFrom().getZ();
        double xto = event.getTo().getX();
        double yto = event.getTo().getY();
        double zto = event.getTo().getZ();
        if (!(xfrom == xto && yfrom == yto && zfrom == zto)) {
            outsideMapZone(getSGPlayer(event.getPlayer()), event.getTo());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameState != GameState.COUNTDOWN) return;
        UHCPlayer uhcPlayer = getSGPlayer(event.getPlayer());
        if (!players.contains(uhcPlayer)) return;
        Location to = event.getTo();
        Location from = event.getFrom();
        event.setTo(new Location(from.getWorld(), from.getX(), to.getY(), from.getZ(), to.getYaw(), to.getPitch()));
    }

    @SuppressWarnings({"ConstantConditions", "Duplicates"})
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCPlayer player = getSGPlayer(event.getEntity());
        for (int x = 0; x <= 20; x++) {
            if (!player.getPlayer().getName().equalsIgnoreCase("NoyHillel1")) continue;
            player.playSound(Sound.ENTITY_COW_HURT);
            player.sendMessage(ChatColor.RED + "ha u bad, ha you dead");
        }
        EntityDamageEvent lastDamageCause = event.getEntity().getLastDamageCause();
        EntityDamageEvent.DamageCause cause = lastDamageCause.getCause();
        switch (cause) {
            case CONTACT: break;
            case ENTITY_ATTACK: break;
            case PROJECTILE: break;
            case SUFFOCATION: break;
            case FALL: break;
            case FIRE: break;
            case FIRE_TICK: break;
            case MELTING: break;
            case LAVA: break;
            case DROWNING: break;
            case BLOCK_EXPLOSION: break;
            case ENTITY_EXPLOSION: break;
            case VOID: break;
            case LIGHTNING: break;
            case SUICIDE: break;
            case STARVATION: break;
            case POISON: break;
            case MAGIC: break;
            case WITHER: break;
            case FALLING_BLOCK: break;
            case THORNS: break;
            case CUSTOM: break;
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
        UHCPlayer killer = getSGPlayer(event.getEntity().getKiller());
        Integer newPoints = NetUHC.getRandom().nextInt(26);
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
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (gameState == GameState.COUNTDOWN || gameState == GameState.OVER) return;
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        UHCPlayer player = getSGPlayer((Player) event.getEntity());
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
        if (gameState == GameState.COUNTDOWN) event.setCancelled(true);
        UHCPlayer uhcPlayer = getSGPlayer(event.getPlayer());
        if (!players.contains(uhcPlayer)) event.setCancelled(true);
        ItemStack itemInHand = uhcPlayer.getPlayer().getItemInHand();
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
        if (gameState == GameState.COUNTDOWN) return;
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
        UHCPlayer uhcPlayer = getSGPlayer(event.getPlayer());
        boolean spectatorSent = isSpectating(uhcPlayer);
        String formatName = spectatorSent ? "chat.spectator-chat" : "chat.player-chat";
        event.setCancelled(true);
        if (event.getMessage().equalsIgnoreCase("help boost")) {
            if (gameState == GameState.GAMEPLAY) return;
            uhcPlayer.sendMessage(MessageManager.getFormat("formats.cant-use-boost", true));
            return;
        }
        String s = MessageManager.getFormat(formatName, false, new String[]{"<player>", uhcPlayer.getDisplayableName()}, new String[]{"<points>", uhcPlayer.getPoints().toString()}) + event.getMessage();
        getAllPlayersForChat().stream().filter(player -> gameState == GameState.OVER || (spectatorSent &&
                (spectators.contains(player) || player.getPlayer().isOp())) || !spectatorSent).forEach(player -> player.sendMessage(s));
        plugin.logInfoInColor(s);
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {
        if (gameState != GameState.GAMEPLAY) return;
        UHCPlayer uhcPlayer = this.getSGPlayer(event.getPlayer());
        Player player = uhcPlayer.getPlayer();
        if (UHCGame.isSpectating(uhcPlayer)) return;
        if (!this.players.contains(uhcPlayer)) return;
        if (event.getMessage().equalsIgnoreCase("help boost")) {
            if (uhcPlayer.getPoints() < 200) {
                player.sendMessage(ChatColor.RED + "You don't have enough points for this!");
                return;
            }
            try {
                NetUHC.getInstance().getCoolDown().testCooldown(player.getName(), 20L, TimeUnit.MINUTES);
            }
            catch (CooldownUnexpiredException e) {
                player.sendMessage(MessageManager.getFormat("formats.boost-cooldown", true, (String[][])new String[][]{{"<time>", String.valueOf(NetUHC.getInstance().getConfig().getLong("formats.boost-cooldown-time"))}}));
                return;
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10.0f, 10.0f);
            player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100, 0));
            player.getInventory().addItem(new ItemStack(Material.RAW_BEEF, 1));
            player.getInventory().addItem(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
            player.sendMessage(MessageManager.getFormat("formats.boost", new String[0][]));
            uhcPlayer.setPoints(uhcPlayer.getPoints() - 200);
        }
    }

    @EventHandler
    public void onHangingDestroyEvent(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (gameState == GameState.COUNTDOWN) return;
        if (gameState == GameState.OVER) event.setCancelled(true);
        if (!(event.getEntity() instanceof Player)) return;
        UHCPlayer uhcPlayer = getSGPlayer((Player) event.getEntity());
        if (!this.players.contains(uhcPlayer)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            UHCPlayer gDamager = getSGPlayer((Player) event.getDamager());
            if (isSpectating(gDamager)) event.setCancelled(true);
            if (!players.contains(gDamager)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final UHCPlayer uhcPlayer = getSGPlayer(event.getPlayer());
        if (!pendingSpectators.contains(uhcPlayer)) return;
        pendingSpectators.remove(uhcPlayer);
        event.setRespawnLocation(arena.getRandomPointSpawns().next().toLocation(arenaWorld));
        if (gameState == GameState.OVER) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> makePlayerSpectator(uhcPlayer), 1L);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && (gameState == GameState.COUNTDOWN  || gameState == GameState.OVER)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) { if (isSpectating(getSGPlayer(event.getPlayer()))) event.setCancelled(true); }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        if (gameState == GameState.OVER) event.setCancelled(true);
        if (isSpectating(getSGPlayer(event.getPlayer()))) event.setCancelled(true);
    }

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
            case MAP_SHRINK_COUNTDOWN:
                event.setCancelled(false);
                break;
            case MAP_SHRINK:
                event.setCancelled(false);
                break;
            case OVER:
                event.setCancelled(true);
        }
    }

    private void playerDied(UHCPlayer player, EntityDamageEvent.DamageCause reason) {
        if (gameState == GameState.OVER) throw new IllegalStateException("This state does not permit death processing!");
        tributeFallen(player);
        Integer deaths = player.getDeaths();
        player.setDeaths(deaths + 1);
        Integer newPoints = NetUHC.getRandom().nextInt(113);
        player.setPoints(player.getPoints() - newPoints);
        player.sendMessage(MessageManager.getFormat("formats.loss-points", true, new String[]{"<points>", String.valueOf(newPoints)}));
    }

    private void tributeFallen(UHCPlayer player) {
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
        if (gameState == GameState.GAMEPLAY) {
                updateState();
        }
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
        for (UHCPlayer uhcPlayer : getAllPlayers()) {
            //uhcPlayer.setScoreBoardSide(spectatorsFormat, spectatorsSize);
            //uhcPlayer.setScoreBoardSide(playersFormat, playersSize);
            //uhcPlayer.setScoreBoardSide(mutationFormat, mutationSize);
            //uhcPlayer.sendMessage(ChatColor.RED + "Welcome, you have joined as a spectator!");
        }
    }

    private void resetScoreboard() {
        getAllPlayers().forEach(UHCPlayer::resetScoreboard);
    }

    private GameCountdown gameWillEndCountdown;
    private GameCountdown deathmatchWillEndCountdown;
    private GameCountdown deathMatchCountdown;

    void updateState() {
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
                getAllPlayers().forEach(UHCPlayer::resetPlayer);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        refillChests();
                    } catch (GameException e) {
                        e.printStackTrace();
                    }
                    broadcast(MessageManager.getFormat("formats.chest-refill", true));
                }, plugin.getConfig().getInt("formats.chest-refill-time") * 20);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (UHCPlayer player : spectators) {
                        player.getPlayer().setAllowFlight(true);
                    }
                }, 20L);
                for (UHCPlayer player : this.getPlayers()) {
                    gameWillEndCountdown = new GameCountdown(new GameWillEnd(this, player), 1503);
                    gameWillEndCountdown.start();
                    break;
                }
            case GAMEPLAY:
                gameState = GameState.MAP_SHRINK_COUNTDOWN;
                if (gameWillEndCountdown.isRunning()) {
                    gameWillEndCountdown.cancel();
                    plugin.logInfoInColor(gameWillEndCountdown + " is canceling.");
                }
                GameCountdown preDeathMatchCountdown = new GameCountdown(new PreDeathmatchCountdownResponder(this), plugin.getConfig().getInt("countdowns.pre-teleport-deathmatch"));
                preDeathMatchCountdown.start();
                break;
            case MAP_SHRINK_COUNTDOWN:
                gameState = GameState.OVER;
                broadcast(MessageManager.getFormat("formats.deathmatch-start", true));
                broadcastSound(Sound.ENTITY_WITHER_SPAWN, 0.5F);
                deathmatchWillEndCountdown = new GameCountdown(new DeathMatchWillEnd(this), 240);
                deathmatchWillEndCountdown.start();
                //TODO SHRINK MAP
                break;
            case MAP_SHRINK:
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

    private void refillChests() throws GameException {
        SGTierUtil.setupPoints(this, arena.getTier2().getPoints(), "tier2.json");
        SGTierUtil.setupPoints(this, arena.getTier1().getPoints(), "tier1.json");
    }

    private void endGame() {
        gameState = GameState.OVER;
        if (this.getPlayers().size() != 1) {
            broadcast(ChatColor.RED + "There is no victor! Server closing, Type /hub to leave now!");
            broadcastSound(Sound.ENTITY_WITHER_SPAWN);
            Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 200L);
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            UHCPlayer player = getSGPlayer(p);
            Integer gamesPlayed = player.getTotalGames();
            player.setTotalGames(gamesPlayed + 1);
        }
        this.victor = (UHCPlayer) this.getPlayers().toArray()[0];
        Integer newWins = victor.getWins();
        victor.setWins(newWins + 1);
        broadcast(MessageManager.getFormat("formats.winner", new String[]{"<victor>", this.victor.getDisplayableName()}));
        getAllPlayers().forEach(UHCPlayer::resetPlayer);
        this.manager.gameEnded();
        victor.getPlayer().setAllowFlight(true);
        broadcastSound(Sound.ENTITY_WITHER_SPAWN);
        for (UHCPlayer uhcPlayer : getAllPlayersForChat()) {
            NetPlayer playerFromNetPlayer = uhcPlayer.getPlayerFromNetPlayer();
            NetEnderBar.setTextFor(playerFromNetPlayer, MessageManager.getFormat("formats.ender-winner", false, new String[]{"<victor>", victor.getDisplayableName()}));
        }
        if (victor != null) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                NetFireworkEffect.shootFireWorks(victor.getPlayerFromNetPlayer(), victor.getPlayer().getLocation());
                NetFireworkEffect.shootFireWorks(victor.getPlayerFromNetPlayer(), arena.getRandomPointSpawns().random().toLocation(arenaWorld));
            }, 20L, 40L);
        }
    }

    private void broadcastSound(Sound sound) {
        for (UHCPlayer uhcPlayer : getAllPlayers()) {
            uhcPlayer.playSound(sound);
        }
    }

    private void broadcastSound(Sound sound, Float pitch) {
        for (UHCPlayer uhcPlayer : getAllPlayers()) {
            uhcPlayer.playSound(sound, pitch);
        }
    }

    /* util methods */

    private Set<UHCPlayer> getAllPlayers() {
        Set<UHCPlayer> players = new HashSet<>();
        players.addAll(spectators);
        players.addAll(this.players);
        return players;
    }

    private Set<UHCPlayer> getAllPlayersForChat() {
        Set<UHCPlayer> players = new HashSet<>();
        players.addAll(spectators);
        players.addAll(pendingSpectators);
        players.addAll(this.players);
        return players;
    }

    private void hideFromAllPlayers(UHCPlayer player) {
        for (UHCPlayer uhcPlayer : getAllPlayers()) {
            uhcPlayer.getPlayer().hidePlayer(player.getPlayer());
        }
    }

    private void removeEnderBar() {
        for (UHCPlayer uhcPlayer : getAllPlayers()) {
            NetPlayer playerFromNetPlayer = uhcPlayer.getPlayerFromNetPlayer();
            NetEnderBar.remove(playerFromNetPlayer);
        }
    }

    @Value
    private static final class PregameCountdownResponder implements CountdownDelegate {
        private final UHCGame game;
        private static Integer[] secondsToBroadcast = {60, 45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {45, 30, 15, 10, 9, 8, 7, 6};
        private static Integer[] secondsToSoundHigher = {5, 4, 3, 2, 1};

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {}

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) {
                game.broadcast(MessageManager.getFormat("formats.game-countdown", true, new String[]{"<seconds>", secondsRemaining.toString()}));
            }
            if (secondsRemaining <= 60 && secondsRemaining >= 1) {
                for (UHCPlayer uhcPlayer : game.getAllPlayers()) {
                    NetPlayer playerFromNetPlayer = uhcPlayer.getPlayerFromNetPlayer();
                    NetEnderBar.setHealthPercent(playerFromNetPlayer, secondsRemaining.doubleValue() / 60);
                    NetEnderBar.setTextFor(playerFromNetPlayer, MessageManager.getFormat("enderbar.game-countdown-time", false, new String[]{"<time>", RandomUtils.formatTime(countdown.getSeconds() - countdown.getSecondsPassed())}));
                    uhcPlayer.getPlayer().setLevel(secondsRemaining);
                    uhcPlayer.getPlayer().setExp(secondsRemaining.floatValue() / 60);
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
        private final UHCGame game;
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
                for (UHCPlayer uhcPlayer : game.getAllPlayers()) {
                    NetPlayer playerFromNetPlayer = uhcPlayer.getPlayerFromNetPlayer();
                    NetEnderBar.setHealthPercent(playerFromNetPlayer, secondsRemaining.doubleValue() / 60);
                    NetEnderBar.setTextFor(playerFromNetPlayer, MessageManager.getFormat("enderbar.deathmatch-countdown-time", false, new String[]{"<time>", RandomUtils.formatTime(countdown.getSeconds() - countdown.getSecondsPassed())}));
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
    private static final class GameWillEnd implements CountdownDelegate {
        private final UHCGame game;
        private final UHCPlayer player;
        private static Integer[] secondsToBroadcast = {1500, 1200, 900, 600, 300, 60, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToSound = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private static Integer[] secondsToShrinkMap = {1200, 900, 600, 300};

        @Override
        public void countdownStarting(Integer maxSeconds, GameCountdown countdown) {
            game.broadcast(MessageManager.getFormat("formats.deathmatch-countdown-start", true));
        }

        @Override
        public void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown) {
            if (RandomUtils.contains(secondsRemaining, secondsToShrinkMap)) {
                game.outsideMapZone(player, player.getPlayer().getLocation());
            }
            if (RandomUtils.contains(secondsRemaining, secondsToBroadcast)) {
                game.broadcast(MessageManager.getFormat("formats.time-to-deathmatch-countdown", true, new String[]{"<seconds>", RandomUtils.formatTime(secondsRemaining)}));
            }
            if (RandomUtils.contains(secondsRemaining, secondsToSound))
                game.broadcastSound(Sound.valueOf(game.getPlugin().getConfig().getString("sounds.timer-sound")));
        }

        @Override
        public void countdownComplete(Integer maxSeconds, GameCountdown countdown) {
            gameState = GameState.MAP_SHRINK;
            game.updateState();
        }
    }

    @Value
    private static final class DeathMatchWillEnd implements CountdownDelegate {
        private final UHCGame game;
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
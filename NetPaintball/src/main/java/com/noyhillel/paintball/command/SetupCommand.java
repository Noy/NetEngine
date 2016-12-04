package com.noyhillel.paintball.command;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.game.arena.ArenaMeta;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.paintball.Paintball;
import com.noyhillel.paintball.game.arena.ArenaSetup;
import com.noyhillel.paintball.game.arena.SetupSession;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

@Permission("paintball.setup")
@CommandMeta(name = "setup", description = "The Setup Command", usage = "/setup")
public final class SetupCommand extends NetAbstractCommandHandler implements Listener {
    static final Map<NetPlayer, SetupSession> setupSessions = new HashMap<>();
    private Set<NetPlayer> playerNameSet = new HashSet<>();
    private Set<NetPlayer> playerAuthorSet = new HashSet<>();
    private Set<NetPlayer> playerSocialLinkSet = new HashSet<>();

    public static String name;
    public static List<String> authors;
    public static String socialLink;

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (!Paintball.getInstance().isSetupOnly()) throw new NewNetCommandException("The server needs to be in setup mode!", NewNetCommandException.ErrorType.SPECIAL);
        if (args.length == 0) throw new NewNetCommandException("Usage: /setup <arena|lobby>", NewNetCommandException.ErrorType.SPECIAL);
        SetupSession session;
        NetPlayer sgPlayer = NetPlayer.getPlayerFromPlayer(player);
        if (args[0].equalsIgnoreCase("arena")) {
            session = new ArenaSetup(sgPlayer, player.getWorld());
            setupSessions.put(sgPlayer, session);
        }
        else if (args[0].equalsIgnoreCase("arenaname")) {
            playerNameSet.add(sgPlayer);
            sgPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena name.");
            return;
        }
        else if (args[0].equalsIgnoreCase("arenaauthors")) {
            playerAuthorSet.add(sgPlayer);
            sgPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena Builder.");
            return;
        }
        else if (args[0].equalsIgnoreCase("arenasocial")) {
            playerSocialLinkSet.add(sgPlayer);
            sgPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena Social Link.");
            return;
        }
        else if (args[0].equalsIgnoreCase("finish")) {
            if (!setupSessions.containsKey(sgPlayer)) throw new NewNetCommandException("You're not currently setting up an arena!", NewNetCommandException.ErrorType.SPECIAL);
            SetupSession setupSession = setupSessions.get(sgPlayer);
            try {
                setupSession.commit();
            } catch (ArenaException e) {
                sgPlayer.sendMessage(ChatColor.RED + "Could not setup the arena! " + e.getMessage());
                return;
            } catch (Exception e) {
                sgPlayer.sendMessage(ChatColor.RED + "Internal server error!");
                e.printStackTrace();
                return;
            }
            HandlerList.unregisterAll(setupSession);
            sgPlayer.sendMessage(ChatColor.GREEN + "Your arena has been setup! But did you remember to set the spawn of the arena to the middle of the map?");
            sgPlayer.resetPlayer();
            sgPlayer.playSound(Sound.ENTITY_PLAYER_LEVELUP);
            setupSessions.remove(sgPlayer);
            return;
        }
        else {
            throw new NewNetCommandException("Use: /setup <arena|lobby|metatype>", NewNetCommandException.ErrorType.SPECIAL);
        }
        session.start();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (setupSessions.containsKey(player)) setupSessions.remove(player);
        if (playerNameSet.contains(player)) playerNameSet.remove(player);
        if (playerAuthorSet.contains(player)) playerAuthorSet.remove(player);
        if (playerSocialLinkSet.contains(player)) playerSocialLinkSet.remove(player);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!playerNameSet.contains(player)) return;
        name = event.getMessage();
        ArenaMeta arenaMeta = ArenaSetup.arenaMeta;
        arenaMeta.setName(name);
        player.sendMessage(ChatColor.YELLOW + "You have set the Map name to: " + name);
        playerNameSet.remove(player);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat1(AsyncPlayerChatEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!playerAuthorSet.contains(player)) return;
        authors = new ArrayList<>();
        String authorNames = event.getMessage();
        authors.add(authorNames);
        ArenaMeta arenaMeta = ArenaSetup.arenaMeta;
        arenaMeta.setAuthors(authors);
        player.sendMessage(ChatColor.YELLOW + "You have set the Map Author to: " + authorNames);
        playerAuthorSet.remove(player);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat2(AsyncPlayerChatEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!playerSocialLinkSet.contains(player)) return;
        socialLink = event.getMessage();
        ArenaMeta arenaMeta = ArenaSetup.arenaMeta;
        arenaMeta.setSocialLink(socialLink);
        player.sendMessage(ChatColor.YELLOW + "You have set the Map Link to: " + socialLink);
        playerSocialLinkSet.remove(player);
        event.setCancelled(true);
    }
}


package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.ArenaException;
import com.noyhillel.survivalgames.arena.ArenaMeta;
import com.noyhillel.survivalgames.arena.setup.ArenaSetup;
import com.noyhillel.survivalgames.arena.setup.LobbySetup;
import com.noyhillel.survivalgames.arena.setup.SetupSession;
import com.noyhillel.survivalgames.player.SGPlayer;
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

import static com.noyhillel.survivalgames.command.LinkChestsCommand.resolveGPlayer;

@Permission("survivalgames.setup")
@CommandMeta(name = "setup", description = "The Setup Command", usage = "/setup")
public final class SetupCommand extends NetAbstractCommandHandler implements Listener {
    static final Map<SGPlayer, SetupSession> setupSessions = new HashMap<>();
    private Set<SGPlayer> playerNameSet = new HashSet<>();
    private Set<SGPlayer> playerAuthorSet = new HashSet<>();
    private Set<SGPlayer> playerSocialLinkSet = new HashSet<>();

    public static String name;
    public static List<String> authors;
    public static String socialLink;

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (!SurvivalGames.getInstance().isSetupOnly()) throw new NewNetCommandException("The server needs to be in setup mode!", NewNetCommandException.ErrorType.Special);
        if (args.length == 0) throw new NewNetCommandException("Usage: /setup <arena|lobby>", NewNetCommandException.ErrorType.Special);
        SetupSession session;
        SGPlayer SGPlayer = resolveGPlayer(player);
        if (args[0].equalsIgnoreCase("arena")) {
            session = new ArenaSetup(SGPlayer, player.getWorld());
            setupSessions.put(SGPlayer, session);
        }
        else if (args[0].equalsIgnoreCase("lobby")) {
            session = new LobbySetup(SGPlayer, player.getWorld());
            setupSessions.put(SGPlayer, session);
        }
        else if (args[0].equalsIgnoreCase("metaname")) {
            playerNameSet.add(SGPlayer);
            SGPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena name.");
            return;
        }
        else if (args[0].equalsIgnoreCase("metaauthors")) {
            playerAuthorSet.add(SGPlayer);
            SGPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena Builder.");
            return;
        }
        else if (args[0].equalsIgnoreCase("metalink")) {
            playerSocialLinkSet.add(SGPlayer);
            SGPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena Social Link.");
            return;
        }
        else if (args[0].equalsIgnoreCase("done")) {
            if (!setupSessions.containsKey(SGPlayer)) throw new NewNetCommandException("You are not currently setting up an arena!", NewNetCommandException.ErrorType.Special);
            SetupSession setupSession = setupSessions.get(SGPlayer);
            try {
                setupSession.commit();
            } catch (ArenaException e) {
                SGPlayer.sendMessage(ChatColor.RED + "Failed to setup the arena! " + e.getMessage());
                return;
            } catch (Exception e) {
                SGPlayer.sendMessage(ChatColor.RED + "Internal server error!");
                e.printStackTrace();
                return;
            }
            HandlerList.unregisterAll(setupSession);
            SGPlayer.sendMessage(ChatColor.GREEN + "Your arena has been setup! But did you remember to set the spawn of the arena to the middle of the map?");
            SGPlayer.resetPlayer();
            SGPlayer.playSound(Sound.LEVEL_UP);
            setupSessions.remove(SGPlayer);
            return;
        }
        else {
            throw new NewNetCommandException("Use: /setup <arena|lobby|metatype>", NewNetCommandException.ErrorType.Special);
        }
        session.start();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        SGPlayer SGPlayer = resolveGPlayer(event.getPlayer());
        if (setupSessions.containsKey(SGPlayer)) setupSessions.remove(SGPlayer);
        if (playerNameSet.contains(SGPlayer)) playerNameSet.remove(SGPlayer);
        if (playerAuthorSet.contains(SGPlayer)) playerAuthorSet.remove(SGPlayer);
        if (playerSocialLinkSet.contains(SGPlayer)) playerSocialLinkSet.remove(SGPlayer);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        SGPlayer player = resolveGPlayer(p);
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
        Player p = event.getPlayer();
        SGPlayer player = resolveGPlayer(p);
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
        Player p = event.getPlayer();
        SGPlayer player = resolveGPlayer(p);
        if (!playerSocialLinkSet.contains(player)) return;
        socialLink = event.getMessage();
        ArenaMeta arenaMeta = ArenaSetup.arenaMeta;
        arenaMeta.setSocialLink(socialLink);
        player.sendMessage(ChatColor.YELLOW + "You have set the Map Link to: " + socialLink);
        playerSocialLinkSet.remove(player);
        event.setCancelled(true);
    }
}

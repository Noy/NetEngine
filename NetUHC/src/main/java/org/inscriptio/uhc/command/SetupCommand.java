package org.inscriptio.uhc.command;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.game.arena.ArenaMeta;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.arena.setup.ArenaSetup;
import org.inscriptio.uhc.arena.setup.LobbySetup;
import org.inscriptio.uhc.arena.setup.SetupSession;
import org.inscriptio.uhc.player.UHCPlayer;

import java.util.*;

import static org.inscriptio.uhc.command.LinkChestsCommand.resolveGPlayer;

@Permission("uhc.setup")
@CommandMeta(name = "setup", description = "The Setup Command", usage = "/setup")
public final class SetupCommand extends NetAbstractCommandHandler implements Listener {
    static final Map<UHCPlayer, SetupSession> setupSessions = new HashMap<>();
    private Set<UHCPlayer> playerNameSet = new HashSet<>();
    private Set<UHCPlayer> playerAuthorSet = new HashSet<>();
    private Set<UHCPlayer> playerSocialLinkSet = new HashSet<>();

    public static String name;
    public static List<String> authors;
    public static String socialLink;

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (!NetUHC.getInstance().isSetupOnly()) throw new NewNetCommandException("The server needs to be in setup mode!", NewNetCommandException.ErrorType.SPECIAL);
        if (args.length == 0) throw new NewNetCommandException("Usage: /setup <arena|lobby>", NewNetCommandException.ErrorType.SPECIAL);
        SetupSession session;
        UHCPlayer uhcPlayer = resolveGPlayer(player);
        if (args[0].equalsIgnoreCase("arena")) {
            session = new ArenaSetup(uhcPlayer, player.getWorld());
            setupSessions.put(uhcPlayer, session);
        }
        else if (args[0].equalsIgnoreCase("lobby")) {
            session = new LobbySetup(uhcPlayer, player.getWorld());
            setupSessions.put(uhcPlayer, session);
        }
        else if (args[0].equalsIgnoreCase("metaname")) {
            playerNameSet.add(uhcPlayer);
            uhcPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena name.");
            return;
        }
        else if (args[0].equalsIgnoreCase("metaauthors")) {
            playerAuthorSet.add(uhcPlayer);
            uhcPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena Builder.");
            return;
        }
        else if (args[0].equalsIgnoreCase("metalink")) {
            playerSocialLinkSet.add(uhcPlayer);
            uhcPlayer.sendMessage(ChatColor.YELLOW + "Please Type The Arena Social Link.");
            return;
        }
        else if (args[0].equalsIgnoreCase("done")) {
            if (!setupSessions.containsKey(uhcPlayer)) throw new NewNetCommandException("You are not currently setting up an arena!", NewNetCommandException.ErrorType.SPECIAL);
            SetupSession setupSession = setupSessions.get(uhcPlayer);
            try {
                setupSession.commit();
            } catch (ArenaException e) {
                uhcPlayer.sendMessage(ChatColor.RED + "Failed to setup the arena! " + e.getMessage());
                return;
            } catch (Exception e) {
                uhcPlayer.sendMessage(ChatColor.RED + "Internal server error!");
                e.printStackTrace();
                return;
            }
            HandlerList.unregisterAll(setupSession);
            uhcPlayer.sendMessage(ChatColor.GREEN + "Your arena has been setup! But did you remember to set the spawn of the arena to the middle of the map?");
            uhcPlayer.resetPlayer();
            uhcPlayer.playSound(Sound.ENTITY_PLAYER_LEVELUP);
            setupSessions.remove(uhcPlayer);
            return;
        }
        else {
            throw new NewNetCommandException("Use: /setup <arena|lobby|metatype>", NewNetCommandException.ErrorType.SPECIAL);
        }
        session.start();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        UHCPlayer uhcPlayer = resolveGPlayer(event.getPlayer());
        if (setupSessions.containsKey(uhcPlayer)) setupSessions.remove(uhcPlayer);
        if (playerNameSet.contains(uhcPlayer)) playerNameSet.remove(uhcPlayer);
        if (playerAuthorSet.contains(uhcPlayer)) playerAuthorSet.remove(uhcPlayer);
        if (playerSocialLinkSet.contains(uhcPlayer)) playerSocialLinkSet.remove(uhcPlayer);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        UHCPlayer player = resolveGPlayer(p);
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
        UHCPlayer player = resolveGPlayer(p);
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
        UHCPlayer player = resolveGPlayer(p);
        if (!playerSocialLinkSet.contains(player)) return;
        socialLink = event.getMessage();
        ArenaMeta arenaMeta = ArenaSetup.arenaMeta;
        arenaMeta.setSocialLink(socialLink);
        player.sendMessage(ChatColor.YELLOW + "You have set the Map Link to: " + socialLink);
        playerSocialLinkSet.remove(player);
        event.setCancelled(true);
    }
}

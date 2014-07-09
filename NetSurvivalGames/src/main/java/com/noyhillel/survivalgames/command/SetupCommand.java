package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.arena.ArenaException;
import com.noyhillel.survivalgames.arena.setup.ArenaSetup;
import com.noyhillel.survivalgames.arena.setup.LobbySetup;
import com.noyhillel.survivalgames.arena.setup.SetupSession;
import com.noyhillel.survivalgames.player.GPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

import static com.noyhillel.survivalgames.command.LinkChestsCommand.resolveGPlayer;

@Permission("survivalgames.setup")
@CommandMeta(name = "setup", description = "The Setup Command", usage = "/setup")
public final class SetupCommand extends NetAbstractCommandHandler implements Listener {
    static final Map<GPlayer, SetupSession> setupSessions = new HashMap<>();

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException("Usage: /setup <arena|lobby>", NewNetCommandException.ErrorType.Special);
        SetupSession session;
        GPlayer gPlayer = resolveGPlayer(player);
        if (args[0].equalsIgnoreCase("arena")) {
            session = new ArenaSetup(gPlayer, player.getWorld());
            setupSessions.put(gPlayer, session);
        }
        else if (args[0].equalsIgnoreCase("lobby")) {
            session = new LobbySetup(gPlayer, player.getWorld());
            setupSessions.put(gPlayer, session);
        }
        else if (args[0].equalsIgnoreCase("done")) {
            if (!setupSessions.containsKey(gPlayer)) throw new NewNetCommandException("You are not currently setting up an arena!", NewNetCommandException.ErrorType.Special);
            SetupSession setupSession = setupSessions.get(gPlayer);
            try {
                setupSession.commit();
            } catch (ArenaException e) {
                gPlayer.sendMessage(ChatColor.RED + "Failed to setup the arena! " + e.getMessage());
                return;
            } catch (Exception e) {
                gPlayer.sendMessage(ChatColor.RED + "Internal server error!");
                e.printStackTrace();
                return;
            }
            HandlerList.unregisterAll(setupSession);
            gPlayer.sendMessage(ChatColor.GREEN + "Your arena has been setup!");
            gPlayer.resetPlayer();
            gPlayer.playSound(Sound.LEVEL_UP);
            setupSessions.remove(gPlayer);
            return;
        }
        else {
            throw new NewNetCommandException("Use: /setup <arena|lobby>", NewNetCommandException.ErrorType.Special);
        }
        session.start();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        GPlayer gPlayer = resolveGPlayer(event.getPlayer());
        if (setupSessions.containsKey(gPlayer)) setupSessions.remove(gPlayer); //GET CANCER
    }
}

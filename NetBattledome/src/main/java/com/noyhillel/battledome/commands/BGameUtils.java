package com.noyhillel.battledome.commands;

import com.noyhillel.battledome.NetBD;
import com.noyhillel.battledome.MessageManager;
import com.noyhillel.battledome.exceptions.BattledomeException;
import com.noyhillel.battledome.game.BGame;
import com.noyhillel.battledome.game.BGameManager;
import com.noyhillel.battledome.game.Phase;
import com.noyhillel.networkengine.command.CommandHandler;
import com.noyhillel.networkengine.command.CommandStatus;
import com.noyhillel.networkengine.command.NetCommand;
import com.noyhillel.networkengine.command.NetCommandSenders;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Created by Noy on 6/23/2014.
 */
@SuppressWarnings("UnusedParameters")
public final class BGameUtils implements CommandHandler, Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (BGameManager.getBGameManager() == null && !event.getPlayer().isOp()) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(MessageManager.getFormat("formats.kick-not-op-no-singleton", false));
        }
    }

    @Override
    public void handleCommand(CommandStatus commandStatus, CommandSender commandSender, NetCommandSenders commandSenders) {
        NetBD.getInstance().handleCommand(commandStatus, commandSender, commandSenders);
    }

    @NetCommand(
            usage = "/setworld",
            senders = {NetCommandSenders.PLAYER},
            permission = "battledome.admin",
            name = "setworld",
            description = "This is the setworld command"
    )
    public CommandStatus setWorld(CommandSender sender, NetCommandSenders type, NetCommand meta, Command command, String[] args) {
        if (BGameManager.getBGameManager() != null) return CommandStatus.NULL;
        new BGameManager(((Player)sender).getWorld());
        sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "Set the world!");
        return CommandStatus.SUCCESS;
    }

    @NetCommand(
            usage = "/startgame",
            senders = {NetCommandSenders.PLAYER},
            permission = "battledome.start",
            name = "startgame",
            description = "This is the setworld command"
    )
    public CommandStatus startGame(CommandSender sender, NetCommandSenders type, NetCommand meta, Command command, String[] args) {
        BGameManager bGameManager = BGameManager.getBGameManager();
        if (bGameManager == null) return CommandStatus.NULL;
        try {
            bGameManager.startGame(((Player) sender).getLocation());
            ((Player) sender).getWorld().setTime(1L);
        } catch (BattledomeException e) {
            sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
            return CommandStatus.SUCCESS;
        }
        return CommandStatus.SUCCESS;
    }

    @NetCommand(
            usage = "/heal",
            senders = {NetCommandSenders.PLAYER},
            permission = "",
            name = "heal",
            description = "The heal command"
    )
    public CommandStatus heal(CommandSender sender, NetCommandSenders type, NetCommand meta, Command command, String[] args) {
        Player p = (Player) sender;
        if (BGameManager.getBGameManager().getGame() == null) return CommandStatus.NULL;
        if (BGameManager.getBGameManager().getGame().getPhase() == Phase.BATTLE) return CommandStatus.NULL;
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.sendMessage(MessageManager.getFormat("formats.heal"));
        return CommandStatus.SUCCESS;
    }

    @NetCommand(
            usage = "",
            senders = {NetCommandSenders.PLAYER},
            permission = "",
            name = "tp",
            description = "The TP Command"
    )
    public CommandStatus tp(CommandSender sender, NetCommandSenders type, NetCommand meta, Command command, String[] args) {
        Player p = (Player)sender;
        if (args.length < 1) return CommandStatus.FEW_ARGUMENTS;
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) return CommandStatus.NULL;
        BGame runningGame = BGameManager.getBGameManager().getGame();
        if (!p.isOp() && (runningGame.getPhase() == Phase.BATTLE || !runningGame.getTeamForPlayer(NetPlayer.getPlayerFromPlayer(p)).equals(runningGame.getTeamForPlayer(NetPlayer.getPlayerFromPlayer(target))))) return CommandStatus.PERMISSION;
        p.teleport(target.getLocation());
        p.sendMessage(MessageManager.getFormat("formats.teleport", true, new String[]{"<player>", target.getName()}));
        return CommandStatus.SUCCESS;
    }
}

package com.noyhillel.networkhub.commands;

import com.noyhillel.networkengine.command.*;
import com.noyhillel.networkhub.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 27/05/2014.
 */
public final class FlyCommand extends AbstractCommandHandler {


    @NetCommand(
            name = "fly",
            description = "The Fly Command",
            usage = "/flyspeed",
            permission = "hub.fly",
            senders = {NetCommandSenders.PLAYER}
    )
    public CommandStatus fly(CommandSender sender, NetCommandSenders senders, NetCommand meta, Command command, String[] args) {
        if (args.length > 1) return CommandStatus.MANY_ARGUMENTS;
        Player player = (Player) sender;
        if (args.length == 0) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.sendMessage(MessageManager.getFormats("formats.fly-on"));
                return CommandStatus.SUCCESS;
            } else {
                player.setAllowFlight(false);
                player.sendMessage(MessageManager.getFormats("formats.fly-off"));
                return CommandStatus.SUCCESS;
            }
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) return CommandStatus.NULL;
        if (!target.getAllowFlight()) {
            target.setAllowFlight(true);
            target.sendMessage(MessageManager.getFormats("formats.fly-on"));
            player.sendMessage(MessageManager.getFormat("formats.set-fly-on", true, new String[]{"<player>", target.getName()}));
            return CommandStatus.SUCCESS;
        } else {
            target.setAllowFlight(false);
            target.sendMessage(MessageManager.getFormats("formats.fly-off"));
            player.sendMessage(MessageManager.getFormat("formats.set-fly-off", true, new String[]{"<player>", target.getName()}));
            return CommandStatus.SUCCESS;
        }
    }

    @NetCommand(
            name = "flyspeed",
            description = "The Fly Speed Command",
            usage = "/flyspeed <speed>",
            permission = "hub.fly",
            senders = {NetCommandSenders.PLAYER}
    )
    public CommandStatus flyspeed(CommandSender sender, NetCommandSenders senders, NetCommand meta, Command command, String[] args) {
        if (args.length == 0) return CommandStatus.FEW_ARGUMENTS;
        try {
            Float flySpeed = Float.valueOf(args[0]) / 10;
            if (flySpeed > 1) return CommandStatus.NULL;
            Player p = (Player) sender;
            if (!p.getAllowFlight()) {
                p.sendMessage(MessageManager.getFormats("formats.not-flying"));
                return CommandStatus.SUCCESS;
            }
            p.setFlySpeed(flySpeed);
            Float value = flySpeed * 10;
            p.sendMessage(MessageManager.getFormat("formats.fly-speed", true, new String[]{"<flyspeed>", value.toString()}));
            return CommandStatus.SUCCESS;
        } catch (NumberFormatException e) {
            return CommandStatus.NULL;
        }
    }
}
package com.noyhillel.networkhub.items.warpitem;

import com.noyhillel.networkengine.command.CommandHandler;
import com.noyhillel.networkengine.command.CommandStatus;
import com.noyhillel.networkengine.command.NetCommand;
import com.noyhillel.networkengine.command.NetCommandSenders;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.NetHub;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Noy on 23/12/13.
 */
public final class WarpItemCommands implements CommandHandler {

    /**
     * Warp Item Commands class.
     * This will set a warp to wherever the Player is Standing
     * It will delete a warp from the config based on it's name.
     */

    @NetCommand(
            name = "setwarp",
            usage = "/setwarp",
            permission = "hub.setwarp",
            description = "The Set Warp Command",
            senders = {NetCommandSenders.PLAYER}
    )
    public CommandStatus setWarp(CommandSender sender, NetCommandSenders type, NetCommand meta, Command command, String[] args) {
        Player p = (Player) sender;
        if (args.length < 3) return CommandStatus.FEW_ARGUMENTS;
        Material m;
        try {
            m = Material.getMaterial(args[0].toUpperCase());
            if (m == null) return CommandStatus.NULL;
        } catch (Exception e) {
            p.sendMessage(e.getCause().toString());
            return CommandStatus.NULL;
        }
        String name = ChatColor.translateAlternateColorCodes('&', args[1]);
        ArrayList<String> lore = new ArrayList<>();
        for (Integer i = 2; i < args.length; i++) {
            lore.add(args[i]);
        }
        ConfigurationSection section = NetHub.getInstance().getConfig().createSection("hub.warps." + ChatColor.stripColor(name).toLowerCase());
        section.set("item", m.name());
        section.set("name", args[1]);
        section.set("lore", lore);
        ConfigurationSection location = section.createSection("location");
        location.set("world", p.getLocation().getWorld().getName());
        location.set("x", p.getLocation().getX());
        location.set("y", p.getLocation().getY());
        location.set("z", p.getLocation().getZ());
        location.set("yaw", p.getLocation().getYaw());
        location.set("pitch", p.getLocation().getPitch());
        section.set("location", location);
        NetHub.getInstance().getConfig().set("hub.warps." + ChatColor.stripColor(name).toLowerCase(), section);
        NetHub.getInstance().saveConfig();
        p.sendMessage(MessageManager.getFormats("formats.warp-set"));
        return CommandStatus.SUCCESS;
    }

    @NetCommand(
            name = "deletewarp",
            usage = "/deletewarp",
            permission = "hub.deletewarp",
            description = "The Delete Warp Command",
            senders = {NetCommandSenders.PLAYER}
    )
    public CommandStatus deleteWarp(CommandSender sender, NetCommandSenders type, NetCommand meta, Command command, String[] args) {
        Player p = (Player) sender;
        if (args.length == 0) return CommandStatus.FEW_ARGUMENTS;
        if (args.length > 1) return CommandStatus.MANY_ARGUMENTS;
        ConfigurationSection warp = NetHub.getInstance().getConfig().getConfigurationSection("hub.warps." + args[0]);
        if (warp == null)  return CommandStatus.NULL;
        NetHub.getInstance().getConfig().set("hub.warps." + args[0], null);
        NetHub.getInstance().saveConfig();
        p.sendMessage(MessageManager.getFormats("formats.delete-warp"));
        return CommandStatus.SUCCESS;
    }

    @Override
    public void handleCommand(CommandStatus status, CommandSender sender, NetCommandSenders senderType) {
        NetHub.getInstance().handleCommand(status, sender, senderType);
    }
}
package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.networkengine.command.AbstractCommandHandler;
import com.noyhillel.networkengine.command.CommandStatus;
import com.noyhillel.networkengine.command.NetCommand;
import com.noyhillel.networkengine.command.NetCommandSenders;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * Created by Armani on 22/01/2017.
 */
@SuppressWarnings("Duplicates")
public final class Spawn extends AbstractCommandHandler {


    @NetCommand(
            name = "spawn",
            permission = "netsentials.spawn",
            senders = NetCommandSenders.PLAYER,
            description = "The Spawn Command"
    )
    public CommandStatus spawn(CommandSender sender, NetCommandSenders senders, NetCommand meta, Command command, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            if (getLocation("spawn") != null) {
                player.teleport(getLocation("spawn"));
                player.sendMessage(MessageManager.getFormats("formats.tp-to-spawn"));
            } else {
                return CommandStatus.NULL;
            }
            return CommandStatus.SUCCESS;
        }
        if (args[0].equalsIgnoreCase("set")) {
            setLocation("spawn", player.getLocation());
            player.sendMessage(MessageManager.getFormats("formats.set-spawn"));
            return CommandStatus.SUCCESS;
        }
        if (args[0] != null) return CommandStatus.NULL;
        return CommandStatus.SUCCESS;
    }

    private static Location getLocation(String path) {
        ConfigurationSection configurationSection = NetSentials.getInstance().getConfig().getConfigurationSection(path);
        if (configurationSection != null) {
            return new Location(
                    NetSentials.getInstance().getServer().getWorld(configurationSection.getString("world")),
                    configurationSection.getDouble("x"),
                    configurationSection.getDouble("y"),
                    configurationSection.getDouble("z"),
                    (float) configurationSection.getDouble("yaw"),
                    (float) configurationSection.getDouble("pitch"));
        }
        return null;
    }

    private void setLocation(String path, Location location) {
        ConfigurationSection configurationSection = NetSentials.getInstance().getConfig().getConfigurationSection(path);
        configurationSection.set("world", location.getWorld().getName());
        configurationSection.set("x", location.getX());
        configurationSection.set("y", location.getY());
        configurationSection.set("z", location.getZ());
        configurationSection.set("pitch", location.getPitch());
        configurationSection.set("yaw", location.getYaw());
        try {
            NetSentials.getInstance().getConfig().save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

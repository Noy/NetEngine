package com.noyhillel.networkhub.commands;

import com.noyhillel.networkengine.command.*;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.NetHub;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 26/05/2014.
 */
public final class SpawnCommand extends AbstractCommandHandler {

    @NetCommand(
            name = "spawn",
            usage = "/spawn",
            permission = "hub.spawn",
            senders = {NetCommandSenders.PLAYER},
            description = "The Spawn Command, Use /spawn <set> to set that spawn."
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

    public static Location getLocation(String path) {
        ConfigurationSection configurationSection = NetHub.getInstance().getConfig().getConfigurationSection(path);
        if (configurationSection != null) {
            return new Location(
                    NetHub.getInstance().getServer().getWorld(configurationSection.getString("world")),
                    configurationSection.getDouble("x"),
                    configurationSection.getDouble("y"),
                    configurationSection.getDouble("z"),
                    (float) configurationSection.getDouble("yaw"),
                    (float) configurationSection.getDouble("pitch"));
        }
        return null;
    }

    private void setLocation(String path, Location location) {
        ConfigurationSection configurationSection = NetHub.getInstance().getConfig().getConfigurationSection(path);
        configurationSection.set("world", location.getWorld().getName());
        configurationSection.set("x", location.getX());
        configurationSection.set("y", location.getY());
        configurationSection.set("z", location.getZ());
        configurationSection.set("pitch", location.getPitch());
        configurationSection.set("yaw", location.getYaw());
    }
}

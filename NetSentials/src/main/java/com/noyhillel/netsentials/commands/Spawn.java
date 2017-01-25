package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.networkengine.command.AbstractCommandHandler;
import com.noyhillel.networkengine.command.CommandStatus;
import com.noyhillel.networkengine.command.NetCommand;
import com.noyhillel.networkengine.command.NetCommandSenders;
import org.bukkit.Bukkit;
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


    private static final ConfigurationSection CONFIGURATION_SECTION = NetSentials.getInstance().getConfig().getConfigurationSection("spawn");

    @NetCommand(
            name = "spawn",
            permission = "netsentials.spawn",
            senders = NetCommandSenders.PLAYER,
            description = "The Spawn Command",
            usage = "/spawn or /spawn set"
    )
    public CommandStatus spawn(CommandSender sender, NetCommandSenders senders, NetCommand meta, Command command, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            if (NetSentials.getInstance().getServer().getWorld(String.valueOf(CONFIGURATION_SECTION.getString("world"))) == null) {
                player.sendMessage(MessageManager.getFormats("formats.set-the-spawn"));
                return CommandStatus.HELP;
            }
            if (getLocation() != null) {
                player.teleport(getLocation());
                player.sendMessage(MessageManager.getFormats("formats.tp-to-spawn"));
            } else {
                return CommandStatus.NULL;
            }
            return CommandStatus.SUCCESS;
        }
        if (args[0].equalsIgnoreCase("set")) {
            setLocation(player.getLocation());
            player.sendMessage(MessageManager.getFormats("formats.set-spawn"));
            Bukkit.getScheduler().runTaskLater(NetSentials.getInstance(), () -> NetSentials.getInstance().reloadConfig(), 1000);
            return CommandStatus.SUCCESS;
        }
        if (args[0] != null) return CommandStatus.NULL;
        return CommandStatus.SUCCESS;
    }

    private static Location getLocation() {
        if (CONFIGURATION_SECTION != null) {
            return new Location(
                    NetSentials.getInstance().getServer().getWorld(CONFIGURATION_SECTION.getString("world")),
                    CONFIGURATION_SECTION.getDouble("x"),
                    CONFIGURATION_SECTION.getDouble("y"),
                    CONFIGURATION_SECTION.getDouble("z"),
                    (float) CONFIGURATION_SECTION.getDouble("yaw"),
                    (float) CONFIGURATION_SECTION.getDouble("pitch"));
        }
        return null;
    }

    private void setLocation(Location location) {
        CONFIGURATION_SECTION.set("world", location.getWorld().getName());
        CONFIGURATION_SECTION.set("x", location.getX());
        CONFIGURATION_SECTION.set("y", location.getY());
        CONFIGURATION_SECTION.set("z", location.getZ());
        CONFIGURATION_SECTION.set("pitch", location.getPitch());
        CONFIGURATION_SECTION.set("yaw", location.getYaw());
        try {
            NetSentials.getInstance().getConfig().save("spawn");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.noyhillel.networkengine.command;

import com.noyhillel.networkengine.util.NetPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Created by Noy on 6/24/2014.
 */
public abstract class AbstractCommandHandler implements CommandHandler {

    @Override
    public void handleCommand(CommandStatus commandStatus, CommandSender commandSender, NetCommandSenders commandSenders) {
        NetPlugin.getInstance().handleCommand(commandStatus, commandSender, commandSenders);
    }
}
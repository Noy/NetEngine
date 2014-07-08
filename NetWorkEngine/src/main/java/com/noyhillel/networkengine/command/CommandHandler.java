package com.noyhillel.networkengine.command;

import org.bukkit.command.CommandSender;

/**
 * Created by Noy on 25/05/2014.
 */
public interface CommandHandler {
    /**
     * Implement this interface in order to use the 'ACommand' annotation.
     * @param commandStatus The Status of the command
     * @param commandSender The CommandSender (Bukkit)
     * @param commandSenders Command Sender Type (Enumerator which we created).
     */
    void handleCommand(CommandStatus commandStatus, CommandSender commandSender, NetCommandSenders commandSenders);
}

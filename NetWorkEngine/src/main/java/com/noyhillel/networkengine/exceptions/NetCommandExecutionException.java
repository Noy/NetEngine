package com.noyhillel.networkengine.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by Noy on 02/05/2014.
 * @author Noy Hillel
 */
@EqualsAndHashCode(callSuper = false)
@Data
public final class NetCommandExecutionException extends NewNetCommandException {
    /**
     * Private field, The Arguments of the Command.
     */
    private final String[] args;
    /**
     * Private field, The Command being executed.
     */
    private final Command command;
    /**
     * Private field, The CommandSender executing the command.
     */
    private final CommandSender sender;

    /**
     * Creating our constructor
     * @param message Error Message.
     * @param errorType Error Type from our static enumerator we created.
     * @param args Arguments of Command
     * @param command The Command.
     * @param sender The Sender executing the Command
     */
    public NetCommandExecutionException(String message, ErrorType errorType, String[] args, Command command, CommandSender sender) {
        super(message, errorType);
        this.args = args;
        this.command = command;
        this.sender = sender;
    }
}

package com.noyhillel.survivalgames.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@EqualsAndHashCode(callSuper = false)
@Data
public final class CommandExecutionException extends CommandException {
    private final String[] args;
    private final Command command;
    private final CommandSender sender;
    public CommandExecutionException(String message, ErrorType errorType, String[] args, Command command, CommandSender sender) {
        super(message, errorType);
        this.args = args;
        this.command = command;
        this.sender = sender;
    }
}

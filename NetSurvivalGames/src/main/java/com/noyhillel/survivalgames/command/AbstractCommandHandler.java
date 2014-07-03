package com.noyhillel.survivalgames.command;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.player.GPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommandHandler implements CommandExecutor, TabCompleter, CommandErrorHandler {
    @Getter(AccessLevel.PROTECTED) private final String command;
    @Getter(AccessLevel.PROTECTED) private final SurvivalGames plugin;
    @Getter(AccessLevel.PROTECTED) private final List<CommandErrorHandler> commandErrorHandlers = new ArrayList<>();

    @Getter private final Permission permission;

    protected AbstractCommandHandler(String command, SurvivalGames plugin) throws CommandException {
        this.command = command;
        this.plugin = plugin;
        this.permission = this.getClass().getAnnotation(Permission.class);
        register();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        try {
            try {
                if (commandSender instanceof BlockCommandSender) {
                    executeBlock((BlockCommandSender) commandSender, strings);
                }
                if (commandSender instanceof Player) {
                    if (this.permission != null && !commandSender.hasPermission(this.permission.value())) throw new CommandException(this.permission.permissionErrorMessage(), CommandException.ErrorType.Permission);
                    executePlayer((Player) commandSender, strings);
                }
                if (commandSender instanceof ConsoleCommandSender) {
                    executeConsole((ConsoleCommandSender) commandSender, strings);
                }
            } catch (Throwable t) {
                if (!(t instanceof CommandException)) {
                    t.printStackTrace();
                    throw new CommandException(t.getClass().getSimpleName() + ": Exception encountered during command execution: " + t.getMessage(), CommandException.ErrorType.Special);
                } else {
                    throw t;
                }
            }
        } catch (CommandException ex) {
            CommandExecutionException commandExecutionException = new CommandExecutionException(ex.getMessage(), ex.getErrorType(), strings, command, commandSender);
            if (commandErrorHandlers.size() >= 1) {
                for (CommandErrorHandler commandErrorHandler : commandErrorHandlers) {
                    commandErrorHandler.handleCommandError(commandExecutionException);
                }
            } else {
                this.handleCommandError(commandExecutionException);
            }
        }
        return true;
    }

    @SuppressWarnings("UnusedParameters")
    protected void executeConsole(ConsoleCommandSender sender, String[] args) throws CommandException {
        throw new CommandException("The console cannot execute this command!", CommandException.ErrorType.Special);
    }

    @SuppressWarnings("UnusedParameters")
    protected void executePlayer(Player sender, String[] args) throws CommandException {
        throw new CommandException("You cannot execute this command!", CommandException.ErrorType.Special);
    }

    @SuppressWarnings("UnusedParameters")
    protected void executeBlock(BlockCommandSender sender, String[] args) throws CommandException {
        throw new CommandException("A block cannot execute this command!", CommandException.ErrorType.Special);
    }

    @SuppressWarnings("UnusedParameters")
    protected List<String> completeArgs(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return completeArgs(commandSender, strings);
    }

    private void register() throws CommandException {
        PluginCommand command1 = plugin.getCommand(command);
        if (command1 == null) throw new CommandException("Could not register command, not in plugin.yml!", CommandException.ErrorType.Special);
        command1.setExecutor(this);
    }

    @Override
    public void handleCommandError(CommandExecutionException ex) {
        ex.getSender().sendMessage(ChatColor.RED + ex.getMessage());
    }

    protected final String combineArgs(String[] args, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(delimiter);
        }
        builder.setLength(builder.length()-1);
        builder.trimToSize();
        return builder.toString();
    }

    protected final String combineArgs(String[] args) {
        return combineArgs(args, " ");
    }

    protected final GPlayer resolveGPlayer(Player player) {
        return plugin.getGPlayerManager().getOnlinePlayer(player);
    }
}

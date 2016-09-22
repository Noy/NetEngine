package com.noyhillel.networkengine.newcommand;

import com.noyhillel.networkengine.exceptions.NetCommandExecutionException;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.util.NetPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Abstract 'SubCommand' class, much easier to control commands.
 * @author Noy Hillel
 */
public abstract class NetAbstractCommandHandler implements CommandExecutor, TabCompleter, CommandErrorHandler {

    /**
     * Private field, the Main Class instance.
     */
    @Getter(AccessLevel.PROTECTED) private final NetPlugin plugin = NetPlugin.getInstance();

    /**
     * Private field, the List of command error handlers, if there are any, iterate through them and send the CommandSender a message
     */
    @Getter(AccessLevel.PROTECTED) private final Set<CommandErrorHandler> commandErrorHandlers = new HashSet<>();

    /**
     * Private field, Our @PERMISSION annotation.
     */
    @Getter private final Permission permission = getClass().getAnnotation(Permission.class);

    /**
     * Private field, Our @CommandMeta annotation.
     */
    @Getter private final CommandMeta meta = getClass().getAnnotation(CommandMeta.class);

    /**
     * Creating our constructor.
     */
    @SneakyThrows
    public NetAbstractCommandHandler() {
        register();
    }

    /**
     * The onCommand method which is overridden from the 'CommandExecutor' interface.
     * @param commandSender The sender which is executing the command.
     * @param command The command being executed.
     * @param label Any aliases of the command
     * @param args Any arguments of the command.
     * @return Overridden onCommand method
     */
    @Override
    public final boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // Creating our try block, trying to execute our next try block
        try {
            // Creating our try block, checking if the CommandSender's are Players, The Console or a CommandBlock.
            try {
                // Checking if the thing whom executed the command is a CommandBlock.
                if (commandSender instanceof BlockCommandSender) {
                    // If so, execute the method which we created.
                    blockCommand((BlockCommandSender) commandSender, args);
                }
                // Checking if the thing whom executed the command is a Player
                if (commandSender instanceof Player) {
                    // Checking if the Player has permission from our @PERMISSION annotation which we created, if not, throw a 'NewCommandException' which takes 2 parameters: String (PERMISSION message, ErrorType)
                    if (permission != null && !commandSender.hasPermission(permission.value())) throw new NewNetCommandException(permission.permissionErrorMessage(), NewNetCommandException.ErrorType.PERMISSION);
                    // If all goes well, execute the method which we created.
                    playerCommand((Player) commandSender, args);
                }
                // Checking if the thing whom executed the command is the Console
                if (commandSender instanceof ConsoleCommandSender) {
                    // If so, execute the method which we created.
                    consoleCommand((ConsoleCommandSender) commandSender, args);
                }
                // Was there an exception?
            } catch (Throwable t) {
                // If the throwable exception was an instance of our 'NewCommandException' class:
                if (!(t instanceof NewNetCommandException)) {
                    // Print out the Stack Trace.
                    t.printStackTrace();
                    // Throw a NewCommandException.
                    throw new NewNetCommandException(t.getClass().getSimpleName() + ": Exception encountered during command execution: " + t.getMessage(), NewNetCommandException.ErrorType.SPECIAL);
                    // If not,
                } else {
                    // Throw a Throwable Exception.
                    throw t;
                }
            }
            // Was there an exception from this try block?
        } catch (NewNetCommandException ex) {
            // Creating a local variable of our CommandExecutionException which takes 5 parameters: A String, ErrorType, List<String>, Command and the CommandSender.
            NetCommandExecutionException commandExecutionException = new NetCommandExecutionException(ex.getMessage(), ex.getErrorType(), args, command, commandSender);
            // If the CommandErrorHandlers are less than or equal to 1,
            if (commandErrorHandlers.size() >= 1) {
                // Iterate through all CommandErrorHandlers.
                for (CommandErrorHandler commandErrorHandler : commandErrorHandlers) {
                    // Handle the error.
                    commandErrorHandler.handleCommandError(commandExecutionException);
                    // Add our error to our HashSet
                    commandErrorHandlers.add(commandErrorHandler);
                }
            } else {
                // If not, use our 'handleCommandError' method from our CommandErrorHandler interface.
                handleCommandError(commandExecutionException);
            }
        }
        return true;
    }

    /**
     * Creating our 'executeConsole' method.
     * @param sender Console
     * @param args Command Arguments
     * @throws com.noyhillel.networkengine.exceptions.NewNetCommandException
     */
    @SuppressWarnings("UnusedParameters")
    protected void consoleCommand(ConsoleCommandSender sender, String[] args) throws NewNetCommandException {
        // If the Console did not execute this command, throw this exception.
        throw new NewNetCommandException("The console cannot execute this command!", NewNetCommandException.ErrorType.SPECIAL);
    }

    /**
     * Creating our 'executePlayer' method.
     * @param sender Player
     * @param args Command Arguments
     * @throws com.noyhillel.networkengine.exceptions.NewNetCommandException
     */
    @SuppressWarnings("UnusedParameters")
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        // If the Player did not execute this command, throw this exception.
        throw new NewNetCommandException("You cannot execute this command!", NewNetCommandException.ErrorType.SPECIAL);
    }

    /**
     * Creating our 'executeBlock' method.
     * @param sender CommandBlock.
     * @param args Command Arguments
     * @throws com.noyhillel.networkengine.exceptions.NewNetCommandException
     */
    @SuppressWarnings({"UnusedParameters"})
    protected void blockCommand(BlockCommandSender sender, String[] args) throws NewNetCommandException {
        // If the CommandBlock did not execute this command, throw this exception.
        throw new NewNetCommandException("A block cannot execute this command!", NewNetCommandException.ErrorType.SPECIAL);
    }

    /**
     * Alias of TabComplete
     * @param sender The sender which is executing the command.
     * @param args The Command Arguments.
     * @return The Complete Args.
     */
    @SuppressWarnings("UnusedParameters")
    protected List<String> completeArgs(CommandSender sender, String[] args) {
        // Returns an ArrayList of Strings.
        return new ArrayList<>();
    }

    /**
     * The 'onTabComplete' method which is overridden from the 'TabCompleter' interface.
     * @param commandSender The sender which is executing the command.
     * @param command The command being executed.
     * @param label Any aliases of the command
     * @param args The Command Arguments.
     * @return Tab Complete Method
     */
    @Override
    public final List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        // Returning our 'completeArgs' method which we created, Which takes 2 parameters: CommandSender and a List<String>.
        return completeArgs(commandSender, args);
    }

    /**
     * The register() method. This is to register the command.
     * @throws com.noyhillel.networkengine.exceptions.NewNetCommandException
     */
    private void register() throws NewNetCommandException {
        // Creating our local variable.
        PluginCommand command1 = plugin.getCommand(meta.name());
        // If the Command which we create in all the sub-classes are not registered in the plugin.yml, print it out in the console.
        if (command1 == null) throw new NewNetCommandException("Could not register command, not in plugin.yml.", NewNetCommandException.ErrorType.SPECIAL);
        // Set the executor as this class.
        command1.setExecutor(this);
        // Setting the description to our description method.
        command1.setDescription(meta.description());
        // Setting the usage to our usage method.
        command1.setUsage(meta.usage());
        // Set the executor as this class.
    }

    /**
     * Overridden method from our 'CommandErrorHandler'
     * @param ex CommandExecutionException class which we created.
     */
    @Override
    public final void handleCommandError(NetCommandExecutionException ex) {
        // If there is an error, send the error message in red.
        ex.getSender().sendMessage(ChatColor.RED + ex.getMessage());
    }

    /**
     * This is so there are spaces in the command arguments
     * @param args The Arguments.
     * @param delimiter Separates the Strings, in our case, a space.
     * @return Combination of Arguments.
     */
    protected String combineArgs(String[] args, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(delimiter);
        }
        builder.setLength(builder.length()-1);
        builder.trimToSize();
        return builder.toString();
    }

    /**
     * Alias for the 'combineArgs' method
     * @param args The Arguments.
     * @return The combineArgs Alias method.
     */
    protected final String combineArgs(String[] args) {
        return combineArgs(args, " ");
    }
}
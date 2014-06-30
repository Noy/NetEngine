package com.noyhillel.networkengine.command;

import com.noyhillel.networkengine.exceptions.NetCommandException;
import com.noyhillel.networkengine.util.NetPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Noy on 25/05/2014.
 */
public final class CommandStructure implements CommandExecutor, TabCompleter {

    /**
     * Creating our constructor.
     * @param plugin The abstract Plugin class.
     */
    public CommandStructure(NetPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Private field, representing the:
     * @see com.noyhillel.networkengine.util.NetPlugin
     */
    private NetPlugin plugin = NetPlugin.getInstance();

    /**
     * Associates commands to their handlers.
     */
    private Map<Command, CommandHandler> handlers = new HashMap<>();

    /**
     * Associates commands to their methods.
     */
    private Map<Command, Method> methods = new HashMap<>();

    /**
     * Associates commands with the Data.
     * @see NetCommand
     */
    private Map<Command, NetCommand> commandMetaAnnotationMap = new HashMap<>();

    /**
     * This is an organised way to store the order of arguments.
     */
    private static final Class[] CLASSES = {CommandSender.class, NetCommandSenders.class, NetCommand.class, Command.class, String[].class};

    /**
     * Checks if a class is valid for commands.
     * @param commandHandler Class to scan.
     * @param plugin A copy of the 'APlugin' class
     */
    private void checkClassForCommands(CommandHandler commandHandler, NetPlugin plugin) {
        // Creating our variable
        Method[] methods1 = commandHandler.getClass().getMethods();
        //Loop through all methods.
        for (Method method : methods1) {
            //This gets the annotation.
            NetCommand annotation = method.getAnnotation(NetCommand.class);
            if (annotation == null) continue; // If the annotation is null, continue on.
            if (method.getReturnType() != CommandStatus.class) continue; // If the Return type is not equal to any of the return type in our 'CommandStatus' class, continue on.
            if (method.getParameterTypes().length != CommandStructure.CLASSES.length) continue; // Checking the argument length
            if (!Arrays.equals(method.getParameterTypes(), CommandStructure.CLASSES)) continue; // Checking the type.
            PluginCommand cmd = plugin.getCommand(annotation.name()); // Command from the annotation 'name' method.
            if (cmd == null) continue; // If there is no command, continue on.
            cmd.setExecutor(this); // Sets the executor to this
            cmd.setUsage(annotation.usage()); // Sets the 'Usage message' to the annotations' 'usage' method.
            cmd.setDescription(annotation.description());
            // Now, let's store our values.
            this.handlers.put(cmd, commandHandler);
            this.methods.put(cmd, method);
            this.commandMetaAnnotationMap.put(cmd, annotation);
        }
    }

    /**
     * Registers the handler
     * @param handler The command handler that needs to be registered.
     */
    public void registerHandler(CommandHandler handler) {
        this.checkClassForCommands(handler, this.plugin);
    }

    /**
     * Un-register a handler
     * @param handler The command handler that needs to be unregistered.
     */
    public void unregisterHandler(CommandHandler handler) {
        ArrayList<Command> commands = new ArrayList<>();
        for (Command cmd : this.handlers.keySet()) {
            CommandHandler handler1 = this.handlers.get(cmd);
            if (handler1.equals(handler1)) commands.add(cmd);
        }
        for (Command cmd : commands) {
            methods.remove(cmd);
            handlers.remove(cmd);
            commandMetaAnnotationMap.remove(cmd);
        }
    }

    /**
     * onCommand method which is overridden from the 'CommandExecutor' interface
     * @param sender The Command Sender.
     * @param command The command.
     * @param label Aliases.
     * @param args Arguments.
     * @return onCommand method.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Creating our try block
        try {
            //Gets the handler/annotation/method, and validates
            CommandHandler handler = getHandler(command);
            if (handler == null) {
                sender.sendMessage(ChatColor.RED + "That Command was not registered somewhere, check back!");
                return true;
            }
            Method method = getMethod(command);
            NetCommand annotation = getAnnotation(command);
            //Gets the command sender type (enum)
            NetCommandSenders type = getType(sender);
            Boolean isValid = false; // Checks if the command sender is valid.
            for (NetCommandSenders sender1 : annotation.senders()) {
                if (sender1 == type) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                handler.handleCommand(CommandStatus.NULL, sender, type);
                return true;
            }
            if (!sender.hasPermission(annotation.permission())) {
                handler.handleCommand(CommandStatus.PERMISSION, sender, type);
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
            Object object = method.invoke(handler, sender, type, annotation, command, args);
            // Validates.
            if (!(object instanceof CommandStatus)) throw new NetCommandException("The method did not return a status!");
            CommandStatus status = (CommandStatus) object;
            switch (status) { // Switching through most of the possible outcomes
                case MANY_ARGUMENTS: // If Sender provides too many arguments, let them know.
                    sender.sendMessage(ChatColor.RED + "You have provided too many arguments!");
                    break; // Stop checking.
                case FEW_ARGUMENTS: // If Sender provides too few arguments, let them know.
                    sender.sendMessage(ChatColor.RED + "You have provided too few arguments!");
                    break;
                case HELP: // If the Sender provides an argument which returns our help center, help them.
                    sender.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Help" + ChatColor.DARK_GREEN + "] " + ChatColor.LIGHT_PURPLE + " >> " + ChatColor.BOLD + annotation.usage());
                    break;
                case NULL: // If the Sender provides an argument which is null, let them know.
                    sender.sendMessage(ChatColor.RED + "An argument you have provided was null!");
                    break;
            }
            handler.handleCommand(status, sender, type);
            // If there is an exception.
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            //Handles any thrown exceptions
            sender.sendMessage(ChatColor.RED + "An error occurred when executing this command. Check console!");
            StackTraceElement s = t.getStackTrace()[0];
            // Prints out an easy message for us to see.
            sender.sendMessage(ChatColor.RED + "Error at: " + ChatColor.YELLOW + s.getClassName() + ChatColor.DARK_BLUE + ":" + ChatColor.YELLOW + s.getLineNumber());
            t.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
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
     * Tab complete method overridden from the 'TabCompleter' interface.
     * @param sender Command sender
     * @param command The command.
     * @param label Label
     * @param args Args
     * @return tabComplete method.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Returning our 'completeArgs' method which we created, Which takes 2 parameters: CommandSender and a List<String>.
        return completeArgs(sender, args);
    }

    /**
     * Validating our enum
     * @param sender Command sender
     * @return getType method.
     */
    private NetCommandSenders getType(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return NetCommandSenders.CONSOLE; // If it's the console.
        if (sender instanceof BlockCommandSender) return NetCommandSenders.BLOCK; // If it's a CommandBlock
        if (sender instanceof Player) return NetCommandSenders.PLAYER; // If it's a Player.
        return null;
        // Obviously...
    }

    /**
     * Gets the handler to the command.
     * @param command The command.
     * @return The getHandler method.
     */
    public CommandHandler getHandler(Command command) {
        return handlers.get(command);
    }

    /**
     * Gets the method.
     * @param command The command.
     * @return The getMethod method.
     */
    public Method getMethod(Command command) {
        return methods.get(command);
    }

    /**
     * Gets the annotation.
     * @param command The command.
     * @return The annotation.
     */
    public NetCommand getAnnotation(Command command) {
        return commandMetaAnnotationMap.get(command);
    }

}

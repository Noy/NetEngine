package com.noyhillel.networkengine.command;

/**
 * Command Statuses
 * Created by Noy on 25/05/2014.
 */
public enum CommandStatus {
    /**
     * Successfully executed the command.
     */
    SUCCESS,
    /**
     * No permission to use the command.
     */
    PERMISSION,
    /**
     * Too few arguments were provided in order to execute the command.
     */
    FEW_ARGUMENTS,
    /**
     * Too many arguments were provided.
     */
    MANY_ARGUMENTS,
    /**
     * Gives back the help information we created here:
     * @see com.noyhillel.networkengine.command.CommandStructure
     */
    HELP,
    /**
     * Argument the sender provided was null.
     */
    NULL
}

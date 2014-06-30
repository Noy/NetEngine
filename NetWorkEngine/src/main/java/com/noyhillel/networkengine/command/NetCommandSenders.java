package com.noyhillel.networkengine.command;

/**
 * Different CommandSenders
 * Created by Noy on 25/05/2014.
 */
public enum NetCommandSenders {
    /**
     * Console Sender, this is going to be representing the Console executing a command.
     */
    CONSOLE,
    /**
     * Player Sender, this is going to be representing a Player executing a command.
     */
    PLAYER,
    /**
     * CommandBlock sender, this is going to be representing a CommandBlock executing a command.
     */
    BLOCK
}
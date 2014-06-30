package com.noyhillel.networkengine.newcommand;

import com.noyhillel.networkengine.exceptions.NetCommandExecutionException;

/**
 * @author Noy Hillel
 * The CommandErrorHandler interface. How will the Command Error be handled?
 */
public interface CommandErrorHandler {
    /**
     * The way to Handle Errors, will be overridden in any implementation.
     * @param ex CommandExecutionException exception.
     */
    void handleCommandError(NetCommandExecutionException ex);
}

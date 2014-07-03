package com.noyhillel.survivalgames.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Command Exception
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class CommandException extends Exception {
    private final String message;
    private final ErrorType errorType;
    public static enum ErrorType {
        Special,
        Permission,
        FewArguments,
        ManyArguments
    }
}

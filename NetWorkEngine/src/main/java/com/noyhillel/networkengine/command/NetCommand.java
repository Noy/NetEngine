package com.noyhillel.networkengine.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * This is the command annotation. They belong over methods which execute commands.
 * @author NoyHillel1
 */
public @interface NetCommand {
    /**
     * The name of the command
     * @return Name of the command
     */
    String name();
    /**
     * The usage of the command.
     * @return This is the help text for the command, should be handled by the plugin.
     */
    String usage() default "/<command>";
    /**
     * PERMISSION to use command.
     * @return PERMISSION to use this command.
     */
    String permission();

    /**
     * The Sender whom can execute the command(s).
     * @return The senders.
     */
    NetCommandSenders[] senders();

    /**
     * The Commands' description.
     * @return The description.
     */
    String description();
}
package com.noyhillel.networkengine.newcommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Noy on 6/24/2014.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandMeta {

    /**
     * This would be the actual command that the player types
     * @return the command name
     */
    String name();

    /**
     * This would be the description of the command.
     * @return the description of the command.
     */
    String description();

    /**
     * This would be the usage of the command.
     * @return the usage of the command.
     */
    String usage();
}

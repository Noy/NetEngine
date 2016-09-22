package com.noyhillel.networkengine.newcommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * This is the permission annotation. Put it above your class name which extends the 'SubCommand' class.
 */
public @interface Permission {

    /**
     * This represents the permission node which is going to be created.
     * @return PERMISSION Value
     */
    String value();

    /**
     * The permission message. The message a player would get when they don't have permission while executing the command
     * @return PERMISSION Error Message.
     */
    String permissionErrorMessage() default "You do not have permission to use this command!";
}
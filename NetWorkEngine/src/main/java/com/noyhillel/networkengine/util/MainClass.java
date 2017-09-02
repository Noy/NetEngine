package com.noyhillel.networkengine.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Noy on 6/24/2014.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MainClass {

    /**
     * This is the 'name' of the plugin, it IS needed in the plugin.yml, however, this one overrides the one in the plugin.yml
     * @return the name of the plugin
     */
    String name();

    /**
     * This is the description of the plugin, it is NOT needed in the plugin.yml.
     * @return the description
     */
    String description();

    /**
     * These are the authors of the plugin, it is NOT needed in the plugin.yml.
     * @return the authors
     */
    String[] authors() default {"NoyHillel1"};

    /**
     * This is the website of the plugin creator, it is NOT needed in the plugin.yml.
     * @return the website
     */
    String website() default "https://noy.sh/";
}

package com.noyhillel.networkengine.game.arena;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Noy on 30/05/2014.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ArenaField {
    public String key();
    public boolean loop() default true;
    public String longName();
    public PointType type();
    public static enum PointType {
        Block,
        Player
    }
}

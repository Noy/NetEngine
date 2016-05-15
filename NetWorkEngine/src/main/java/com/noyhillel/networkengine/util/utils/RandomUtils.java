package com.noyhillel.networkengine.util.utils;

import lombok.Getter;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by Noy on 30/05/2014.
 */
public final class RandomUtils {

    @Getter private static Random random = new Random();

    public static String getRandomString(Integer length) {
        return (new BigInteger(130, random).toString(length));
    }

    public static <T> Integer indexOf(T object, T[] array) {
        for (int x = 0; x < array.length; x++) {
            if (object.equals(array[x])) return x;
        }
        throw new IndexOutOfBoundsException("Could not find object in array!");
    }

    public static String formatTime(Integer seconds) {
        Integer hours = seconds / 3600;
        Integer remainder = seconds % 3600;
        Integer mins = remainder / 60;
        Integer secs = remainder % 60;
        if (hours > 0) {
            return hours + ":" + mins + ":" + secs;
        }
        else if (mins > 0) {
            if (secs < 10) {
                return mins + ":0" + secs + " minutes";
            }
            else return mins + ":" + secs + " minutes";
        } else {
            return secs.toString() + " seconds";
        }
    }

    public static <T> boolean contains(T object, T[] array) {
        for (T t : array) {
            if (t.equals(object)) return true;
        }
        return false;
    }

    public static <T> boolean contains(T[] ts, T t) {
        if (t == null || ts == null) return false;
        for (T t1 : ts) {
            if (t1 == null) continue;
            if (t1.equals(t)) return true;
        }
        return false;
    }
}

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

    public static <T> boolean contains(T object, T[] array) {
        for (T t : array) {
            if (t.equals(object)) return true;
        }
        return false;
    }
}

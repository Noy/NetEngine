package com.noyhillel.networkengine.util;

import java.math.BigInteger;

/**
 * Created by Noy on 30/05/2014.
 */
public final class RandomUtils {
    public static String getRandomString(Integer length) {
        return (new BigInteger(130, NetPlugin.getRandom()).toString(length));
    }
}

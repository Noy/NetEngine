package com.noyhillel.networkengine.util.utils;

import com.noyhillel.networkengine.exceptions.CooldownUnexpiredException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Joey on 29/05/2014.
 */
public final class NetCoolDown {

    private final Map<String, Date> cooldownMilliseconds = new HashMap<>();

    public void testCooldown(String key, Long time, TimeUnit unit, Boolean reset) throws CooldownUnexpiredException {
        Date lastFiredDate = cooldownMilliseconds.get(key);
        Date currentDate = new Date();
        if (lastFiredDate == null) {
            cooldownMilliseconds.put(key, currentDate);
            return;
        }
        long millisecondsPassed = currentDate.getTime() - lastFiredDate.getTime();
        long milliseconds = unit.toMillis(time);
        if (milliseconds >= millisecondsPassed) {
            if (reset) cooldownMilliseconds.put(key, currentDate);
            throw new CooldownUnexpiredException(unit.toMillis(milliseconds - millisecondsPassed), unit);
        }
        cooldownMilliseconds.put(key, currentDate);
    }

    public void testCooldown(String key, Long time, TimeUnit unit) throws CooldownUnexpiredException {
        testCooldown(key, time, unit, false);
    }

    public void testCooldown(String key, Long seconds) throws CooldownUnexpiredException {
        testCooldown(key, seconds, TimeUnit.SECONDS);
    }
}

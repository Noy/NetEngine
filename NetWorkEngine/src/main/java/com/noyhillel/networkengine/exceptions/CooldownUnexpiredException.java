package com.noyhillel.networkengine.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.concurrent.TimeUnit;

/**
 * Created by Noy on 29/05/2014.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public final class CooldownUnexpiredException extends Exception {
    private final Long timeRemaining;
    private final TimeUnit timeUnit;
}

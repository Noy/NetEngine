package com.noyhillel.battledome.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Noy on 6/23/2014.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public final class BattledomeException extends Exception {
    private final String errorMessage;
}

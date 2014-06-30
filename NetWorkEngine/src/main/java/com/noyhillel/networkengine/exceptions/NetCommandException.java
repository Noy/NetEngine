package com.noyhillel.networkengine.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Noy on 25/05/2014.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public final class NetCommandException extends Exception {
    /**
     * The ErrorMessage
     */
    private final String msg;
}

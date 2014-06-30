package com.noyhillel.networkengine.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Noy on 12/06/2014.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class NetPlayerJoinException extends Exception {
    private final String disconectMessage;
    private boolean disconnect;
}

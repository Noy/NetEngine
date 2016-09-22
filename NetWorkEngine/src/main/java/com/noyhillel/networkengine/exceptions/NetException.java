package com.noyhillel.networkengine.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Noy on 30/05/2014.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class NetException extends Exception {
    private String errorMessage;
}

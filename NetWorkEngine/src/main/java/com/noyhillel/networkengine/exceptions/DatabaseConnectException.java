package com.noyhillel.networkengine.exceptions;

import com.noyhillel.networkengine.storage.NetDatabase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Noy on 11/06/2014.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public final class DatabaseConnectException extends Exception {
    private final String message;
    private final Exception cause;
    private final NetDatabase database;
}

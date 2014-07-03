package com.noyhillel.survivalgames.player;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public final class StorageError extends Exception {
    private final String errorMessage;
    private final Exception cause;
}

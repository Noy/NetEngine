package com.noyhillel.networkengine.exceptions;

import com.noyhillel.networkengine.game.WorldStrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = false)
@Data
public final class ArenaException extends NetException {
    private final WorldStrapped arena;
    private final Exception cause;
    @NonNull private final String message;
}

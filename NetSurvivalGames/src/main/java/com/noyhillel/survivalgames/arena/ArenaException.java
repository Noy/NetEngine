package com.noyhillel.survivalgames.arena;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = false)
@Data
public final class ArenaException extends Exception {
    private final WorldStrapped arena;
    private final Exception cause;
    @NonNull private final String message;
}

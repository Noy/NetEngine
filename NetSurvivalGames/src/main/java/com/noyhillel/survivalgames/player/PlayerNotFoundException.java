package com.noyhillel.survivalgames.player;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public final class PlayerNotFoundException extends Exception {
    private final String message;
    private final GOfflinePlayer player;
    private final Exception cause;
}

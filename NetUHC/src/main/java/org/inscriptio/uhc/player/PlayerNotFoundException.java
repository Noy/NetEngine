package org.inscriptio.uhc.player;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public final class PlayerNotFoundException extends Exception {
    private final String message;
    private final UHCOfflinePlayer player;
    private final Exception cause;
}

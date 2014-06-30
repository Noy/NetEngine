package com.noyhillel.survivalgames.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public final class GameStartException extends Exception {
    private final String message;
}

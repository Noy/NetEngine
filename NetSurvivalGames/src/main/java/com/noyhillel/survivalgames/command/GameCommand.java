package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import org.bukkit.entity.Player;

/**
 * Unregistered command, on purpose.
 */
@Permission("survivalgames.admin.game")
//@CommandMeta(name = "game", usage = "/game", description = "The Game command.")
public final class GameCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        throw new UnsupportedOperationException("This operation has yet to be implemented.");
    }
}

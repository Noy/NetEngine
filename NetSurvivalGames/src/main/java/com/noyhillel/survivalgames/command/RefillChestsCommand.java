package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.game.GameException;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.entity.Player;

/**
 * Created by Belfort on 5/7/2016.
 */
@Permission("survivalgames.refill")
@CommandMeta(name = "refillchests", description = "The Refill Chests Command", usage = "/refillchests")
public final class RefillChestsCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too many arguments.", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        SGGame runningSGGame = SurvivalGames.getInstance().getGameManager().getRunningSGGame();
        if (runningSGGame == null) throw new NewNetCommandException("There is no active game right now!", NewNetCommandException.ErrorType.NULL);
        try {
            runningSGGame.refillChests();
            sender.sendMessage(MessageManager.getFormat("formats.refill-chests", true));
        } catch (GameException e) {
            e.printStackTrace();
        }
    }
}

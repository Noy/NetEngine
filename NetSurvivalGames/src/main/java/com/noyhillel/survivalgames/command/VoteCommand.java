package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.entity.Player;

@CommandMeta(name = "vote", description = "The Vote Command", usage = "/vote")
public final class VoteCommand extends NetAbstractCommandHandler {

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        GameManager gameManager = SurvivalGames.getInstance().getGameManager();
        if (gameManager == null) throw new NewNetCommandException("You can't run this command!", NewNetCommandException.ErrorType.Special);
        if (gameManager.isPlayingGame()) throw new NewNetCommandException("You can't run this command now!", NewNetCommandException.ErrorType.Special);
        if (args.length < 1) throw new NewNetCommandException("You must specify a map to play on!", NewNetCommandException.ErrorType.FewArguments);
        Arena detectedArena = null;
        String s = combineArgs(args);
        for (Arena arena : gameManager.getAllAreans()) {
            if (arena.getMeta().getName().equalsIgnoreCase(s)) {
                detectedArena = arena;
                break;
            }
        }
        if (detectedArena == null) throw new NewNetCommandException("The map you specified is invalid", NewNetCommandException.ErrorType.Special);
        gameManager.voteFor(player, detectedArena);
        player.sendMessage(MessageManager.getFormat("formats.voted-for-map", true, new String[]{"<map>", detectedArena.getMeta().getName()}));
    }
}
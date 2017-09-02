package org.inscriptio.uhc.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.arena.Arena;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.utils.MessageManager;

@CommandMeta(name = "vote", description = "The Vote Command", usage = "/vote")
public final class VoteCommand extends NetAbstractCommandHandler {
    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        GameManager gameManager = NetUHC.getInstance().getGameManager();
        if (gameManager == null) throw new NewNetCommandException("You can't run this command!", NewNetCommandException.ErrorType.SPECIAL);
        if (gameManager.isPlayingGame()) throw new NewNetCommandException("You can't run this command now!", NewNetCommandException.ErrorType.SPECIAL);
        if (args.length < 1) throw new NewNetCommandException("You must specify a map to play on!", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        Arena detectedArena = null;
        String s = combineArgs(args);
        for (Arena arena : gameManager.getAllAreans()) {
            if (arena.getMeta().getName().equalsIgnoreCase(s)) {
                detectedArena = arena;
                break;
            }
        }
        if (detectedArena == null) throw new NewNetCommandException("The map you specified is invalid", NewNetCommandException.ErrorType.SPECIAL);
        gameManager.voteFor(player, detectedArena);
        player.sendMessage(MessageManager.getFormat("formats.voted-for-map", true, new String[]{"<map>", detectedArena.getMeta().getName()}));
    }
}
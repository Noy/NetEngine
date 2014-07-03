package com.noyhillel.survivalgames.command;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.entity.Player;

public final class VoteCommand extends AbstractCommandHandler {
    public VoteCommand() throws CommandException {
        super("vote", SurvivalGames.getInstance());
    }

    @Override
    public void executePlayer(Player player, String[] args) throws CommandException {
        GameManager gameManager = getPlugin().getGameManager();
        if (gameManager == null) throw new CommandException("You can't run this command!", CommandException.ErrorType.Special);
        if (gameManager.isPlayingGame()) throw new CommandException("You can't run this command now!", CommandException.ErrorType.Special);
        if (args.length < 1) throw new CommandException("You must specify a map to play on!", CommandException.ErrorType.FewArguments);
        Arena detectedArena = null;
        String s = combineArgs(args);
        for (Arena arena : gameManager.getAllAreans()) {
            if (arena.getMeta().getName().equalsIgnoreCase(s)) {
                detectedArena = arena;
                break;
            }
        }
        if (detectedArena == null) throw new CommandException("The map you specified is invalid", CommandException.ErrorType.Special);
        gameManager.voteFor(player, detectedArena);
        player.sendMessage(MessageManager.getFormat("formats.voted-for-map", true, new String[]{"<map>", detectedArena.getMeta().getName()}));
    }
}

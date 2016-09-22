package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.noyhillel.survivalgames.command.LinkChestsCommand.resolveGPlayer;

@Permission("survivalgames.nick")
@CommandMeta(name = "nick", description = "The Nick Command", usage = "/nick")
public final class NickCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException("Too few arguments, use /nick <name>", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        if (args.length > 1) throw new NewNetCommandException("Too many arguments, use /nick <name>", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        String nick = args[0];
        if (!nick.matches("^[a-zA-Z_0-9\u00a7]+$")) throw new NewNetCommandException("Nicknames must be AlphaNumeric!", NewNetCommandException.ErrorType.SPECIAL);
        SGPlayer sgPlayer = resolveGPlayer(player);
        if (SGGame.spectators.contains(sgPlayer)) throw new NewNetCommandException("You cannot nick as a spectator!", NewNetCommandException.ErrorType.SPECIAL);
        if (nick.length() > 16) throw new NewNetCommandException("This nickname is too long!", NewNetCommandException.ErrorType.MANY_ARGUMENTS); // That's the limit, otherwise throws a NPE.
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (nick.equalsIgnoreCase(onlinePlayer.getName())) throw new NewNetCommandException("This nickname is already taken!", NewNetCommandException.ErrorType.SPECIAL); // Not sure about this one, change to a message if this isn't correct.
            if (nick.equalsIgnoreCase(onlinePlayer.getDisplayName())) throw new NewNetCommandException("This nickname is already taken!", NewNetCommandException.ErrorType.SPECIAL);
            if (nick.equalsIgnoreCase(onlinePlayer.getPlayerListName())) throw new NewNetCommandException("This nickname is already taken!", NewNetCommandException.ErrorType.SPECIAL);
        }
        if (nick.equalsIgnoreCase("remove") || nick.equalsIgnoreCase("off")) {
            sgPlayer.setNick(null);
            player.setDisplayName(player.getName());
            player.sendMessage(MessageManager.getFormat("formats.nick-off"));
            return;
        }
        sgPlayer.setNick(nick);
        sgPlayer.sendMessage(MessageManager.getFormat("formats.disguised-player", true, new String[]{"<nickname>", nick}));
    }
}

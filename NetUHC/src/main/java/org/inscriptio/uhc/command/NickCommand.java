package org.inscriptio.uhc.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.game.impl.UHCGame;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.utils.MessageManager;

import static org.inscriptio.uhc.command.LinkChestsCommand.resolveGPlayer;

@Permission("uhc.nick")
@CommandMeta(name = "nick", description = "The Nick Command", usage = "/nick")
public final class NickCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException("Too few arguments, use /nick <name>", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        if (args.length > 1) throw new NewNetCommandException("Too many arguments, use /nick <name>", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        String nick = args[0];
        if (!nick.matches("^[a-zA-Z_0-9\u00a7]+$")) throw new NewNetCommandException("Nicknames must be AlphaNumeric!", NewNetCommandException.ErrorType.SPECIAL);
        UHCPlayer uhcPlayer = resolveGPlayer(player);
        if (UHCGame.spectators.contains(uhcPlayer)) throw new NewNetCommandException("You cannot nick as a spectator!", NewNetCommandException.ErrorType.SPECIAL);
        if (nick.length() > 16) throw new NewNetCommandException("This nickname is too long!", NewNetCommandException.ErrorType.MANY_ARGUMENTS); // That's the limit, otherwise throws a NPE.
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (nick.equalsIgnoreCase(onlinePlayer.getName())) throw new NewNetCommandException("This nickname is already taken!", NewNetCommandException.ErrorType.SPECIAL); // Not sure about this one, change to a message if this isn't correct.
            if (nick.equalsIgnoreCase(onlinePlayer.getDisplayName())) throw new NewNetCommandException("This nickname is already taken!", NewNetCommandException.ErrorType.SPECIAL);
            if (nick.equalsIgnoreCase(onlinePlayer.getPlayerListName())) throw new NewNetCommandException("This nickname is already taken!", NewNetCommandException.ErrorType.SPECIAL);
        }
        if (nick.equalsIgnoreCase("remove") || nick.equalsIgnoreCase("off")) {
            uhcPlayer.setNick(null);
            player.setDisplayName(player.getName());
            player.sendMessage(MessageManager.getFormat("formats.nick-off"));
            return;
        }
        uhcPlayer.setNick(nick);
        uhcPlayer.sendMessage(MessageManager.getFormat("formats.disguised-player", true, new String[]{"<nickname>", nick}));
    }
}

package com.noyhillel.survivalgames.command;


import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.noyhillel.survivalgames.command.LinkChestsCommand.resolveGPlayer;

@Permission("survivalgames.disguise")
@CommandMeta(name = "nick", description = "The Nick Command", usage = "/nick")
public final class NickCommand extends NetAbstractCommandHandler { // Just for you <3

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException("Too few arguments, use /nick <name>", NewNetCommandException.ErrorType.FewArguments);
        if (args.length > 1) throw new NewNetCommandException("Too many arguments, use /nick <name>", NewNetCommandException.ErrorType.ManyArguments);
        String nick = args[0];
        GPlayer gPlayer = resolveGPlayer(player);
        if (SGGame.spectators.contains(gPlayer)) throw new NewNetCommandException("You cannot nick as a spectator!", NewNetCommandException.ErrorType.Special);
        if (nick.length() > 16) throw new NewNetCommandException("This nickname is too long!", NewNetCommandException.ErrorType.ManyArguments); // That's the limit, otherwise throws a NPE.
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (nick.equalsIgnoreCase(onlinePlayer.getName())) throw new NewNetCommandException("This nickname is already taken!", NewNetCommandException.ErrorType.Special); // Not sure about this one, change to a message if this isn't correct.
        }
        if (nick.equalsIgnoreCase("remove") || nick.equalsIgnoreCase("off")) {
            gPlayer.setNick(null);
            player.setDisplayName(player.getName());
            player.sendMessage(MessageManager.getFormat("formats.nick-off"));
            return;
        }
        if (!StringUtils.isAlphanumeric(nick)) throw new NewNetCommandException("Nicknames must be AlphaNumeric", NewNetCommandException.ErrorType.Special);
        gPlayer.setNick(nick);
        gPlayer.sendMessage(MessageManager.getFormat("formats.disguised-player", true, new String[]{"<nickname>", nick}));
    }
}

package com.noyhillel.survivalgames.command;


import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Permission("survivalgames.disguise")
public final class NickCommand extends AbstractCommandHandler { // Just for you <3

    public NickCommand() throws CommandException {
        super("nick", SurvivalGames.getInstance());
    }


    @Override
    protected void executePlayer(Player player, String[] args) throws CommandException {
        if (args.length == 0) throw new CommandException("Too few arguments, use /nick <name>", CommandException.ErrorType.FewArguments);
        if (args.length > 1) throw new CommandException("Too many arguments, use /nick <name>", CommandException.ErrorType.ManyArguments);
        String nick = args[0];
        GPlayer gPlayer = resolveGPlayer(player);
        if (SGGame.spectators.contains(gPlayer)) throw new CommandException("You cannot nick as a spectator!", CommandException.ErrorType.Special);
        if (nick.length() > 16) throw new CommandException("This nickname is too long!", CommandException.ErrorType.ManyArguments); // That's the limit, otherwise throws a NPE.
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (nick.equalsIgnoreCase(onlinePlayer.getName())) throw new CommandException("This nickname is already taken!", CommandException.ErrorType.Special); // Not sure about this one, change to a message if this isn't correct.
        }
        if (nick.equalsIgnoreCase("remove") || nick.equalsIgnoreCase("off")) {
            gPlayer.setNick(null);
            player.setDisplayName(player.getName());
            player.sendMessage(MessageManager.getFormat("formats.nick-off"));
            return;
        }
        if (!StringUtils.isAlphanumeric(nick)) throw new CommandException("Nicknames must be AlphaNumeric", CommandException.ErrorType.Special);
        gPlayer.setNick(nick);
        gPlayer.sendMessage(MessageManager.getFormat("formats.disguised-player", true, new String[]{"<nickname>", nick}));
    }
}

package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p/>
 * Latest Change: 29/07/2014.
 * <p/>
 *
 * @author Noy
 * @since 29/07/2014.
 */
@CommandMeta(name = "spectate", description = "The Spectate Command", usage = "/spectate <player>")
public final class SpectateCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        SGPlayer sgPlayer = LinkChestsCommand.resolveGPlayer(sender);
        if (!SGGame.getSpectators().contains(sgPlayer)) throw new NewNetCommandException("You need to be a spectator to perform this command!", NewNetCommandException.ErrorType.SPECIAL);
        if (args.length == 0) throw new NewNetCommandException("You need to provide a player!", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        if (args.length > 1) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) throw new NewNetCommandException("Player not found!", NewNetCommandException.ErrorType.NULL);
        if (target == sender) throw new NewNetCommandException("You cannot spectate yourself!", NewNetCommandException.ErrorType.NULL);
        if (SGGame.getSpectators().contains(LinkChestsCommand.resolveGPlayer(target))) throw new NewNetCommandException("That player is a spectator!", NewNetCommandException.ErrorType.SPECIAL);
        try {
            sender.teleport(target);
            sender.sendMessage(MessageManager.getFormat("formats.teleport-spectator", true, new String[]{"<target>", target.getDisplayName()}));
        }catch (Exception e) {
            sender.sendMessage("Error, check console!");
            e.printStackTrace();
        }
    }

    @Override
    protected List<String> completeArgs(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();
        if (args[0].equals("")) {
            names.addAll(Bukkit.getOnlinePlayers().stream().filter
                    (player -> player.getName().startsWith(args[0])).map((Function<Player, String>)
                    Player::getName).collect(Collectors.toList()));
            Collections.sort(names);
            return names;
        }
        return null;
    }
}

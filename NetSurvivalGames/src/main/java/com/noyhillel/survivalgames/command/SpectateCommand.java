package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        GPlayer gPlayer = LinkChestsCommand.resolveGPlayer(sender);
        if (!SGGame.getSpectators().contains(gPlayer)) throw new NewNetCommandException("You need to be a spectator to perform this command!", NewNetCommandException.ErrorType.Special);
        if (args.length == 0) throw new NewNetCommandException("You need to provide a player!", NewNetCommandException.ErrorType.FewArguments);
        if (args.length > 1) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.FewArguments);
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) throw new NewNetCommandException("Player not found!", NewNetCommandException.ErrorType.Null);
        if (target == sender) throw new NewNetCommandException("You cannot spectate yourself!", NewNetCommandException.ErrorType.Null);
        if (SGGame.getSpectators().contains(LinkChestsCommand.resolveGPlayer(target))) throw new NewNetCommandException("That player is a spectator!", NewNetCommandException.ErrorType.Special);
        try {
            sender.teleport(target);
            sender.sendMessage(MessageManager.getFormat("formats.teleport-spectator", true, new String[]{"<target>", target.getDisplayName()}));
        }catch (Exception e) {
            sender.sendMessage("Error, check console!");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> completeArgs(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();
        if (args[0].equals("")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().startsWith(args[0])) {
                    names.add(player.getName());
                }
            }
            Collections.sort(names);
            return names;
        }
        return null;
    }
}

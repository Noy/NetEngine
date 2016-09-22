package com.noyhillel.survivalgames.command.setcommands;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Belfort on 5/4/2016.
 */
@Permission("survivalgames.setcommand")
@CommandMeta(name = "setpoints", description = "The Set Points Command", usage = "/setpoints <number>")
public final class SetPointsCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException("Too few arguments, use /setpoints <name> <points>", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        if (args.length >= 3) throw new NewNetCommandException("Too many arguments, use /setpoints <name> <points>", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        SGPlayer sgPlayer = resolveGPlayer(player);
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) throw new NewNetCommandException("Player cannot be found!", NewNetCommandException.ErrorType.NULL);
        SGPlayer gTarget = resolveGPlayer(target);
        if (gTarget == null) throw new NewNetCommandException("Player cannot be found!", NewNetCommandException.ErrorType.NULL);
        try {
            Integer x = Integer.parseInt(args[1]);
            if (x > 100000)
                throw new NewNetCommandException("You cannot set that high amount of points.", NewNetCommandException.ErrorType.SPECIAL);
            gTarget.setPoints(gTarget.getPoints() + x);
            sgPlayer.sendMessage(MessageManager.getFormat("formats.setpoints", true, new String[]{"<player>", gTarget.getDisplayableName()}, new String[]{"<points>", String.valueOf(gTarget.getPoints())}));
            gTarget.sendMessage(MessageManager.getFormat("formats.sendpoints", true, new String[]{"<player>", sgPlayer.getDisplayableName()}, new String[]{"<points>", String.valueOf(gTarget.getPoints())}));
        }catch (NumberFormatException e) {
            throw new NewNetCommandException("Cannot recognise argument, most likely not a number!", NewNetCommandException.ErrorType.NULL);
        }
    }

    @SneakyThrows
    private SGPlayer resolveGPlayer(Player player) {
        if (player == null) throw new NewNetCommandException("Player not found!", NewNetCommandException.ErrorType.NULL);
        return SurvivalGames.getInstance().getSgPlayerManager().getOnlinePlayer(player);
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

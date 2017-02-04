package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.noyhillel.survivalgames.command.LinkChestsCommand.resolveGPlayer;

@CommandMeta(name = "stats", description = "The Stats Command", usage = "/stats")
public final class StatsCommand extends NetAbstractCommandHandler {

    /*
     Only works when Database is setup.
     */

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length > 1) throw new NewNetCommandException("Too many Arguments!", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        SGPlayer sgPlayer = resolveGPlayer(player);
        List<String> yourStats = new ArrayList<>();
        yourStats.add(MessageManager.getFormat("formats.stats.header", false));
        yourStats.add(getFormattedStat("Kills", sgPlayer.getKills()));
        yourStats.add(getFormattedStat("Deaths", sgPlayer.getDeaths()));
        yourStats.add(getFormattedStat("Wins", sgPlayer.getWins()));
        if (sgPlayer.getKills() > 0 && sgPlayer.getDeaths() > 0) {
            yourStats.add(getFormattedStat("KDR", (float) sgPlayer.getKills()/ sgPlayer.getDeaths()));
        } else sgPlayer.sendMessage(ChatColor.RED + "Cannot get your KDR with either 0 kills or 0 deaths!");
        yourStats.add(getFormattedStat("Points", sgPlayer.getPoints()));
        yourStats.add(getFormattedStat("Games played", sgPlayer.getTotalGames()));
        //yourStats.add(getFormattedStat("Mutation Credits", sgPlayer.getMutationCredits()));
        if (args.length == 0) {
            yourStats.forEach(sgPlayer::sendMessage);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) throw new NewNetCommandException("Can't find that player!", NewNetCommandException.ErrorType.NULL);
        SGPlayer gTarget = resolveGPlayer(target);
        if (gTarget == null) throw new NewNetCommandException("Can't find that player!", NewNetCommandException.ErrorType.NULL);
        List<String> targetStats = new ArrayList<>();
        targetStats.add(MessageManager.getFormat("formats.stats.header", false));
        targetStats.add(ChatColor.YELLOW + gTarget.getDisplayableName() + "'s stats are:");
        targetStats.add(getFormattedStat("Kills", gTarget.getKills()));
        targetStats.add(getFormattedStat("Deaths", gTarget.getDeaths()));
        targetStats.add(getFormattedStat("Wins", gTarget.getWins()));
        if (gTarget.getKills() > 0 && gTarget.getDeaths() > 0) {
            targetStats.add(getFormattedStat("KDR", (float)gTarget.getKills()/gTarget.getDeaths()));
        } else sgPlayer.sendMessage(ChatColor.RED + "Cannot get your KDR with either 0 kills or 0 deaths!");
        targetStats.add(getFormattedStat("Points", gTarget.getPoints()));
        targetStats.add(getFormattedStat("Games played", gTarget.getTotalGames()));
        //targetStats.add(getFormattedStat("Mutation Credits", gTarget.getMutationCredits()));
        targetStats.forEach(sgPlayer::sendMessage);
    }

    private String getFormattedStat(String statName, Object stat) {
        return MessageManager.getFormat("formats.stats.stat-display", false, new String[]{"<stat>", stat.toString()}, new String[]{"<name>", statName});
    }

    @Override
    public List<String> completeArgs(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();
        if (args[0].equals("")) {
            names.addAll(Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().startsWith(args[0])).map((Function<Player, String>) Player::getName).collect(Collectors.toList()));
            Collections.sort(names);
            return names;
        }
        return null;
    }
}

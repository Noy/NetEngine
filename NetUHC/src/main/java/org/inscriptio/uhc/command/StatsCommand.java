package org.inscriptio.uhc.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.utils.MessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.inscriptio.uhc.command.LinkChestsCommand.resolveGPlayer;

@CommandMeta(name = "stats", description = "The Stats Command", usage = "/stats")
public final class StatsCommand extends NetAbstractCommandHandler {

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length > 1) throw new NewNetCommandException("Too many Arguments!", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        UHCPlayer uhcPlayer = resolveGPlayer(player);
        List<String> yourStats = new ArrayList<>();
        yourStats.add(MessageManager.getFormat("formats.stats.header", false));
        yourStats.add(getFormattedStat("Kills", uhcPlayer.getKills()));
        yourStats.add(getFormattedStat("Deaths", uhcPlayer.getDeaths()));
        yourStats.add(getFormattedStat("Wins", uhcPlayer.getWins()));
        if (uhcPlayer.getKills() > 0 && uhcPlayer.getDeaths() > 0) {
            yourStats.add(getFormattedStat("KDR", (float) uhcPlayer.getKills()/ uhcPlayer.getDeaths()));
        } else uhcPlayer.sendMessage(ChatColor.RED + "Cannot get your KDR with either 0 kills or 0 deaths!");
        yourStats.add(getFormattedStat("Points", uhcPlayer.getPoints()));
        yourStats.add(getFormattedStat("Games played", uhcPlayer.getTotalGames()));
        //yourStats.add(getFormattedStat("Mutation Credits", uhcPlayer.getMutationCredits()));
        if (args.length == 0) {
            yourStats.forEach(uhcPlayer::sendMessage);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) throw new NewNetCommandException("Can't find that player!", NewNetCommandException.ErrorType.NULL);
        UHCPlayer gTarget = resolveGPlayer(target);
        if (gTarget == null) throw new NewNetCommandException("Can't find that player!", NewNetCommandException.ErrorType.NULL);
        List<String> targetStats = new ArrayList<>();
        targetStats.add(MessageManager.getFormat("formats.stats.header", false));
        targetStats.add(ChatColor.YELLOW + gTarget.getDisplayableName() + "'s stats are:");
        targetStats.add(getFormattedStat("Kills", gTarget.getKills()));
        targetStats.add(getFormattedStat("Deaths", gTarget.getDeaths()));
        targetStats.add(getFormattedStat("Wins", gTarget.getWins()));
        if (gTarget.getKills() > 0 && gTarget.getDeaths() > 0) {
            targetStats.add(getFormattedStat("KDR", (float)gTarget.getKills()/gTarget.getDeaths()));
        } else uhcPlayer.sendMessage(ChatColor.RED + "Cannot get your KDR with either 0 kills or 0 deaths!");
        targetStats.add(getFormattedStat("Points", gTarget.getPoints()));
        targetStats.add(getFormattedStat("Games played", gTarget.getTotalGames()));
        //targetStats.add(getFormattedStat("Mutation Credits", gTarget.getMutationCredits()));
        targetStats.forEach(uhcPlayer::sendMessage);
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

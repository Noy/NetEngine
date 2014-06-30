package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.MessageManager;
import com.noyhillel.survivalgames.player.GPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandMeta(name = "stats", usage = "/stats", description = "The stats command.")
public final class StatsCommand extends NetAbstractCommandHandler {

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        GPlayer gPlayer = LinkChestsCommand.resolveGPlayer(player);
        List<String> strings = new ArrayList<>();
        strings.add(MessageManager.getFormat("formats.stats.header", false));
        strings.add(getFormattedStat("Kills", gPlayer.getKills()));
        strings.add(getFormattedStat("Deaths", gPlayer.getDeaths()));
        strings.add(getFormattedStat("KDR", gPlayer.getKills()/gPlayer.getDeaths()));
        strings.add(getFormattedStat("Wins", gPlayer.getWins()));
        strings.add(getFormattedStat("Games played", gPlayer.getTotalGames()));
        strings.add(getFormattedStat("Points", gPlayer.getPoints()));
        strings.add(getFormattedStat("Mutation Credits", gPlayer.getMutationCredits()));
        for (String string : strings) {
            player.sendMessage(string);
        }
    }

    private String getFormattedStat(String statName, Object stat) {
        return MessageManager.getFormat("formats.stats.stat-display", false, new String[]{"<stat>", stat.toString()}, new String[]{"<name>", statName});
    }
}

package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.storage.ForgetfulStorage;
import com.noyhillel.survivalgames.storage.GStorage;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.noyhillel.survivalgames.command.LinkChestsCommand.resolveGPlayer;

@CommandMeta(name = "stats", description = "The Stats Command", usage = "/stats")
public final class StatsCommand extends NetAbstractCommandHandler {

    @Override
    public void playerCommand(Player player, String[] args) throws NewNetCommandException {
        GPlayer gPlayer = resolveGPlayer(player);
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

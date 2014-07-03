package com.noyhillel.survivalgames.command;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class StatsCommand extends AbstractCommandHandler {
    public StatsCommand() throws CommandException {
        super("stats", SurvivalGames.getInstance());
    }

    @Override
    public void executePlayer(Player player, String[] args) throws CommandException {
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

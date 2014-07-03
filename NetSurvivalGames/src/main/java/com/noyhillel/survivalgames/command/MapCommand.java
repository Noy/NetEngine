package com.noyhillel.survivalgames.command;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.ArenaMeta;
import com.noyhillel.survivalgames.arena.setup.ArenaSetup;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class MapCommand extends AbstractCommandHandler {

    public MapCommand() throws CommandException {
        super("map", SurvivalGames.getInstance());
    }

    @Override
    protected void executePlayer(Player sender, String[] args) throws CommandException {
        if (args.length > 0) throw new CommandException("Too many arguments!", CommandException.ErrorType.ManyArguments);
        ArenaMeta arenaMeta = ArenaSetup.arenaMeta;
        List<String> authors = arenaMeta.getAuthors();
        String socialLink = arenaMeta.getSocialLink();
        String name = arenaMeta.getName();
        sender.sendMessage(MessageManager.getFormat("arena.name", true, new String[]{"<name>", name}));
        sender.sendMessage(MessageManager.getFormat("arena.social-link", true, new String[]{"<social>", socialLink}));
        sender.sendMessage(MessageManager.getFormat("arena.author", true, new String[]{"<author>", String.valueOf(authors)}));
    }
}

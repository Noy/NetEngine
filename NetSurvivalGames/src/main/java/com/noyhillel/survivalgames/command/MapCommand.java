package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.arena.ArenaMeta;
import com.noyhillel.survivalgames.arena.setup.ArenaSetup;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(name = "map", description = "The Map Command", usage = "/map")
public final class MapCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.ManyArguments);
        ArenaMeta arenaMeta = ArenaSetup.arenaMeta;
        List<String> authors = arenaMeta.getAuthors();
        String socialLink = arenaMeta.getSocialLink();
        String name = arenaMeta.getName();
        sender.sendMessage(MessageManager.getFormat("arena.name", true, new String[]{"<name>", name}));
        sender.sendMessage(MessageManager.getFormat("arena.social-link", true, new String[]{"<social>", socialLink}));
        sender.sendMessage(MessageManager.getFormat("arena.author", true, new String[]{"<author>", String.valueOf(authors)}));
    }
}

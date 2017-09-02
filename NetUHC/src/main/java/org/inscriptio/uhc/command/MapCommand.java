package org.inscriptio.uhc.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.game.arena.ArenaMeta;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.utils.MessageManager;

import java.util.List;

@CommandMeta(name = "map", description = "The Map Command", usage = "/map")
public final class MapCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        GameManager manager = NetUHC.getInstance().getGameManager();
        if (manager.getRunningUHCGame() == null) throw new NewNetCommandException("Map is not set yet! Type /vote <map>", NewNetCommandException.ErrorType.SPECIAL);
        ArenaMeta arenaMeta = manager.getRunningUHCGame().getArena().getMeta();
        String socialLink = arenaMeta.getSocialLink();
        String name = arenaMeta.getName();
        List<String> authors = arenaMeta.getAuthors();
        sender.sendMessage(MessageManager.getFormat("arena.name", true, new String[]{"<name>", name}));
        sender.sendMessage(MessageManager.getFormat("arena.social-link", true, new String[]{"<social>", socialLink}));
        sender.sendMessage(MessageManager.getFormat("arena.author", true, new String[]{"<author>", String.valueOf(authors)}));
    }
}

package org.inscriptio.uhc.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandMeta(name = "hub", description = "The Hub Command", usage = "/hub")
public final class HubCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        //SurvivalGames.getInstance().sendToServer("hub", sender); // Only works with lillypad, etc.
        sender.sendMessage(ChatColor.GREEN + "Sending you to the hub!");
    }


}

package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.survivalgames.SurvivalGames;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 10/07/2014.
 */
@CommandMeta(name = "hub", description = "The Hub Command", usage = "/hub")
public class HubCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.ManyArguments);
        SurvivalGames.getInstance().sendToServer("hub", sender);
        sender.sendMessage(ChatColor.GREEN + "Sending you to the hub!");
    }
}

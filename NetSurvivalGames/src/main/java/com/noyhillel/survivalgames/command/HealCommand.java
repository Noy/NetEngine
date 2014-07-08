package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 08/07/2014.
 */
@Permission("survivalgames.heal")
@CommandMeta(name = "heal", description = "The Heal Command", usage = "/heal")
public class HealCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        GPlayer player = LinkChestsCommand.resolveGPlayer(sender);
        if (args.length == 0) {
            player.heal();
        } else if (args.length == 1) {
            Player target = Bukkit.getServer().getPlayerExact(args[0]);
            GPlayer gTarget = LinkChestsCommand.resolveGPlayer(target);
            gTarget.heal();
            player.sendMessage(MessageManager.getFormat("formats.healed-player", true, new String[]{"<target>", gTarget.getDisplayableName()}));
        }
    }
}
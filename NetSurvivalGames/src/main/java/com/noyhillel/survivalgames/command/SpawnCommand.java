package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 10/07/2014.
 */
@Permission("survivalgames.spawn")
@CommandMeta(name = "spawn", description = "The Spawn Command", usage = "/spawn")
public final class SpawnCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too many arguemnts.", NewNetCommandException.ErrorType.ManyArguments);
        player.teleport(player.getWorld().getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "You have teleported to spawn!");
    }
}

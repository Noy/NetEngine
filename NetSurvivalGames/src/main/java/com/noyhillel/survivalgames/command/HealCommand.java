package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.noyhillel.survivalgames.command.LinkChestsCommand.resolveGPlayer;

/**
 * Created by Noy on 08/07/2014.
 */
@Permission("survivalgames.heal")
@CommandMeta(name = "heal", description = "The Heal Command", usage = "/heal [player]")
public final class HealCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        GPlayer player = resolveGPlayer(sender);
        if (args.length == 0) {
            player.heal();
            Bukkit.broadcastMessage(ChatColor.RED + player.getDisplayableName() + ChatColor.GOLD + " healed themselves!");
        } else if (args.length == 1) {
            Player target = Bukkit.getServer().getPlayerExact(args[0]);
            GPlayer gTarget = resolveGPlayer(target);
            gTarget.heal();
            player.sendMessage(MessageManager.getFormat("formats.healed-player", true, new String[]{"<target>", gTarget.getDisplayableName()}));
        } else if (args.length > 1) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.ManyArguments);
    }

    @Override
    public List<String> completeArgs(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();
        if (args[0].equals("")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().startsWith(args[0])) {
                    names.add(player.getName());
                }
            }
            Collections.sort(names);
            return names;
        }
        return null;
    }
}
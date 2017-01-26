package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.noyhillel.netsentials.commands.Message.receivedMessage;

@Permission("netsentials.msg")
@CommandMeta(name = "reply", usage = "/r", description = "The Reply command.")
public final class Reply extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException(NetSentials.getPrefix() + "Specify your message.", NewNetCommandException.ErrorType.NULL);
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(player);
        NetPlayer target = NetPlayer.getPlayerFromPlayer(Bukkit.getServer().getPlayer(receivedMessage.get(netPlayer.getUuid())));
        String message = "";
        if (target == null) {
            netPlayer.sendMessage(MessageManager.getFormats("formats.not-found"));
            return;
        }
        for (int i = 0; i != args.length; i++) {
            message += args[i] + " ";
        }
        netPlayer.sendMessage(MessageManager.getFormat("formats.send-message", false, new String[]{"<target>", target.getName()}, new String[]{"<message>", message}));
        target.sendMessage(MessageManager.getFormat("formats.rec-message", false, new String[]{"<target>", netPlayer.getName()}, new String[]{"<message>", message}));
        //receivedMessage.remove(netPlayer.getUuid());
    }
}

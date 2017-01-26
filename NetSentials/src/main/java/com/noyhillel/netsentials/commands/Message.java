package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Permission("netsentials.msg")
@CommandMeta(name = "msg", usage = "/msg <Online Player>", description = "The message command.")
public final class Message extends NetAbstractCommandHandler {

    public static Map<UUID, UUID> receivedMessage = new HashMap<>();

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException(NetSentials.getPrefix() + "Specify who to message.", NewNetCommandException.ErrorType.NULL);
        if (args.length == 1) throw new NewNetCommandException(NetSentials.getPrefix() + "Specify a message.", NewNetCommandException.ErrorType.NULL);
        Player target = Bukkit.getPlayerExact(args[0]);
        String message = "";
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(player);
        if (target != null) {
            for (int i = 1; i != args.length; i++) {
                message += args[i] + " ";
            }
            receivedMessage.put(target.getUniqueId(), netPlayer.getUuid());
            receivedMessage.put(netPlayer.getUuid(), target.getUniqueId());
            netPlayer.sendMessage(MessageManager.getFormat("formats.send-message", false, new String[]{"<target>", target.getName()}, new String[]{"<message>", message}));
            target.sendMessage(MessageManager.getFormat("formats.rec-message", false, new String[]{"<target>", netPlayer.getName()}, new String[]{"<message>", message}));
        } else {
            netPlayer.sendMessage(MessageManager.getFormats("formats.not-found"));
        }
    }

    @Override
    public List<String> completeArgs(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();
        if (args[0].equals("")) {
            names.addAll(Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().startsWith(args[0])).map((Function<Player, String>) Player::getName).collect(Collectors.toList()));
            Collections.sort(names);
            return names;
        }
        return null;
    }

    static List<String> tabComplete(CommandSender sender, String[] args) {
        Message message = new Message();
        return message.completeArgs(sender, args);
    }
}
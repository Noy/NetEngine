package com.noyhillel.netsentials.commands;

import com.google.common.collect.ImmutableList;
import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
@Permission("netsentials.i")
@CommandMeta(name = "i", usage = "/i [item] <amount> <value>", description = "The Give Me Item command.")
public final class I extends NetAbstractCommandHandler { // awesome class name lol

    private static final List<String> MATERIALS;

    static {
        ArrayList<String> materialList = new ArrayList<>();
        for (Material m : Material.values()) {
            materialList.add(m.name());
        }
        Collections.sort(materialList);
        MATERIALS = ImmutableList.copyOf(materialList);
    }

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(player);
        if (args.length == 0) throw new NewNetCommandException(NetSentials.getPrefix() + "You have provided too few arguments.", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        if (args.length > 3) throw new NewNetCommandException(NetSentials.getPrefix() + "You have provided too many arguments.", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        Material m;
        Integer i;
        if (args.length == 1) {
            try {
                m = Material.valueOf(args[0].toUpperCase());
                String[] itemName = {"<item>", m.toString().toLowerCase()};
                netPlayer.giveItem(m, 1);
                netPlayer.sendMessage(MessageManager.getFormat("formats.give-item-1", true, itemName));
                return;
            } catch (IllegalArgumentException e) {
                throw new NewNetCommandException(NetSentials.getPrefix() + "Unrecognized Item.", NewNetCommandException.ErrorType.NULL);
            }
        }
        if (args.length == 2) {
            try {
                m = Material.valueOf(args[0].toUpperCase());
                i = Integer.valueOf(args[1]);
                String[] itemName = {"<item>", m.toString().toLowerCase()};
                netPlayer.giveItem(m, i);
                netPlayer.sendMessage(MessageManager.getFormat("formats.give-item-2", true, new String[]{"<amount>", i.toString()}, itemName));
                return;
            } catch (IllegalArgumentException e) {
                throw new NewNetCommandException(NetSentials.getPrefix() + "Unrecognized Item or Amount.", NewNetCommandException.ErrorType.NULL);
            }
        }
        if (args.length == 3) {
            try {
                m = Material.valueOf(args[0].toUpperCase());
                i = Integer.valueOf(args[1]);
                Short s = Short.valueOf(args[2]);
                String[] itemName = {"<item>", m.toString().toLowerCase()};
                netPlayer.giveItem(m, i, s);
                netPlayer.sendMessage(MessageManager.getFormat("formats.give-item-3", true, new String[]{"<amount>", i.toString()},
                        itemName, new String[]{"<durability>", s.toString()}));
            } catch (IllegalArgumentException e) {
                throw new NewNetCommandException(NetSentials.getPrefix() + "Unrecognized Item, Amount or Durability.", NewNetCommandException.ErrorType.NULL);
            }
        }
    }

    @Override
    public List<String> completeArgs(CommandSender sender, String[] args) {
        if (sender.hasPermission("netsentials.i")) {
            if (args.length == 1) {
                String arg = args[0];
                List<String> materials = I.MATERIALS;
                List<String> completion = null;
                Integer size = materials.size();
                Integer i = Collections.binarySearch(materials, arg, String.CASE_INSENSITIVE_ORDER);
                if (i < 0) {
                    i = -1 - i;
                }
                while (i < size) {
                    String mat = materials.get(i);
                    if (StringUtil.startsWithIgnoreCase(mat, arg)) {
                        if (completion == null) completion = new ArrayList<>();
                        completion.add(mat);
                    } else break;
                    i++;
                }
                if (completion != null) return completion;
            }
        }
        return ImmutableList.of();
    }
}

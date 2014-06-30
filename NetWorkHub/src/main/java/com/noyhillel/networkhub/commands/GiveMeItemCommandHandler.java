package com.noyhillel.networkhub.commands;

import com.google.common.collect.ImmutableList;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkhub.MessageManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Noy on 29/05/2014.
 */
@Permission("hub.giveme")
@CommandMeta(name = "giveme", usage = "/giveme [item] <amount> <value>", description = "The Give Me Item command.")
public final class GiveMeItemCommandHandler extends NetAbstractCommandHandler {

    /*
    g freaking g
     */

    private final static List<String> materials;

    static {
        ArrayList<String> materialList = new ArrayList<>();
        for (Material m : Material.values()) {
            materialList.add(m.name());
        }
        Collections.sort(materialList);
        materials = ImmutableList.copyOf(materialList);
    }

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException("You have provided too few arguments!", NewNetCommandException.ErrorType.FewArguments);
        if (args.length > 3) throw new NewNetCommandException("You have provided too many arguments!", NewNetCommandException.ErrorType.ManyArguments);
        Material m;
        Integer i;
        if (args.length == 1) {
            try {
                m = Material.valueOf(args[0].toUpperCase());
                player.getInventory().addItem(new ItemStack(m, 1));
                player.sendMessage(MessageManager.getFormat("formats.give-item-1", true, new String[]{"<item>", m.toString().toLowerCase()}));
                //player.sendMessage("you have recieved 1" + m.toString().toLowerCase());
                return;
            } catch (IllegalArgumentException e) {
                throw new NewNetCommandException("Unrecognized Item!", NewNetCommandException.ErrorType.Null);
            }
        }
        if (args.length == 2) {
            try {
                m = Material.valueOf(args[0].toUpperCase());
                i = Integer.valueOf(args[1]);
                player.getInventory().addItem(new ItemStack(m, i));
                player.sendMessage(MessageManager.getFormat("formats.give-item-2", true, new String[]{"<amount>", i.toString()},
                        new String[]{"<item>", m.toString().toLowerCase()}));
                //player.sendMessage("you have recieved " + i.toString() + " " + m.toString().toLowerCase());
                return;
            } catch (IllegalArgumentException e) {
                throw new NewNetCommandException("Unrecognized Item or Amount!", NewNetCommandException.ErrorType.Null);
            }
        }
        if (args.length == 3) {
            try {
                m = Material.valueOf(args[0].toUpperCase());
                i = Integer.valueOf(args[1]);
                Short s = Short.valueOf(args[2]);
                player.getInventory().addItem(new ItemStack(m, i, s));
                player.sendMessage(MessageManager.getFormat("formats.give-item-3", true, new String[]{"<amount>", i.toString()},
                        new String[]{"<item>", m.toString().toLowerCase()}, new String[]{"<durability>", s.toString()}));
            } catch (IllegalArgumentException e) {
                throw new NewNetCommandException("Unrecognized Item, Amount or Durability!", NewNetCommandException.ErrorType.Null);
            }
        }
    }

    @Override
    public List<String> completeArgs(CommandSender sender, String[] args) {
        if (sender.hasPermission("hub.giveme")) {
            if (args.length == 1) {
                String arg = args[0];
                List<String> materials = GiveMeItemCommandHandler.materials;
                List<String> completion = null;
                Integer size = materials.size();
                Integer i = Collections.binarySearch(materials, arg, String.CASE_INSENSITIVE_ORDER);
                if (i < 0) {
                    i = -1 - i;
                }
                while (i < size) {
                    String material = materials.get(i);
                    if (StringUtil.startsWithIgnoreCase(material, arg)) {
                        if (completion == null) completion = new ArrayList<>();
                        completion.add(material);
                    } else break;
                    i++;
                }
                if (completion != null) return completion;
            }
        }
        return ImmutableList.of();
    }
}
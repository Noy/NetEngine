package com.noyhillel.netkitpvp.commands;

import com.noyhillel.netkitpvp.MessageManager;
import com.noyhillel.netkitpvp.NetKitPVP;
import com.noyhillel.netkitpvp.game.ConfigManager;
import com.noyhillel.networkengine.exceptions.CooldownUnexpiredException;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.InventoryGUI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noyhillel1 on 01/07/2014.
 */
@CommandMeta(name = "kit", description = "The Kit Command", usage = "/kit")
public final class KitCommand extends NetAbstractCommandHandler {

    private InventoryGUI kitInventoryGUI;

    public KitCommand() {
        final ConfigManager configManager = new ConfigManager();
        kitInventoryGUI = new InventoryGUI(configManager.getInventoryItems(), "Test", new InventoryGUI.InventoryGUIDelegate() {
            @Override
            public void onOpen(InventoryGUI gui, NetPlayer player) {}

            @Override
            public void onClose(InventoryGUI gui, NetPlayer player) {}

            @Override
            public void onClickItem(InventoryGUI gui, InventoryGUI.InventoryItem item, NetPlayer player) {
                player.playSound(Sound.BLOCK_CHEST_OPEN);
                List<ItemStack> items = new ArrayList<>();
                for (String s : NetKitPVP.getInstance().getConfig().getString("kit.items." + item.getName() + ".items").split(", ")) {
                    try {
                        if (s.contains(":")) {
                            ItemStack temp = new ItemStack(Material.valueOf(s.split(":")[0]), 1);
                            temp.setData(new MaterialData(temp.getType()));
                            items.add(temp);
                        } else {
                            items.add(new ItemStack(Material.valueOf(s)));
                        }
                    } catch (Exception ex) {
                        player.sendMessage("Something went wrong, check below.");
                        player.sendException(ex);
                    }
                }
                for (ItemStack theItems : items) {
                    player.getPlayer().getInventory().addItem(theItems);

                }
                player.closeInventory();
                player.sendMessage(MessageManager.getFormat("formats.get-kit", true, new String[]{"<kit>", item.getName()}));
            }
        });
    }

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too few Arguments.", NewNetCommandException.ErrorType.ManyArguments);
        try {
            NetKitPVP.getCoolDown().testCooldown(sender.getName(), 20L);
        } catch (CooldownUnexpiredException e) {
            sender.sendMessage("you cant do that for that amount of seconds nigga");
            return;
        }
        kitInventoryGUI.openInventory(NetPlayer.getPlayerFromPlayer(sender));
    }

    public void givePlayerKit(NetPlayer player) {
        player.giveItem(Material.ACACIA_STAIRS);
        player.giveItem(Material.COOKED_BEEF);
        player.giveItem(Material.COOKED_CHICKEN);
        player.giveItem(Material.COOKED_FISH);
        player.giveItem(Material.GOLDEN_APPLE, 64, (short)2);
    }
}

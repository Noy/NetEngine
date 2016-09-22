package com.noyhillel.netkitpvp.commands;

import com.noyhillel.netkitpvp.MessageManager;
import com.noyhillel.netkitpvp.NetKitPVP;
import com.noyhillel.netkitpvp.game.ConfigManager;
import com.noyhillel.networkengine.exceptions.CooldownUnexpiredException;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.util.effects.NetFireworkEffect;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkengine.util.utils.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;


/**
 * Created by noyhillel1 on 01/07/2014.
 */
@CommandMeta(name = "kit", description = "The Kit Command", usage = "/kit")
public final class KitCommand extends NetAbstractCommandHandler {

    private InventoryGUI kitInventoryGUI;
    private Set<UUID> cooldown = new HashSet<>();

    public KitCommand() {
        final ConfigManager configManager = new ConfigManager();
        kitInventoryGUI = new InventoryGUI(configManager.getInventoryItems(), ChatColor.GOLD + "Kits!", new InventoryGUI.InventoryGUIDelegate() {
            @Override
            public void onOpen(InventoryGUI gui, NetPlayer player) {
                NetFireworkEffect.shootFireWorks(player, player.getLocation());

            }

            @Override
            public void onClose(InventoryGUI gui, NetPlayer player) {}

            @Override
            public void onClickItem(InventoryGUI gui, InventoryGUI.InventoryItem item, NetPlayer player) {
                if (cooldown.contains(player.getUuid())) {
                    player.sendMessage(ChatColor.RED + "You cannot get this kit now.");
                    player.closeInventory();
                    return;
                }
                player.playSound(Sound.CHEST_OPEN);
                List<ItemStack> items = new ArrayList<>();
                for (String s : NetKitPVP.getInstance().getConfig().getString("kit.items." + item.getName() + ".items").split(", ")) {
                    if (s == null) {
                        player.sendMessage(ChatColor.RED + "No Items set! Go to the config and set the items!");
                        return;
                    }
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
                cooldown.add(player.getUuid());
                Bukkit.getScheduler().scheduleSyncDelayedTask(NetKitPVP.getInstance(), () -> {
                    cooldown.remove(player.getUuid());
                    NetKitPVP.logInfo("removed them from the set");
                }, 100L);

            }
        });
    }

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0) throw new NewNetCommandException("Too many Arguments.", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        if (!sender.isOp()) {
            try {
                NetKitPVP.getCoolDown().testCooldown(sender.getName(), 20L);
            } catch (CooldownUnexpiredException e) {
                sender.sendMessage("cooldown works lol");
                return;
            }
        }
        kitInventoryGUI.openInventory(NetPlayer.getPlayerFromPlayer(sender));
    }
}

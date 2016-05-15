package com.noyhillel.survivalgames.utils.inventory;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.player.SGPlayer;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@ToString
public class InventoryGUI implements Listener {
    private final InventoryGUIDelegate delegate;
    private Inventory inv;
    private String name;
    private List<InventoryGUIItem> items;
    private boolean eventsRegistered = false;

    private List<SGPlayer> playersWithInventories = new ArrayList<>();

    public InventoryGUI(List<InventoryGUIItem> guiItems, InventoryGUIDelegate delegate, String name) {
        this.items = guiItems;
        this.name = name;
        this.delegate = delegate;
        updateInventory();
    }

    public InventoryGUI(List<InventoryGUIItem> guiItems, InventoryGUIDelegate delegate) {
        this(guiItems, delegate, null);
    }

    private Integer getSize() {
        return 27;
    }

    void updateInventory() {
        String finalName = name == null ? "Inventory GUI" : name;
        boolean reopen = false;
        boolean createNewInventory = this.inv == null;
        if (createNewInventory || !finalName.equals(this.inv.getTitle())) {
            reopen = createNewInventory;
            this.inv = Bukkit.createInventory(null, getSize(), finalName);
        }
        else this.inv.clear();
        for (InventoryGUIItem item : this.items) {
            Integer slot = item.getSlot();
            ItemStack representationItem = item.getRepresentationItem();
            if (slot == null || slot == -1) this.inv.addItem(representationItem);
            else this.inv.setItem(slot, representationItem);
        }
        if (reopen) {
            for (SGPlayer playersWithInventory : playersWithInventories) {
                Player player = playersWithInventory.getPlayer();
                player.closeInventory();
                player.openInventory(this.inv);
            }
        }
    }

    public void open(Player player) {
        open(SurvivalGames.getInstance().getSGPlayerManager().getOnlinePlayer(player));
    }

    public void open(SGPlayer player) {
        if (this.playersWithInventories.contains(player)) return;
        this.playersWithInventories.add(player);
        player.getPlayer().openInventory(inv);
        if (!eventsRegistered) {
            SurvivalGames.getInstance().registerListener(this);
            this.eventsRegistered = true;
        }
    }

    void closed(SGPlayer player) {
        this.playersWithInventories.remove(player);
        if (this.playersWithInventories.size() == 0) {
            HandlerList.unregisterAll(this);
            this.eventsRegistered = false;
        }
    }

    public void closeAll() {
        for (SGPlayer player : this.playersWithInventories) {
            player.getPlayer().closeInventory();
            closed(player);
        }
    }

    public void close(SGPlayer player) {
        player.getPlayer().closeInventory();
        closed(player);
    }

    public void setItems(List<InventoryGUIItem> items) {
        this.items = items;
        updateInventory();
    }

    public void setTitle(String title) {
        this.name = title;
        updateInventory();
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player)) return;
        SGPlayer SGPlayer = SurvivalGames.getInstance().getSGPlayerManager().getOnlinePlayer((Player) whoClicked);
        if (!this.playersWithInventories.contains(SGPlayer)) return;
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        InventoryGUIItem finalItem = null;
        for (InventoryGUIItem item : this.items) {
            if ((item.getSlot() == null && item.getRepresentationItem().equals(event.getCurrentItem())) || (item.getSlot() != null && item.getSlot().equals(event.getSlot()))) {
                finalItem = item;
                break;
            }
        }
        this.delegate.playerClickedItem(SGPlayer, finalItem, this);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity player = event.getPlayer();
        if (!(player instanceof Player)) return;
        SGPlayer SGPlayer = SurvivalGames.getInstance().getSGPlayerManager().getOnlinePlayer((Player) player);
        if (!this.playersWithInventories.contains(SGPlayer)) return;
        closed(SGPlayer);
    }
}
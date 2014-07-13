package com.noyhillel.networkengine.util.utils;

        import com.noyhillel.networkengine.util.NetPlugin;
        import com.noyhillel.networkengine.util.player.NetPlayer;
        import lombok.Getter;
        import lombok.Setter;
        import lombok.ToString;
        import org.bukkit.Bukkit;
        import org.bukkit.ChatColor;
        import org.bukkit.entity.Player;
        import org.bukkit.event.EventHandler;
        import org.bukkit.event.Listener;
        import org.bukkit.event.inventory.InventoryClickEvent;
        import org.bukkit.event.inventory.InventoryCloseEvent;
        import org.bukkit.inventory.Inventory;
        import org.bukkit.inventory.ItemStack;
        import org.bukkit.inventory.meta.ItemMeta;

        import java.util.ArrayList;
        import java.util.List;

/**
 * Created by Noy on 26/05/2014.
 */
public final class InventoryGUI implements Listener {

    /**
     * Private field, Creating a list of our 'Inventory' class we created earlier.
     */
    @Getter private final List<InventoryItem> items;
    /**
     * Private field, The title of the Inventory.
     */
    @Getter private final String title;
    /**
     * Private field, our Interface we created earlier
     */
    @Getter private final InventoryGUIDelegate delegate;
    /**
     * Private field, the Inventory.
     */
    @Getter private final Inventory inventory;

    /**
     * Creating our constructor.
     * @param items The Items.
     * @param title The Title.
     * @param delegate The delegate.
     */
    public InventoryGUI(ArrayList<InventoryItem> items, String title, InventoryGUIDelegate delegate) {
        Bukkit.getServer().getPluginManager().registerEvents(this, NetPlugin.getInstance());
        this.items = items;
        this.title = title;
        this.delegate = delegate;
        this.inventory = Bukkit.createInventory(null, checkInventorySize(), title);
        updateInventoryItems(items);
    }

    /**
     * Update Inventory Item method.
     * @param items The Items.
     */
    public void updateInventoryItems(ArrayList<InventoryItem> items) {
        inventory.clear();
        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            InventoryItem item = items.get(i);
            if (item == null) continue;
            item.setSlot(i);
            inventory.setItem(i, item.getItem());
        }
    }

    /**
     * Making sure the Inventory size is a multiple of 9
     * @return a multiple of 9
     */
    private Integer checkInventorySize() {
        if (items == null) return 9; // If there are no items, 9 slots.
        if (items.size() <= 9) return 9; // If less than or equal to, 9 slots.
        else if (items.size() <= 18) return 18; // If more? 18 slots.
        else if (items.size() <= 27) return 27; // More? 27 slots.
        else return 36; // Even more? The default double chest size. 36 slots.
    }

    /**
     * Opens the player's Inventory.
     * @param player The Player
     */
    public void openInventory(NetPlayer player) {
        player.openInventory(inventory);
        delegate.onOpen(this, player);
    }

    /**
     * Closes the player's Inventory.
     * @param player The Player
     */
    public void closeInventory(NetPlayer player) {
        player.closeInventory();
    }

    /* Events */

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if(!event.getInventory().getTitle().equalsIgnoreCase(this.inventory.getTitle())) return;
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer((Player) event.getPlayer());
        delegate.onClose(this, netPlayer);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getInventory().getTitle().equalsIgnoreCase(this.inventory.getTitle())) return;
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer((Player) event.getWhoClicked());
        Boolean b = false;
        switch (event.getClick()) {
            case RIGHT:
            case LEFT:
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
            case MIDDLE:
            case NUMBER_KEY:
            case DROP:
                b = true;
                break;
        }
        if (!b) return;
        for (InventoryItem item : this.items) {
            if (item == null) continue;
            if (event.getCurrentItem() == null) continue;
            if (!(event.getCurrentItem().equals(item.getItem()))) continue;
            this.delegate.onClickItem(this, item, netPlayer);
        }
        event.setCancelled(true); // That's why it didn't work...
    }

    /**
     * Our static class, represents the Inventory Item.
     */
    @ToString
    public static class InventoryItem {
        /**
         * Private field, The item.
         */
        @Getter private ItemStack item;
        /**
         * Private field, The name of the item.
         */
        @Getter private String name;

        /**
         * Private field, the slot of the item.
         */
        @Getter @Setter private Integer slot;

        /**
         * Simplified constructor
         * @param item The Item
         * @param name The Item name.
         */
        public InventoryItem(ItemStack item, String name) {
            this(item, name, null);
        }

        /**
         * Creating our constructor.
         * @param item The Item
         * @param name The Item name.
         * @param lore The lore of the Item
         */
        public InventoryItem(ItemStack item, String name, List<String> lore) {
            this.item = item;
            this.name = ChatColor.stripColor(name);
            ItemMeta meta = item.getItemMeta(); // The ItemMeta
            meta.setDisplayName(name); // Sets the display name as our constructor parameter.
            if (lore != null) { // Loop through lore Strings.
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
                meta.setLore(lore); // Set lore
            }
            item.setItemMeta(meta); // Set the Item Meta
        }
    }

    /**
     * Our Inventory Delegate. This represents what happens when the Item is clicked, when the Inventory is Opened and when the Inventory is closed.
     */
    public interface InventoryGUIDelegate {
        void onOpen(InventoryGUI gui, NetPlayer player);
        void onClose(InventoryGUI gui, NetPlayer player);
        void onClickItem(InventoryGUI gui, InventoryItem item, NetPlayer player);

    }
}

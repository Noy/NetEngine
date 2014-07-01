package com.noyhillel.netkitpvp.game;

import com.noyhillel.netkitpvp.NetKitPVP;
import com.noyhillel.networkengine.util.utils.InventoryGUI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by noyhillel1 on 01/07/2014.
 */
public class ConfigManager {

    /**
     * Our List which Stored the Inventory Item
     */
    @Getter private ArrayList<InventoryGUI.InventoryItem> inventoryItems;

    /**
     * Creating our constructor
     */
    public ConfigManager() {
        // Initialise both.
        inventoryItems = new ArrayList<>();
        // Creating local variable.
        ConfigurationSection section = NetKitPVP.getInstance().getConfig().getConfigurationSection("kit.items");
        Set<String> sectionKeys = section.getKeys(false); // Storing a String in a Set, just so it wouldn't create duplicates.
        for (String s1 : sectionKeys) {
            ConfigurationSection section1 = section.getConfigurationSection(s1);
            String name = ChatColor.translateAlternateColorCodes('&', section1.getString("name")); // Set the name
            List<String> lore = section1.getStringList("lore");
            for (String s2 : lore) {
                lore.set(lore.indexOf(s2), ChatColor.translateAlternateColorCodes('&', s2));
            } // Set the lore.
            inventoryItems.add(new InventoryGUI.InventoryItem(getItem(section1.getString("item")), name, lore)); // Adding our Item, Item name and Lore to the Item.
        }
    }

    /**
     * Get Item Method
     * @param name Name of Item
     * @return the Material.
     */
    private ItemStack getItem(String name) {
        Material material;
        try {
            material = Material.getMaterial(name.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
            material = Material.CARROT; // Carrot seeds, who would use this?
        }
        return new ItemStack(material);
    }
}

package com.noyhillel.networkhub.items.warpitem;

import com.noyhillel.networkengine.util.utils.InventoryGUI;
import com.noyhillel.networkhub.NetHub;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Noy on 26/05/2014.
 */
public final class ConfigManager {

    /**
     * Our List which Stored the Inventory Item
     */
    @Getter private ArrayList<InventoryGUI.InventoryItem> inventoryItems = new ArrayList<>();
    private HashMap<String, Location> locations = new HashMap<>();

    /**
     * Creating our constructor
     */
    public ConfigManager() {
        // Creating local variable.
        ConfigurationSection section = NetHub.getInstance().getConfig().getConfigurationSection("hub.warps");
        if (section == null) {
            System.out.println("Please set some locations");
            return;
        }
        Set<String> sectionKeys = section.getKeys(false); // Storing a String in a Set, just so it wouldn't create duplicates.
        for (String s1 : sectionKeys) {
            ConfigurationSection section1 = section.getConfigurationSection(s1);
            String name = ChatColor.translateAlternateColorCodes('&', section1.getString("name")); // Set the name
            List<String> lore = section1.getStringList("lore");
            for (String s2 : lore) {
                lore.set(lore.indexOf(s2), ChatColor.translateAlternateColorCodes('&', s2));
            } // Set the lore.
            inventoryItems.add(new InventoryGUI.InventoryItem(getItem(section1.getString("item")), name, lore)); // Adding our Item, Item name and Lore to the Item.
            locations.put(s1.toLowerCase(), getLocation(section1.getConfigurationSection("location"))); // Store the location.
        }
    }

    /**
     * The getLocation method, this determines where the Player will teleport to (Stored in the config)
     * @param s The Configuration section
     * @return Location
     */
    private Location getLocation(ConfigurationSection s) {
        if (s == null) return null; // I know I'm gonna forget this. EDIT: I did.
        return new Location(
                Bukkit.getWorld(s.getString("world")),
                s.getDouble("x"),
                s.getDouble("y"),
                s.getDouble("z"),
                (float) s.getDouble("yaw"),
                (float) s.getDouble("pitch")
        );
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

    /**
     * getLocation Method.
     * @param s String
     * @return null
     */
    Location getLocation(String s) {
        if (locations.containsKey(s.toLowerCase())) return locations.get(s.toLowerCase());
        return null;
    }
}
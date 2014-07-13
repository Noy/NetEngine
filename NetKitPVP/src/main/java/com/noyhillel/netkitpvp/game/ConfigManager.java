package com.noyhillel.netkitpvp.game;

import com.noyhillel.netkitpvp.NetKitPVP;
import com.noyhillel.networkengine.util.utils.InventoryGUI;
import lombok.Getter;
import org.bukkit.ChatColor;
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

    @Getter
    private ArrayList<InventoryGUI.InventoryItem> inventoryItems = new ArrayList<>();
    ;

    public ConfigManager() {
        // Creating local variable.
        ConfigurationSection section = NetKitPVP.getInstance().getConfig().getConfigurationSection("kit.items");
        Set<String> sectionKeys = section.getKeys(false); // Storing a String in a Set, just so it wouldn't create duplicates.
        for (String s1 : sectionKeys) {
            ConfigurationSection section1 = section.getConfigurationSection(s1);
            String name = ChatColor.LIGHT_PURPLE + section1.getString("name"); // Set the name
            List<String> lore = section1.getStringList("lore");
            for (String s2 : lore) {
                lore.set(lore.indexOf(s2), ChatColor.translateAlternateColorCodes('&', s2));
            } // Set the lore.
            inventoryItems.add(new InventoryGUI.InventoryItem(getItem(section1.getString("item")), name, lore)); // Adding our Item, Item name and Lore to the Item.
        }
    }

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

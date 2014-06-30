package com.noyhillel.survivalgames.game.loots;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.game.GameException;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public final class Tier {
    @NonNull private final String filename;
    private List<RawTierItem> rawItems;

    List<ItemStack> getRandomItems(Integer amount) {
        ArrayList<ItemStack> items = new ArrayList<>();
        Random random = SurvivalGames.getRandom();
        int size = rawItems.size();
        do {
            items.add(rawItems.get(random.nextInt(size)).getItemStack());
        } while (items.size() < amount);

        return items;
    }

    public void load() throws GameException {
        try {
            this.rawItems = getAllRawTierItems((JSONArray) getValueOfResourceFile(filename).get("items"));
        } catch (Exception e) {
            throw new GameException(e, null, "Could not load tier " + filename);
        }
    }

    static JSONObject getValueOfResourceFile(String filename) {
        String fileData = SurvivalGames.getInstance().getFileData(filename);
        if (fileData == null) return null;
        try {
            return (JSONObject) JSONValue.parse(fileData);
        } catch (ClassCastException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static List<RawTierItem> getAllRawTierItems(JSONArray object) {
        List<RawTierItem> rawTierItems = new ArrayList<>();
        for (Object o : object) {
            if (!(o instanceof JSONObject)) continue;
            JSONObject rawTierItemRaw = (JSONObject)o;
            String materialName = getJSONValue(rawTierItemRaw, "material", String.class);
            Long quantity = getJSONValue(rawTierItemRaw, "quantity", Long.class);
            JSONArray enchantments = getJSONValue(rawTierItemRaw, "enchantments", JSONArray.class);
            String name = getJSONValue(rawTierItemRaw, "name", String.class);
            JSONArray lore = getJSONValue(rawTierItemRaw, "lore", JSONArray.class);

            if (materialName == null || quantity == null) continue;
            Material material = Material.valueOf(materialName);
            if (material == null) continue;

            List<RawItemEnchant> rawItemEnchants = new ArrayList<>();
            if (enchantments != null) {
                for (Object enchantment : enchantments) {
                    if (!(enchantment instanceof JSONObject)) continue;
                    JSONObject enchant = (JSONObject) enchantment;
                    String enchantment1 = getJSONValue(enchant, "enchantment", String.class);
                    Integer level = getJSONValue(enchant, "level", Integer.class, 1);
                    if (enchantment1 == null) continue;
                    Enchantment byName = Enchantment.getByName(enchantment1);
                    if (byName == null) continue;
                    rawItemEnchants.add(new RawItemEnchant(byName, level));
                }
            }

            List<String> loreFinal = new ArrayList<>();
            for (Object o1 : lore) {
                if (!(o1 instanceof String)) continue;
                loreFinal.add((String)o1);
            }
            rawTierItems.add(new RawTierItem(material, quantity.intValue(), rawItemEnchants, name, loreFinal));
        }
        return rawTierItems;
    }

    public static <T> T getJSONValue(JSONObject object, String key, Class<T> expected, T defaultValue) {
        try {
            //noinspection unchecked
            return (T) object.get(key);
        } catch (ClassCastException ex) {
            return defaultValue;
        }
    }

    public static <T> T getJSONValue(JSONObject object, String key, Class<T> expected) {
        return getJSONValue(object, key, expected, null);
    }

    @Data
    private static final class RawTierItem {
        @NonNull private final Material material;
        @NonNull private final Integer quantity;
        private final List<RawItemEnchant> itemEnchants;
        private final String name;
        private final List<String> lore;

        public ItemStack getItemStack() {
            ItemStack result = new ItemStack(material, quantity);
            if (this.itemEnchants != null) {
                for (RawItemEnchant itemEnchant : itemEnchants) {
                    result.addUnsafeEnchantment(itemEnchant.getEnchantment(), itemEnchant.getLevel());
                }
            }
            ItemMeta meta = result.getItemMeta();
            if (this.name != null) {
                meta.setDisplayName(colorize(name));
            }
            if (this.lore != null) {
                List<String> lorez = new ArrayList<>();
                for (String s : lore) {
                    lorez.add(colorize(s));
                }
                meta.setLore(lorez);
            }
            result.setItemMeta(meta);
            return result;
        }

        private static String colorize(String s) {return ChatColor.translateAlternateColorCodes('&', s);}
    }

    @Data
    private static final class RawItemEnchant {
        private final Enchantment enchantment;
        private final Integer level;
    }
}

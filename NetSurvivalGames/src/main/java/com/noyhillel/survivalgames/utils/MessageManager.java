package com.noyhillel.survivalgames.utils;

import com.noyhillel.survivalgames.SurvivalGames;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * This class is in here to prevent IntelliJ messin' up.
 */
public final class MessageManager {

    @SuppressWarnings("NullArgumentToVariableArgMethod")
    public static String getFormats(String format) {
        return getFormat(format, true, null);
    }

    public static String getFormat(String format, boolean prefix, String[]... dataArgs) {
        String format1 = getFormat(format, dataArgs);
        if (!prefix) return format1;
        String prefixString = getFormat("formats.prefix");
        return prefixString + format1;
    }

    public static String getFormat(String format, String[]... dataArgs) {
        FileConfiguration config = SurvivalGames.getInstance().getConfig();
        if (!config.contains(format))
            return format;
        Object t = config.get(format);
        String tigga;
        try {
            tigga = (String)t;
        } catch (ClassCastException ex) {
            return format;
        }
        if (dataArgs != null) {
            for (String[] dataPart : dataArgs) {
                if (dataPart.length != 2) continue;
                tigga = tigga.replaceAll(dataPart[0], dataPart[1]);
            }
        }
        tigga = ChatColor.translateAlternateColorCodes('&', tigga);
        return tigga;
    }
}

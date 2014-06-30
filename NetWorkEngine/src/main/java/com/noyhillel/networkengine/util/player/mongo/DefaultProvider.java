package com.noyhillel.networkengine.util.player.mongo;

import com.noyhillel.networkengine.util.NetPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by Noy on 12/06/2014.
 */
public final class DefaultProvider implements Provider {

    @Override
    public NetDatabase getNewDatabase(NetPlugin plugin) throws DatabaseConnectException {
        FileConfiguration config = plugin.getDatabaseConfiguration().getConfig();
        //This will get the database values from the database.yml file
        return new NetMongoDatabase(
                config.getString("host", "127.0.0.1"),
                config.getInt("port", 28017),
                config.getString("database", "minecraft"),
                config.getString("username", null),
                config.getString("password", null),
                config.getString("collectionPrefix")
        );
    }
}
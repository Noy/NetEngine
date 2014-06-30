package com.noyhillel.survivalgames.storage;

import com.noyhillel.survivalgames.SurvivalGames;
import org.bukkit.configuration.ConfigurationSection;

public enum StorageTypes {
    MYSQL(new GStorageSetup() {
        @Override
        public GStorage getStorage() {
            ConfigurationSection database = SurvivalGames.getInstance().getConfig().getConfigurationSection("database");
            return new MySQLStorage(
                    database.getString("host", "localhost"), //Host
                    database.getInt("port", 3306), // Port
                    database.getString("database", "minecraft"), //Database
                    database.getString("username", "root"), //Username
                    database.getString("password", "root") //Password
            );
        }
    }, MySQLStorage.class),
    FORGETFUL(new GStorageSetup() {
        @Override
        public GStorage getStorage() {
            return new ForgetfulStorage();
        }
    }, ForgetfulStorage.class);

    private Class<? extends GStorage> clazz;
    private GStorageSetup setupDelegate;

    StorageTypes(GStorageSetup setupDelegate, Class<? extends GStorage> clazz) {
        this.setupDelegate = setupDelegate;
        this.clazz = clazz;
    }

    public Class<? extends GStorage> getClazz() {
        return clazz;
    }

    public GStorageSetup getSetupDelegate() {
        return setupDelegate;
    }
}

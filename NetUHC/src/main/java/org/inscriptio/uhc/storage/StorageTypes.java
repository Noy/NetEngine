package org.inscriptio.uhc.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.inscriptio.uhc.NetUHC;

public enum StorageTypes {
    MYSQL((GStorageSetup) () -> {
        ConfigurationSection database = NetUHC.getInstance().getConfig().getConfigurationSection("database");
        return new MySQLStorage(
                database.getString("host", "localhost"), //Host
                database.getInt("port", 3306), // Port
                database.getString("database", "minecraft"), //Database
                database.getString("username", "root"), //Username
                database.getString("password", "root") //Password
        );
    }, MySQLStorage.class),
    FORGETFUL((GStorageSetup) ForgetfulStorage::new, ForgetfulStorage.class);

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

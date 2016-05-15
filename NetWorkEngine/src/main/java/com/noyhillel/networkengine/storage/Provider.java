package com.noyhillel.networkengine.storage;

import com.noyhillel.networkengine.exceptions.DatabaseConnectException;
import com.noyhillel.networkengine.util.NetPlugin;

/**
 * Created by Noy on 12/06/2014.
 */
public interface Provider {
    NetDatabase getNewDatabase(NetPlugin plugin) throws DatabaseConnectException;
}

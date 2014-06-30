package com.noyhillel.networkengine.util.player.mongo;

/**
 * Created by Noy on 11/06/2014.
 */
public interface NetDatabase {
    void connect() throws DatabaseConnectException;
    void disconnect() throws DatabaseConnectException;
}

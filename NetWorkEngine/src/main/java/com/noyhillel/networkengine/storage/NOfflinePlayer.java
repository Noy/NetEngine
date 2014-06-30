package com.noyhillel.networkengine.storage;

import com.noyhillel.networkengine.util.player.mongo.DatabaseConnectException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Noy on 12/06/2014.
 */
public interface NOfflinePlayer {

    List<String> getKnownUsersNames();

    String getLastKnownUserNames();

    UUID getUUID();

    List<String> getKnownIPAddress();

    Date getFirstTimeOnline();

    Date getLastTimeOnline();

    Long getMillisecondsOnline();

    NPlayer getPlayer();

    void updateFromDatabase() throws DatabaseConnectException;

    void saveIntoDatabase() throws DatabaseConnectException;



}

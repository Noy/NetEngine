package com.noyhillel.networkengine.storage;

import com.noyhillel.networkengine.exceptions.NPlayerJoinException;

import java.net.InetAddress;

/**
 * Created by Noy on 12/06/2014.
 */
public interface NPlayerConnectionListener {

    void onPlayerLogin(NPlayer player, InetAddress address) throws NPlayerJoinException;

    void onPlayerDisconnect(NPlayer player);
}

package com.noyhillel.networkengine.storage;

import com.noyhillel.networkengine.exceptions.NPlayerJoinException;
import com.noyhillel.networkengine.util.player.mongo.DatabaseConnectException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by Noy on 12/06/2014.
 */
public interface NPlayerManager extends Iterable<NPlayer> {

    List<NOfflinePlayer> getOfflinePlayerByName(String name);

    NOfflinePlayer getOfflinePlayerByUUID(UUID uuid);

    List<NOfflinePlayer> getOfflinePlayersByUUIDS(List<UUID> uuids);

    NOfflinePlayer getNOfflinePlayerForOfflinePlayer(OfflinePlayer player);

    Collection<NPlayer> getOnlinePlayers();

    NPlayer getNPlayerForPlayer(Player player);

    NPlayer getOnlineCPlayerForUUID(UUID uuid);

    NPlayer getOnlineNPlayerForName(String name);


    List<NPlayer> getCPlayerByStartOfName(String name);

    void savePlayerData(NOfflinePlayer player) throws DatabaseConnectException;

    void playerLoggedIn(Player player, InetAddress address) throws NPlayerJoinException;

    void playerLoggedOut(Player player);

    void onDisable();

    void registerCPlayerConnectionListener(NPlayerConnectionListener processor);

    void unregisterCPlayerConnectionListener(NPlayerConnectionListener processor);

}

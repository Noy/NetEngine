package com.noyhillel.networkengine.storage;

import com.noyhillel.networkengine.util.player.mongo.DatabaseConnectException;
import com.noyhillel.networkengine.util.utils.NetWorkCoolDown;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 12/06/2014.
 */
public interface NPlayer {

    String getName();

    String getIpAddress();

    boolean isOnline();

    void sendMessage(String... args);

    void sendFullChatMessage(String... args);

    void clearChat();

    void playSoundForPlayer(Sound sound, Float volume, Float pitch);

    void playSoundForPlayer(Sound sound, Float pitch);

    void playSoundForPlayer(Sound sound);

    Player getBukkitPlayer();

    NetWorkCoolDown getCooldown();

    void saveIntoDatabase() throws DatabaseConnectException;

}

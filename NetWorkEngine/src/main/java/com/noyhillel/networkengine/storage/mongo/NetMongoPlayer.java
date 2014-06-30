package com.noyhillel.networkengine.storage.mongo;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.noyhillel.networkengine.storage.NPlayer;
import com.noyhillel.networkengine.util.player.mongo.DatabaseConnectException;
import com.noyhillel.networkengine.util.player.mongo.MongoKey;
import com.noyhillel.networkengine.util.utils.NetWorkCoolDown;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.UUID;

/**
 * Created by Noy on 12/06/2014.
 */
@Data
public final class NetMongoPlayer implements NPlayer {

    @Getter private final String username;
    @SuppressWarnings("Lombok")
    @Getter private Player bukkitPlayer;
    @Getter private UUID uniqueIdentifier;
    private PermissionAttachment permissionAttachment;
    @Getter private boolean firstJoin = false;
    @Getter private String address = null;
    @Getter private final NetWorkCoolDown cooldownManager = new NetWorkCoolDown();
    @Getter @Setter
    private ObjectId objectId;

    public NetMongoPlayer(UUID uniqueIdentifier, Player player, DBObject dbObject) {
        if (dbObject == null) {
            this.objectId = null;
            this.uniqueIdentifier = uniqueIdentifier;
        }
        this.username = player.getName();
        bukkitPlayer = player;
        this.objectId = getValueFrom(dbObject, MongoKey.ID_KEY, ObjectId.class);
    }

    public final DBObject getObjectForPlayer() {
        BasicDBObjectBuilder objectBuilder = new BasicDBObjectBuilder();
        if (this.objectId != null) objectBuilder.add(MongoKey.ID_KEY.toString(), this.objectId);
        objectBuilder.add(MongoKey.UUID_KEY.toString(), uniqueIdentifier.toString());
        return objectBuilder.get();
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    @Override
    public String getIpAddress() {
        return bukkitPlayer.getAddress().getHostString();
    }

    @Override
    public boolean isOnline() {
        return bukkitPlayer.isOnline();
    }

    @Override
    public void sendMessage(String... args) {
        for (String m : args) {
            bukkitPlayer.sendMessage(m);
        }
    }

    @Override
    public void sendFullChatMessage(String... args) {
    }

    @Override
    public void clearChat() {

    }

    @Override
    public void playSoundForPlayer(Sound sound, Float volume, Float pitch) {
        bukkitPlayer.playSound(bukkitPlayer.getLocation(), sound, volume, pitch);
    }

    @Override
    public void playSoundForPlayer(Sound sound, Float pitch) {
        playSoundForPlayer(sound, 10F, pitch);
    }

    @Override
    public void playSoundForPlayer(Sound sound) {
        playSoundForPlayer(sound, 10F, 1F);
    }

    @Override
    public Player getBukkitPlayer() {
        return bukkitPlayer.getPlayer();
    }

    @Override
    public NetWorkCoolDown getCooldown() {
        return null;
    }

    @Override
    public void saveIntoDatabase() throws DatabaseConnectException {
    }

    public static <T> T getValueFrom(DBObject object, @NonNull Object key, Class<T> clazz) {
        return getValueFrom(object, key.toString(), clazz);
    }

    @SuppressWarnings("UnusedParameters")
    public static <T> T getValueFrom(DBObject object, @NonNull String key, Class<T> clazz) {
        if (object == null) return null;
        try {
            //noinspection unchecked
            return (T) (object.get(key));
        } catch (ClassCastException ex) {
            return null;
        }
    }
}


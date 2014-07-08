package com.noyhillel.networkengine.util.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;

public final class Packet28EntityMetadata extends AbstractPacket {

    public static final int ID = 40;

    public Packet28EntityMetadata() {
        super(new PacketContainer(ID), ID);
        handle.getModifier().writeDefaults();
    }

    @SuppressWarnings("unused")
    public Packet28EntityMetadata(PacketContainer packet) {
        super(packet, ID);
    }

    @SuppressWarnings("unused")
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int value) {
        handle.getIntegers().write(0, value);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    @SuppressWarnings("unused")
    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    @SuppressWarnings("unused")
    public List<WrappedWatchableObject> getEntityMetadata() {
        return handle.getWatchableCollectionModifier().read(0);
    }

    public void setEntityMetadata(List<WrappedWatchableObject> value) {
        handle.getWatchableCollectionModifier().write(0, value);
    }
}

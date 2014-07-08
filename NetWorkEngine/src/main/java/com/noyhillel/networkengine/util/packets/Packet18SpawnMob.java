package com.noyhillel.networkengine.util.packets;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public final class Packet18SpawnMob extends AbstractPacket {

    public static final Integer ID = 24;

    private static PacketConstructor entityConstructor;

    public Packet18SpawnMob() {
        super(new PacketContainer(ID), ID);
        handle.getModifier().writeDefaults();
    }

    public Packet18SpawnMob(PacketContainer packet) {
        super(packet, ID);
    }

    public Packet18SpawnMob(Entity entity) {
        super(fromEntity(entity), ID);
    }

    private static PacketContainer fromEntity(Entity entity) {
        if (entityConstructor == null) {
            entityConstructor = ProtocolLibrary.getProtocolManager().createPacketConstructor(ID, entity);
        }
        return entityConstructor.createPacket(entity);
    }

    public Integer getEntityID() {
        return handle.getIntegers().read(0);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    
    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    public void setEntityID(Integer value) {
        handle.getIntegers().write(0, value);
    }

    public EntityType getType() {
        return EntityType.fromId(handle.getIntegers().read(1));
    }

    public void setType(EntityType value) {
        handle.getIntegers().write(1, (int) value.getTypeId());
    }

    public Double getX() {
        return handle.getIntegers().read(2) / 32.0D;
    }

    public void setX(Double value) {
        handle.getIntegers().write(2, (int) Math.floor(value * 32.0D));
    }

    public Double getY() {
        return handle.getIntegers().read(3) / 32.0D;
    }

    public void setY(Double value) {
        handle.getIntegers().write(3, (int) Math.floor(value * 32.0D));
    }

    public Double getZ() {
        return handle.getIntegers().read(4) / 32.0D;
    }

    public void setZ(Double value) {
        handle.getIntegers().write(4, (int) Math.floor(value * 32.0D));
    }

    public Float getYaw() {
        return (handle.getBytes().read(0) * 360.F) / 256.0F;
    }

    public void setYaw(Float value) {
        handle.getBytes().write(0, (byte) (value * 256.0F / 360.0F));
    }

    public Float getPitch() {
        return (handle.getBytes().read(1) * 360.F) / 256.0F;
    }

    public void setPitch(Float value) {
        handle.getBytes().write(1, (byte) (value * 256.0F / 360.0F));
    }

    public Float getHeadYaw() {
        return (handle.getBytes().read(2) * 360.F) / 256.0F;
    }

    public void setHeadYaw(Float value) {
        handle.getBytes().write(2, (byte) (value * 256.0F / 360.0F));
    }

    public Double getVelocityX() {
        return handle.getIntegers().read(5) / 8000.0D;
    }
    
    public void setVelocityX(Double value) {
        handle.getIntegers().write(5, (int) (value * 8000.0D));
    }
    
    public Double getVelocityY() {
        return handle.getIntegers().read(6) / 8000.0D;
    }
    
    public void setVelocityY(Double value) {
        handle.getIntegers().write(6, (int) (value * 8000.0D));
    }
    
    public Double getVelocityZ() {
        return handle.getIntegers().read(7) / 8000.0D;
    }

    
    public void setVelocityZ(Double value) {
        handle.getIntegers().write(7, (int) (value * 8000.0D));
    }
    
    public WrappedDataWatcher getMetadata() {
        return handle.getDataWatcherModifier().read(0);
    }

    public void setMetadata(WrappedDataWatcher value) {
        handle.getDataWatcherModifier().write(0, value);
    }
}

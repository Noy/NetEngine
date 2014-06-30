package com.noyhillel.networkengine.util.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.google.common.primitives.Ints;

import java.util.List;

/**
 * Created by Noy on 28/05/2014.
 */
public final class Packet1DDestroyEntity extends AbstractPacket {
    public static final Integer ID = 29;

    public Packet1DDestroyEntity() {
        super(new PacketContainer(ID), ID);
        handle.getModifier().writeDefaults();
    }

    public Packet1DDestroyEntity(PacketContainer packet) {
        super(packet, ID);
    }

    public List<Integer> getEntities() {
        return Ints.asList(handle.getIntegerArrays().read(0));
    }

    public void setEntities(int[] entities) {
        handle.getIntegerArrays().write(0, entities);
    }

    public void setEntities(List<Integer> entities) {
        setEntities(Ints.toArray(entities));
    }


}

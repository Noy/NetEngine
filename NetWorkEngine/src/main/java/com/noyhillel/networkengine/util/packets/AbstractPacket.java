package com.noyhillel.networkengine.util.packets;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.PacketContainer;
import lombok.Getter;

/**
 * Created by Noy on 28/05/2014.
 */
public abstract class AbstractPacket {

    // The packet we will be modifying
    @Getter protected PacketContainer handle;

    protected AbstractPacket(PacketContainer handle, Integer packetID) {
        // Make sure we're given a valid packet
        if (handle == null) {
            throw new IllegalArgumentException("Packet handle cannot be mull.");
        }
        if (handle.getID() != packetID) {
            throw new IllegalArgumentException(handle.getHandle() + " is not a packet " + Packets.getDeclaredName(packetID) + "(" + packetID + ")");
        }
        this.handle = handle;
    }
}
package net.arvaux.core.util.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public abstract class AbstractPacket {

    // The packet we will be modifying
    protected PacketContainer handle;

    /**
     * Constructs a new strongly typed wrapper for the given packet.
     *
     * @param handle - handle to the raw packet data.
     * @param type   - the packet type.
     */
    protected AbstractPacket(PacketContainer handle, PacketType type) {
        // Make sure we're given a valid packet
        Objects.requireNonNull(handle, "Container can't be null!");
        Objects.requireNonNull(type, "Type can't be null!");

        if (!Objects.equals(handle.getType(), type)) {
            throw new IllegalArgumentException(handle.getHandle() + " is not a packet of type " + type);
        }

        this.handle = handle;
    }

    /**
     * Retrieve a handle to the raw packet data.
     *
     * @return Raw packet data.
     */
    public PacketContainer getHandle() {
        return handle;
    }

    public void sendPacket(Player... receivers) {
        for (Player player : receivers) {
            this.sendPacket(player);
        }
    }

    /**
     * Send the current packet to the given receiver.
     *
     * @param receiver - the receiver.
     * @throws RuntimeException If the packet cannot be sent.
     */
    public void sendPacket(Player receiver) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, getHandle());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet.", e);
        }
    }

    /**
     * Simulate receiving the current packet from the given sender.
     *
     * @param sender - the sender.
     * @throws RuntimeException If the packet cannot be received.
     */
    public void recievePacket(Player sender) {
        try {
            ProtocolLibrary.getProtocolManager().recieveClientPacket(sender, getHandle());
        } catch (Exception e) {
            throw new RuntimeException("Cannot receive packet.", e);
        }
    }
}

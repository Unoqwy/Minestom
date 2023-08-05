package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listen to outgoing packets asynchronously.
 * <p>
 * Currently, do not support viewable packets.
 */
@ApiStatus.Experimental
public class PlayerPacketOutEvent implements PlayerEvent, CancellableEvent {
    private final Player player;
    private final ServerPacket packet;
    private ServerPacket packetOverride = null;
    private boolean cancelled;

    public PlayerPacketOutEvent(Player player, ServerPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Gets the original packet this event was created for.
     *
     * @return packet being sent
     */
    public @NotNull ServerPacket getPacket() {
        return packet;
    }

    /**
     * Gets the packet that will be sent to the player,
     * taking into consideration {@link #getPacketOverride()}.
     *
     * @return effective packet that will be sent
     */
    public @NotNull ServerPacket getEffectivePacket() {
        return packetOverride != null ? packetOverride : packet;
    }

    /**
     * Gets the overriding packet.
     * If not null, it will be sent in place of the original packet.
     *
     * @return overriding packet
     */
    public @Nullable ServerPacket getPacketOverride() {
        return packetOverride;
    }

    /**
     * Sets the overriding packet.
     * When not null, it will be sent in place of the original packet.
     * <p>
     * WARNING: This is unnecessary and a bad practice in most cases, use with caution.
     *
     * @param packetOverride Overriding packet
     */
    public void setPacketOverride(@Nullable ServerPacket packetOverride) {
        this.packetOverride = packetOverride;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

package net.gameoverse.eatduration;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** The server's eating-time settings, sent to each player on join. */
public record SyncPayload(double offset, double slope, double minSeconds, double maxSeconds) implements CustomPacketPayload {
    public static final Type<SyncPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath("gameoverse_eat_duration", "sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, SyncPayload::offset,
        ByteBufCodecs.DOUBLE, SyncPayload::slope,
        ByteBufCodecs.DOUBLE, SyncPayload::minSeconds,
        ByteBufCodecs.DOUBLE, SyncPayload::maxSeconds,
        SyncPayload::new);

    static SyncPayload of(EatDurationConfig config) {
        return new SyncPayload(config.offset, config.slope, config.minSeconds, config.maxSeconds);
    }

    EatDurationConfig toConfig() {
        EatDurationConfig config = new EatDurationConfig();
        config.offset = offset;
        config.slope = slope;
        config.minSeconds = minSeconds;
        config.maxSeconds = maxSeconds;
        return config;
    }

    @Override
    public Type<SyncPayload> type() {
        return TYPE;
    }
}

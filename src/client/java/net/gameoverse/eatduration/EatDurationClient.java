package net.gameoverse.eatduration;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class EatDurationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPayload.TYPE,
            (payload, context) -> EatDurationConfig.setFromServer(payload.toConfig()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EatDurationConfig.setFromServer(null));
    }
}

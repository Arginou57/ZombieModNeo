package com.zombiemod.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Mettre à jour les animations de points à chaque tick client (20 TPS)
        PointsAnimationManager.tick();
    }
}

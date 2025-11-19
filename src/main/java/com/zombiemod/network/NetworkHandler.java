package com.zombiemod.network;

import com.zombiemod.ZombieMod;
import com.zombiemod.network.packet.DoorSyncPacket;
import com.zombiemod.network.packet.GameSyncPacket;
import com.zombiemod.network.packet.JukeboxSyncPacket;
import com.zombiemod.network.packet.PointsAnimationPacket;
import com.zombiemod.network.packet.WeaponCrateSyncPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ZombieMod.MODID).versioned("1.0");

        registrar.playToClient(
                GameSyncPacket.TYPE,
                GameSyncPacket.STREAM_CODEC,
                GameSyncPacket::handle
        );

        registrar.playToClient(
                PointsAnimationPacket.TYPE,
                PointsAnimationPacket.STREAM_CODEC,
                PointsAnimationPacket::handle
        );

        registrar.playToClient(
                WeaponCrateSyncPacket.TYPE,
                WeaponCrateSyncPacket.STREAM_CODEC,
                WeaponCrateSyncPacket::handle
        );

        registrar.playToClient(
                JukeboxSyncPacket.TYPE,
                JukeboxSyncPacket.STREAM_CODEC,
                JukeboxSyncPacket::handle
        );

        registrar.playToClient(
                DoorSyncPacket.TYPE,
                DoorSyncPacket.STREAM_CODEC,
                DoorSyncPacket::handle
        );
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToAllPlayers(CustomPacketPayload packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}

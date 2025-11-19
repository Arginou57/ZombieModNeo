package com.zombiemod.network.packet;

import com.zombiemod.client.ClientDoorData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record DoorSyncPacket(Map<BlockPos, DoorData> doors) implements CustomPacketPayload {

    public static final Type<DoorSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zombiemod", "door_sync"));

    // Classe pour stocker les données d'une porte (numéro, coût, état ouvert/fermé)
    public record DoorData(int doorNumber, int cost, boolean isOpen) {
        public static final StreamCodec<ByteBuf, DoorData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                DoorData::doorNumber,
                ByteBufCodecs.INT,
                DoorData::cost,
                ByteBufCodecs.BOOL,
                DoorData::isOpen,
                DoorData::new
        );
    }

    public static final StreamCodec<ByteBuf, DoorSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    BlockPos.STREAM_CODEC,
                    DoorData.STREAM_CODEC
            ),
            DoorSyncPacket::doors,
            DoorSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DoorSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            System.out.println("[DoorSyncPacket] Packet reçu côté client avec " + packet.doors().size() + " porte(s)");
            // Mettre à jour le cache client
            ClientDoorData.setAll(packet.doors());
        });
    }
}

package com.zombiemod.network.packet;

import com.zombiemod.client.ClientJukeboxData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record JukeboxSyncPacket(Map<BlockPos, Integer> jukeboxes) implements CustomPacketPayload {

    public static final Type<JukeboxSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zombiemod", "jukebox_sync"));

    public static final StreamCodec<ByteBuf, JukeboxSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    BlockPos.STREAM_CODEC,
                    ByteBufCodecs.INT
            ),
            JukeboxSyncPacket::jukeboxes,
            JukeboxSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(JukeboxSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            System.out.println("[JukeboxSyncPacket] Packet reçu côté client avec " + packet.jukeboxes().size() + " jukeboxes");
            // Mettre à jour le cache client
            ClientJukeboxData.setAll(packet.jukeboxes());
        });
    }
}

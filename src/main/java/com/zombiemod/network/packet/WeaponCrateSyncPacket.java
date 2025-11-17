package com.zombiemod.network.packet;

import com.zombiemod.client.ClientWeaponCrateData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record WeaponCrateSyncPacket(Map<BlockPos, Integer> crates) implements CustomPacketPayload {

    public static final Type<WeaponCrateSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zombiemod", "weapon_crate_sync"));

    public static final StreamCodec<ByteBuf, WeaponCrateSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    BlockPos.STREAM_CODEC,
                    ByteBufCodecs.INT
            ),
            WeaponCrateSyncPacket::crates,
            WeaponCrateSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WeaponCrateSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Mettre à jour le cache client
            ClientWeaponCrateData.setAll(packet.crates());
        });
    }
}

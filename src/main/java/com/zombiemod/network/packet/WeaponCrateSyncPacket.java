package com.zombiemod.network.packet;

import com.zombiemod.client.ClientWeaponCrateData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record WeaponCrateSyncPacket(Map<BlockPos, CrateData> crates) implements CustomPacketPayload {

    public static final Type<WeaponCrateSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zombiemod", "weapon_crate_sync"));

    // Classe pour stocker les données d'une caisse (coût + munitions)
    public record CrateData(int cost, ListTag ammo) {
        public static final StreamCodec<ByteBuf, CrateData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                CrateData::cost,
                ByteBufCodecs.COMPOUND_TAG.map(
                        tag -> tag.getList("Ammo", 10), // 10 = TAG_COMPOUND
                        ammo -> {
                            CompoundTag tag = new CompoundTag();
                            tag.put("Ammo", ammo);
                            return tag;
                        }
                ),
                CrateData::ammo,
                CrateData::new
        );
    }

    public static final StreamCodec<ByteBuf, WeaponCrateSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    BlockPos.STREAM_CODEC,
                    CrateData.STREAM_CODEC
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
            System.out.println("[WeaponCrateSyncPacket] Packet reçu côté client avec " + packet.crates().size() + " caisses");
            // Mettre à jour le cache client
            ClientWeaponCrateData.setAll(packet.crates());
        });
    }
}

package com.zombiemod.network.packet;

import com.zombiemod.ZombieMod;
import com.zombiemod.client.PointsAnimationManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PointsAnimationPacket(int points) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PointsAnimationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ZombieMod.MODID, "points_animation"));

    public static final StreamCodec<ByteBuf, PointsAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PointsAnimationPacket::points,
            PointsAnimationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // Ajouter une animation de points côté client
            PointsAnimationManager.addAnimation(points);
        });
    }
}

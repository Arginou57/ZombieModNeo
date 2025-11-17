package com.zombiemod.network.packet;

import com.zombiemod.ZombieMod;
import com.zombiemod.client.ClientGameData;
import com.zombiemod.manager.GameManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public record GameSyncPacket(
        String gameState,
        int currentWave,
        int zombiesRemaining,
        int startCountdownTicks,
        int waveCountdownTicks,
        List<PlayerData> activePlayers,
        List<UUID> waitingPlayers,
        UUID localPlayerUUID
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GameSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ZombieMod.MODID, "game_sync"));

    public static final StreamCodec<ByteBuf, GameSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GameSyncPacket decode(ByteBuf buf) {
            String gameState = ByteBufCodecs.STRING_UTF8.decode(buf);
            int currentWave = ByteBufCodecs.VAR_INT.decode(buf);
            int zombiesRemaining = ByteBufCodecs.VAR_INT.decode(buf);
            int startCountdownTicks = ByteBufCodecs.VAR_INT.decode(buf);
            int waveCountdownTicks = ByteBufCodecs.VAR_INT.decode(buf);
            List<PlayerData> activePlayers = PlayerData.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            List<UUID> waitingPlayers = UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            UUID localPlayerUUID = UUIDUtil.STREAM_CODEC.decode(buf);

            return new GameSyncPacket(
                    gameState,
                    currentWave,
                    zombiesRemaining,
                    startCountdownTicks,
                    waveCountdownTicks,
                    activePlayers,
                    waitingPlayers,
                    localPlayerUUID
            );
        }

        @Override
        public void encode(ByteBuf buf, GameSyncPacket packet) {
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.gameState());
            ByteBufCodecs.VAR_INT.encode(buf, packet.currentWave());
            ByteBufCodecs.VAR_INT.encode(buf, packet.zombiesRemaining());
            ByteBufCodecs.VAR_INT.encode(buf, packet.startCountdownTicks());
            ByteBufCodecs.VAR_INT.encode(buf, packet.waveCountdownTicks());
            PlayerData.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, packet.activePlayers());
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, packet.waitingPlayers());
            UUIDUtil.STREAM_CODEC.encode(buf, packet.localPlayerUUID());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update client-side game data
            ClientGameData.updateGameState(this);
        });
    }

    public record PlayerData(UUID uuid, String name, int points) {
        public static final StreamCodec<ByteBuf, PlayerData> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                PlayerData::uuid,
                ByteBufCodecs.STRING_UTF8,
                PlayerData::name,
                ByteBufCodecs.VAR_INT,
                PlayerData::points,
                PlayerData::new
        );
    }
}

package com.zombiemod.manager;

import com.zombiemod.map.MapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RespawnManager {

    private static Set<UUID> deadPlayers = new HashSet<>();

    // Utilise la map sélectionnée via MapManager
    public static BlockPos getRespawnPoint() {
        return MapManager.getRespawnPoint();
    }

    public static void onPlayerDeath(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (!GameManager.isPlayerActive(uuid)) {
            return; // Pas dans la partie
        }

        deadPlayers.add(uuid);

        // Message au joueur mort
        player.sendSystemMessage(Component.literal("§cVous êtes mort ! Vous respawnerez à la fin de la vague..."));

        // Broadcast aux autres joueurs
        GameManager.broadcastToActivePlayers((ServerLevel) player.level(),
                "§7" + player.getName().getString() + " §cest mort !");
    }

    public static void respawnAllDeadPlayers(ServerLevel level) {
        if (deadPlayers.isEmpty()) {
            return;
        }

        for (UUID uuid : new HashSet<>(deadPlayers)) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            BlockPos respawnPoint = getRespawnPoint();
            if (player != null && respawnPoint != null) {
                // Téléporter au respawn
                player.teleportTo(respawnPoint.getX() + 0.5, respawnPoint.getY(), respawnPoint.getZ() + 0.5);

                // Passer en survie
                player.setGameMode(GameType.SURVIVAL);

                // Restaurer santé et faim
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0f);

                player.sendSystemMessage(Component.literal("§a§lVous êtes revenu dans la bataille !"));
                GameManager.broadcastToActivePlayers(level,
                        "§a" + player.getName().getString() + " §eest revenu dans la bataille !");

                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        deadPlayers.clear();
    }

    public static boolean isPlayerDead(UUID uuid) {
        return deadPlayers.contains(uuid);
    }

    public static void removeDeadPlayer(UUID uuid) {
        deadPlayers.remove(uuid);
    }

    public static void reset() {
        deadPlayers.clear();
    }
}

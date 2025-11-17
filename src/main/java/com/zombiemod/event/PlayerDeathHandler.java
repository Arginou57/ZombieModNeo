package com.zombiemod.event;

import com.zombiemod.manager.GameManager;
import com.zombiemod.manager.RespawnManager;
import com.zombiemod.manager.WaveManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.UUID;

public class PlayerDeathHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Vérifier si le joueur est actif
            if (!GameManager.isPlayerActive(player.getUUID())) {
                return; // Pas dans la partie
            }

            RespawnManager.onPlayerDeath(player);

            // Vérifier game over
            if (GameManager.areAllActivePlayersDead((ServerLevel) player.level())) {
                gameOver((ServerLevel) player.level());
            }
        }
    }

    private static void gameOver(ServerLevel level) {
        GameManager.broadcastToAll(level, "");
        GameManager.broadcastToAll(level, "§4§l=== GAME OVER ===");
        GameManager.broadcastToAll(level, "§cVous avez survécu jusqu'à la vague §e" + WaveManager.getCurrentWave());
        GameManager.broadcastToAll(level, "");

        // Nettoyer tous les mobs de la map
        WaveManager.killAllMobs();

        // Téléporter les joueurs à leur respawn point vanilla
        for (UUID uuid : GameManager.getActivePlayers()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                // Mettre en survie
                player.setGameMode(GameType.SURVIVAL);

                // Soigner le joueur
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setFoodLevel(20);

                // Téléporter au respawn point vanilla
                net.minecraft.core.BlockPos spawnPos = player.getRespawnPosition();
                ServerLevel spawnLevel = level.getServer().getLevel(player.getRespawnDimension());

                if (spawnPos != null && spawnLevel != null) {
                    // Téléporter au spawn point défini
                    player.teleportTo(spawnLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                } else {
                    // Téléporter au spawn du monde
                    net.minecraft.core.BlockPos worldSpawn = level.getSharedSpawnPos();
                    player.teleportTo(level, worldSpawn.getX() + 0.5, worldSpawn.getY(), worldSpawn.getZ() + 0.5, 0, 0);
                }

                player.sendSystemMessage(Component.literal("§7Vous avez été renvoyé à votre point de spawn."));
            }
        }

        // Reset la partie
        GameManager.reset();
        WaveManager.reset();
        RespawnManager.reset();

        level.playSound(null, level.getSharedSpawnPos(), SoundEvents.WITHER_DEATH, SoundSource.MASTER, 1.0f, 0.5f);
    }
}

package com.zombiemod.event;

import com.zombiemod.manager.GameManager;
import com.zombiemod.manager.InventoryManager;
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

            // Annuler l'événement de mort pour éviter l'écran de mort
            event.setCanceled(true);

            // Respawn immédiat en spectateur
            ServerLevel level = (ServerLevel) player.level();
            net.minecraft.core.BlockPos respawnPoint = RespawnManager.getRespawnPoint();

            // Restaurer la santé pour éviter que le joueur reste "mort"
            player.setHealth(player.getMaxHealth());

            // Passer en spectateur
            player.setGameMode(GameType.SPECTATOR);

            // Téléporter au point de respawn
            if (respawnPoint != null) {
                player.teleportTo(respawnPoint.getX() + 0.5, respawnPoint.getY(), respawnPoint.getZ() + 0.5);
            }

            // Enregistrer le joueur comme mort
            RespawnManager.onPlayerDeath(player);

            // Vérifier game over
            if (GameManager.areAllActivePlayersDead(level)) {
                gameOver(level);
            }
        }
    }

    private static void gameOver(ServerLevel level) {
        GameManager.broadcastToActivePlayers(level, "");
        GameManager.broadcastToActivePlayers(level, "§4§l=== GAME OVER ===");
        GameManager.broadcastToActivePlayers(level, "§cVous avez survécu jusqu'à la vague §e" + WaveManager.getCurrentWave());
        GameManager.broadcastToActivePlayers(level, "");

        // Son de game over (avant le reset pour avoir accès aux joueurs actifs)
        GameManager.playSoundToActivePlayers(level, SoundEvents.WITHER_DEATH, 0.5f);

        // Nettoyer tous les mobs de la map
        WaveManager.killAllMobs();

        // Restaurer les inventaires et téléporter les joueurs à leur respawn point vanilla
        for (UUID uuid : GameManager.getActivePlayers()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                // Clear l'inventaire de la partie puis restaurer l'inventaire original
                InventoryManager.clearInventory(player);
                InventoryManager.restoreInventory(player);

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
                player.sendSystemMessage(Component.literal("§aVotre inventaire a été restauré."));
            }
        }

        // Restaurer aussi les inventaires des joueurs en attente
        for (UUID uuid : GameManager.getWaitingPlayers()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                InventoryManager.clearInventory(player);
                InventoryManager.restoreInventory(player);
                player.setGameMode(GameType.SURVIVAL);
                player.sendSystemMessage(Component.literal("§aVotre inventaire a été restauré."));
            }
        }

        // Reset la partie
        GameManager.reset();
        WaveManager.reset();
        RespawnManager.reset();

        // Réinitialiser les portes (fermer physiquement et réinitialiser l'état)
        com.zombiemod.command.DoorCommand.resetAllDoors(level);
    }
}

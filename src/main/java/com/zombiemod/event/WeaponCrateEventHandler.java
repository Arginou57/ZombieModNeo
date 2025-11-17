package com.zombiemod.event;

import com.zombiemod.system.ServerWeaponCrateTracker;
import com.zombiemod.system.WeaponCrateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Gère les events liés aux weapon crates :
 * - Suppression du tracker quand un coffre est cassé
 * - Rechargement du tracker au démarrage et au chargement des chunks
 */
@EventBusSubscriber(modid = "zombiemod")
public class WeaponCrateEventHandler {

    /**
     * Quand un bloc est cassé, vérifier si c'est une weapon crate
     * et la supprimer du tracker
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();

        // Vérifier si c'est un coffre
        if (event.getState().getBlock() == Blocks.CHEST) {
            // Vérifier si c'était une weapon crate
            if (WeaponCrateManager.isWeaponCrate(level, pos)) {
                System.out.println("[WeaponCrateEventHandler] Weapon crate cassée à " + pos);

                // Supprimer du tracker côté serveur
                if (!level.isClientSide()) {
                    ServerWeaponCrateTracker.removeWeaponCrate(pos);
                }
            }
        }
    }

    /**
     * Au démarrage du serveur, scanner tous les chunks chargés
     * pour trouver les weapon crates existantes
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        System.out.println("[WeaponCrateEventHandler] Démarrage du serveur - Scan des weapon crates...");

        // Parcourir tous les niveaux (dimensions)
        event.getServer().getAllLevels().forEach(level -> {
            if (level instanceof ServerLevel serverLevel) {
                scanLevelForWeaponCrates(serverLevel);
            }
        });

        // Synchroniser avec tous les clients (quand ils se connecteront)
        ServerWeaponCrateTracker.syncToAllPlayers();

        System.out.println("[WeaponCrateEventHandler] Scan terminé - " +
            ServerWeaponCrateTracker.getAllCrates().size() + " weapon crates trouvées");
    }

    /**
     * Quand un chunk est chargé, scanner les weapon crates dedans
     * (pour les chunks qui n'étaient pas chargés au démarrage)
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (event.getChunk() instanceof LevelChunk chunk) {
                scanChunkForWeaponCrates(serverLevel, chunk);
            }
        }
    }

    /**
     * Quand un chunk est déchargé, supprimer ses weapon crates du tracker
     * (pour économiser la mémoire)
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (event.getChunk() instanceof LevelChunk chunk) {
                removeChunkWeaponCrates(chunk);
            }
        }
    }

    /**
     * Scanne un niveau complet pour les weapon crates
     */
    private static void scanLevelForWeaponCrates(ServerLevel level) {
        System.out.println("[WeaponCrateEventHandler] Scan du niveau " + level.dimension().location());

        // Parcourir tous les chunks chargés
        level.getChunkSource().chunkMap.getChunks().forEach(holder -> {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                scanChunkForWeaponCrates(level, chunk);
            }
        });
    }

    /**
     * Scanne un chunk pour les weapon crates
     */
    private static void scanChunkForWeaponCrates(ServerLevel level, LevelChunk chunk) {
        // Parcourir toutes les BlockEntities du chunk
        chunk.getBlockEntities().forEach((pos, blockEntity) -> {
            if (blockEntity instanceof ChestBlockEntity chest) {
                // Vérifier si c'est une weapon crate
                if (chest.getPersistentData().getBoolean("IsWeaponCrate")) {
                    int cost = chest.getPersistentData().getInt("Cost");

                    // Ajouter au tracker (sans sync pour éviter spam réseau)
                    ServerWeaponCrateTracker.addWeaponCrateNoSync(pos, cost);

                    System.out.println("[WeaponCrateEventHandler] Weapon crate trouvée: " + pos + " -> " + cost + " points");
                }
            }
        });
    }

    /**
     * Supprime les weapon crates d'un chunk du tracker
     */
    private static void removeChunkWeaponCrates(LevelChunk chunk) {
        chunk.getBlockEntities().forEach((pos, blockEntity) -> {
            if (blockEntity instanceof ChestBlockEntity chest) {
                if (chest.getPersistentData().getBoolean("IsWeaponCrate")) {
                    ServerWeaponCrateTracker.removeWeaponCrateNoSync(pos);
                    System.out.println("[WeaponCrateEventHandler] Weapon crate retirée du cache: " + pos);
                }
            }
        });
    }
}

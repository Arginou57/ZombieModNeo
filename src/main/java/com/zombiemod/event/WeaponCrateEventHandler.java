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
     * Au démarrage du serveur, réinitialiser le tracker
     * Les weapon crates seront rechargées progressivement via ChunkLoad events
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        System.out.println("[WeaponCrateEventHandler] Démarrage du serveur - Réinitialisation du tracker");
        ServerWeaponCrateTracker.reset();
        System.out.println("[WeaponCrateEventHandler] Les weapon crates seront rechargées lors du chargement des chunks");
    }

    /**
     * Quand un chunk est chargé, scanner les weapon crates dedans
     * Reconstruit progressivement le tracker après redémarrage
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (event.getChunk() instanceof LevelChunk chunk) {
                scanChunkForWeaponCrates(serverLevel, chunk);
                // Synchroniser après chaque chunk chargé (les joueurs verront progressivement les crates)
                ServerWeaponCrateTracker.syncToAllPlayers();
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
                // Synchroniser après suppression
                ServerWeaponCrateTracker.syncToAllPlayers();
            }
        }
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

                    // Ajouter au tracker (sans sync - sera fait après scan du chunk complet)
                    ServerWeaponCrateTracker.addWeaponCrateNoSync(pos, cost);

                    System.out.println("[WeaponCrateEventHandler] Weapon crate trouvée dans chunk: " + pos + " -> " + cost + " points");
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

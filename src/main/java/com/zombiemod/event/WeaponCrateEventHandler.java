package com.zombiemod.event;

import com.zombiemod.system.ServerWeaponCrateTracker;
import com.zombiemod.system.WeaponCrateAnimationManager;
import com.zombiemod.system.WeaponCrateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Gère les events liés aux weapon crates :
 * - Suppression du tracker quand un coffre est cassé
 * - Chargement du tracker au démarrage du serveur
 */
@EventBusSubscriber(modid = "zombiemod")
public class WeaponCrateEventHandler {

    /**
     * Quand un bloc est cassé, vérifier si c'est une weapon crate
     * et la supprimer du tracker + arrêter l'animation
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

                    // Supprimer l'animation/affichage statique
                    WeaponCrateAnimationManager.stopAnimation(pos);
                    System.out.println("[WeaponCrateEventHandler] Animation supprimée pour " + pos);
                }
            }
        }
    }

    /**
     * Au démarrage du serveur, charger les weapon crates depuis la sauvegarde persistante
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        System.out.println("[WeaponCrateEventHandler] Démarrage du serveur - Chargement des weapon crates");

        // Récupérer l'overworld (dimension principale)
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            ServerWeaponCrateTracker.initialize(overworld);
            System.out.println("[WeaponCrateEventHandler] Weapon crates chargées depuis la sauvegarde");
        }
    }
}

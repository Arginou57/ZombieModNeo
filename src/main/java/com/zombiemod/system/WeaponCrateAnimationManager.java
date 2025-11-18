package com.zombiemod.system;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Gère les animations des weapon crates :
 * - Affichage statique d'un item unique
 * - Animation de roulette pour plusieurs items
 */
public class WeaponCrateAnimationManager {

    private static class CrateAnimation {
        BlockPos pos;
        Display.ItemDisplay displayEntity;
        List<ItemStack> possibleItems;
        ItemStack wonItem;
        int ticksRunning;
        int changeDelay; // Délai entre chaque changement d'item
        int ticksSinceLastChange;
        boolean isRoulette; // true = animation roulette, false = affichage statique

        CrateAnimation(BlockPos pos, Display.ItemDisplay entity, List<ItemStack> items, ItemStack won, boolean roulette) {
            this.pos = pos;
            this.displayEntity = entity;
            this.possibleItems = items;
            this.wonItem = won;
            this.ticksRunning = 0;
            this.changeDelay = 2; // Commence rapide (2 ticks = 0.1s)
            this.ticksSinceLastChange = 0;
            this.isRoulette = roulette;
        }
    }

    private static final Map<BlockPos, CrateAnimation> activeAnimations = new HashMap<>();
    private static final Random random = new Random();

    /**
     * Démarre une animation d'affichage statique (1 seul item dans la caisse)
     */
    public static void startStaticDisplay(ServerLevel level, BlockPos cratePos, ItemStack item) {
        // Supprimer toute animation existante à cette position
        stopAnimation(cratePos);

        // Créer la display entity
        Display.ItemDisplay display = createItemDisplay(level, cratePos, item);
        if (display == null) return;

        // Créer l'animation statique
        CrateAnimation animation = new CrateAnimation(cratePos, display,
            Collections.singletonList(item), item, false);
        activeAnimations.put(cratePos, animation);

        System.out.println("[WeaponCrateAnimation] Affichage statique démarré à " + cratePos);
    }

    /**
     * Démarre une animation de roulette (plusieurs items dans la caisse)
     */
    public static void startRouletteAnimation(ServerLevel level, BlockPos cratePos,
                                               List<ItemStack> possibleItems, ItemStack wonItem) {
        // Supprimer toute animation existante à cette position
        stopAnimation(cratePos);

        // Vérifier qu'on a au moins 2 items pour la roulette
        if (possibleItems.size() < 2) {
            startStaticDisplay(level, cratePos, wonItem);
            return;
        }

        // Créer la display entity avec le premier item
        Display.ItemDisplay display = createItemDisplay(level, cratePos, possibleItems.get(0));
        if (display == null) return;

        // Créer l'animation de roulette
        CrateAnimation animation = new CrateAnimation(cratePos, display,
            possibleItems, wonItem, true);
        activeAnimations.put(cratePos, animation);

        // Son de démarrage
        level.playSound(null, cratePos, SoundEvents.NOTE_BLOCK_PLING.value(),
            SoundSource.BLOCKS, 1.0f, 1.0f);

        System.out.println("[WeaponCrateAnimation] Animation roulette démarrée à " + cratePos
            + " avec " + possibleItems.size() + " items");
    }

    /**
     * Crée une ItemDisplay entity au-dessus du coffre
     * Utilise la même structure NBT que /summon minecraft:item_display
     */
    private static Display.ItemDisplay createItemDisplay(ServerLevel level, BlockPos cratePos, ItemStack item) {
        // Créer le NBT COMPLET avant de créer l'entité
        CompoundTag nbt = new CompoundTag();

        // Position
        nbt.putDouble("x", cratePos.getX() + 0.5);
        nbt.putDouble("y", cratePos.getY() + 1.3);
        nbt.putDouble("z", cratePos.getZ() + 0.5);

        // Item à afficher
        CompoundTag itemTag = new CompoundTag();
        item.save(level.registryAccess(), itemTag);
        nbt.put("item", itemTag);

        // Mode d'affichage: "fixed"
        nbt.putString("item_display", "fixed");

        // Créer l'entité et charger TOUTES les données NBT
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
        display.load(nbt);

        // Maintenant ajouter au monde avec TOUTES les données chargées
        if (!level.addFreshEntity(display)) {
            System.err.println("[WeaponCrateAnimation] Impossible de créer la display entity");
            return null;
        }

        System.out.println("[WeaponCrateAnimation] Display entity créée: " + display.getId()
            + " pour item " + item.getDisplayName().getString()
            + " à " + cratePos);

        return display;
    }

    /**
     * Tick system - appelé depuis ServerTickEvent
     */
    public static void tick(ServerLevel level) {
        if (activeAnimations.isEmpty()) return;

        Iterator<Map.Entry<BlockPos, CrateAnimation>> iterator = activeAnimations.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CrateAnimation> entry = iterator.next();
            CrateAnimation anim = entry.getValue();

            // Vérifier que la display entity existe toujours
            if (anim.displayEntity == null || !anim.displayEntity.isAlive()) {
                iterator.remove();
                continue;
            }

            anim.ticksRunning++;

            // Animation STATIQUE : reste affichée indéfiniment (ou jusqu'à suppression manuelle)
            if (!anim.isRoulette) {
                // Rotation lente pour rendre joli
                rotateDisplay(anim.displayEntity, anim.ticksRunning);
                continue;
            }

            // Animation ROULETTE : 3 secondes (60 ticks)
            if (anim.ticksRunning >= 60) {
                // Fin de l'animation : afficher l'item gagné pendant 1 seconde puis supprimer
                if (anim.ticksRunning >= 80) { // 60 + 20 ticks (1 seconde)
                    // Supprimer l'entity
                    anim.displayEntity.remove(Display.ItemDisplay.RemovalReason.DISCARDED);
                    iterator.remove();
                    System.out.println("[WeaponCrateAnimation] Animation terminée à " + anim.pos);
                    continue;
                }

                // Afficher l'item gagné (déjà fait à la fin du ralentissement)
                rotateDisplay(anim.displayEntity, anim.ticksRunning);
                continue;
            }

            // Phase de roulette
            anim.ticksSinceLastChange++;

            // Ralentissement progressif
            if (anim.ticksRunning < 20) {
                anim.changeDelay = 2; // Rapide : 0.1s
            } else if (anim.ticksRunning < 40) {
                anim.changeDelay = 4; // Moyen : 0.2s
            } else {
                anim.changeDelay = 8; // Lent : 0.4s
            }

            // Changer l'item affiché
            if (anim.ticksSinceLastChange >= anim.changeDelay) {
                anim.ticksSinceLastChange = 0;

                // À la fin (tick 56+), commencer à montrer l'item gagné
                ItemStack nextItem;
                if (anim.ticksRunning >= 56) {
                    nextItem = anim.wonItem;
                    // Son de victoire
                    if (anim.ticksRunning == 56) {
                        level.playSound(null, anim.pos, SoundEvents.PLAYER_LEVELUP,
                            SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                } else {
                    // Item aléatoire
                    nextItem = anim.possibleItems.get(random.nextInt(anim.possibleItems.size()));
                    // Son de tick
                    level.playSound(null, anim.pos, SoundEvents.NOTE_BLOCK_HAT.value(),
                        SoundSource.BLOCKS, 0.5f, 1.0f + (anim.ticksRunning * 0.01f));
                }

                // Mettre à jour l'item affiché
                updateDisplayItem(anim.displayEntity, nextItem, level);
            }

            // Rotation
            rotateDisplay(anim.displayEntity, anim.ticksRunning);
        }
    }

    /**
     * Met à jour l'item affiché par une Display entity
     */
    private static void updateDisplayItem(Display.ItemDisplay display, ItemStack item, ServerLevel level) {
        // Sauvegarder l'état actuel
        CompoundTag nbt = new CompoundTag();
        display.saveWithoutId(nbt);

        // Mettre à jour l'item dans le NBT
        CompoundTag itemTag = new CompoundTag();
        item.save(level.registryAccess(), itemTag);
        nbt.put("item", itemTag);

        // Garder item_display en "fixed"
        nbt.putString("item_display", "fixed");

        // Recharger les données
        display.load(nbt);
    }

    /**
     * Applique une rotation à la display entity
     */
    private static void rotateDisplay(Display.ItemDisplay display, int ticks) {
        // Rotation autour de l'axe Y (yaw)
        float yaw = (ticks * 3) % 360; // 3 degrés par tick
        display.setYRot(yaw);
    }

    /**
     * Arrête une animation à une position donnée
     */
    public static void stopAnimation(BlockPos pos) {
        CrateAnimation anim = activeAnimations.remove(pos);
        if (anim != null && anim.displayEntity != null && anim.displayEntity.isAlive()) {
            anim.displayEntity.remove(Display.ItemDisplay.RemovalReason.DISCARDED);
            System.out.println("[WeaponCrateAnimation] Animation arrêtée à " + pos);
        }
    }

    /**
     * Arrête toutes les animations
     */
    public static void stopAllAnimations() {
        for (CrateAnimation anim : activeAnimations.values()) {
            if (anim.displayEntity != null && anim.displayEntity.isAlive()) {
                anim.displayEntity.remove(Display.ItemDisplay.RemovalReason.DISCARDED);
            }
        }
        activeAnimations.clear();
        System.out.println("[WeaponCrateAnimation] Toutes les animations arrêtées");
    }

    /**
     * Vérifie si une animation est en cours à une position
     */
    public static boolean hasActiveAnimation(BlockPos pos) {
        return activeAnimations.containsKey(pos);
    }
}

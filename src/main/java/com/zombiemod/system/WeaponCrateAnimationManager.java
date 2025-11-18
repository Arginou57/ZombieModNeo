package com.zombiemod.system;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
 * - Animation de roulette pour plusieurs items (summon/kill à chaque frame)
 */
public class WeaponCrateAnimationManager {

    private static class CrateAnimation {
        BlockPos pos;
        ServerLevel level;
        ServerPlayer player;  // Le joueur qui a ouvert la caisse
        Display.ItemDisplay currentDisplay;  // L'entité affichée actuellement
        List<ItemStack> possibleItems;
        ItemStack wonItem;
        int ticksRunning;
        int changeDelay; // Délai entre chaque changement d'item
        int ticksSinceLastChange;
        boolean isRoulette; // true = animation roulette, false = affichage statique

        CrateAnimation(ServerLevel level, BlockPos pos, ServerPlayer player, List<ItemStack> items, ItemStack won, boolean roulette) {
            this.level = level;
            this.pos = pos;
            this.player = player;
            this.currentDisplay = null;
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
     * Démarre une animation de roulette (plusieurs items dans la caisse)
     * L'item sera donné au joueur à la FIN de l'animation
     */
    public static void startRouletteAnimation(ServerLevel level, BlockPos cratePos, ServerPlayer player,
                                               List<ItemStack> possibleItems, ItemStack wonItem) {
        // Supprimer toute animation existante à cette position
        stopAnimation(cratePos);

        // Si un seul item, pas d'animation
        if (possibleItems.size() < 2) {
            // Donner l'item immédiatement au joueur
            player.getInventory().add(wonItem.copy());
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "§6Vous avez reçu : §e" + wonItem.getDisplayName().getString()), false);
            return;
        }

        // Créer l'animation de roulette (sans display pour l'instant)
        CrateAnimation animation = new CrateAnimation(level, cratePos, player,
            possibleItems, wonItem, true);
        activeAnimations.put(cratePos, animation);

        // Son de démarrage
        level.playSound(null, cratePos, SoundEvents.NOTE_BLOCK_PLING.value(),
            SoundSource.BLOCKS, 1.0f, 1.0f);

        System.out.println("[WeaponCrateAnimation] Animation roulette démarrée à " + cratePos
            + " avec " + possibleItems.size() + " items pour joueur " + player.getName().getString());
    }

    /**
     * Tick system - appelé depuis ServerTickEvent
     * Utilise summon/kill pour changer les items (synchronisation automatique)
     */
    public static void tick(ServerLevel level) {
        if (activeAnimations.isEmpty()) return;

        Iterator<Map.Entry<BlockPos, CrateAnimation>> iterator = activeAnimations.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CrateAnimation> entry = iterator.next();
            CrateAnimation anim = entry.getValue();

            anim.ticksRunning++;
            anim.ticksSinceLastChange++;

            // Animation ROULETTE : 3 secondes (60 ticks)
            if (anim.ticksRunning >= 60) {
                // Fin de l'animation : afficher l'item gagné pendant 1 seconde puis supprimer
                if (anim.ticksRunning >= 80) { // 60 + 20 ticks (1 seconde)
                    // DONNER L'ITEM AU JOUEUR À LA FIN !
                    anim.player.getInventory().add(anim.wonItem.copy());
                    anim.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§6Vous avez reçu : §e" + anim.wonItem.getDisplayName().getString()), false);

                    // Supprimer l'entity
                    if (anim.currentDisplay != null && anim.currentDisplay.isAlive()) {
                        anim.currentDisplay.kill();
                    }
                    iterator.remove();
                    System.out.println("[WeaponCrateAnimation] Animation terminée à " + anim.pos + " - Item donné au joueur");
                    continue;
                }

                // Continuer d'afficher l'item gagné avec rotation
                continue;
            }

            // Ralentissement progressif
            if (anim.ticksRunning < 20) {
                anim.changeDelay = 2; // Rapide : 0.1s (10 fps)
            } else if (anim.ticksRunning < 40) {
                anim.changeDelay = 4; // Moyen : 0.2s (5 fps)
            } else {
                anim.changeDelay = 8; // Lent : 0.4s (2.5 fps)
            }

            // Changer l'item affiché ?
            if (anim.ticksSinceLastChange >= anim.changeDelay) {
                anim.ticksSinceLastChange = 0;

                // Déterminer l'item à afficher
                ItemStack nextItem;
                if (anim.ticksRunning >= 56) {
                    // Afficher l'item gagné à partir du tick 56
                    nextItem = anim.wonItem;
                    // Son de victoire au premier tick
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

                // KILL l'ancienne entity et SUMMON une nouvelle
                if (anim.currentDisplay != null && anim.currentDisplay.isAlive()) {
                    anim.currentDisplay.kill();
                }
                anim.currentDisplay = summonItemDisplay(level, anim.pos, nextItem);
            }
        }
    }

    /**
     * Summon une nouvelle ItemDisplay entity
     * Méthode simplifiée qui laisse Minecraft gérer la synchronisation
     */
    private static Display.ItemDisplay summonItemDisplay(ServerLevel level, BlockPos cratePos, ItemStack item) {
        // Créer le NBT complet pour l'entité
        CompoundTag nbt = new CompoundTag();

        // Position au-dessus du coffre
        nbt.putDouble("x", cratePos.getX() + 0.5);
        nbt.putDouble("y", cratePos.getY() + 1.3);
        nbt.putDouble("z", cratePos.getZ() + 0.5);

        // Item à afficher
        CompoundTag itemTag = new CompoundTag();
        item.save(level.registryAccess(), itemTag);
        nbt.put("item", itemTag);

        // Mode d'affichage: "fixed"
        nbt.putString("item_display", "fixed");

        // Créer et charger l'entité
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
        display.load(nbt);

        // Ajouter au monde → Minecraft synchronise automatiquement !
        if (!level.addFreshEntity(display)) {
            System.err.println("[WeaponCrateAnimation] Impossible de summon la display entity");
            return null;
        }

        return display;
    }

    /**
     * Arrête une animation à une position donnée
     */
    public static void stopAnimation(BlockPos pos) {
        CrateAnimation anim = activeAnimations.remove(pos);
        if (anim != null && anim.currentDisplay != null && anim.currentDisplay.isAlive()) {
            anim.currentDisplay.kill();
            System.out.println("[WeaponCrateAnimation] Animation arrêtée à " + pos);
        }
    }

    /**
     * Arrête toutes les animations
     */
    public static void stopAllAnimations() {
        for (CrateAnimation anim : activeAnimations.values()) {
            if (anim.currentDisplay != null && anim.currentDisplay.isAlive()) {
                anim.currentDisplay.kill();
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

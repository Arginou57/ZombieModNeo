package com.zombiemod.system;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
     * Démarre un affichage permanent (1 seul item dans la caisse)
     * L'item reste affiché au-dessus du coffre
     */
    public static void startStaticDisplay(ServerLevel level, BlockPos cratePos, ItemStack item) {
        // Supprimer toute animation existante à cette position
        stopAnimation(cratePos);

        // Créer l'affichage permanent
        Display.ItemDisplay display = summonItemDisplay(level, cratePos, item);
        if (display == null) return;

        // Créer une animation statique (sans joueur ni roulette)
        CrateAnimation animation = new CrateAnimation(level, cratePos, null,
            Collections.singletonList(item), item, false);
        animation.currentDisplay = display;
        activeAnimations.put(cratePos, animation);

        System.out.println("[WeaponCrateAnimation] Affichage statique démarré à " + cratePos
            + " pour item " + item.getDisplayName().getString());
    }

    /**
     * Démarre une animation de roulette (plusieurs items dans la caisse)
     * L'item sera donné au joueur à la FIN de l'animation
     */
    public static void startRouletteAnimation(ServerLevel level, BlockPos cratePos, ServerPlayer player,
                                               List<ItemStack> possibleItems, ItemStack wonItem) {
        // Supprimer toute animation existante à cette position
        stopAnimation(cratePos);

        // Si un seul item, affichage statique au-dessus du coffre
        if (possibleItems.size() < 2) {
            startStaticDisplay(level, cratePos, wonItem);
            // Donner l'item immédiatement au joueur quand même
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

            // Affichage STATIQUE : ne rien faire, reste en permanence
            if (!anim.isRoulette) {
                continue;
            }

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

            // Au tick 56, FORCER l'affichage de l'item gagné (peu importe le délai)
            if (anim.ticksRunning == 56) {
                anim.ticksSinceLastChange = 0;

                // KILL l'ancienne entity et SUMMON l'item GAGNÉ
                if (anim.currentDisplay != null && anim.currentDisplay.isAlive()) {
                    anim.currentDisplay.kill();
                }
                anim.currentDisplay = summonItemDisplay(level, anim.pos, anim.wonItem);

                // Son de victoire
                level.playSound(null, anim.pos, SoundEvents.PLAYER_LEVELUP,
                    SoundSource.BLOCKS, 1.0f, 1.0f);

                System.out.println("[WeaponCrateAnimation] Tick 56 - Affichage de l'item gagné: "
                    + anim.wonItem.getDisplayName().getString());
                continue;
            }

            // Changer l'item affiché selon le délai (seulement AVANT tick 56)
            if (anim.ticksSinceLastChange >= anim.changeDelay) {
                anim.ticksSinceLastChange = 0;

                // Item aléatoire pendant la roulette
                ItemStack nextItem = anim.possibleItems.get(random.nextInt(anim.possibleItems.size()));

                // KILL l'ancienne entity et SUMMON une nouvelle
                if (anim.currentDisplay != null && anim.currentDisplay.isAlive()) {
                    anim.currentDisplay.kill();
                }
                anim.currentDisplay = summonItemDisplay(level, anim.pos, nextItem);

                // Son de tick
                level.playSound(null, anim.pos, SoundEvents.NOTE_BLOCK_HAT.value(),
                    SoundSource.BLOCKS, 0.5f, 1.0f + (anim.ticksRunning * 0.01f));
            }
        }
    }

    /**
     * Summon une nouvelle ItemDisplay entity en utilisant une commande
     * Utilise le format SNBT correct pour Minecraft 1.21+
     */
    private static Display.ItemDisplay summonItemDisplay(ServerLevel level, BlockPos cratePos, ItemStack item) {
        // Obtenir la direction du coffre
        BlockState chestState = level.getBlockState(cratePos);
        Direction facing = chestState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? chestState.getValue(BlockStateProperties.HORIZONTAL_FACING)
            : Direction.NORTH;

        // Position de base au-dessus du coffre
        double x = cratePos.getX() + 0.5;
        double y = cratePos.getY() + 1.3;
        double z = cratePos.getZ() + 0.5;

        // Ajuster la position selon la direction du coffre (légèrement devant)
        double offsetDistance = 0.2; // Distance devant le coffre
        x += facing.getStepX() * offsetDistance;
        z += facing.getStepZ() * offsetDistance;

        // Calculer la rotation (yaw) selon la direction
        float yaw = switch (facing) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case EAST -> 270f;
            case WEST -> 90f;
            default -> 0f;
        };

        // Sauvegarder l'item en NBT complet (utiliser le retour de save())
        // save() retourne Tag, on le cast en CompoundTag
        CompoundTag itemNbt = (CompoundTag) item.save(level.registryAccess());

        // Convertir en SNBT (Stringified NBT) compatible avec les commandes
        // CompoundTag.toString() retourne directement le format SNBT
        String itemSnbt = itemNbt.toString();

        // Construire la commande summon avec rotation
        String command = String.format(
            "summon minecraft:item_display %.2f %.2f %.2f {item:%s,item_display:\"fixed\",Rotation:[%.1ff,0f]}",
            x, y, z, itemSnbt, yaw
        );

        System.out.println("[WeaponCrateAnimation] Commande summon: " + command);

        try {
            // Exécuter la commande côté serveur
            net.minecraft.commands.Commands commands = level.getServer().getCommands();
            net.minecraft.commands.CommandSourceStack source = level.getServer().createCommandSourceStack()
                .withLevel(level)
                .withPosition(new Vec3(x, y, z))
                .withSuppressedOutput();

            // Exécuter la commande (performPrefixedCommand ne retourne rien en 1.21)
            commands.performPrefixedCommand(source, command);

            // Attendre un tick pour que l'entité soit créée
            // Trouver l'entité qui vient d'être créée
            java.util.List<Display.ItemDisplay> nearbyDisplays = level.getEntitiesOfClass(
                Display.ItemDisplay.class,
                new net.minecraft.world.phys.AABB(
                    x - 0.1, y - 0.1, z - 0.1,
                    x + 0.1, y + 0.1, z + 0.1
                )
            );

            if (!nearbyDisplays.isEmpty()) {
                Display.ItemDisplay display = nearbyDisplays.get(0);
                System.out.println("[WeaponCrateAnimation] Display entity trouvée: " + display.getId());
                return display;
            } else {
                System.err.println("[WeaponCrateAnimation] Aucune Display entity trouvée après summon");
                return null;
            }

        } catch (Exception e) {
            System.err.println("[WeaponCrateAnimation] Erreur lors du summon: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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

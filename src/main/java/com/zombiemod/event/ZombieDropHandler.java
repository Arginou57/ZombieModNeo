package com.zombiemod.event;

import com.zombiemod.config.ZombieDropsConfig;
import com.zombiemod.manager.WaveManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

public class ZombieDropHandler {

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        // Empêcher les mobs des vagues de dropper de l'XP
        if (WaveManager.isWaveMob(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLootDrop(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        // Vérifier si c'est un mob spawné par les vagues
        if (!WaveManager.isWaveMob(entity)) {
            return; // Pas un mob de vague, laisser le comportement normal
        }

        // Annuler les drops par défaut
        event.setCanceled(true);

        // Ajouter les drops configurables
        for (ZombieDropsConfig.DropEntry drop : ZombieDropsConfig.get().getDrops()) {
            // Vérifier si le drop est activé
            if (!drop.enabled) {
                continue;
            }

            // Vérifier la chance de drop
            if (entity.level().getRandom().nextDouble() < drop.chance) {
                // Calculer le nombre d'items à dropper
                int count = drop.minCount;
                if (drop.maxCount > drop.minCount) {
                    count += entity.level().getRandom().nextInt(drop.maxCount - drop.minCount + 1);
                }

                // Créer l'ItemStack
                try {
                    ResourceLocation itemRL = ResourceLocation.parse(drop.item);
                    ItemStack stack = new ItemStack(
                            entity.level().registryAccess().registryOrThrow(Registries.ITEM).get(itemRL),
                            count
                    );

                    // Spawner l'item
                    ItemEntity itemEntity = new ItemEntity(
                            entity.level(),
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            stack
                    );
                    entity.level().addFreshEntity(itemEntity);
                } catch (Exception e) {
                    System.err.println("[ZombieMod] Erreur lors du drop de l'item: " + drop.item);
                    e.printStackTrace();
                }
            }
        }
    }
}

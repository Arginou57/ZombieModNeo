package com.zombiemod.event;

import com.zombiemod.config.ZombieDropsConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

public class ZombieDropHandler {

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        // Empêcher les zombies de dropper de l'XP
        if (event.getEntity() instanceof Zombie) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLootDrop(LivingDropsEvent event) {
        if (event.getEntity() instanceof Zombie zombie) {
            // Annuler les drops par défaut
            event.setCanceled(true);

            // Ajouter les drops configurables
            for (ZombieDropsConfig.DropEntry drop : ZombieDropsConfig.get().getDrops()) {
                // Vérifier la chance de drop
                if (zombie.level().getRandom().nextDouble() < drop.chance) {
                    // Calculer le nombre d'items à dropper
                    int count = drop.minCount;
                    if (drop.maxCount > drop.minCount) {
                        count += zombie.level().getRandom().nextInt(drop.maxCount - drop.minCount + 1);
                    }

                    // Créer l'ItemStack
                    try {
                        ResourceLocation itemRL = ResourceLocation.parse(drop.item);
                        ItemStack stack = new ItemStack(
                                zombie.level().registryAccess().registryOrThrow(Registries.ITEM).get(itemRL),
                                count
                        );

                        // Spawner l'item
                        ItemEntity itemEntity = new ItemEntity(
                                zombie.level(),
                                zombie.getX(),
                                zombie.getY(),
                                zombie.getZ(),
                                stack
                        );
                        zombie.level().addFreshEntity(itemEntity);
                    } catch (Exception e) {
                        System.err.println("[ZombieMod] Erreur lors du drop de l'item: " + drop.item);
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

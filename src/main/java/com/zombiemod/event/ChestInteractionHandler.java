package com.zombiemod.event;

import com.zombiemod.manager.GameManager;
import com.zombiemod.manager.PointsManager;
import com.zombiemod.system.WeaponCrateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ChestInteractionHandler {

    @SubscribeEvent
    public static void onChestInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        // Vérifier si c'est un coffre
        if (!(level.getBlockState(pos).getBlock() instanceof ChestBlock)) {
            return;
        }

        // Vérifier si c'est une caisse d'armes
        if (!WeaponCrateManager.isWeaponCrate(level, pos)) {
            return;
        }

        // Toujours annuler l'ouverture normale
        event.setCanceled(true);

        if (!level.isClientSide) {
            // Vérifier si le joueur est actif
            if (!GameManager.isPlayerActive(player.getUUID())) {
                player.sendSystemMessage(Component.literal("§cVous devez être dans la partie pour acheter ! §7(/zombiejoin)"));
                return;
            }

            int cost = WeaponCrateManager.getCost(level, pos);
            int playerPoints = PointsManager.getPoints(player.getUUID());

            if (playerPoints < cost) {
                player.sendSystemMessage(Component.literal("§c✖ Pas assez de points ! §7(§e" + cost + " §7requis)"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0f, 0.8f);
                return;
            }

            // Retirer les points
            PointsManager.removePoints(player.getUUID(), cost);

            // Obtenir une arme aléatoire
            WeaponCrateManager.WeaponConfig weapon = WeaponCrateManager.getRandomWeapon(level, pos, level.random);

            if (weapon != null) {
                ItemStack item = weapon.toItemStack(level);

                // Ajouter flèches si arc/arbalète
                if (weapon.itemId.contains("bow") || weapon.itemId.contains("crossbow")) {
                    player.addItem(new ItemStack(Items.ARROW, 64));
                }

                player.addItem(item);
                player.sendSystemMessage(Component.literal("§6§l✦ §e" + weapon.displayName + " §6§l✦"));
                player.sendSystemMessage(Component.literal("§7Points restants: §e" + PointsManager.getPoints(player.getUUID())));

                level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.5f);

                spawnParticles((ServerLevel) level, pos);
            } else {
                player.sendSystemMessage(Component.literal("§cErreur: Cette caisse ne contient aucune arme !"));
                // Rembourser
                PointsManager.addPoints(player.getUUID(), cost);
            }
        }
    }

    private static void spawnParticles(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;
            double y = pos.getY() + 0.5 + level.random.nextDouble();
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0, 0.5, 0, 0);
        }
    }
}

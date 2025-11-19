package com.zombiemod.event;

import com.zombiemod.manager.GameManager;
import com.zombiemod.manager.PointsManager;
import com.zombiemod.system.WeaponCrateAnimationManager;
import com.zombiemod.system.WeaponCrateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ChestInteractionHandler {

    // Cooldown pour éviter le double achat (UUID du joueur -> timestamp du dernier achat)
    private static final java.util.Map<java.util.UUID, Long> ammoPurchaseCooldown = new java.util.HashMap<>();
    private static final long COOLDOWN_MS = 500; // 500ms de cooldown

    // Gestionnaire pour le clique gauche (achat de munitions)
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
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

        // Permettre aux OPs en mode créatif de casser la crate normalement
        if (player.hasPermissions(2) && player.isCreative()) {
            // Ne pas annuler l'événement, permettre la destruction normale
            return;
        }

        // Annuler l'événement pour empêcher la destruction du bloc
        event.setCanceled(true);

        if (!level.isClientSide) {
            // Vérifier le cooldown pour éviter le double achat
            long currentTime = System.currentTimeMillis();
            Long lastPurchase = ammoPurchaseCooldown.get(player.getUUID());
            if (lastPurchase != null && (currentTime - lastPurchase) < COOLDOWN_MS) {
                return; // Encore en cooldown, ignorer
            }
            // Vérifier si le joueur est actif
            if (!GameManager.isPlayerActive(player.getUUID())) {
                player.sendSystemMessage(Component.literal("§cVous devez être dans la partie pour acheter ! §7(/zombiejoin)"));
                return;
            }

            // Récupérer les munitions
            ListTag ammoList = WeaponCrateManager.getAmmo(level, pos);

            if (ammoList.isEmpty()) {
                // Pas de munitions, ne rien faire (le clique droit gère les armes)
                return;
            }

            // Acheter toutes les munitions disponibles
            int totalCost = 0;
            java.util.List<ItemStack> itemsToBuy = new java.util.ArrayList<>();
            java.util.List<String> names = new java.util.ArrayList<>();

            for (int i = 0; i < ammoList.size(); i++) {
                CompoundTag ammoTag = ammoList.getCompound(i);
                int prix = ammoTag.getInt("Prix");
                totalCost += prix;

                // Reconstruire l'ItemStack depuis les données
                if (ammoTag.contains("ItemStackData")) {
                    CompoundTag itemData = ammoTag.getCompound("ItemStackData");
                    ItemStack stack = ItemStack.parseOptional(level.registryAccess(), itemData);
                    if (!stack.isEmpty()) {
                        itemsToBuy.add(stack);
                        names.add(ammoTag.getString("Name"));
                    }
                }
            }

            // Vérifier si le joueur a assez de points
            int playerPoints = PointsManager.getPoints(player.getUUID());
            if (playerPoints < totalCost) {
                player.sendSystemMessage(Component.literal("§c✖ Pas assez de points ! §7(§e" + totalCost + " §7requis pour toutes les munitions)"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0f, 0.8f);
                return;
            }

            // Retirer les points
            PointsManager.removePoints(player.getUUID(), totalCost);

            // Enregistrer le timestamp pour éviter le double achat
            ammoPurchaseCooldown.put(player.getUUID(), System.currentTimeMillis());

            // Donner tous les items au joueur
            net.minecraft.server.level.ServerPlayer serverPlayer = (net.minecraft.server.level.ServerPlayer) player;
            for (int i = 0; i < itemsToBuy.size(); i++) {
                ItemStack stack = itemsToBuy.get(i);
                serverPlayer.addItem(stack);
                player.sendSystemMessage(Component.literal("§6§l✦ §e" + names.get(i) + " §7(x" + stack.getCount() + ")"));
            }

            player.sendSystemMessage(Component.literal("§7Points restants: §e" + PointsManager.getPoints(player.getUUID())));
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.2f);
            spawnParticles((ServerLevel) level, pos);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
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

        // IMPORTANT: Ne traiter que la main principale pour éviter les doublons
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            // Ne pas annuler ici si déjà annulé, sinon forcer l'annulation
            if (!event.isCanceled()) {
                event.setCanceled(true);
            }
            return;
        }

        // IMPORTANT: Si l'événement a déjà été annulé par un plugin de protection,
        // on le traite quand même pour les weapon crates
        // On force l'annulation pour empêcher l'ouverture du coffre normal
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

            // Obtenir toutes les armes possibles pour l'animation
            java.util.List<WeaponCrateManager.WeaponConfig> allWeapons = WeaponCrateManager.getAllWeapons(level, pos);

            // Obtenir l'arme gagnée
            WeaponCrateManager.WeaponConfig weapon = WeaponCrateManager.getRandomWeapon(level, pos, level.random);

            if (weapon != null) {
                ItemStack wonItem = weapon.toItemStack(level);
                ServerLevel serverLevel = (ServerLevel) level;
                net.minecraft.server.level.ServerPlayer serverPlayer = (net.minecraft.server.level.ServerPlayer) player;

                // Ajouter flèches si arc/arbalète (immédiatement)
                if (weapon.itemId.contains("bow") || weapon.itemId.contains("crossbow")) {
                    serverPlayer.addItem(new ItemStack(Items.ARROW, 64));
                }

                // Déclencher l'animation ou donner l'item immédiatement
                if (allWeapons.size() >= 2) {
                    // Plusieurs armes : animation de roulette
                    // L'item sera donné au joueur À LA FIN de l'animation (par l'AnimationManager)
                    java.util.List<ItemStack> itemsForAnimation = new java.util.ArrayList<>();
                    for (WeaponCrateManager.WeaponConfig wc : allWeapons) {
                        itemsForAnimation.add(wc.toItemStack(level));
                    }
                    WeaponCrateAnimationManager.startRouletteAnimation(serverLevel, pos, serverPlayer, itemsForAnimation, wonItem);
                } else {
                    // Une seule arme : donner immédiatement (pas d'animation)
                    serverPlayer.addItem(wonItem);
                    serverPlayer.sendSystemMessage(Component.literal("§6§l✦ §e" + weapon.displayName + " §6§l✦"));
                }

                player.sendSystemMessage(Component.literal("§7Points restants: §e" + PointsManager.getPoints(player.getUUID())));

                level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);

                spawnParticles(serverLevel, pos);
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

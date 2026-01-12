package com.zombiemod.event;

import com.zombiemod.manager.GameManager;
import com.zombiemod.manager.PointsManager;
import com.zombiemod.manager.WaveManager;
import com.zombiemod.network.NetworkHandler;
import com.zombiemod.network.packet.PointsAnimationPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class ZombieEventHandler {

    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {
        // Vérifier qu'on est côté serveur
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Zombie zombie) {
            DamageSource source = event.getSource();
            Entity attacker = source.getEntity();

            if (attacker instanceof ServerPlayer player) {
                // Vérifier si le joueur est actif dans la partie
                if (!GameManager.isPlayerActive(player.getUUID())) {
                    return;
                }

                // Ajouter 10 points pour le hit
                PointsManager.addPoints(player.getUUID(), 10);

                // Envoyer le packet d'animation au client
                NetworkHandler.sendToPlayer(player, new PointsAnimationPacket(10));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        // Vérifier qu'on est côté serveur
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Zombie zombie) {
            // Un zombie est mort
            DamageSource source = event.getSource();
            Entity killer = source.getEntity();

            if (killer instanceof ServerPlayer player) {
                // Vérifier si le joueur est actif dans la partie
                if (!GameManager.isPlayerActive(player.getUUID())) {
                    return; // Les spectateurs ne gagnent pas de points
                }

                // Ajouter des points
                PointsManager.addPoints(player.getUUID(), 100);

                // Envoyer le packet d'animation au client
                NetworkHandler.sendToPlayer(player, new PointsAnimationPacket(100));

                // Décrémenter le compteur de zombies
                WaveManager.onZombieKilled();
            }
        }
    }
}

package com.zombiemod.event;

import com.zombiemod.manager.GameManager;
import com.zombiemod.system.ServerWeaponCrateTracker;
import com.zombiemod.network.NetworkHandler;
import com.zombiemod.network.packet.WeaponCrateSyncPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerConnectionHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Envoyer la liste des weapon crates au joueur qui vient de se connecter
            ServerWeaponCrateTracker.syncToPlayer(player);

            // Envoyer la liste des jukeboxes au joueur qui vient de se connecter
            com.zombiemod.system.ServerJukeboxTracker.syncToPlayer(player);

            // Envoyer la liste des portes au joueur qui vient de se connecter
            com.zombiemod.system.ServerDoorTracker.syncToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();

            // Vérifier si une partie est en cours
            if (GameManager.getGameState() != GameManager.GameState.WAITING) {
                // Vérifier si tous les joueurs sont déconnectés
                if (GameManager.areAllPlayersDisconnected(level)) {
                    System.out.println("[ZombieMod] Tous les joueurs sont déconnectés - Arrêt automatique de la partie");
                    GameManager.stopGameAutomatic(level);
                }
            }
        }
    }
}

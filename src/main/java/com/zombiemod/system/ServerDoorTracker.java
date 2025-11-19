package com.zombiemod.system;

import com.zombiemod.map.DoorConfig;
import com.zombiemod.map.MapConfig;
import com.zombiemod.map.MapManager;
import com.zombiemod.network.NetworkHandler;
import com.zombiemod.network.packet.DoorSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracker côté serveur pour les portes
 * Synchronise l'état des portes de la map active avec les clients
 */
public class ServerDoorTracker {

    private static ServerLevel currentLevel = null;

    /**
     * Initialise le tracker avec le niveau serveur
     */
    public static void initialize(ServerLevel level) {
        currentLevel = level;
        syncToAllPlayers();
    }

    /**
     * Construit la map des données de portes depuis la MapConfig active
     */
    private static Map<BlockPos, DoorSyncPacket.DoorData> buildDoorDataMap() {
        Map<BlockPos, DoorSyncPacket.DoorData> doorDataMap = new HashMap<>();

        MapConfig map = MapManager.getSelectedMap();
        if (map != null) {
            Map<Integer, DoorConfig> doors = map.getDoors();
            for (DoorConfig door : doors.values()) {
                BlockPos signPos = door.getSignPosition();
                if (signPos != null) {
                    doorDataMap.put(signPos, new DoorSyncPacket.DoorData(
                        door.getDoorNumber(),
                        door.getCost(),
                        door.isOpen()
                    ));
                }
            }
        }

        return doorDataMap;
    }

    /**
     * Synchronise toutes les portes avec tous les joueurs
     */
    public static void syncToAllPlayers() {
        Map<BlockPos, DoorSyncPacket.DoorData> doorData = buildDoorDataMap();
        System.out.println("[ServerDoorTracker] Synchronisation de " + doorData.size() + " porte(s) avec tous les joueurs");
        NetworkHandler.sendToAllPlayers(new DoorSyncPacket(doorData));
    }

    /**
     * Synchronise toutes les portes avec un joueur spécifique
     */
    public static void syncToPlayer(net.minecraft.server.level.ServerPlayer player) {
        Map<BlockPos, DoorSyncPacket.DoorData> doorData = buildDoorDataMap();
        System.out.println("[ServerDoorTracker] Synchronisation de " + doorData.size() + " porte(s) avec " + player.getName().getString());
        NetworkHandler.sendToPlayer(player, new DoorSyncPacket(doorData));
    }
}

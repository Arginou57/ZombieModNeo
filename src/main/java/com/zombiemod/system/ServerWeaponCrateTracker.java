package com.zombiemod.system;

import com.zombiemod.network.NetworkHandler;
import com.zombiemod.network.packet.WeaponCrateSyncPacket;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracker côté serveur pour les weapon crates
 * Garde une map des positions et coûts et synchronise avec les clients
 */
public class ServerWeaponCrateTracker {

    private static Map<BlockPos, Integer> weaponCrates = new HashMap<>();

    public static void addWeaponCrate(BlockPos pos, int cost) {
        weaponCrates.put(pos, cost);
        syncToAllPlayers();
    }

    public static void removeWeaponCrate(BlockPos pos) {
        weaponCrates.remove(pos);
        syncToAllPlayers();
    }

    public static void clear() {
        weaponCrates.clear();
        syncToAllPlayers();
    }

    public static Map<BlockPos, Integer> getAllCrates() {
        return new HashMap<>(weaponCrates);
    }

    public static void syncToAllPlayers() {
        NetworkHandler.sendToAllPlayers(new WeaponCrateSyncPacket(getAllCrates()));
    }

    public static void reset() {
        weaponCrates.clear();
    }
}

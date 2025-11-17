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
        System.out.println("[ServerWeaponCrateTracker] Caisse ajoutée: " + pos + " -> " + cost + " points");
        System.out.println("[ServerWeaponCrateTracker] Total caisses: " + weaponCrates.size());
        syncToAllPlayers();
    }

    /**
     * Ajoute une weapon crate SANS synchroniser (pour scan initial)
     */
    public static void addWeaponCrateNoSync(BlockPos pos, int cost) {
        weaponCrates.put(pos, cost);
    }

    public static void removeWeaponCrate(BlockPos pos) {
        weaponCrates.remove(pos);
        System.out.println("[ServerWeaponCrateTracker] Caisse supprimée: " + pos);
        syncToAllPlayers();
    }

    /**
     * Supprime une weapon crate SANS synchroniser
     */
    public static void removeWeaponCrateNoSync(BlockPos pos) {
        weaponCrates.remove(pos);
    }

    public static void clear() {
        weaponCrates.clear();
        System.out.println("[ServerWeaponCrateTracker] Toutes les caisses effacées");
        syncToAllPlayers();
    }

    public static Map<BlockPos, Integer> getAllCrates() {
        return new HashMap<>(weaponCrates);
    }

    public static void syncToAllPlayers() {
        System.out.println("[ServerWeaponCrateTracker] Synchronisation de " + weaponCrates.size() + " caisses avec tous les joueurs");
        NetworkHandler.sendToAllPlayers(new WeaponCrateSyncPacket(getAllCrates()));
    }

    public static void reset() {
        weaponCrates.clear();
    }
}

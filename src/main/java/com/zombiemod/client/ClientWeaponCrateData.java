package com.zombiemod.client;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache côté client pour les données des weapon crates
 */
public class ClientWeaponCrateData {

    private static Map<BlockPos, Integer> weaponCrates = new HashMap<>();

    public static void setWeaponCrate(BlockPos pos, int cost) {
        weaponCrates.put(pos, cost);
        System.out.println("[ClientWeaponCrateData] Caisse ajoutée: " + pos + " -> " + cost + " points");
    }

    public static void removeWeaponCrate(BlockPos pos) {
        weaponCrates.remove(pos);
        System.out.println("[ClientWeaponCrateData] Caisse supprimée: " + pos);
    }

    public static boolean isWeaponCrate(BlockPos pos) {
        return weaponCrates.containsKey(pos);
    }

    public static int getCost(BlockPos pos) {
        return weaponCrates.getOrDefault(pos, 0);
    }

    public static void clear() {
        weaponCrates.clear();
        System.out.println("[ClientWeaponCrateData] Cache effacé");
    }

    public static void setAll(Map<BlockPos, Integer> crates) {
        weaponCrates.clear();
        weaponCrates.putAll(crates);
        System.out.println("[ClientWeaponCrateData] Cache mis à jour avec " + crates.size() + " caisses:");
        crates.forEach((pos, cost) -> System.out.println("  - " + pos + " -> " + cost + " points"));
    }
}

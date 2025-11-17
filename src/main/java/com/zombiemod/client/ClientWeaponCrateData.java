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
    }

    public static void removeWeaponCrate(BlockPos pos) {
        weaponCrates.remove(pos);
    }

    public static boolean isWeaponCrate(BlockPos pos) {
        return weaponCrates.containsKey(pos);
    }

    public static int getCost(BlockPos pos) {
        return weaponCrates.getOrDefault(pos, 0);
    }

    public static void clear() {
        weaponCrates.clear();
    }

    public static void setAll(Map<BlockPos, Integer> crates) {
        weaponCrates.clear();
        weaponCrates.putAll(crates);
    }
}

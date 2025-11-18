package com.zombiemod.system;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Sauvegarde persistante des weapon crates
 * Permet de recharger les données après un redémarrage du serveur
 */
public class WeaponCrateSavedData extends SavedData {

    private static final String DATA_NAME = "zombiemod_weapon_crates";
    private final Map<BlockPos, Integer> weaponCrates = new HashMap<>();

    public WeaponCrateSavedData() {
        super();
    }

    /**
     * Charge les données depuis NBT
     */
    public static WeaponCrateSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        WeaponCrateSavedData data = new WeaponCrateSavedData();

        ListTag cratesList = tag.getList("WeaponCrates", Tag.TAG_COMPOUND);
        for (Tag crateTag : cratesList) {
            CompoundTag crateData = (CompoundTag) crateTag;

            int x = crateData.getInt("x");
            int y = crateData.getInt("y");
            int z = crateData.getInt("z");
            int cost = crateData.getInt("cost");

            BlockPos pos = new BlockPos(x, y, z);
            data.weaponCrates.put(pos, cost);
        }

        System.out.println("[WeaponCrateSavedData] Chargé " + data.weaponCrates.size() + " weapon crates depuis la sauvegarde");
        return data;
    }

    /**
     * Sauvegarde les données en NBT
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag cratesList = new ListTag();

        for (Map.Entry<BlockPos, Integer> entry : weaponCrates.entrySet()) {
            CompoundTag crateData = new CompoundTag();
            BlockPos pos = entry.getKey();

            crateData.putInt("x", pos.getX());
            crateData.putInt("y", pos.getY());
            crateData.putInt("z", pos.getZ());
            crateData.putInt("cost", entry.getValue());

            cratesList.add(crateData);
        }

        tag.put("WeaponCrates", cratesList);
        System.out.println("[WeaponCrateSavedData] Sauvegardé " + weaponCrates.size() + " weapon crates");
        return tag;
    }

    /**
     * Récupère ou crée l'instance de sauvegarde pour le niveau
     */
    public static WeaponCrateSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(
                WeaponCrateSavedData::new,
                WeaponCrateSavedData::load,
                null
            ),
            DATA_NAME
        );
    }

    /**
     * Ajoute une weapon crate
     */
    public void addCrate(BlockPos pos, int cost) {
        weaponCrates.put(pos, cost);
        setDirty();
    }

    /**
     * Supprime une weapon crate
     */
    public void removeCrate(BlockPos pos) {
        weaponCrates.remove(pos);
        setDirty();
    }

    /**
     * Efface toutes les crates
     */
    public void clearAll() {
        weaponCrates.clear();
        setDirty();
    }

    /**
     * Récupère toutes les crates
     */
    public Map<BlockPos, Integer> getAllCrates() {
        return new HashMap<>(weaponCrates);
    }

    /**
     * Nombre de crates sauvegardées
     */
    public int size() {
        return weaponCrates.size();
    }
}

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
 * Sauvegarde persistante des jukeboxes zombie
 * Permet de recharger les données après un redémarrage du serveur
 */
public class JukeboxSavedData extends SavedData {

    private static final String DATA_NAME = "zombiemod_jukeboxes";
    private final Map<BlockPos, Integer> jukeboxes = new HashMap<>();

    public JukeboxSavedData() {
        super();
    }

    /**
     * Charge les données depuis NBT
     */
    public static JukeboxSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        JukeboxSavedData data = new JukeboxSavedData();

        ListTag jukeboxesList = tag.getList("Jukeboxes", Tag.TAG_COMPOUND);
        for (Tag jukeboxTag : jukeboxesList) {
            CompoundTag jukeboxData = (CompoundTag) jukeboxTag;

            int x = jukeboxData.getInt("x");
            int y = jukeboxData.getInt("y");
            int z = jukeboxData.getInt("z");
            int cost = jukeboxData.getInt("cost");

            BlockPos pos = new BlockPos(x, y, z);
            data.jukeboxes.put(pos, cost);
        }

        System.out.println("[JukeboxSavedData] Chargé " + data.jukeboxes.size() + " jukeboxes depuis la sauvegarde");
        return data;
    }

    /**
     * Sauvegarde les données en NBT
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag jukeboxesList = new ListTag();

        for (Map.Entry<BlockPos, Integer> entry : jukeboxes.entrySet()) {
            CompoundTag jukeboxData = new CompoundTag();
            BlockPos pos = entry.getKey();

            jukeboxData.putInt("x", pos.getX());
            jukeboxData.putInt("y", pos.getY());
            jukeboxData.putInt("z", pos.getZ());
            jukeboxData.putInt("cost", entry.getValue());

            jukeboxesList.add(jukeboxData);
        }

        tag.put("Jukeboxes", jukeboxesList);
        System.out.println("[JukeboxSavedData] Sauvegardé " + jukeboxes.size() + " jukeboxes");
        return tag;
    }

    /**
     * Récupère ou crée l'instance de sauvegarde pour le niveau
     */
    public static JukeboxSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(
                JukeboxSavedData::new,
                JukeboxSavedData::load,
                null
            ),
            DATA_NAME
        );
    }

    /**
     * Ajoute un jukebox
     */
    public void addJukebox(BlockPos pos, int cost) {
        jukeboxes.put(pos, cost);
        setDirty();
    }

    /**
     * Supprime un jukebox
     */
    public void removeJukebox(BlockPos pos) {
        jukeboxes.remove(pos);
        setDirty();
    }

    /**
     * Efface tous les jukeboxes
     */
    public void clearAll() {
        jukeboxes.clear();
        setDirty();
    }

    /**
     * Récupère tous les jukeboxes
     */
    public Map<BlockPos, Integer> getAllJukeboxes() {
        return new HashMap<>(jukeboxes);
    }

    /**
     * Nombre de jukeboxes sauvegardés
     */
    public int size() {
        return jukeboxes.size();
    }
}

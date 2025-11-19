package com.zombiemod.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class MapManager {

    private static Map<String, MapConfig> maps = new HashMap<>();
    private static String selectedMapName = null;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File mapsFile;

    public static void init(File configDir) {
        mapsFile = new File(configDir, "zombiemod-maps.json");

        if (mapsFile.exists()) {
            load();
        } else {
            // Créer une map par défaut
            createMap("default");
            selectMap("default");
            save();
        }
    }

    public static void load() {
        try (FileReader reader = new FileReader(mapsFile)) {
            Type type = new TypeToken<MapData>() {}.getType();
            MapData data = GSON.fromJson(reader, type);

            if (data != null) {
                maps = data.maps != null ? data.maps : new HashMap<>();
                selectedMapName = data.selectedMap;

                // Si aucune map sélectionnée mais des maps existent, sélectionner la première
                if (selectedMapName == null && !maps.isEmpty()) {
                    selectedMapName = maps.keySet().iterator().next();
                }

                System.out.println("[ZombieMod] " + maps.size() + " map(s) chargée(s) depuis " + mapsFile.getPath());
            }
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors du chargement des maps");
            e.printStackTrace();
            // Créer une map par défaut en cas d'erreur
            createMap("default");
            selectMap("default");
        }
    }

    public static void save() {
        try {
            mapsFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(mapsFile)) {
                MapData data = new MapData();
                data.maps = maps;
                data.selectedMap = selectedMapName;
                GSON.toJson(data, writer);
            }
            System.out.println("[ZombieMod] " + maps.size() + " map(s) sauvegardée(s) dans " + mapsFile.getPath());
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors de la sauvegarde des maps");
            e.printStackTrace();
        }
    }

    public static boolean createMap(String name) {
        if (maps.containsKey(name)) {
            return false;
        }
        maps.put(name, new MapConfig(name));
        save();
        return true;
    }

    public static boolean deleteMap(String name) {
        if (!maps.containsKey(name)) {
            return false;
        }
        maps.remove(name);

        // Si on supprime la map sélectionnée, en sélectionner une autre
        if (name.equals(selectedMapName)) {
            if (!maps.isEmpty()) {
                selectedMapName = maps.keySet().iterator().next();
            } else {
                selectedMapName = null;
            }
        }

        save();
        return true;
    }

    public static boolean selectMap(String name) {
        if (!maps.containsKey(name)) {
            return false;
        }
        selectedMapName = name;
        save();
        return true;
    }

    public static MapConfig getSelectedMap() {
        if (selectedMapName == null) {
            return null;
        }
        return maps.get(selectedMapName);
    }

    public static String getSelectedMapName() {
        return selectedMapName;
    }

    public static MapConfig getMap(String name) {
        return maps.get(name);
    }

    public static Collection<MapConfig> getAllMaps() {
        return maps.values();
    }

    public static Set<String> getAllMapNames() {
        return maps.keySet();
    }

    public static boolean mapExists(String name) {
        return maps.containsKey(name);
    }

    public static int getMapCount() {
        return maps.size();
    }

    // Méthodes de raccourci pour la map sélectionnée
    public static void setRespawnPoint(BlockPos pos) {
        MapConfig map = getSelectedMap();
        if (map != null) {
            map.setRespawnPoint(pos);
            save();
        }
    }

    public static BlockPos getRespawnPoint() {
        MapConfig map = getSelectedMap();
        return map != null ? map.getRespawnPoint() : null;
    }

    public static void addZombieSpawnPoint(BlockPos pos) {
        MapConfig map = getSelectedMap();
        if (map != null) {
            map.addZombieSpawnPoint(pos);
            save();
        }
    }

    public static void clearZombieSpawnPoints() {
        MapConfig map = getSelectedMap();
        if (map != null) {
            map.clearZombieSpawnPoints();
            save();
        }
    }

    public static List<BlockPos> getZombieSpawnPoints() {
        MapConfig map = getSelectedMap();
        return map != null ? map.getZombieSpawnPoints() : new ArrayList<>();
    }

    /**
     * Récupère les points de spawn actifs en fonction des portes ouvertes
     */
    public static List<BlockPos> getActiveZombieSpawnPoints() {
        MapConfig map = getSelectedMap();
        return map != null ? map.getActiveZombieSpawnPoints() : new ArrayList<>();
    }

    // Classe pour la sérialisation JSON
    private static class MapData {
        Map<String, MapConfig> maps;
        String selectedMap;
    }
}

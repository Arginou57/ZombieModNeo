package com.zombiemod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZombieDropsConfig {

    public static class DropEntry {
        public String item; // ID de l'item (ex: "minecraft:gunpowder")
        public double chance; // Chance de drop (0.0 à 1.0)
        public int minCount; // Nombre minimum d'items à drop
        public int maxCount; // Nombre maximum d'items à drop
        public boolean enabled; // Activer/désactiver ce drop

        public DropEntry(String item, double chance, int minCount, int maxCount, boolean enabled) {
            this.item = item;
            this.chance = chance;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.enabled = enabled;
        }
    }

    // Configuration par défaut
    private List<DropEntry> drops = new ArrayList<>();

    private static ZombieDropsConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public ZombieDropsConfig() {
        // Drops par défaut (désactivés)
        drops.add(new DropEntry("minecraft:gunpowder", 0.5, 1, 3, false));
        drops.add(new DropEntry("minecraft:copper_ingot", 0.3, 1, 2, false));
    }

    public static void init(File configDir) {
        configFile = new File(configDir, "zombiedrops.json");

        if (!configFile.exists()) {
            instance = new ZombieDropsConfig();
            save();
        } else {
            load();
        }
    }

    public static void load() {
        try (FileReader reader = new FileReader(configFile)) {
            instance = GSON.fromJson(reader, ZombieDropsConfig.class);
            if (instance == null) {
                instance = new ZombieDropsConfig();
            }
            System.out.println("[ZombieMod] Configuration des drops chargée depuis " + configFile.getPath());
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors du chargement de la config drops, utilisation des valeurs par défaut");
            instance = new ZombieDropsConfig();
        }
    }

    public static void save() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(instance, writer);
            }
            System.out.println("[ZombieMod] Configuration des drops sauvegardée dans " + configFile.getPath());
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors de la sauvegarde de la config drops");
            e.printStackTrace();
        }
    }

    public static ZombieDropsConfig get() {
        if (instance == null) {
            instance = new ZombieDropsConfig();
        }
        return instance;
    }

    // Getters
    public List<DropEntry> getDrops() {
        return drops;
    }

    // Setters (si besoin de modifier via commandes)
    public void addDrop(String item, double chance, int minCount, int maxCount, boolean enabled) {
        drops.add(new DropEntry(item, chance, minCount, maxCount, enabled));
        save();
    }

    public void clearDrops() {
        drops.clear();
        save();
    }
}

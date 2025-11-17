package com.zombiemod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ZombieConfig {

    // Configuration par défaut
    private int maxZombiesOnMap = 32;
    private double spawnDelaySeconds = 2.0;
    private double heartsPerWave = 0.5; // 1 coeur = 2 HP
    private int startingHearts = 1; // HP de départ (1 coeur = 2 HP)
    private double zombieFollowRange = 32.0; // Portée de détection des joueurs en blocs
    private double armoredZombieChance = 0.15; // 15% de chance de spawner avec armure
    private int waveTimeoutSeconds = 50; // Temps max pour finir une vague (0 = désactivé)
    private int glowingZombiesCount = 5; // Nombre de derniers zombies avec effet glowing

    private static ZombieConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public static void init(File configDir) {
        configFile = new File(configDir, "zombiemod.json");

        if (!configFile.exists()) {
            instance = new ZombieConfig();
            save();
        } else {
            load();
        }
    }

    public static void load() {
        try (FileReader reader = new FileReader(configFile)) {
            instance = GSON.fromJson(reader, ZombieConfig.class);
            if (instance == null) {
                instance = new ZombieConfig();
            }
            System.out.println("[ZombieMod] Configuration chargée depuis " + configFile.getPath());
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors du chargement de la config, utilisation des valeurs par défaut");
            instance = new ZombieConfig();
        }
    }

    public static void save() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(instance, writer);
            }
            System.out.println("[ZombieMod] Configuration sauvegardée dans " + configFile.getPath());
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors de la sauvegarde de la config");
            e.printStackTrace();
        }
    }

    public static ZombieConfig get() {
        if (instance == null) {
            instance = new ZombieConfig();
        }
        return instance;
    }

    // Getters
    public int getMaxZombiesOnMap() {
        return maxZombiesOnMap;
    }

    public double getSpawnDelaySeconds() {
        return spawnDelaySeconds;
    }

    public int getSpawnDelayTicks() {
        return (int) (spawnDelaySeconds * 20);
    }

    public double getHeartsPerWave() {
        return heartsPerWave;
    }

    public int getStartingHearts() {
        return startingHearts;
    }

    public int getStartingHP() {
        return startingHearts * 2;
    }

    // Calcul des HP pour une vague donnée
    public float getHealthForWave(int wave) {
        // HP = startingHP + (wave - 1) * heartsPerWave * 2
        int baseHP = getStartingHP();
        float additionalHP = (wave - 1) * (float) heartsPerWave * 2.0f;
        return baseHP + additionalHP;
    }

    public double getZombieFollowRange() {
        return zombieFollowRange;
    }

    public double getArmoredZombieChance() {
        return armoredZombieChance;
    }

    public int getWaveTimeoutSeconds() {
        return waveTimeoutSeconds;
    }

    public int getWaveTimeoutTicks() {
        return waveTimeoutSeconds * 20;
    }

    public int getGlowingZombiesCount() {
        return glowingZombiesCount;
    }

    // Setters (pour modification via commandes si besoin)
    public void setMaxZombiesOnMap(int value) {
        this.maxZombiesOnMap = value;
        save();
    }

    public void setSpawnDelaySeconds(double value) {
        this.spawnDelaySeconds = value;
        save();
    }

    public void setHeartsPerWave(double value) {
        this.heartsPerWave = value;
        save();
    }

    public void setStartingHearts(int value) {
        this.startingHearts = value;
        save();
    }
}

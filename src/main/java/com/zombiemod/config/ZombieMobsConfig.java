package com.zombiemod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZombieMobsConfig {

    public static class MobEntry {
        public String mobType; // Type de mob (ex: "minecraft:zombie", "minecraft:skeleton")
        public double chance; // Chance d'apparition (0.0 à 1.0, doit totaliser 1.0 ou moins)
        public double baseSpeed; // Vitesse de base du mob
        public double speedPerWave; // Vitesse supplémentaire ajoutée par vague
        public double maxSpeed; // Vitesse maximale (plafond)
        public int startingHearts; // HP de départ en cœurs (1 cœur = 2 HP)
        public double heartsPerWave; // HP supplémentaires par vague en cœurs
        public double startingDamage; // Dégâts de départ (en cœurs, 1 cœur = 2 HP)
        public double damagePerWave; // Dégâts supplémentaires par vague (en cœurs)
        public double maxDamage; // Dégâts maximums (en cœurs, plafond)

        public MobEntry(String mobType, double chance, double baseSpeed, double speedPerWave, double maxSpeed, int startingHearts, double heartsPerWave, double startingDamage, double damagePerWave, double maxDamage) {
            this.mobType = mobType;
            this.chance = chance;
            this.baseSpeed = baseSpeed;
            this.speedPerWave = speedPerWave;
            this.maxSpeed = maxSpeed;
            this.startingHearts = startingHearts;
            this.heartsPerWave = heartsPerWave;
            this.startingDamage = startingDamage;
            this.damagePerWave = damagePerWave;
            this.maxDamage = maxDamage;
        }

        // Méthode pour calculer les HP pour une vague donnée
        public float getHealthForWave(int wave) {
            // HP = startingHP + (wave - 1) * heartsPerWave * 2
            int baseHP = startingHearts * 2;
            float additionalHP = (wave - 1) * (float) heartsPerWave * 2.0f;
            return baseHP + additionalHP;
        }
    }

    // Configuration par défaut
    private List<MobEntry> mobs = new ArrayList<>();

    private static ZombieMobsConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public ZombieMobsConfig() {
        // Zombie normal (90% de spawn)
        mobs.add(new MobEntry("minecraft:zombie", 0.9, 0.13, 0.007, 0.29, 1, 0.5, 1.5, 0.007, 10.0));

        // Husk (zombie du désert) (10% de spawn)
        mobs.add(new MobEntry("minecraft:husk", 0.1, 0.13, 0.005, 0.25, 3, 0.7, 2.5, 0.007, 15.0));
    }

    public static void init(File configDir) {
        configFile = new File(configDir, "zombiemobs.json");

        if (!configFile.exists()) {
            instance = new ZombieMobsConfig();
            save();
        } else {
            load();
        }
    }

    public static void load() {
        try (FileReader reader = new FileReader(configFile)) {
            instance = GSON.fromJson(reader, ZombieMobsConfig.class);
            if (instance == null) {
                instance = new ZombieMobsConfig();
            }

            // Vérifier que les chances totalisent <= 1.0
            double totalChance = 0;
            for (MobEntry mob : instance.mobs) {
                totalChance += mob.chance;
            }
            if (totalChance > 1.0) {
                System.err.println("[ZombieMod] ATTENTION: Les chances de spawn totalisent " + totalChance + " > 1.0 !");
            }

            System.out.println("[ZombieMod] Configuration des mobs chargée depuis " + configFile.getPath());
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors du chargement de la config mobs, utilisation des valeurs par défaut");
            instance = new ZombieMobsConfig();
        }
    }

    public static void save() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(instance, writer);
            }
            System.out.println("[ZombieMod] Configuration des mobs sauvegardée dans " + configFile.getPath());
        } catch (IOException e) {
            System.err.println("[ZombieMod] Erreur lors de la sauvegarde de la config mobs");
            e.printStackTrace();
        }
    }

    public static ZombieMobsConfig get() {
        if (instance == null) {
            instance = new ZombieMobsConfig();
        }
        return instance;
    }

    // Getters
    public List<MobEntry> getMobs() {
        return mobs;
    }

    // Méthode pour sélectionner un type de mob aléatoire selon les chances
    public String getRandomMobType(double randomValue) {
        double currentChance = 0;

        for (MobEntry mob : mobs) {
            currentChance += mob.chance;
            if (randomValue < currentChance) {
                return mob.mobType;
            }
        }

        // Fallback sur le premier mob si aucun n'a été sélectionné
        return mobs.isEmpty() ? "minecraft:zombie" : mobs.get(0).mobType;
    }

    // Méthode pour sélectionner une MobEntry aléatoire selon les chances
    public MobEntry getRandomMobEntry(double randomValue) {
        double currentChance = 0;

        for (MobEntry mob : mobs) {
            currentChance += mob.chance;
            if (randomValue < currentChance) {
                return mob;
            }
        }

        // Fallback sur le premier mob si aucun n'a été sélectionné
        if (mobs.isEmpty()) {
            return new MobEntry("minecraft:zombie", 1.0, 0.13, 0.007, 0.29, 1, 0.5, 1.5, 0.007, 10.0);
        }
        return mobs.get(0);
    }
}

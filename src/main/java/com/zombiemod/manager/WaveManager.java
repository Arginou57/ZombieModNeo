package com.zombiemod.manager;

import com.zombiemod.ModSounds;
import com.zombiemod.config.ZombieConfig;
import com.zombiemod.config.ZombieMobsConfig;
import com.zombiemod.map.MapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaveManager {

    private static int currentWave = 0;
    private static int zombiesRemaining = 0;
    private static int zombiesToSpawn = 0;
    private static int waveCountdownTicks = 0;
    private static int spawnDelayTicks = 0;
    private static int timeoutTicks = 0; // Compteur pour le timeout de vague
    private static int lastZombieCount = 0; // Dernier nombre de zombies pour détecter la progression
    private static List<Mob> activeMobs = new ArrayList<>();
    private static int targetUpdateTicks = 0; // Compteur pour la mise à jour du ciblage

    public static int getCurrentWave() {
        return currentWave;
    }

    public static int getZombiesRemaining() {
        return zombiesRemaining;
    }

    public static int getCountdownSeconds() {
        return waveCountdownTicks / 20;
    }

    // Utilise la map sélectionnée via MapManager
    public static void addSpawnPoint(BlockPos pos) {
        MapManager.addZombieSpawnPoint(pos);
    }

    public static List<BlockPos> getSpawnPoints() {
        return MapManager.getZombieSpawnPoints();
    }

    public static void clearSpawnPoints() {
        MapManager.clearZombieSpawnPoints();
    }

    public static void startWave(ServerLevel level) {
        // Vérifier si tous les joueurs sont déconnectés avant de démarrer la vague
        if (GameManager.areAllPlayersDisconnected(level)) {
            System.out.println("[ZombieMod] Tous les joueurs sont déconnectés - Arrêt automatique de la partie avant la vague " + (currentWave + 1));
            GameManager.stopGameAutomatic(level);
            return;
        }

        currentWave++;
        zombiesRemaining = 6 + (currentWave * 6);
        zombiesToSpawn = zombiesRemaining;
        activeMobs.clear();
        spawnDelayTicks = 0;
        timeoutTicks = 0; // Reset timeout counter
        lastZombieCount = zombiesRemaining; // Initialiser le compteur
        targetUpdateTicks = 0; // Reset target update counter

        GameManager.broadcastToAll(level, "§6§l=== VAGUE " + currentWave + " ===");
        GameManager.broadcastToAll(level, "§c" + zombiesRemaining + " zombies §eà éliminer !");

        // Utiliser les spawns actifs (en fonction des portes ouvertes)
        List<BlockPos> spawnPoints = MapManager.getActiveZombieSpawnPoints();
        if (spawnPoints.isEmpty()) {
            String mapName = MapManager.getSelectedMapName();
            GameManager.broadcastToAll(level, "§c§lERREUR: Aucun point de spawn actif pour la map '" + mapName + "' !");
            GameManager.broadcastToAll(level, "§7Vérifiez que des portes sont ouvertes ou ajoutez des spawns sans porte.");
            return;
        }

        // Son de début de round (uniquement pour les joueurs de la partie)
        GameManager.playSoundToActivePlayers(level, ModSounds.ROUND_START.get(), 1.0f);
    }

    private static void spawnMob(ServerLevel level, BlockPos pos, boolean glowing) {
        // Sélectionner le mob aléatoirement selon la configuration
        ZombieMobsConfig.MobEntry mobEntry = ZombieMobsConfig.get().getRandomMobEntry(level.getRandom().nextDouble());
        ResourceLocation mobTypeRL = ResourceLocation.parse(mobEntry.mobType);

        // Récupérer l'EntityType depuis le registry
        EntityType<?> entityType = level.registryAccess()
                .registryOrThrow(Registries.ENTITY_TYPE)
                .get(mobTypeRL);

        if (entityType == null) {
            System.err.println("[ZombieMod] Type de mob inconnu: " + mobEntry.mobType + ", fallback sur zombie");
            entityType = EntityType.ZOMBIE;
        }

        // Créer l'entité
        net.minecraft.world.entity.Entity entity = entityType.create(level);
        if (!(entity instanceof Mob mob)) {
            System.err.println("[ZombieMod] L'entité " + mobEntry.mobType + " n'est pas un Mob, spawn annulé");
            return;
        }

        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        mob.setPersistenceRequired();

        // HP progressifs basés sur la vague (utilise la config du mob)
        float health = mobEntry.getHealthForWave(currentWave);
        mob.setHealth(health);
        mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(health);

        // Portée de détection des joueurs (Follow Range) - s'applique à tous les mobs
        double followRange = ZombieConfig.get().getMobFollowRange();
        mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).setBaseValue(followRange);

        // Vitesse progressive : baseSpeed + (speedPerWave * currentWave), plafonnée à maxSpeed
        double speed = mobEntry.baseSpeed + (mobEntry.speedPerWave * currentWave);
        // Appliquer la limite de vitesse si maxSpeed > 0 (rétrocompatibilité)
        double finalSpeed = (mobEntry.maxSpeed > 0) ? Math.min(speed, mobEntry.maxSpeed) : speed;
        mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(finalSpeed);

        // Dégâts progressifs : startingDamage + (damagePerWave * currentWave), plafonné à maxDamage
        // Convertir les cœurs en HP (1 cœur = 2 HP)
        double damage = mobEntry.startingDamage + (mobEntry.damagePerWave * (currentWave - 1));
        // Appliquer la limite de dégâts si maxDamage > 0 (rétrocompatibilité)
        double finalDamage = (mobEntry.maxDamage > 0) ? Math.min(damage, mobEntry.maxDamage) : damage;
        // Convertir en HP (multiply by 2)
        double damageHP = finalDamage * 2.0;

        // Appliquer les dégâts au mob
        if (mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null) {
            mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(damageHP);
        }

        // Désactiver les drops
        mob.setCanPickUpLoot(false);

        // Marqueur pour identifier les mobs des vagues : oak_button sur la tête (invisible)
        mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_BUTTON));
        mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.0f);

        // Effet glowing pour les derniers
        if (glowing) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        }

        // Chance de spawner un super zombie avec armure (uniquement pour les zombies)
        if (mob instanceof Zombie zombie) {
            double armorChance = ZombieConfig.get().getArmoredZombieChance();
            if (level.getRandom().nextDouble() < armorChance) {
                equipZombieWithArmor(zombie, level.getRandom());
            }
        }

        level.addFreshEntity(mob);
        activeMobs.add(mob);
    }

    private static void equipZombieWithArmor(Zombie zombie, RandomSource random) {
        // Déterminer le type d'armure selon la vague
        // 0-10 : Cuir
        // 10-20 : Or
        // 20-30 : Fer
        // 30+ : Diamant

        net.minecraft.world.item.Item helmetItem = null;
        net.minecraft.world.item.Item chestplateItem = null;
        net.minecraft.world.item.Item leggingsItem = null;
        net.minecraft.world.item.Item bootsItem = null;

        if (currentWave < 10) {
            // Armure en cuir
            helmetItem = net.minecraft.world.item.Items.LEATHER_HELMET;
            chestplateItem = net.minecraft.world.item.Items.LEATHER_CHESTPLATE;
            leggingsItem = net.minecraft.world.item.Items.LEATHER_LEGGINGS;
            bootsItem = net.minecraft.world.item.Items.LEATHER_BOOTS;
        } else if (currentWave < 20) {
            // Armure en or
            helmetItem = net.minecraft.world.item.Items.GOLDEN_HELMET;
            chestplateItem = net.minecraft.world.item.Items.GOLDEN_CHESTPLATE;
            leggingsItem = net.minecraft.world.item.Items.GOLDEN_LEGGINGS;
            bootsItem = net.minecraft.world.item.Items.GOLDEN_BOOTS;
        } else if (currentWave < 30) {
            // Armure en fer
            helmetItem = net.minecraft.world.item.Items.IRON_HELMET;
            chestplateItem = net.minecraft.world.item.Items.IRON_CHESTPLATE;
            leggingsItem = net.minecraft.world.item.Items.IRON_LEGGINGS;
            bootsItem = net.minecraft.world.item.Items.IRON_BOOTS;
        } else {
            // Armure en diamant
            helmetItem = net.minecraft.world.item.Items.DIAMOND_HELMET;
            chestplateItem = net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
            leggingsItem = net.minecraft.world.item.Items.DIAMOND_LEGGINGS;
            bootsItem = net.minecraft.world.item.Items.DIAMOND_BOOTS;
        }

        // Équiper des pièces aléatoires (chaque pièce a 50% de chance d'être équipée)
        if (random.nextBoolean()) {
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(helmetItem));
        }
        if (random.nextBoolean()) {
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(chestplateItem));
        }
        if (random.nextBoolean()) {
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(leggingsItem));
        }
        if (random.nextBoolean()) {
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(bootsItem));
        }

        // Empêcher le zombie de dropper son armure
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.0f);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.0f);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.LEGS, 0.0f);
        zombie.setDropChance(net.minecraft.world.entity.EquipmentSlot.FEET, 0.0f);
    }

    // Méthode pour forcer les mobs à cibler les joueurs même à travers les murs
    private static void updateMobTargets(ServerLevel level) {
        for (Mob mob : activeMobs) {
            if (!mob.isAlive() || mob.isRemoved()) {
                continue;
            }

            // Récupérer le follow range du mob
            double followRange = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).getValue();

            // Trouver le joueur actif le plus proche dans le follow range
            net.minecraft.server.level.ServerPlayer nearestPlayer = null;
            double nearestDistance = Double.MAX_VALUE;

            for (java.util.UUID uuid : GameManager.getActivePlayers()) {
                net.minecraft.server.level.ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
                if (player != null && player.level() == level && !player.isSpectator()) {
                    double distance = mob.distanceTo(player);
                    if (distance < followRange && distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestPlayer = player;
                    }
                }
            }

            // Forcer le ciblage du joueur le plus proche (même à travers les murs)
            if (nearestPlayer != null) {
                // Vérifier si le mob est un monstre avec un système de ciblage
                if (mob instanceof net.minecraft.world.entity.monster.Monster monster) {
                    monster.setTarget(nearestPlayer);
                }
            }
        }
    }

    public static void onZombieKilled() {
        zombiesRemaining--;

        if (zombiesRemaining <= 0) {
            // Vague terminée !
            onWaveComplete();
        }
    }

    private static void onWaveComplete() {
        ServerLevel level = activeMobs.isEmpty() ? null : (ServerLevel) activeMobs.get(0).level();
        if (level == null) return;

        GameManager.setGameState(GameManager.GameState.WAVE_COOLDOWN);

        // Respawn les morts
        RespawnManager.respawnAllDeadPlayers(level);

        // Activer les joueurs en attente
        GameManager.activateWaitingPlayers(level);

        // Countdown 10s
        waveCountdownTicks = 200;
        GameManager.broadcastToActivePlayers(level, "§a§lVague " + currentWave + " terminée !");

        // Son de fin de round (uniquement pour les joueurs de la partie)
        GameManager.playSoundToActivePlayers(level, ModSounds.ROUND_END.get(), 1.0f);
    }

    public static void tick(ServerLevel level) {
        // Gérer le countdown entre vagues
        if (GameManager.getGameState() == GameManager.GameState.WAVE_COOLDOWN) {
            waveCountdownTicks--;

            if (waveCountdownTicks <= 0) {
                startWave(level);
                GameManager.setGameState(GameManager.GameState.WAVE_ACTIVE);
            }
            return;
        }

        // Gérer le spawn progressif des zombies et timeout
        if (GameManager.getGameState() == GameManager.GameState.WAVE_ACTIVE) {
            // Mettre à jour le ciblage des mobs toutes les 20 ticks (1 seconde)
            targetUpdateTicks++;
            if (targetUpdateTicks >= 20) {
                updateMobTargets(level);
                targetUpdateTicks = 0;
            }

            // Incrémenter le timeout uniquement si le nombre de zombies ne diminue pas
            int timeoutMax = ZombieConfig.get().getWaveTimeoutTicks();
            if (timeoutMax > 0) {
                // Vérifier si des zombies ont été tués depuis la dernière vérification
                if (zombiesRemaining < lastZombieCount) {
                    // Progression détectée - réinitialiser le timeout
                    timeoutTicks = 0;
                    lastZombieCount = zombiesRemaining;
                } else {
                    // Pas de progression - incrémenter le timeout
                    timeoutTicks++;
                }

                int timeoutSeconds = timeoutTicks / 20;
                int maxSeconds = timeoutMax / 20;

                // Avertissements
                if (timeoutSeconds == maxSeconds - 10 && timeoutTicks % 20 == 0) {
                    GameManager.broadcastToActivePlayers(level, "§c⚠ Attention ! Aucun zombie tué depuis " + (maxSeconds - 10) + "s !");
                    GameManager.playSoundToActivePlayers(level, SoundEvents.ANVIL_LAND, 1.0f);
                } else if (timeoutSeconds == maxSeconds - 5 && timeoutTicks % 20 == 0) {
                    GameManager.broadcastToActivePlayers(level, "§c§l⚠ TIMEOUT DANS 5 SECONDES !");
                    GameManager.playSoundToActivePlayers(level, SoundEvents.ANVIL_LAND, 1.2f);
                } else if (timeoutSeconds >= maxSeconds - 3 && timeoutSeconds < maxSeconds && timeoutTicks % 20 == 0) {
                    int remaining = maxSeconds - timeoutSeconds;
                    GameManager.broadcastToActivePlayers(level, "§c§l" + remaining + "...");
                    GameManager.playSoundToActivePlayers(level, SoundEvents.ANVIL_LAND, 1.5f);
                }

                // Timeout atteint - forcer la fin de vague
                if (timeoutTicks >= timeoutMax) {
                    GameManager.broadcastToActivePlayers(level, "§4§l✖ TIMEOUT ! Aucune progression depuis " + maxSeconds + "s - Vague terminée de force.");
                    GameManager.playSoundToActivePlayers(level, SoundEvents.WITHER_DEATH, 0.8f);

                    // Tuer tous les mobs restants
                    for (Mob mob : new ArrayList<>(activeMobs)) {
                        if (mob.isAlive()) {
                            mob.kill();
                        }
                    }

                    zombiesRemaining = 0;
                    onWaveComplete();
                    return;
                }
            }

            // Spawn progressif
            if (zombiesToSpawn > 0) {
                spawnDelayTicks++;

                // Nettoyer les mobs morts de la liste
                Iterator<Mob> it = activeMobs.iterator();
                while (it.hasNext()) {
                    Mob m = it.next();
                    if (!m.isAlive() || m.isRemoved()) {
                        it.remove();
                    }
                }

                // Vérifier la limite de zombies sur la map
                int maxZombies = ZombieConfig.get().getMaxZombiesOnMap();
                int currentZombieCount = activeMobs.size();

                // Si on peut spawner (limite non atteinte + délai écoulé)
                int spawnDelay = ZombieConfig.get().getSpawnDelayTicks();
                if (currentZombieCount < maxZombies && spawnDelayTicks >= spawnDelay) {
                    // Spawner un zombie
                    RandomSource random = level.getRandom();
                    // Utiliser les spawns actifs (en fonction des portes ouvertes)
                    List<BlockPos> spawnPoints = MapManager.getActiveZombieSpawnPoints();
                    if (spawnPoints.isEmpty()) {
                        System.err.println("[WaveManager] Aucun point de spawn actif ! Impossible de spawner des zombies.");
                        spawnDelayTicks = 0; // Reset spawn delay
                    } else {
                        BlockPos spawnPos = spawnPoints.get(random.nextInt(spawnPoints.size()));

                        // Déterminer si ce zombie sera glowing (X derniers configurables)
                        int totalZombies = 6 + (currentWave * 6);
                        int zombieIndex = totalZombies - zombiesToSpawn;
                        int glowingCount = ZombieConfig.get().getGlowingZombiesCount();
                        boolean glowing = zombieIndex >= totalZombies - glowingCount;

                        spawnMob(level, spawnPos, glowing);

                        zombiesToSpawn--;
                        spawnDelayTicks = 0;
                    }
                }
            }
        }
    }

    public static void reset() {
        currentWave = 0;
        zombiesRemaining = 0;
        zombiesToSpawn = 0;
        waveCountdownTicks = 0;
        spawnDelayTicks = 0;
        timeoutTicks = 0;
        lastZombieCount = 0;
        targetUpdateTicks = 0;
        activeMobs.clear();
        // Les spawn points sont maintenant gérés par MapManager
    }

    // Vérifie si un mob fait partie des mobs spawnés par les vagues
    public static boolean isWaveMob(net.minecraft.world.entity.Entity entity) {
        return activeMobs.contains(entity);
    }

    // Méthode pour nettoyer tous les mobs actifs (appelée par zombiestop et game over)
    public static void killAllMobs() {
        for (Mob mob : new ArrayList<>(activeMobs)) {
            if (mob.isAlive()) {
                mob.kill();
            }
        }
        activeMobs.clear();
        zombiesToSpawn = 0; // Arrêter le spawn
    }

    // Méthode pour skip la vague en cours (commande admin)
    public static void skipWave(ServerLevel level) {
        // Tuer tous les zombies restants
        killAllMobs();

        // Forcer la fin de la vague
        zombiesRemaining = 0;
        zombiesToSpawn = 0;

        // Passer à l'état cooldown
        GameManager.setGameState(GameManager.GameState.WAVE_COOLDOWN);

        // Respawn les morts
        RespawnManager.respawnAllDeadPlayers(level);

        // Activer les joueurs en attente
        GameManager.activateWaitingPlayers(level);

        // Countdown 10s
        waveCountdownTicks = 200;
        GameManager.broadcastToActivePlayers(level, "§a§lVague " + currentWave + " terminée !");

        // Son de fin de round (uniquement pour les joueurs de la partie)
        GameManager.playSoundToActivePlayers(level, ModSounds.ROUND_END.get(), 1.0f);
    }
}

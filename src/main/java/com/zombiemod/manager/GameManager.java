package com.zombiemod.manager;

import com.zombiemod.network.NetworkHandler;
import com.zombiemod.network.packet.GameSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameManager {

    public enum GameState {
        WAITING,        // Avant le /zombiestart
        STARTING,       // Countdown 60s
        WAVE_ACTIVE,    // Vague en cours
        WAVE_COOLDOWN   // 10s entre les vagues
    }

    private static GameState currentState = GameState.WAITING;
    private static Set<UUID> activePlayers = new HashSet<>();
    private static Set<UUID> waitingPlayers = new HashSet<>();
    private static int startCountdownTicks = 0;

    public static GameState getGameState() {
        return currentState;
    }

    public static void setGameState(GameState state) {
        currentState = state;
    }

    public static Set<UUID> getActivePlayers() {
        return new HashSet<>(activePlayers);
    }

    public static Set<UUID> getWaitingPlayers() {
        return new HashSet<>(waitingPlayers);
    }

    public static boolean isPlayerActive(UUID uuid) {
        return activePlayers.contains(uuid);
    }

    public static boolean isPlayerWaiting(UUID uuid) {
        return waitingPlayers.contains(uuid);
    }

    public static int getStartCountdownSeconds() {
        return startCountdownTicks / 20;
    }

    public static void startGame(ServerLevel level) {
        if (currentState != GameState.WAITING) {
            return; // Déjà lancée
        }

        currentState = GameState.STARTING;
        startCountdownTicks = 1200; // 60 secondes

        broadcastToAll(level, "§6§l=== PARTIE ZOMBIE ===");
        broadcastToAll(level, "§eLa partie démarre dans §c60 secondes §e!");
        broadcastToAll(level, "§7Tapez §6/zombiejoin §7pour rejoindre !");
    }

    private static int syncTickCounter = 0;

    public static void tick(ServerLevel level) {
        // Maintenir la saturation des joueurs actifs à 100% pour régénération constante
        if (currentState == GameState.WAVE_ACTIVE || currentState == GameState.WAVE_COOLDOWN) {
            for (UUID uuid : activePlayers) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
                if (player != null && !player.isSpectator() && !player.isDeadOrDying()) {
                    player.getFoodData().setFoodLevel(20);
                    player.getFoodData().setSaturation(20.0f);
                }
            }
        }

        // Envoyer packet de sync toutes les 5 ticks (4 fois par seconde)
        syncTickCounter++;
        if (syncTickCounter >= 5) {
            sendSyncPacketToAll(level);
            syncTickCounter = 0;
        }

        if (currentState == GameState.STARTING) {
            startCountdownTicks--;
            int seconds = startCountdownTicks / 20;

            // Messages toutes les 10 secondes
            if (startCountdownTicks % 200 == 0 && seconds > 5) {
                broadcastToAll(level, "§ePartie démarre dans §6" + seconds + "s §e! Tapez §6/zombiejoin §epour rejoindre !");
                playGlobalSound(level, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f);
            }

            // Messages chaque seconde pour les 5 dernières
            if (seconds <= 5 && startCountdownTicks % 20 == 0 && seconds > 0) {
                broadcastToAll(level, "§c§l" + seconds + "...");
                playGlobalSound(level, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f + (0.2f * (5 - seconds)));
            }

            // Démarrage !
            if (startCountdownTicks <= 0) {
                activateAllPlayers(level);
                WaveManager.startWave(level);
                currentState = GameState.WAVE_ACTIVE;
            }
        }

        // Tick du WaveManager pour spawn progressif et countdown entre vagues
        if (currentState == GameState.WAVE_ACTIVE || currentState == GameState.WAVE_COOLDOWN) {
            WaveManager.tick(level);
        }
    }

    private static void activateAllPlayers(ServerLevel level) {
        // Tous les joueurs qui ont join deviennent actifs
        for (UUID uuid : new HashSet<>(waitingPlayers)) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                activePlayers.add(uuid);
                waitingPlayers.remove(uuid);

                player.setGameMode(GameType.SURVIVAL);
                BlockPos respawn = RespawnManager.getRespawnPoint();
                if (respawn != null) {
                    player.teleportTo(respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5);
                }
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setFoodLevel(20);

                PointsManager.setPoints(player.getUUID(), 500);

                player.sendSystemMessage(Component.literal("§a§l✦ QUE LA BATAILLE COMMENCE ! ✦"));
            }
        }

        broadcastToAll(level, "§6§l=== VAGUE 1 ===");
    }

    public static void joinGame(ServerPlayer player, ServerLevel level) {
        UUID uuid = player.getUUID();

        if (activePlayers.contains(uuid) || waitingPlayers.contains(uuid)) {
            player.sendSystemMessage(Component.literal("§cVous êtes déjà dans la partie !"));
            return;
        }

        BlockPos respawn = RespawnManager.getRespawnPoint();
        if (respawn == null) {
            player.sendSystemMessage(Component.literal("§cErreur: Point de respawn non défini ! Un admin doit faire §e/zombierespawn"));
            return;
        }

        switch (currentState) {
            case WAITING:
                player.sendSystemMessage(Component.literal("§cAucune partie en cours ! Un admin doit faire §e/zombiestart"));
                break;

            case STARTING: // Countdown 60s
                waitingPlayers.add(uuid);
                player.teleportTo(respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5);
                player.setGameMode(GameType.ADVENTURE); // Empêcher de casser des blocs
                player.sendSystemMessage(Component.literal("§aVous avez rejoint la partie !"));
                player.sendSystemMessage(Component.literal("§7Début dans §e" + (startCountdownTicks / 20) + "s"));
                broadcastToAll(level, "§7" + player.getName().getString() + " §ea rejoint ! §7(" +
                        (activePlayers.size() + waitingPlayers.size()) + " joueurs)");
                break;

            case WAVE_ACTIVE: // Vague en cours
                waitingPlayers.add(uuid);
                player.setGameMode(GameType.SPECTATOR);
                player.teleportTo(respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5);
                player.sendSystemMessage(Component.literal("§eVague en cours..."));
                player.sendSystemMessage(Component.literal("§7Vous rejoindrez à la fin (§c" +
                        WaveManager.getZombiesRemaining() + " §7zombies restants)"));
                broadcastToActivePlayers(level, "§7" + player.getName().getString() + " §eattend de rejoindre");
                break;

            case WAVE_COOLDOWN: // Entre vagues (10s)
                activePlayers.add(uuid);
                player.setGameMode(GameType.SURVIVAL);
                player.teleportTo(respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5);
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setFoodLevel(20);
                PointsManager.setPoints(uuid, 500);

                player.sendSystemMessage(Component.literal("§a§l✦ Bienvenue dans la bataille ! ✦"));
                player.sendSystemMessage(Component.literal("§7Vague actuelle: §e" + WaveManager.getCurrentWave()));
                broadcastToActivePlayers(level, "§a" + player.getName().getString() + " §ea rejoint le combat !");

                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS, 1.0f, 1.2f);
                break;
        }
    }

    public static void leaveGame(ServerPlayer player, ServerLevel level) {
        UUID uuid = player.getUUID();

        if (!activePlayers.contains(uuid) && !waitingPlayers.contains(uuid)) {
            player.sendSystemMessage(Component.literal("§cVous n'êtes pas dans la partie !"));
            return;
        }

        activePlayers.remove(uuid);
        waitingPlayers.remove(uuid);
        RespawnManager.removeDeadPlayer(uuid);

        player.setGameMode(GameType.SPECTATOR);
        player.sendSystemMessage(Component.literal("§7Vous avez quitté la partie."));
        broadcastToAll(level, "§7" + player.getName().getString() + " §ca quitté la partie.");
    }

    public static void activateWaitingPlayers(ServerLevel level) {
        for (UUID uuid : new HashSet<>(waitingPlayers)) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                activePlayers.add(uuid);
                waitingPlayers.remove(uuid);

                player.setGameMode(GameType.SURVIVAL);
                BlockPos respawn = RespawnManager.getRespawnPoint();
                if (respawn != null) {
                    player.teleportTo(respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5);
                }
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setFoodLevel(20);
                PointsManager.setPoints(uuid, 500);

                player.sendSystemMessage(Component.literal("§a§l✦ Vous êtes maintenant dans la bataille ! ✦"));
                broadcastToActivePlayers(level, "§a" + player.getName().getString() + " §ea rejoint le combat !");

                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS, 1.0f, 1.2f);
            }
        }
    }

    public static boolean areAllActivePlayersDead(ServerLevel level) {
        if (activePlayers.isEmpty()) {
            return false;
        }

        int deadCount = 0;
        int totalActivePlayers = 0;

        for (UUID uuid : activePlayers) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                totalActivePlayers++;
                // Vérifier si le joueur est mort (en spectateur ET dans la liste des morts)
                if (RespawnManager.isPlayerDead(uuid)) {
                    deadCount++;
                }
            }
        }

        // Game over seulement si tous les joueurs actifs sont morts
        return totalActivePlayers > 0 && deadCount == totalActivePlayers;
    }

    public static void reset() {
        currentState = GameState.WAITING;
        activePlayers.clear();
        waitingPlayers.clear();
        startCountdownTicks = 0;
    }

    public static void broadcastToAll(ServerLevel level, String message) {
        Component comp = Component.literal(message);
        level.getServer().getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(comp));
    }

    public static void broadcastToActivePlayers(ServerLevel level, String message) {
        Component comp = Component.literal(message);
        for (UUID uuid : activePlayers) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.sendSystemMessage(comp);
            }
        }
    }

    public static void playGlobalSound(ServerLevel level, net.minecraft.sounds.SoundEvent sound, float pitch) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            level.playSound(null, player.blockPosition(), sound, SoundSource.MASTER, 1.0f, pitch);
        }
    }

    public static void sendSyncPacketToAll(ServerLevel level) {
        // Créer la liste des joueurs actifs avec leurs données
        List<GameSyncPacket.PlayerData> playerDataList = new ArrayList<>();
        for (UUID uuid : activePlayers) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                int points = PointsManager.getPoints(uuid);
                playerDataList.add(new GameSyncPacket.PlayerData(uuid, player.getName().getString(), points));
            }
        }

        // Envoyer à chaque joueur connecté
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            GameSyncPacket packet = new GameSyncPacket(
                    currentState.name(),
                    WaveManager.getCurrentWave(),
                    WaveManager.getZombiesRemaining(),
                    startCountdownTicks,
                    WaveManager.getCountdownSeconds() * 20,
                    playerDataList,
                    new ArrayList<>(waitingPlayers),
                    player.getUUID()
            );

            NetworkHandler.sendToPlayer(player, packet);
        }
    }
}

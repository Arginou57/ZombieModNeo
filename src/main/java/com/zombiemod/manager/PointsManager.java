package com.zombiemod.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PointsManager {

    private static final Map<UUID, Integer> playerPoints = new HashMap<>();

    public static int getPoints(UUID playerUUID) {
        return playerPoints.getOrDefault(playerUUID, 0);
    }

    public static void setPoints(UUID playerUUID, int points) {
        playerPoints.put(playerUUID, points);
    }

    public static void addPoints(UUID playerUUID, int points) {
        int current = getPoints(playerUUID);
        setPoints(playerUUID, current + points);
    }

    public static boolean removePoints(UUID playerUUID, int points) {
        int current = getPoints(playerUUID);
        if (current >= points) {
            setPoints(playerUUID, current - points);
            return true;
        }
        return false;
    }

    public static void reset() {
        playerPoints.clear();
    }

    public static void resetPlayer(UUID playerUUID) {
        playerPoints.remove(playerUUID);
    }
}

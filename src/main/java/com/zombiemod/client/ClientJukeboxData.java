package com.zombiemod.client;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache côté client pour les données des jukeboxes zombie
 */
public class ClientJukeboxData {

    private static Map<BlockPos, Integer> jukeboxes = new HashMap<>();

    public static void setJukebox(BlockPos pos, int cost) {
        jukeboxes.put(pos, cost);
        System.out.println("[ClientJukeboxData] Jukebox ajouté: " + pos + " -> " + cost + " points");
    }

    public static void removeJukebox(BlockPos pos) {
        jukeboxes.remove(pos);
        System.out.println("[ClientJukeboxData] Jukebox supprimé: " + pos);
    }

    public static boolean isJukebox(BlockPos pos) {
        return jukeboxes.containsKey(pos);
    }

    public static int getCost(BlockPos pos) {
        return jukeboxes.getOrDefault(pos, 0);
    }

    public static void clear() {
        jukeboxes.clear();
        System.out.println("[ClientJukeboxData] Cache effacé");
    }

    public static void setAll(Map<BlockPos, Integer> boxes) {
        jukeboxes.clear();
        jukeboxes.putAll(boxes);
        System.out.println("[ClientJukeboxData] Cache mis à jour avec " + boxes.size() + " jukeboxes:");
        boxes.forEach((pos, cost) -> System.out.println("  - " + pos + " -> " + cost + " points"));
    }
}

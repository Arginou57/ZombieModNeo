package com.zombiemod.map;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class MapConfig {

    private String name;
    private SerializableBlockPos respawnPoint;
    private List<SerializableBlockPos> zombieSpawnPoints;

    // Constructeur pour Gson
    public MapConfig() {
        this.zombieSpawnPoints = new ArrayList<>();
    }

    public MapConfig(String name) {
        this.name = name;
        this.zombieSpawnPoints = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setRespawnPoint(BlockPos pos) {
        this.respawnPoint = new SerializableBlockPos(pos);
    }

    public BlockPos getRespawnPoint() {
        return respawnPoint != null ? respawnPoint.toBlockPos() : null;
    }

    public void addZombieSpawnPoint(BlockPos pos) {
        zombieSpawnPoints.add(new SerializableBlockPos(pos));
    }

    public void clearZombieSpawnPoints() {
        zombieSpawnPoints.clear();
    }

    public List<BlockPos> getZombieSpawnPoints() {
        List<BlockPos> result = new ArrayList<>();
        for (SerializableBlockPos pos : zombieSpawnPoints) {
            result.add(pos.toBlockPos());
        }
        return result;
    }

    public int getZombieSpawnPointCount() {
        return zombieSpawnPoints.size();
    }

    public boolean hasRespawnPoint() {
        return respawnPoint != null;
    }

    public boolean hasZombieSpawnPoints() {
        return !zombieSpawnPoints.isEmpty();
    }

    // Classe interne pour sérialiser BlockPos en JSON
    public static class SerializableBlockPos {
        private int x;
        private int y;
        private int z;

        public SerializableBlockPos() {}

        public SerializableBlockPos(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }

        public BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }
}

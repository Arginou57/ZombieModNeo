package com.zombiemod.map;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapConfig {

    private String name;
    private SerializableBlockPos respawnPoint;
    private List<ZombieSpawnPoint> zombieSpawnPoints;
    private Map<Integer, DoorConfig> doors; // Numéro de porte -> Config

    // Constructeur pour Gson
    public MapConfig() {
        this.zombieSpawnPoints = new ArrayList<>();
        this.doors = new HashMap<>();
    }

    public MapConfig(String name) {
        this.name = name;
        this.zombieSpawnPoints = new ArrayList<>();
        this.doors = new HashMap<>();
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
        zombieSpawnPoints.add(new ZombieSpawnPoint(pos));
    }

    public void addZombieSpawnPoint(BlockPos pos, int doorNumber) {
        zombieSpawnPoints.add(new ZombieSpawnPoint(pos, doorNumber));
    }

    public void clearZombieSpawnPoints() {
        zombieSpawnPoints.clear();
    }

    public List<BlockPos> getZombieSpawnPoints() {
        List<BlockPos> result = new ArrayList<>();
        for (ZombieSpawnPoint spawn : zombieSpawnPoints) {
            result.add(spawn.getPosition());
        }
        return result;
    }

    /**
     * Récupère les points de spawn actifs en fonction des portes ouvertes
     */
    public List<BlockPos> getActiveZombieSpawnPoints() {
        List<BlockPos> result = new ArrayList<>();
        for (ZombieSpawnPoint spawn : zombieSpawnPoints) {
            // Toujours actif si pas lié à une porte
            if (spawn.isAlwaysActive()) {
                result.add(spawn.getPosition());
            }
            // Actif si la porte est ouverte
            else {
                Integer doorNum = spawn.getDoorNumber();
                if (doorNum != null && doors.containsKey(doorNum) && doors.get(doorNum).isOpen()) {
                    result.add(spawn.getPosition());
                }
            }
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

    // === Gestion des portes ===

    public void addDoor(int doorNumber, BlockPos position, int cost) {
        doors.put(doorNumber, new DoorConfig(doorNumber, position, cost));
    }

    public void addDoor(DoorConfig door) {
        doors.put(door.getDoorNumber(), door);
    }

    public boolean hasDoor(int doorNumber) {
        return doors.containsKey(doorNumber);
    }

    public DoorConfig getDoor(int doorNumber) {
        return doors.get(doorNumber);
    }

    public void removeDoor(int doorNumber) {
        doors.remove(doorNumber);
    }

    public void openDoor(int doorNumber) {
        if (doors.containsKey(doorNumber)) {
            doors.get(doorNumber).setOpen(true);
        }
    }

    public void closeDoor(int doorNumber) {
        if (doors.containsKey(doorNumber)) {
            doors.get(doorNumber).setOpen(false);
        }
    }

    public boolean isDoorOpen(int doorNumber) {
        return doors.containsKey(doorNumber) && doors.get(doorNumber).isOpen();
    }

    public Map<Integer, DoorConfig> getDoors() {
        return new HashMap<>(doors);
    }

    public void clearDoors() {
        doors.clear();
    }

    public void resetDoors() {
        for (DoorConfig door : doors.values()) {
            door.setOpen(false);
        }
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

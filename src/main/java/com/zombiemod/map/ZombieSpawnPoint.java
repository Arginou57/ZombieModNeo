package com.zombiemod.map;

import net.minecraft.core.BlockPos;

/**
 * Point de spawn de zombie avec lien optionnel à une porte
 */
public class ZombieSpawnPoint {

    private MapConfig.SerializableBlockPos position;
    private Integer doorNumber; // null = toujours actif, sinon actif si porte ouverte

    // Constructeur pour Gson
    public ZombieSpawnPoint() {}

    public ZombieSpawnPoint(BlockPos position) {
        this.position = new MapConfig.SerializableBlockPos(position);
        this.doorNumber = null; // Toujours actif par défaut
    }

    public ZombieSpawnPoint(BlockPos position, int doorNumber) {
        this.position = new MapConfig.SerializableBlockPos(position);
        this.doorNumber = doorNumber;
    }

    public BlockPos getPosition() {
        return position != null ? position.toBlockPos() : null;
    }

    public Integer getDoorNumber() {
        return doorNumber;
    }

    public boolean isAlwaysActive() {
        return doorNumber == null;
    }

    public boolean isActiveWithDoor(int doorNum) {
        return doorNumber != null && doorNumber == doorNum;
    }
}

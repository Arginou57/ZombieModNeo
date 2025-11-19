package com.zombiemod.map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration d'une porte dans une map
 * Une porte a un numéro unique et peut être ouverte/fermée
 * Stocke la pancarte et les blocs du mur 3x3 derrière
 */
public class DoorConfig {

    private int doorNumber;
    private MapConfig.SerializableBlockPos signPosition; // Position de la pancarte
    private List<SavedBlock> wallBlocks; // Blocs du mur 3x3
    private boolean isOpen;
    private int cost; // Coût pour ouvrir la porte

    // Constructeur pour Gson
    public DoorConfig() {
        this.isOpen = false;
        this.cost = 1000; // Coût par défaut
        this.wallBlocks = new ArrayList<>();
    }

    public DoorConfig(int doorNumber, BlockPos signPosition, int cost) {
        this.doorNumber = doorNumber;
        this.signPosition = new MapConfig.SerializableBlockPos(signPosition);
        this.isOpen = false;
        this.cost = cost;
        this.wallBlocks = new ArrayList<>();
    }

    public int getDoorNumber() {
        return doorNumber;
    }

    public BlockPos getSignPosition() {
        return signPosition != null ? signPosition.toBlockPos() : null;
    }

    public void addWallBlock(BlockPos pos, BlockState state) {
        wallBlocks.add(new SavedBlock(pos, state));
    }

    public List<SavedBlock> getWallBlocks() {
        return new ArrayList<>(wallBlocks);
    }

    public void clearWallBlocks() {
        wallBlocks.clear();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * Classe interne pour sauvegarder un bloc avec sa position et son état
     */
    public static class SavedBlock {
        private MapConfig.SerializableBlockPos position;
        private String blockStateString; // Format: "minecraft:stone_bricks" ou avec propriétés

        public SavedBlock() {}

        public SavedBlock(BlockPos pos, BlockState state) {
            this.position = new MapConfig.SerializableBlockPos(pos);
            // Sauvegarder le nom du bloc (on pourrait aussi sauvegarder les propriétés si nécessaire)
            this.blockStateString = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(state.getBlock()).toString();
        }

        public BlockPos getPosition() {
            return position != null ? position.toBlockPos() : null;
        }

        public String getBlockStateString() {
            return blockStateString;
        }

        public BlockState getBlockState() {
            try {
                net.minecraft.resources.ResourceLocation location =
                    net.minecraft.resources.ResourceLocation.parse(blockStateString);
                net.minecraft.world.level.block.Block block =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(location);
                return block != null ? block.defaultBlockState() :
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            } catch (Exception e) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }
    }
}


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
    private SavedBlock signBlock; // État de la pancarte (direction, texte, etc.)
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

    public void setSignBlock(BlockPos pos, BlockState state) {
        this.signBlock = new SavedBlock(pos, state);
    }

    public SavedBlock getSignBlock() {
        return signBlock;
    }

    /**
     * Classe interne pour sauvegarder un bloc avec sa position et son état
     */
    public static class SavedBlock {
        private MapConfig.SerializableBlockPos position;
        private String blockStateString; // Format: "minecraft:stone_bricks[property=value]"
        private String blockEntityDataString; // Données NBT en format String (SNBT)

        public SavedBlock() {}

        public SavedBlock(BlockPos pos, BlockState state) {
            this.position = new MapConfig.SerializableBlockPos(pos);
            // Sauvegarder le BlockState complet avec ses propriétés
            this.blockStateString = serializeBlockState(state);
        }

        private String serializeBlockState(BlockState state) {
            StringBuilder sb = new StringBuilder();
            sb.append(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());

            // Ajouter les propriétés
            if (!state.getValues().isEmpty()) {
                sb.append("[");
                boolean first = true;
                for (var entry : state.getValues().entrySet()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append(entry.getKey().getName());
                    sb.append("=");
                    sb.append(entry.getValue().toString());
                }
                sb.append("]");
            }

            return sb.toString();
        }

        public BlockPos getPosition() {
            return position != null ? position.toBlockPos() : null;
        }

        public String getBlockStateString() {
            return blockStateString;
        }

        public void setBlockEntityData(net.minecraft.nbt.CompoundTag data) {
            if (data != null) {
                // Convertir CompoundTag en String (SNBT)
                this.blockEntityDataString = data.getAsString();
            } else {
                this.blockEntityDataString = null;
            }
        }

        public net.minecraft.nbt.CompoundTag getBlockEntityData() {
            if (blockEntityDataString == null || blockEntityDataString.isEmpty()) {
                return null;
            }
            try {
                // Convertir String (SNBT) en CompoundTag
                return net.minecraft.nbt.TagParser.parseTag(blockEntityDataString);
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                System.err.println("[DoorConfig] Erreur lors du parsing NBT: " + e.getMessage());
                return null;
            }
        }

        public BlockState getBlockState() {
            try {
                // Parser le blockStateString pour recréer le BlockState avec ses propriétés
                String[] parts = blockStateString.split("\\[", 2);
                String blockId = parts[0];

                net.minecraft.resources.ResourceLocation location =
                    net.minecraft.resources.ResourceLocation.parse(blockId);
                net.minecraft.world.level.block.Block block =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(location);

                if (block == null) {
                    return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                }

                BlockState state = block.defaultBlockState();

                // Appliquer les propriétés si elles existent
                if (parts.length > 1) {
                    String propertiesStr = parts[1].replace("]", "");
                    String[] properties = propertiesStr.split(",");

                    for (String prop : properties) {
                        String[] keyValue = prop.split("=");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();

                            // Essayer d'appliquer la propriété
                            for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
                                if (property.getName().equals(key)) {
                                    state = setPropertyValue(state, property, value);
                                    break;
                                }
                            }
                        }
                    }
                }

                return state;
            } catch (Exception e) {
                System.err.println("[SavedBlock] Erreur lors de la désérialisation du BlockState: " + blockStateString);
                e.printStackTrace();
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }

        @SuppressWarnings("unchecked")
        private <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, net.minecraft.world.level.block.state.properties.Property<T> property, String valueStr) {
            try {
                java.util.Optional<T> value = property.getValue(valueStr);
                if (value.isPresent()) {
                    return state.setValue(property, value.get());
                }
            } catch (Exception e) {
                System.err.println("[SavedBlock] Erreur lors de l'application de la propriété: " + property.getName() + "=" + valueStr);
            }
            return state;
        }
    }
}


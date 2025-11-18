package com.zombiemod.system;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class WeaponCrateManager {

    public static class WeaponConfig {
        public String itemId;
        public int count;
        public int weight;
        public String displayName;
        public List<EnchantmentData> enchantments = new ArrayList<>();
        public ItemStack fullItemStack = null; // ItemStack complet avec tous les DataComponents

        public static class EnchantmentData {
            public String enchantmentId;
            public int level;

            public EnchantmentData(String id, int lvl) {
                this.enchantmentId = id;
                this.level = lvl;
            }
        }

        public ItemStack toItemStack(Level level) {
            // Si on a un ItemStack complet sauvegardé, l'utiliser directement
            if (fullItemStack != null && !fullItemStack.isEmpty()) {
                System.out.println("[ZombieMod] Utilisation de fullItemStack pour: " + displayName);
                return fullItemStack.copy();
            }

            // Sinon, reconstruire depuis les données séparées (ancien format)
            System.out.println("[ZombieMod] Reconstruction depuis itemId pour: " + displayName + " (itemId=" + itemId + ")");

            if (itemId == null || itemId.isEmpty()) {
                System.err.println("[ZombieMod] ERREUR: itemId est null ou vide pour " + displayName);
                return ItemStack.EMPTY;
            }

            ResourceLocation itemRL = ResourceLocation.parse(itemId);
            ItemStack stack = new ItemStack(level.registryAccess().registryOrThrow(Registries.ITEM).get(itemRL), count);

            if (displayName != null && !displayName.isEmpty()) {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));
            }

            if (!enchantments.isEmpty()) {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

                for (EnchantmentData ench : enchantments) {
                    ResourceLocation enchRL = ResourceLocation.parse(ench.enchantmentId);
                    Holder<Enchantment> enchantmentHolder = level.registryAccess()
                            .registryOrThrow(Registries.ENCHANTMENT)
                            .getHolder(enchRL)
                            .orElse(null);

                    if (enchantmentHolder != null) {
                        mutable.set(enchantmentHolder, ench.level);
                    }
                }

                stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
            }

            return stack;
        }
    }

    public static boolean isWeaponCrate(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            CompoundTag data = chest.getPersistentData();
            return data.getBoolean("IsWeaponCrate");
        }
        return false;
    }

    public static void setWeaponCrate(Level level, BlockPos pos, int cost) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            CompoundTag data = chest.getPersistentData();
            data.putBoolean("IsWeaponCrate", true);
            data.putInt("Cost", cost);
            data.put("Weapons", new ListTag());
            data.put("Ammo", new ListTag()); // Liste des munitions
            chest.setChanged();

            // Synchroniser avec les clients
            if (!level.isClientSide()) {
                ServerWeaponCrateTracker.addWeaponCrate(pos, cost);
            }
        }
    }

    public static int getCost(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            return chest.getPersistentData().getInt("Cost");
        }
        return 0;
    }

    public static void addWeapon(Level level, BlockPos pos, String itemId, int count, int weight, String displayName, List<WeaponConfig.EnchantmentData> enchantments) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            CompoundTag data = chest.getPersistentData();
            ListTag weapons = data.getList("Weapons", Tag.TAG_COMPOUND);

            int weaponCountBefore = weapons.size();
            System.out.println("[WeaponCrate] addWeapon à " + pos + " - Armes AVANT: " + weaponCountBefore);

            CompoundTag weapon = new CompoundTag();
            weapon.putString("Item", itemId);
            weapon.putInt("Count", count);
            weapon.putInt("Weight", weight);
            weapon.putString("Name", displayName);

            if (enchantments != null && !enchantments.isEmpty()) {
                ListTag enchList = new ListTag();
                for (WeaponConfig.EnchantmentData ench : enchantments) {
                    CompoundTag enchTag = new CompoundTag();
                    enchTag.putString("Id", ench.enchantmentId);
                    enchTag.putInt("Level", ench.level);
                    enchList.add(enchTag);
                }
                weapon.put("Enchantments", enchList);
            }

            weapons.add(weapon);
            data.put("Weapons", weapons);
            chest.setChanged();

            int weaponCountAfter = weapons.size();
            System.out.println("[WeaponCrate] addWeapon à " + pos + " - Armes APRÈS: " + weaponCountAfter);

            // Gérer l'affichage après ajout
            if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                updateDisplayAfterWeaponChange(serverLevel, pos, weaponCountBefore, weaponCountAfter);
            }
        }
    }

    // Nouvelle méthode pour ajouter un ItemStack complet avec tous ses DataComponents
    public static void addWeaponFromItemStack(Level level, BlockPos pos, ItemStack itemStack, int weight, String displayName) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            CompoundTag data = chest.getPersistentData();
            ListTag weapons = data.getList("Weapons", Tag.TAG_COMPOUND);

            int weaponCountBefore = weapons.size();
            System.out.println("[WeaponCrate] addWeaponFromItemStack à " + pos + " - Armes AVANT: " + weaponCountBefore);

            CompoundTag weapon = new CompoundTag();
            weapon.putInt("Weight", weight);
            weapon.putString("Name", displayName);

            // IMPORTANT: Sauvegarder aussi Item et Count séparément pour fallback
            String itemId = level.registryAccess().registryOrThrow(Registries.ITEM)
                    .getKey(itemStack.getItem()).toString();
            weapon.putString("Item", itemId);
            weapon.putInt("Count", itemStack.getCount());

            // Sauvegarder l'ItemStack complet avec tous ses DataComponents
            // IMPORTANT: save() retourne un nouveau Tag, il faut le caster en CompoundTag!
            CompoundTag itemData = (CompoundTag) itemStack.save(level.registryAccess());
            weapon.put("ItemStackData", itemData);

            weapons.add(weapon);
            data.put("Weapons", weapons);
            chest.setChanged();

            int weaponCountAfter = weapons.size();
            System.out.println("[WeaponCrate] addWeaponFromItemStack à " + pos + " - Armes APRÈS: " + weaponCountAfter);

            // Gérer l'affichage après ajout
            if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                updateDisplayAfterWeaponChange(serverLevel, pos, weaponCountBefore, weaponCountAfter);
            }
        }
    }

    // Nouvelle méthode pour ajouter des munitions avec prix (pas de Display entity)
    public static void addAmmo(Level level, BlockPos pos, ItemStack itemStack, int prix) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            CompoundTag data = chest.getPersistentData();
            ListTag ammo = data.getList("Ammo", Tag.TAG_COMPOUND);

            System.out.println("[WeaponCrate] addAmmo à " + pos + " - Munitions AVANT: " + ammo.size());

            CompoundTag ammoTag = new CompoundTag();
            ammoTag.putInt("Prix", prix);

            // Sauvegarder l'ItemStack complet avec tous ses DataComponents
            String itemId = level.registryAccess().registryOrThrow(Registries.ITEM)
                    .getKey(itemStack.getItem()).toString();
            ammoTag.putString("Item", itemId);
            ammoTag.putInt("Count", itemStack.getCount());

            // Sauvegarder l'ItemStack complet
            CompoundTag itemData = (CompoundTag) itemStack.save(level.registryAccess());
            ammoTag.put("ItemStackData", itemData);

            // Récupérer le nom pour affichage
            String displayName;
            if (itemStack.has(DataComponents.CUSTOM_NAME)) {
                displayName = itemStack.get(DataComponents.CUSTOM_NAME).getString();
            } else {
                displayName = itemStack.getDescriptionId();
            }
            ammoTag.putString("Name", displayName);

            ammo.add(ammoTag);
            data.put("Ammo", ammo);
            chest.setChanged();

            System.out.println("[WeaponCrate] addAmmo à " + pos + " - Munitions APRÈS: " + ammo.size());

            // Synchroniser avec les clients
            if (!level.isClientSide()) {
                ServerWeaponCrateTracker.syncToAllPlayers();
            }
        }
    }

    // Récupérer la liste des munitions pour l'UI
    public static ListTag getAmmo(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            CompoundTag data = chest.getPersistentData();
            return data.getList("Ammo", Tag.TAG_COMPOUND);
        }
        return new ListTag();
    }

    public static WeaponConfig getRandomWeapon(Level level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChestBlockEntity chest)) {
            return null;
        }

        CompoundTag data = chest.getPersistentData();
        ListTag weapons = data.getList("Weapons", Tag.TAG_COMPOUND);

        if (weapons.isEmpty()) {
            return null;
        }

        // Calculer poids total
        int totalWeight = 0;
        for (Tag tag : weapons) {
            CompoundTag weaponTag = (CompoundTag) tag;
            totalWeight += weaponTag.getInt("Weight");
        }

        // Sélection pondérée
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (Tag tag : weapons) {
            CompoundTag weaponTag = (CompoundTag) tag;
            currentWeight += weaponTag.getInt("Weight");

            if (randomValue < currentWeight) {
                WeaponConfig config = new WeaponConfig();
                config.weight = weaponTag.getInt("Weight");
                config.displayName = weaponTag.getString("Name");

                // Nouveau format : ItemStack complet sauvegardé
                if (weaponTag.contains("ItemStackData")) {
                    CompoundTag itemData = weaponTag.getCompound("ItemStackData");
                    ItemStack stack = ItemStack.parseOptional(level.registryAccess(), itemData);

                    // Toujours stocker le stack, même si vide (pour débug)
                    config.fullItemStack = stack;

                    if (!stack.isEmpty()) {
                        // Extraire les infos de l'ItemStack
                        config.itemId = level.registryAccess().registryOrThrow(Registries.ITEM)
                                .getKey(stack.getItem()).toString();
                        config.count = stack.getCount();

                        // Copier les enchantements pour compatibilité
                        ItemEnchantments itemEnchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                        itemEnchantments.entrySet().forEach(entry -> {
                            String enchId = level.registryAccess()
                                    .registryOrThrow(Registries.ENCHANTMENT)
                                    .getKey(entry.getKey().value())
                                    .toString();
                            int enchLevel = entry.getIntValue();
                            config.enchantments.add(new WeaponConfig.EnchantmentData(enchId, enchLevel));
                        });
                    } else {
                        // Fallback si désérialisation échoue - essayer de récupérer depuis les tags séparés
                        if (weaponTag.contains("Item")) {
                            config.itemId = weaponTag.getString("Item");
                            config.count = weaponTag.getInt("Count");
                        } else {
                            System.err.println("[ZombieMod] ERREUR: ItemStack vide après désérialisation et pas de fallback!");
                        }
                    }
                } else {
                    // Ancien format : données séparées (rétrocompatibilité)
                    config.itemId = weaponTag.getString("Item");
                    config.count = weaponTag.getInt("Count");

                    if (weaponTag.contains("Enchantments")) {
                        ListTag enchList = weaponTag.getList("Enchantments", Tag.TAG_COMPOUND);
                        for (Tag enchTag : enchList) {
                            CompoundTag ench = (CompoundTag) enchTag;
                            config.enchantments.add(new WeaponConfig.EnchantmentData(
                                    ench.getString("Id"),
                                    ench.getInt("Level")
                            ));
                        }
                    }
                }

                return config;
            }
        }

        return null;
    }

    /**
     * Récupère toutes les armes possibles d'une caisse (pour l'animation)
     */
    public static List<WeaponConfig> getAllWeapons(Level level, BlockPos pos) {
        List<WeaponConfig> allWeapons = new ArrayList<>();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChestBlockEntity chest)) {
            return allWeapons;
        }

        CompoundTag data = chest.getPersistentData();
        ListTag weapons = data.getList("Weapons", Tag.TAG_COMPOUND);

        if (weapons.isEmpty()) {
            return allWeapons;
        }

        // Parcourir toutes les armes
        for (Tag tag : weapons) {
            CompoundTag weaponTag = (CompoundTag) tag;

            WeaponConfig config = new WeaponConfig();
            config.weight = weaponTag.getInt("Weight");
            config.displayName = weaponTag.getString("Name");

            // Nouveau format : ItemStack complet sauvegardé
            if (weaponTag.contains("ItemStackData")) {
                CompoundTag itemData = weaponTag.getCompound("ItemStackData");
                ItemStack stack = ItemStack.parseOptional(level.registryAccess(), itemData);
                config.fullItemStack = stack;

                if (!stack.isEmpty()) {
                    config.itemId = level.registryAccess().registryOrThrow(Registries.ITEM)
                            .getKey(stack.getItem()).toString();
                    config.count = stack.getCount();

                    ItemEnchantments itemEnchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                    itemEnchantments.entrySet().forEach(entry -> {
                        String enchId = level.registryAccess()
                                .registryOrThrow(Registries.ENCHANTMENT)
                                .getKey(entry.getKey().value())
                                .toString();
                        int enchLevel = entry.getIntValue();
                        config.enchantments.add(new WeaponConfig.EnchantmentData(enchId, enchLevel));
                    });
                } else if (weaponTag.contains("Item")) {
                    config.itemId = weaponTag.getString("Item");
                    config.count = weaponTag.getInt("Count");
                }
            } else {
                // Ancien format
                config.itemId = weaponTag.getString("Item");
                config.count = weaponTag.getInt("Count");

                if (weaponTag.contains("Enchantments")) {
                    ListTag enchList = weaponTag.getList("Enchantments", Tag.TAG_COMPOUND);
                    for (Tag enchTag : enchList) {
                        CompoundTag ench = (CompoundTag) enchTag;
                        config.enchantments.add(new WeaponConfig.EnchantmentData(
                                ench.getString("Id"),
                                ench.getInt("Level")
                        ));
                    }
                }
            }

            allWeapons.add(config);
        }

        return allWeapons;
    }

    public static void clearWeapons(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            CompoundTag data = chest.getPersistentData();
            data.put("Weapons", new ListTag());
            chest.setChanged();
        }
    }

    /**
     * Met à jour l'affichage statique quand des armes sont ajoutées/supprimées
     * - 0 -> 1 arme : créer affichage statique
     * - 1 -> 2 armes : supprimer affichage statique (passera en mode roulette lors de l'ouverture)
     */
    private static void updateDisplayAfterWeaponChange(net.minecraft.server.level.ServerLevel level, BlockPos pos, int countBefore, int countAfter) {
        System.out.println("[WeaponCrate] updateDisplayAfterWeaponChange à " + pos + " - Transition: " + countBefore + " -> " + countAfter);

        // Passage de 0 à 1 arme : créer affichage statique
        if (countBefore == 0 && countAfter == 1) {
            System.out.println("[WeaponCrate] Détection: 0 -> 1 arme, création affichage statique");
            List<WeaponConfig> allWeapons = getAllWeapons(level, pos);
            if (!allWeapons.isEmpty()) {
                WeaponConfig weapon = allWeapons.get(0);
                ItemStack itemStack = weapon.toItemStack(level);
                if (!itemStack.isEmpty()) {
                    WeaponCrateAnimationManager.startStaticDisplay(level, pos, itemStack);
                    System.out.println("[WeaponCrate] Affichage statique créé pour la première arme à " + pos);
                }
            }
        }
        // Passage de 1 à 2 armes : supprimer affichage statique
        else if (countBefore == 1 && countAfter == 2) {
            System.out.println("[WeaponCrate] Détection: 1 -> 2 armes, suppression affichage statique");
            WeaponCrateAnimationManager.stopAnimation(level, pos);
            System.out.println("[WeaponCrate] Affichage statique supprimé (passage en mode roulette) à " + pos);
        }
        else {
            System.out.println("[WeaponCrate] Aucune action (transition non gérée)");
        }
    }

    // Presets
    public static void createStarterCrate(Level level, BlockPos pos) {
        setWeaponCrate(level, pos, 500);

        addWeapon(level, pos, "minecraft:wooden_sword", 1, 40, "§7Couteau de Survie", null);
        addWeapon(level, pos, "minecraft:stone_sword", 1, 30, "§fÉpée Basique", null);
        addWeapon(level, pos, "minecraft:iron_sword", 1, 20, "§fLame de Fer", null);

        List<WeaponConfig.EnchantmentData> bowEnch = new ArrayList<>();
        bowEnch.add(new WeaponConfig.EnchantmentData("minecraft:infinity", 1));
        addWeapon(level, pos, "minecraft:bow", 1, 10, "§aArc Simple", bowEnch);
    }

    public static void createAdvancedCrate(Level level, BlockPos pos) {
        setWeaponCrate(level, pos, 1500);

        List<WeaponConfig.EnchantmentData> diamondEnch = new ArrayList<>();
        diamondEnch.add(new WeaponConfig.EnchantmentData("minecraft:sharpness", 3));
        addWeapon(level, pos, "minecraft:diamond_sword", 1, 30, "§bÉpée Diamant", diamondEnch);

        List<WeaponConfig.EnchantmentData> bowEnch = new ArrayList<>();
        bowEnch.add(new WeaponConfig.EnchantmentData("minecraft:power", 4));
        bowEnch.add(new WeaponConfig.EnchantmentData("minecraft:infinity", 1));
        addWeapon(level, pos, "minecraft:bow", 1, 25, "§dArc Puissant", bowEnch);

        List<WeaponConfig.EnchantmentData> crossbowEnch = new ArrayList<>();
        crossbowEnch.add(new WeaponConfig.EnchantmentData("minecraft:quick_charge", 3));
        addWeapon(level, pos, "minecraft:crossbow", 1, 25, "§9Arbalète Rapide", crossbowEnch);

        List<WeaponConfig.EnchantmentData> tridentEnch = new ArrayList<>();
        tridentEnch.add(new WeaponConfig.EnchantmentData("minecraft:loyalty", 3));
        addWeapon(level, pos, "minecraft:trident", 1, 20, "§6Trident", tridentEnch);
    }

    public static void createLegendaryCrate(Level level, BlockPos pos) {
        setWeaponCrate(level, pos, 5000);

        List<WeaponConfig.EnchantmentData> netheriteEnch = new ArrayList<>();
        netheriteEnch.add(new WeaponConfig.EnchantmentData("minecraft:sharpness", 5));
        netheriteEnch.add(new WeaponConfig.EnchantmentData("minecraft:fire_aspect", 2));
        netheriteEnch.add(new WeaponConfig.EnchantmentData("minecraft:looting", 3));
        addWeapon(level, pos, "minecraft:netherite_sword", 1, 50, "§4§lLAME INFERNALE", netheriteEnch);

        List<WeaponConfig.EnchantmentData> bowEnch = new ArrayList<>();
        bowEnch.add(new WeaponConfig.EnchantmentData("minecraft:power", 5));
        bowEnch.add(new WeaponConfig.EnchantmentData("minecraft:flame", 1));
        bowEnch.add(new WeaponConfig.EnchantmentData("minecraft:infinity", 1));
        addWeapon(level, pos, "minecraft:bow", 1, 30, "§5§lARC DIVIN", bowEnch);

        List<WeaponConfig.EnchantmentData> tridentEnch = new ArrayList<>();
        tridentEnch.add(new WeaponConfig.EnchantmentData("minecraft:loyalty", 3));
        tridentEnch.add(new WeaponConfig.EnchantmentData("minecraft:impaling", 5));
        tridentEnch.add(new WeaponConfig.EnchantmentData("minecraft:channeling", 1));
        addWeapon(level, pos, "minecraft:trident", 1, 20, "§b§lTRIDENT DE POSÉIDON", tridentEnch);
    }
}

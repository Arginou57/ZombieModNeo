package com.zombiemod.manager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {

    // Stockage des inventaires sauvegardés (UUID -> NBT de l'inventaire complet)
    private static final Map<UUID, CompoundTag> savedInventories = new HashMap<>();

    // Cache pour savoir si Curios est installé
    private static Boolean curiosLoaded = null;

    /**
     * Vérifie si Curios API est installé
     */
    private static boolean isCuriosLoaded() {
        if (curiosLoaded == null) {
            curiosLoaded = ModList.get().isLoaded("curios");
        }
        return curiosLoaded;
    }

    /**
     * Sauvegarde l'inventaire complet d'un joueur (inventaire + armure + offhand + curios si disponible)
     */
    public static void saveInventory(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Inventory inventory = player.getInventory();

        CompoundTag inventoryData = new CompoundTag();

        // Sauvegarder l'inventaire principal + hotbar (slots 0-35)
        ListTag mainInventory = new ListTag();
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                mainInventory.add(stack.save(player.registryAccess(), itemTag));
            }
        }
        inventoryData.put("Items", mainInventory);

        // Sauvegarder l'armure (slots 0-3)
        ListTag armorInventory = new ListTag();
        for (int i = 0; i < inventory.armor.size(); i++) {
            ItemStack stack = inventory.armor.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                armorInventory.add(stack.save(player.registryAccess(), itemTag));
            }
        }
        inventoryData.put("Armor", armorInventory);

        // Sauvegarder l'offhand
        ListTag offhandInventory = new ListTag();
        for (int i = 0; i < inventory.offhand.size(); i++) {
            ItemStack stack = inventory.offhand.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                offhandInventory.add(stack.save(player.registryAccess(), itemTag));
            }
        }
        inventoryData.put("Offhand", offhandInventory);

        // Sauvegarder le niveau d'XP
        inventoryData.putInt("XpLevel", player.experienceLevel);
        inventoryData.putFloat("XpProgress", player.experienceProgress);
        inventoryData.putInt("XpTotal", player.totalExperience);

        // Sauvegarder Curios si disponible
        if (isCuriosLoaded()) {
            try {
                saveCuriosInventory(player, inventoryData);
            } catch (Exception e) {
                System.err.println("[InventoryManager] Erreur lors de la sauvegarde Curios: " + e.getMessage());
            }
        }

        savedInventories.put(uuid, inventoryData);
        System.out.println("[InventoryManager] Inventaire sauvegardé pour " + player.getName().getString() + (isCuriosLoaded() ? " (avec Curios)" : ""));
    }

    /**
     * Sauvegarde l'inventaire Curios (méthode séparée pour isolation)
     */
    private static void saveCuriosInventory(ServerPlayer player, CompoundTag inventoryData) {
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            ListTag curiosData = handler.saveInventory(true);
            inventoryData.put("Curios", curiosData);
        });
    }

    /**
     * Restaure l'inventaire complet d'un joueur
     */
    public static void restoreInventory(ServerPlayer player) {
        UUID uuid = player.getUUID();
        CompoundTag inventoryData = savedInventories.get(uuid);

        if (inventoryData == null) {
            System.out.println("[InventoryManager] Aucun inventaire sauvegardé pour " + player.getName().getString());
            return;
        }

        Inventory inventory = player.getInventory();

        // Clear l'inventaire actuel avant restauration
        inventory.clearContent();

        // Restaurer l'inventaire principal + hotbar
        if (inventoryData.contains("Items")) {
            ListTag mainInventory = inventoryData.getList("Items", 10); // 10 = CompoundTag
            for (int i = 0; i < mainInventory.size(); i++) {
                CompoundTag itemTag = mainInventory.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < inventory.items.size()) {
                    inventory.items.set(slot, ItemStack.parse(player.registryAccess(), itemTag).orElse(ItemStack.EMPTY));
                }
            }
        }

        // Restaurer l'armure
        if (inventoryData.contains("Armor")) {
            ListTag armorInventory = inventoryData.getList("Armor", 10);
            for (int i = 0; i < armorInventory.size(); i++) {
                CompoundTag itemTag = armorInventory.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < inventory.armor.size()) {
                    inventory.armor.set(slot, ItemStack.parse(player.registryAccess(), itemTag).orElse(ItemStack.EMPTY));
                }
            }
        }

        // Restaurer l'offhand
        if (inventoryData.contains("Offhand")) {
            ListTag offhandInventory = inventoryData.getList("Offhand", 10);
            for (int i = 0; i < offhandInventory.size(); i++) {
                CompoundTag itemTag = offhandInventory.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < inventory.offhand.size()) {
                    inventory.offhand.set(slot, ItemStack.parse(player.registryAccess(), itemTag).orElse(ItemStack.EMPTY));
                }
            }
        }

        // Restaurer l'XP
        if (inventoryData.contains("XpLevel")) {
            player.experienceLevel = inventoryData.getInt("XpLevel");
            player.experienceProgress = inventoryData.getFloat("XpProgress");
            player.totalExperience = inventoryData.getInt("XpTotal");
        }

        // Restaurer Curios si disponible
        if (isCuriosLoaded() && inventoryData.contains("Curios")) {
            try {
                restoreCuriosInventory(player, inventoryData);
            } catch (Exception e) {
                System.err.println("[InventoryManager] Erreur lors de la restauration Curios: " + e.getMessage());
            }
        }

        // Supprimer la sauvegarde après restauration
        savedInventories.remove(uuid);
        System.out.println("[InventoryManager] Inventaire restauré pour " + player.getName().getString() + (isCuriosLoaded() ? " (avec Curios)" : ""));
    }

    /**
     * Restaure l'inventaire Curios (méthode séparée pour isolation)
     */
    private static void restoreCuriosInventory(ServerPlayer player, CompoundTag inventoryData) {
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            ListTag curiosData = inventoryData.getList("Curios", 10); // 10 = CompoundTag
            handler.loadInventory(curiosData);
        });
    }

    /**
     * Clear l'inventaire d'un joueur (pour le début de partie)
     */
    public static void clearInventory(ServerPlayer player) {
        player.getInventory().clearContent();
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.totalExperience = 0;

        // Clear Curios si disponible
        if (isCuriosLoaded()) {
            try {
                clearCuriosInventory(player);
            } catch (Exception e) {
                System.err.println("[InventoryManager] Erreur lors du clear Curios: " + e.getMessage());
            }
        }

        System.out.println("[InventoryManager] Inventaire cleared pour " + player.getName().getString() + (isCuriosLoaded() ? " (avec Curios)" : ""));
    }

    /**
     * Clear l'inventaire Curios (méthode séparée pour isolation)
     */
    private static void clearCuriosInventory(ServerPlayer player) {
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.getCurios().forEach((slotId, slotHandler) -> {
                for (int i = 0; i < slotHandler.getSlots(); i++) {
                    slotHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                }
            });
        });
    }

    /**
     * Vérifie si un joueur a un inventaire sauvegardé
     */
    public static boolean hasSavedInventory(UUID uuid) {
        return savedInventories.containsKey(uuid);
    }

    /**
     * Supprime la sauvegarde d'inventaire d'un joueur (sans restaurer)
     */
    public static void removeSavedInventory(UUID uuid) {
        savedInventories.remove(uuid);
    }

    /**
     * Reset complet (fin de partie)
     */
    public static void reset() {
        savedInventories.clear();
    }
}

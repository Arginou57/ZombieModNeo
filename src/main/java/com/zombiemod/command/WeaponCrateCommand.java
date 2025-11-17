package com.zombiemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.zombiemod.system.WeaponCrateManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class WeaponCrateCommand {

    // Suggestion provider pour tous les items (vanilla + mods)
    private static final SuggestionProvider<CommandSourceStack> ITEM_SUGGESTIONS = (context, builder) -> {
        return SharedSuggestionProvider.suggestResource(
                BuiltInRegistries.ITEM.keySet().stream(),
                builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /weaponcrate create <cost>
        dispatcher.register(Commands.literal("weaponcrate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("create")
                        .then(Commands.argument("cost", IntegerArgumentType.integer(0))
                                .executes(WeaponCrateCommand::createCrate))));

        // /weaponcrate addweapon <item> [count] [weight] [name]
        dispatcher.register(Commands.literal("weaponcrate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("addweapon")
                        .then(Commands.literal("hand")
                                // /weaponcrate addweapon hand [weight] [name]
                                .executes(WeaponCrateCommand::addWeaponFromHand)
                                .then(Commands.argument("weight", IntegerArgumentType.integer(1))
                                        .executes(WeaponCrateCommand::addWeaponFromHandWithWeight)
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(WeaponCrateCommand::addWeaponFromHandWithName))))
                        .then(Commands.argument("item", StringArgumentType.word())
                                .suggests(ITEM_SUGGESTIONS)
                                // Version simple : juste l'item (count=1, weight=10, nom auto)
                                .executes(WeaponCrateCommand::addWeaponSimple)
                                // Version avec count
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(WeaponCrateCommand::addWeaponWithCount)
                                        // Version avec count et weight
                                        .then(Commands.argument("weight", IntegerArgumentType.integer(1))
                                                .executes(WeaponCrateCommand::addWeaponWithWeight)
                                                // Version complète avec name
                                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                                        .executes(WeaponCrateCommand::addWeapon)))))));

        // /weaponcrate preset starter
        dispatcher.register(Commands.literal("weaponcrate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("preset")
                        .then(Commands.literal("starter")
                                .executes(WeaponCrateCommand::createStarterPreset))));

        // /weaponcrate preset advanced
        dispatcher.register(Commands.literal("weaponcrate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("preset")
                        .then(Commands.literal("advanced")
                                .executes(WeaponCrateCommand::createAdvancedPreset))));

        // /weaponcrate preset legendary
        dispatcher.register(Commands.literal("weaponcrate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("preset")
                        .then(Commands.literal("legendary")
                                .executes(WeaponCrateCommand::createLegendaryPreset))));
    }

    private static int createCrate(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        int cost = IntegerArgumentType.getInteger(context, "cost");
        BlockPos chestPos = getTargetedChest(player);

        if (chestPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un coffre !"));
            return 0;
        }

        WeaponCrateManager.setWeaponCrate(player.level(), chestPos, cost);
        player.sendSystemMessage(Component.literal("§aCaisse d'armes créée avec un coût de §e" + cost + " points§a."));
        player.sendSystemMessage(Component.literal("§7Utilisez §e/weaponcrate addweapon §7pour ajouter des armes."));

        return 1;
    }

    private static int addWeapon(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String item = StringArgumentType.getString(context, "item");
        int count = IntegerArgumentType.getInteger(context, "count");
        int weight = IntegerArgumentType.getInteger(context, "weight");
        String name = StringArgumentType.getString(context, "name");

        BlockPos chestPos = getTargetedChest(player);

        if (chestPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un coffre !"));
            return 0;
        }

        if (!WeaponCrateManager.isWeaponCrate(player.level(), chestPos)) {
            player.sendSystemMessage(Component.literal("§cCe coffre n'est pas une caisse d'armes ! Utilisez §e/weaponcrate create <cost>"));
            return 0;
        }

        WeaponCrateManager.addWeapon(player.level(), chestPos, item, count, weight, name, null);
        player.sendSystemMessage(Component.literal("§aArme ajoutée: §e" + name + " §7(poids: " + weight + ")"));

        return 1;
    }

    // Version simple : /weaponcrate addweapon minecraft:wooden_sword
    private static int addWeaponSimple(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String item = StringArgumentType.getString(context, "item");
        int count = 1;
        int weight = 10;
        String name = generateDefaultName(item);

        return addWeaponInternal(player, item, count, weight, name);
    }

    // Version avec count : /weaponcrate addweapon minecraft:wooden_sword 1
    private static int addWeaponWithCount(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String item = StringArgumentType.getString(context, "item");
        int count = IntegerArgumentType.getInteger(context, "count");
        int weight = 10;
        String name = generateDefaultName(item);

        return addWeaponInternal(player, item, count, weight, name);
    }

    // Version avec weight : /weaponcrate addweapon minecraft:wooden_sword 1 20
    private static int addWeaponWithWeight(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String item = StringArgumentType.getString(context, "item");
        int count = IntegerArgumentType.getInteger(context, "count");
        int weight = IntegerArgumentType.getInteger(context, "weight");
        String name = generateDefaultName(item);

        return addWeaponInternal(player, item, count, weight, name);
    }

    // /weaponcrate addweapon hand - Ajoute l'item en main (weight=10, nom de l'item)
    private static int addWeaponFromHand(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        return addWeaponFromHandInternal(player, 10, null);
    }

    // /weaponcrate addweapon hand <weight>
    private static int addWeaponFromHandWithWeight(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        int weight = IntegerArgumentType.getInteger(context, "weight");
        return addWeaponFromHandInternal(player, weight, null);
    }

    // /weaponcrate addweapon hand <weight> <name>
    private static int addWeaponFromHandWithName(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        int weight = IntegerArgumentType.getInteger(context, "weight");
        String name = StringArgumentType.getString(context, "name");
        return addWeaponFromHandInternal(player, weight, name);
    }

    // Méthode pour ajouter l'item en main avec tous ses tags
    private static int addWeaponFromHandInternal(ServerPlayer player, int weight, String customName) {
        // Vérifier le coffre
        BlockPos chestPos = getTargetedChest(player);
        if (chestPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un coffre !"));
            return 0;
        }

        if (!WeaponCrateManager.isWeaponCrate(player.level(), chestPos)) {
            player.sendSystemMessage(Component.literal("§cCe coffre n'est pas une caisse d'armes ! Utilisez §e/weaponcrate create <cost>"));
            return 0;
        }

        // Récupérer l'item en main
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cVous devez tenir un item dans votre main !"));
            return 0;
        }

        // Extraire les informations de l'item
        String itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
        int count = heldItem.getCount();

        // Utiliser le nom custom fourni, ou le nom de l'item, ou générer un nom par défaut
        String displayName;
        if (customName != null && !customName.isEmpty()) {
            displayName = customName;
        } else if (heldItem.has(DataComponents.CUSTOM_NAME)) {
            displayName = heldItem.get(DataComponents.CUSTOM_NAME).getString();
        } else {
            displayName = generateDefaultName(itemId);
        }

        // Utiliser la nouvelle méthode qui sauvegarde l'ItemStack complet avec tous ses DataComponents
        WeaponCrateManager.addWeaponFromItemStack(player.level(), chestPos, heldItem, weight, displayName);

        // Compter les enchantements pour le message
        ItemEnchantments itemEnchantments = heldItem.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int enchantmentCount = itemEnchantments.size();

        // Message de confirmation
        String enchInfo = enchantmentCount > 0 ? " §7avec §e" + enchantmentCount + " enchantement(s)" : "";
        player.sendSystemMessage(Component.literal("§aArme ajoutée: §e" + displayName + " §7(x" + count + ", poids: " + weight + ")" + enchInfo));
        player.sendSystemMessage(Component.literal("§7Tous les tags et DataComponents ont été préservés."));

        return 1;
    }

    // Méthode interne partagée
    private static int addWeaponInternal(ServerPlayer player, String item, int count, int weight, String name) {
        BlockPos chestPos = getTargetedChest(player);

        if (chestPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un coffre !"));
            return 0;
        }

        if (!WeaponCrateManager.isWeaponCrate(player.level(), chestPos)) {
            player.sendSystemMessage(Component.literal("§cCe coffre n'est pas une caisse d'armes ! Utilisez §e/weaponcrate create <cost>"));
            return 0;
        }

        WeaponCrateManager.addWeapon(player.level(), chestPos, item, count, weight, name, null);
        player.sendSystemMessage(Component.literal("§aArme ajoutée: §e" + name + " §7(x" + count + ", poids: " + weight + ")"));

        return 1;
    }

    // Génère un nom par défaut basé sur l'ID de l'item
    private static String generateDefaultName(String itemId) {
        // Retirer "minecraft:" si présent
        String cleanId = itemId.replace("minecraft:", "");

        // Remplacer underscores par espaces et capitaliser
        String[] parts = cleanId.split("_");
        StringBuilder name = new StringBuilder();

        for (String part : parts) {
            if (!name.isEmpty()) {
                name.append(" ");
            }
            if (!part.isEmpty()) {
                name.append(Character.toUpperCase(part.charAt(0)));
                name.append(part.substring(1));
            }
        }

        return "§f" + name.toString();
    }

    private static int createStarterPreset(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        BlockPos chestPos = getTargetedChest(player);

        if (chestPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un coffre !"));
            return 0;
        }

        WeaponCrateManager.createStarterCrate(player.level(), chestPos);
        player.sendSystemMessage(Component.literal("§aCaisse §6STARTER §acréée (500 points) !"));

        return 1;
    }

    private static int createAdvancedPreset(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        BlockPos chestPos = getTargetedChest(player);

        if (chestPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un coffre !"));
            return 0;
        }

        WeaponCrateManager.createAdvancedCrate(player.level(), chestPos);
        player.sendSystemMessage(Component.literal("§aCaisse §bADVANCED §acréée (1500 points) !"));

        return 1;
    }

    private static int createLegendaryPreset(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        BlockPos chestPos = getTargetedChest(player);

        if (chestPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un coffre !"));
            return 0;
        }

        WeaponCrateManager.createLegendaryCrate(player.level(), chestPos);
        player.sendSystemMessage(Component.literal("§aCaisse §4§lLEGENDARY §acréée (5000 points) !"));

        return 1;
    }

    private static BlockPos getTargetedChest(ServerPlayer player) {
        HitResult hit = player.pick(5.0, 0, false);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();

            if (player.level().getBlockState(pos).getBlock() instanceof ChestBlock) {
                return pos;
            }
        }

        return null;
    }
}

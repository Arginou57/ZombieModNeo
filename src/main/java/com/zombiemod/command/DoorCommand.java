package com.zombiemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zombiemod.map.DoorConfig;
import com.zombiemod.map.MapConfig;
import com.zombiemod.map.MapManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Map;

public class DoorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /zombiedoor add <mapname> <doorNumber> <cost>
        dispatcher.register(Commands.literal("zombiedoor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("mapname", StringArgumentType.string())
                                .then(Commands.argument("doorNumber", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("cost", IntegerArgumentType.integer(0))
                                                .executes(DoorCommand::addDoor))))));

        // /zombiedoor remove <mapname> <doorNumber>
        dispatcher.register(Commands.literal("zombiedoor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("remove")
                        .then(Commands.argument("mapname", StringArgumentType.string())
                                .then(Commands.argument("doorNumber", IntegerArgumentType.integer(0))
                                        .executes(DoorCommand::removeDoor)))));

        // /zombiedoor list <mapname>
        dispatcher.register(Commands.literal("zombiedoor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                        .then(Commands.argument("mapname", StringArgumentType.string())
                                .executes(DoorCommand::listDoors))));

        // /zombiedoor open <mapname> <doorNumber>
        dispatcher.register(Commands.literal("zombiedoor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("open")
                        .then(Commands.argument("mapname", StringArgumentType.string())
                                .then(Commands.argument("doorNumber", IntegerArgumentType.integer(0))
                                        .executes(DoorCommand::openDoor)))));

        // /zombiedoor close <mapname> <doorNumber>
        dispatcher.register(Commands.literal("zombiedoor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("close")
                        .then(Commands.argument("mapname", StringArgumentType.string())
                                .then(Commands.argument("doorNumber", IntegerArgumentType.integer(0))
                                        .executes(DoorCommand::closeDoor)))));
    }

    private static int addDoor(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String mapName = StringArgumentType.getString(context, "mapname");
        int doorNumber = IntegerArgumentType.getInteger(context, "doorNumber");
        int cost = IntegerArgumentType.getInteger(context, "cost");

        // Vérifier que la map existe
        if (!MapManager.mapExists(mapName)) {
            player.sendSystemMessage(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            return 0;
        }

        // Vérifier que le joueur regarde une pancarte
        HitResult hit = player.pick(5.0, 0, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder une pancarte !"));
            return 0;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos signPos = blockHit.getBlockPos();
        ServerLevel level = player.serverLevel();
        BlockState signState = level.getBlockState(signPos);

        // Vérifier que c'est une pancarte murale
        if (!(signState.getBlock() instanceof WallSignBlock)) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder une pancarte murale !"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);

        // Vérifier si la porte existe déjà
        if (map.hasDoor(doorNumber)) {
            player.sendSystemMessage(Component.literal("§cLa porte n°" + doorNumber + " existe déjà sur cette map !"));
            return 0;
        }

        // Créer la porte
        DoorConfig door = new DoorConfig(doorNumber, signPos, cost);

        // Récupérer la direction de la pancarte
        Direction signFacing = signState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction wallDirection = signFacing.getOpposite(); // Le mur est derrière la pancarte

        // Scanner les blocs 3x3 derrière la pancarte
        scanWallBlocks(level, signPos, wallDirection, door);

        // Ajouter la porte à la map
        map.addDoor(door);
        MapManager.save();

        // Synchroniser avec tous les clients
        com.zombiemod.system.ServerDoorTracker.syncToAllPlayers();

        int blockCount = door.getWallBlocks().size();
        player.sendSystemMessage(Component.literal("§aPorte n°" + doorNumber + " créée sur la map '§e" + mapName + "§a':"));
        player.sendSystemMessage(Component.literal("  §fPancarte: §e" + signPos.getX() + ", " + signPos.getY() + ", " + signPos.getZ()));
        player.sendSystemMessage(Component.literal("  §fDirection: §e" + signFacing));
        player.sendSystemMessage(Component.literal("  §fBlocs sauvegardés: §e" + blockCount + " §7blocs"));
        player.sendSystemMessage(Component.literal("  §fCoût: §e" + cost + " points"));
        player.sendSystemMessage(Component.literal("  §fÉtat: §c§lFERMÉE"));

        return 1;
    }

    /**
     * Scanne et sauvegarde les blocs 3x3 derrière la pancarte
     */
    private static void scanWallBlocks(ServerLevel level, BlockPos signPos, Direction wallDirection, DoorConfig door) {
        // Position de départ : 1 bloc dans la direction du mur
        BlockPos startPos = signPos.relative(wallDirection);

        // Déterminer les axes perpendiculaires
        Direction right, up;
        up = Direction.UP;

        // Déterminer la direction "droite" selon la direction du mur
        if (wallDirection == Direction.NORTH || wallDirection == Direction.SOUTH) {
            right = Direction.EAST;
        } else {
            right = Direction.SOUTH;
        }

        // Scanner 3x3 (centré sur startPos)
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                BlockPos blockPos = startPos.relative(up, dy).relative(right, dx);
                BlockState state = level.getBlockState(blockPos);

                // Ne sauvegarder que les blocs solides (pas l'air)
                if (!state.isAir()) {
                    door.addWallBlock(blockPos, state);
                }
            }
        }
    }

    private static int removeDoor(CommandContext<CommandSourceStack> context) {
        String mapName = StringArgumentType.getString(context, "mapname");
        int doorNumber = IntegerArgumentType.getInteger(context, "doorNumber");

        if (!MapManager.mapExists(mapName)) {
            context.getSource().sendFailure(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);

        if (!map.hasDoor(doorNumber)) {
            context.getSource().sendFailure(Component.literal("§cLa porte n°" + doorNumber + " n'existe pas sur cette map !"));
            return 0;
        }

        map.removeDoor(doorNumber);
        MapManager.save();

        // Synchroniser avec tous les clients
        com.zombiemod.system.ServerDoorTracker.syncToAllPlayers();

        context.getSource().sendSuccess(() -> Component.literal("§aPorte n°" + doorNumber + " supprimée de la map '§e" + mapName + "§a'"), true);
        return 1;
    }

    private static int listDoors(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String mapName = StringArgumentType.getString(context, "mapname");

        if (!MapManager.mapExists(mapName)) {
            player.sendSystemMessage(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);
        Map<Integer, DoorConfig> doors = map.getDoors();

        if (doors.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Aucune porte sur la map '§e" + mapName + "§7'"));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6§l=== PORTES DE " + mapName.toUpperCase() + " ==="));
        for (Map.Entry<Integer, DoorConfig> entry : doors.entrySet()) {
            DoorConfig door = entry.getValue();
            String status = door.isOpen() ? "§a§lOUVERTE" : "§c§lFERMÉE";
            BlockPos pos = door.getSignPosition();

            player.sendSystemMessage(Component.literal("§ePorte #" + door.getDoorNumber() + " " + status));
            player.sendSystemMessage(Component.literal("  §7Pancarte: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
            player.sendSystemMessage(Component.literal("  §7Blocs: §e" + door.getWallBlocks().size()));
            player.sendSystemMessage(Component.literal("  §7Coût: §e" + door.getCost() + " points"));
        }

        return 1;
    }

    private static int openDoor(CommandContext<CommandSourceStack> context) {
        String mapName = StringArgumentType.getString(context, "mapname");
        int doorNumber = IntegerArgumentType.getInteger(context, "doorNumber");

        if (!MapManager.mapExists(mapName)) {
            context.getSource().sendFailure(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);

        if (!map.hasDoor(doorNumber)) {
            context.getSource().sendFailure(Component.literal("§cLa porte n°" + doorNumber + " n'existe pas sur cette map !"));
            return 0;
        }

        DoorConfig door = map.getDoor(doorNumber);
        ServerLevel level = context.getSource().getLevel();

        // Ouvrir physiquement la porte (détruire les blocs + pancarte)
        openDoorPhysically(level, door);

        // Marquer comme ouverte
        map.openDoor(doorNumber);
        MapManager.save();

        // Synchroniser avec tous les clients
        com.zombiemod.system.ServerDoorTracker.syncToAllPlayers();

        context.getSource().sendSuccess(() -> Component.literal("§aPorte n°" + doorNumber + " ouverte ! Blocs détruits."), true);
        return 1;
    }

    private static int closeDoor(CommandContext<CommandSourceStack> context) {
        String mapName = StringArgumentType.getString(context, "mapname");
        int doorNumber = IntegerArgumentType.getInteger(context, "doorNumber");

        if (!MapManager.mapExists(mapName)) {
            context.getSource().sendFailure(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);

        if (!map.hasDoor(doorNumber)) {
            context.getSource().sendFailure(Component.literal("§cLa porte n°" + doorNumber + " n'existe pas sur cette map !"));
            return 0;
        }

        DoorConfig door = map.getDoor(doorNumber);
        ServerLevel level = context.getSource().getLevel();

        // Fermer physiquement la porte (remettre les blocs)
        closeDoorPhysically(level, door);

        // Marquer comme fermée
        map.closeDoor(doorNumber);
        MapManager.save();

        // Synchroniser avec tous les clients
        com.zombiemod.system.ServerDoorTracker.syncToAllPlayers();

        context.getSource().sendSuccess(() -> Component.literal("§aPorte n°" + doorNumber + " fermée ! Blocs remis."), true);
        return 1;
    }

    /**
     * Ouvre physiquement la porte : détruit les blocs du mur et la pancarte
     */
    private static void openDoorPhysically(ServerLevel level, DoorConfig door) {
        // Détruire la pancarte
        BlockPos signPos = door.getSignPosition();
        if (signPos != null) {
            level.setBlock(signPos, Blocks.AIR.defaultBlockState(), 3);
        }

        // Détruire les blocs du mur
        for (DoorConfig.SavedBlock savedBlock : door.getWallBlocks()) {
            BlockPos pos = savedBlock.getPosition();
            if (pos != null) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Ferme physiquement la porte : remet les blocs du mur (pas la pancarte car elle sera recréée manuellement)
     */
    private static void closeDoorPhysically(ServerLevel level, DoorConfig door) {
        // Remettre les blocs du mur
        for (DoorConfig.SavedBlock savedBlock : door.getWallBlocks()) {
            BlockPos pos = savedBlock.getPosition();
            BlockState state = savedBlock.getBlockState();
            if (pos != null && state != null) {
                level.setBlock(pos, state, 3);
            }
        }

        // Note: On ne remet pas automatiquement la pancarte car elle a du texte
        // L'admin devra la replacer manuellement
    }
}

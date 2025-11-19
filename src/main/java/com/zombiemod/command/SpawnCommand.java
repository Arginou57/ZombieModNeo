package com.zombiemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zombiemod.map.MapConfig;
import com.zombiemod.map.MapManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class SpawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /zombiespawn <mapname> [doorNumber]
        dispatcher.register(Commands.literal("zombiespawn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("mapname", StringArgumentType.string())
                        .executes(SpawnCommand::addSpawnPoint)
                        .then(Commands.argument("doorNumber", IntegerArgumentType.integer(0))
                                .executes(SpawnCommand::addSpawnPointWithDoor))));

        // /zombiespawn clear <mapname>
        dispatcher.register(Commands.literal("zombiespawn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clear")
                        .then(Commands.argument("mapname", StringArgumentType.string())
                                .executes(SpawnCommand::clearSpawnPoints))));
    }

    private static int addSpawnPoint(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String mapName = StringArgumentType.getString(context, "mapname");

        // Vérifier que la map existe
        if (!MapManager.mapExists(mapName)) {
            player.sendSystemMessage(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            player.sendSystemMessage(Component.literal("§7Utilisez §f/zombiemap list §7pour voir les maps disponibles"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);
        BlockPos pos = player.blockPosition();

        // Ajouter le point de spawn
        map.addZombieSpawnPoint(pos);
        MapManager.save();

        player.sendSystemMessage(Component.literal("§aPoint de spawn ajouté à la map '§e" + mapName + "§a':"));
        player.sendSystemMessage(Component.literal("  §fX: §e" + pos.getX()));
        player.sendSystemMessage(Component.literal("  §fY: §e" + pos.getY()));
        player.sendSystemMessage(Component.literal("  §fZ: §e" + pos.getZ()));
        player.sendSystemMessage(Component.literal("  §fPorte: §7Toujours actif"));
        player.sendSystemMessage(Component.literal("§7Total: §e" + map.getZombieSpawnPointCount() + " §7point(s) de spawn"));

        return 1;
    }

    private static int addSpawnPointWithDoor(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        String mapName = StringArgumentType.getString(context, "mapname");
        int doorNumber = IntegerArgumentType.getInteger(context, "doorNumber");

        // Vérifier que la map existe
        if (!MapManager.mapExists(mapName)) {
            player.sendSystemMessage(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            player.sendSystemMessage(Component.literal("§7Utilisez §f/zombiemap list §7pour voir les maps disponibles"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);

        // Vérifier que la porte existe
        if (!map.hasDoor(doorNumber)) {
            player.sendSystemMessage(Component.literal("§cLa porte n°" + doorNumber + " n'existe pas sur cette map !"));
            player.sendSystemMessage(Component.literal("§7Créez la porte avec §f/zombiedoor add " + mapName + " " + doorNumber + " <cost>"));
            return 0;
        }

        BlockPos pos = player.blockPosition();

        // Ajouter le point de spawn avec le numéro de porte
        map.addZombieSpawnPoint(pos, doorNumber);
        MapManager.save();

        player.sendSystemMessage(Component.literal("§aPoint de spawn ajouté à la map '§e" + mapName + "§a':"));
        player.sendSystemMessage(Component.literal("  §fX: §e" + pos.getX()));
        player.sendSystemMessage(Component.literal("  §fY: §e" + pos.getY()));
        player.sendSystemMessage(Component.literal("  §fZ: §e" + pos.getZ()));
        player.sendSystemMessage(Component.literal("  §fPorte: §e#" + doorNumber + " §7(actif si porte ouverte)"));
        player.sendSystemMessage(Component.literal("§7Total: §e" + map.getZombieSpawnPointCount() + " §7point(s) de spawn"));

        return 1;
    }

    private static int clearSpawnPoints(CommandContext<CommandSourceStack> context) {
        String mapName = StringArgumentType.getString(context, "mapname");

        // Vérifier que la map existe
        if (!MapManager.mapExists(mapName)) {
            context.getSource().sendFailure(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
            return 0;
        }

        MapConfig map = MapManager.getMap(mapName);
        int count = map.getZombieSpawnPointCount();

        map.clearZombieSpawnPoints();
        MapManager.save();

        context.getSource().sendSuccess(() -> Component.literal("§a" + count + " point(s) de spawn supprimé(s) de la map '§e" + mapName + "§a'"), true);
        return 1;
    }
}

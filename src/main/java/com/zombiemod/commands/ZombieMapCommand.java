package com.zombiemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zombiemod.map.MapConfig;
import com.zombiemod.map.MapManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class ZombieMapCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zombiemap")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ZombieMapCommand::createMap)))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ZombieMapCommand::deleteMap)))
                .then(Commands.literal("select")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ZombieMapCommand::selectMap)))
                .then(Commands.literal("list")
                        .executes(ZombieMapCommand::listMaps))
                .then(Commands.literal("info")
                        .executes(context -> showMapInfo(context, null))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> showMapInfo(context, StringArgumentType.getString(context, "name"))))));
    }

    private static int createMap(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (MapManager.mapExists(name)) {
            source.sendFailure(Component.literal("§cUne map nommée '" + name + "' existe déjà !"));
            return 0;
        }

        if (MapManager.createMap(name)) {
            source.sendSuccess(() -> Component.literal("§aMap '§e" + name + "§a' créée avec succès !"), true);
            source.sendSuccess(() -> Component.literal("§7Utilisez §f/zombiemap select " + name + " §7pour la sélectionner"), false);
            source.sendSuccess(() -> Component.literal("§7Puis configurez-la avec §f/respawnpoint " + name + " §7et §f/zombiespawn " + name), false);
            return 1;
        }

        source.sendFailure(Component.literal("§cErreur lors de la création de la map"));
        return 0;
    }

    private static int deleteMap(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!MapManager.mapExists(name)) {
            source.sendFailure(Component.literal("§cLa map '" + name + "' n'existe pas !"));
            return 0;
        }

        if (MapManager.getMapCount() <= 1) {
            source.sendFailure(Component.literal("§cVous ne pouvez pas supprimer la dernière map !"));
            source.sendFailure(Component.literal("§7Créez d'abord une autre map avec §f/zombiemap create <nom>"));
            return 0;
        }

        if (MapManager.deleteMap(name)) {
            source.sendSuccess(() -> Component.literal("§aMap '§e" + name + "§a' supprimée avec succès !"), true);

            String selected = MapManager.getSelectedMapName();
            if (selected != null) {
                source.sendSuccess(() -> Component.literal("§7Map active: §e" + selected), false);
            }
            return 1;
        }

        source.sendFailure(Component.literal("§cErreur lors de la suppression de la map"));
        return 0;
    }

    private static int selectMap(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!MapManager.mapExists(name)) {
            source.sendFailure(Component.literal("§cLa map '" + name + "' n'existe pas !"));
            source.sendFailure(Component.literal("§7Utilisez §f/zombiemap list §7pour voir les maps disponibles"));
            return 0;
        }

        if (MapManager.selectMap(name)) {
            source.sendSuccess(() -> Component.literal("§aMap '§e" + name + "§a' sélectionnée !"), true);

            MapConfig map = MapManager.getMap(name);
            if (map != null) {
                boolean hasRespawn = map.hasRespawnPoint();
                boolean hasSpawns = map.hasZombieSpawnPoints();

                if (!hasRespawn || !hasSpawns) {
                    source.sendSuccess(() -> Component.literal("§e⚠ Configuration incomplète:"), false);
                    if (!hasRespawn) {
                        source.sendSuccess(() -> Component.literal("§7  • Point de respawn: §cNon défini §7(§f/respawnpoint " + name + "§7)"), false);
                    } else {
                        source.sendSuccess(() -> Component.literal("§7  • Point de respawn: §a✓ Défini"), false);
                    }
                    if (!hasSpawns) {
                        source.sendSuccess(() -> Component.literal("§7  • Spawn zombies: §cAucun §7(§f/zombiespawn " + name + "§7)"), false);
                    } else {
                        source.sendSuccess(() -> Component.literal("§7  • Spawn zombies: §a✓ " + map.getZombieSpawnPointCount() + " point(s)"), false);
                    }
                }
            }
            return 1;
        }

        source.sendFailure(Component.literal("§cErreur lors de la sélection de la map"));
        return 0;
    }

    private static int listMaps(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Collection<MapConfig> maps = MapManager.getAllMaps();
        String selectedName = MapManager.getSelectedMapName();

        if (maps.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§cAucune map disponible"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6§l===== MAPS DISPONIBLES ====="), false);
        source.sendSuccess(() -> Component.literal(""), false);

        for (MapConfig map : maps) {
            String name = map.getName();
            boolean isSelected = name.equals(selectedName);

            String prefix = isSelected ? "§a▶ " : "§7  ";
            String status = isSelected ? " §e(active)" : "";

            source.sendSuccess(() -> Component.literal(prefix + "§f" + name + status), false);

            if (map.hasRespawnPoint()) {
                BlockPos pos = map.getRespawnPoint();
                source.sendSuccess(() -> Component.literal("    §7Respawn: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
            } else {
                source.sendSuccess(() -> Component.literal("    §7Respawn: §cNon défini"), false);
            }

            int spawnCount = map.getZombieSpawnPointCount();
            if (spawnCount > 0) {
                source.sendSuccess(() -> Component.literal("    §7Spawns: §f" + spawnCount + " point(s)"), false);
            } else {
                source.sendSuccess(() -> Component.literal("    §7Spawns: §cAucun"), false);
            }

            source.sendSuccess(() -> Component.literal(""), false);
        }

        source.sendSuccess(() -> Component.literal("§7Total: §f" + maps.size() + " map(s)"), false);
        source.sendSuccess(() -> Component.literal("§7Utilisez §f/zombiemap select <nom> §7pour changer de map"), false);

        return 1;
    }

    private static int showMapInfo(CommandContext<CommandSourceStack> context, String mapName) {
        CommandSourceStack source = context.getSource();

        MapConfig map;
        String name;

        if (mapName == null) {
            map = MapManager.getSelectedMap();
            name = MapManager.getSelectedMapName();

            if (map == null) {
                source.sendFailure(Component.literal("§cAucune map sélectionnée !"));
                source.sendFailure(Component.literal("§7Utilisez §f/zombiemap select <nom> §7pour sélectionner une map"));
                return 0;
            }
        } else {
            map = MapManager.getMap(mapName);
            name = mapName;

            if (map == null) {
                source.sendFailure(Component.literal("§cLa map '" + mapName + "' n'existe pas !"));
                return 0;
            }
        }

        boolean isSelected = name.equals(MapManager.getSelectedMapName());

        source.sendSuccess(() -> Component.literal("§6§l===== INFO MAP: " + name.toUpperCase() + " ====="), false);
        source.sendSuccess(() -> Component.literal(""), false);

        if (isSelected) {
            source.sendSuccess(() -> Component.literal("§aStatut: §e✓ Active"), false);
        } else {
            source.sendSuccess(() -> Component.literal("§7Statut: Inactive"), false);
        }

        source.sendSuccess(() -> Component.literal(""), false);

        if (map.hasRespawnPoint()) {
            BlockPos pos = map.getRespawnPoint();
            source.sendSuccess(() -> Component.literal("§e§lPoint de Respawn:"), false);
            source.sendSuccess(() -> Component.literal("§f  X: " + pos.getX()), false);
            source.sendSuccess(() -> Component.literal("§f  Y: " + pos.getY()), false);
            source.sendSuccess(() -> Component.literal("§f  Z: " + pos.getZ()), false);
        } else {
            source.sendSuccess(() -> Component.literal("§c§lPoint de Respawn: Non défini"), false);
            source.sendSuccess(() -> Component.literal("§7  Utilisez: §f/respawnpoint " + name), false);
        }

        source.sendSuccess(() -> Component.literal(""), false);

        int spawnCount = map.getZombieSpawnPointCount();
        if (spawnCount > 0) {
            source.sendSuccess(() -> Component.literal("§e§lSpawns Zombies: §f" + spawnCount + " point(s)"), false);
            int i = 1;
            for (BlockPos pos : map.getZombieSpawnPoints()) {
                final int index = i;
                source.sendSuccess(() -> Component.literal("§7  " + index + ". §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                i++;
            }
        } else {
            source.sendSuccess(() -> Component.literal("§c§lSpawns Zombies: Aucun"), false);
            source.sendSuccess(() -> Component.literal("§7  Utilisez: §f/zombiespawn " + name), false);
        }

        return 1;
    }
}

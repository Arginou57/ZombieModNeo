package com.zombiemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zombiemod.map.MapConfig;
import com.zombiemod.map.MapManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RespawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /respawnpoint <mapname>
        dispatcher.register(Commands.literal("respawnpoint")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("mapname", StringArgumentType.string())
                        .executes(RespawnCommand::setRespawnPoint)));
    }

    private static int setRespawnPoint(CommandContext<CommandSourceStack> context) {
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

        // Définir le point de respawn
        map.setRespawnPoint(pos);
        MapManager.save();

        player.sendSystemMessage(Component.literal("§aPoint de respawn de la map '§e" + mapName + "§a' défini à:"));
        player.sendSystemMessage(Component.literal("  §fX: §e" + pos.getX()));
        player.sendSystemMessage(Component.literal("  §fY: §e" + pos.getY()));
        player.sendSystemMessage(Component.literal("  §fZ: §e" + pos.getZ()));

        // Téléporter le joueur (pour faciliter la configuration)
        player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        return 1;
    }
}

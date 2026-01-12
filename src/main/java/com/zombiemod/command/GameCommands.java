package com.zombiemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zombiemod.manager.GameManager;
import com.zombiemod.manager.InventoryManager;
import com.zombiemod.manager.PointsManager;
import com.zombiemod.manager.WaveManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class GameCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /zombiestart [mapName]
        dispatcher.register(Commands.literal("zombiestart")
                .requires(source -> source.hasPermission(2))
                .executes(GameCommands::startGame)
                .then(Commands.argument("mapName", StringArgumentType.word())
                        .executes(GameCommands::startGameWithMap)));

        // /zombiestop
        dispatcher.register(Commands.literal("zombiestop")
                .requires(source -> source.hasPermission(2))
                .executes(GameCommands::stopGame));

        // /zombiejoin
        dispatcher.register(Commands.literal("zombiejoin")
                .executes(GameCommands::joinGame));

        // /zombieleave
        dispatcher.register(Commands.literal("zombieleave")
                .executes(GameCommands::leaveGame));

        // /zombiestatus
        dispatcher.register(Commands.literal("zombiestatus")
                .executes(GameCommands::showStatus));

        // /zombieskip
        dispatcher.register(Commands.literal("zombieskip")
                .requires(source -> source.hasPermission(2))
                .executes(GameCommands::skipWave));
    }

    private static int startGame(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        if (GameManager.getGameState() != GameManager.GameState.WAITING) {
            context.getSource().sendFailure(Component.literal("§cUne partie est déjà en cours !"));
            return 0;
        }

        GameManager.startGame(level, null); // Pas de nom de map spécifié
        context.getSource().sendSuccess(() -> Component.literal("§aPartie lancée ! Countdown de 60 secondes démarré."), true);

        return 1;
    }

    private static int startGameWithMap(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        String mapName = StringArgumentType.getString(context, "mapName");

        if (GameManager.getGameState() != GameManager.GameState.WAITING) {
            context.getSource().sendFailure(Component.literal("§cUne partie est déjà en cours !"));
            return 0;
        }

        GameManager.startGame(level, mapName);
        context.getSource().sendSuccess(() -> Component.literal("§aPartie lancée sur la map §e" + mapName + " §a! Countdown de 60 secondes démarré."), true);

        return 1;
    }

    private static int stopGame(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        if (GameManager.getGameState() == GameManager.GameState.WAITING) {
            context.getSource().sendFailure(Component.literal("§cAucune partie en cours !"));
            return 0;
        }

        // Annonce de l'arrêt
        GameManager.broadcastToAll(level, "§c§l=== PARTIE ARRÊTÉE PAR UN ADMIN ===");

        // Nettoyer tous les mobs avec la méthode dédiée
        WaveManager.killAllMobs();

        // Restaurer les inventaires et mettre tous les joueurs en survival
        for (UUID uuid : GameManager.getActivePlayers()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                InventoryManager.clearInventory(player);
                InventoryManager.restoreInventory(player);
                player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                player.sendSystemMessage(Component.literal("§7La partie a été arrêtée."));
                player.sendSystemMessage(Component.literal("§aVotre inventaire a été restauré."));
            }
        }

        for (UUID uuid : GameManager.getWaitingPlayers()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                InventoryManager.clearInventory(player);
                InventoryManager.restoreInventory(player);
                player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                player.sendSystemMessage(Component.literal("§7La partie a été arrêtée."));
                player.sendSystemMessage(Component.literal("§aVotre inventaire a été restauré."));
            }
        }

        // Réinitialiser tous les managers
        GameManager.reset();
        WaveManager.reset();

        // Réinitialiser les portes (fermer physiquement et réinitialiser l'état)
        DoorCommand.resetAllDoors(level);

        context.getSource().sendSuccess(() -> Component.literal("§aPartie arrêtée, zombies nettoyés et jeu réinitialisé."), true);

        return 1;
    }

    private static int joinGame(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        GameManager.joinGame(player, level);

        return 1;
    }

    private static int leaveGame(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        GameManager.leaveGame(player, level);

        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        if (player == null) return 0;

        player.sendSystemMessage(Component.literal("§6§l=== STATUT ZOMBIE ==="));
        String mapName = GameManager.getCurrentMapName();
        if (mapName != null && !mapName.isEmpty()) {
            player.sendSystemMessage(Component.literal("§eMap: §6" + mapName));
        }
        player.sendSystemMessage(Component.literal("§eÉtat: §f" + getStateName(GameManager.getGameState())));
        player.sendSystemMessage(Component.literal("§eVague actuelle: §f" + WaveManager.getCurrentWave()));
        player.sendSystemMessage(Component.literal("§eZombies restants: §f" + WaveManager.getZombiesRemaining()));
        player.sendSystemMessage(Component.literal("§eJoueurs actifs: §f" + GameManager.getActivePlayers().size()));
        player.sendSystemMessage(Component.literal("§eJoueurs en attente: §f" + GameManager.getWaitingPlayers().size()));

        if (GameManager.isPlayerActive(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§aVous êtes dans la partie !"));
            player.sendSystemMessage(Component.literal("§eVos points: §f" + PointsManager.getPoints(player.getUUID())));
        } else if (GameManager.isPlayerWaiting(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§eVous êtes en attente..."));
        } else {
            player.sendSystemMessage(Component.literal("§7Vous n'êtes pas dans la partie. §e/zombiejoin §7pour rejoindre."));
        }

        return 1;
    }

    private static int skipWave(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        GameManager.GameState state = GameManager.getGameState();

        if (state != GameManager.GameState.WAVE_ACTIVE) {
            context.getSource().sendFailure(Component.literal("§cAucune vague en cours ! État actuel: " + getStateName(state)));
            return 0;
        }

        int currentWave = WaveManager.getCurrentWave();
        int zombiesRemaining = WaveManager.getZombiesRemaining();

        // Annonce du skip
        GameManager.broadcastToAll(level, "§6§l[ADMIN] §eVague " + currentWave + " passée ! §7(" + zombiesRemaining + " zombies restants)");

        // Skip la vague
        WaveManager.skipWave(level);

        context.getSource().sendSuccess(() -> Component.literal("§aVague " + currentWave + " passée avec succès !"), true);

        return 1;
    }

    private static String getStateName(GameManager.GameState state) {
        return switch (state) {
            case WAITING -> "En attente";
            case STARTING -> "Démarrage (" + GameManager.getStartCountdownSeconds() + "s)";
            case WAVE_ACTIVE -> "Vague en cours";
            case WAVE_COOLDOWN -> "Entre vagues (" + WaveManager.getCountdownSeconds() + "s)";
        };
    }
}

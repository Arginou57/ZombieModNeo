package com.zombiemod.event;

import com.zombiemod.manager.GameManager;
import com.zombiemod.manager.PointsManager;
import com.zombiemod.map.DoorConfig;
import com.zombiemod.map.MapConfig;
import com.zombiemod.map.MapManager;
import com.zombiemod.system.ServerDoorTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class DoorInteractionHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSignInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        // Vérifier si c'est une pancarte murale
        if (!(state.getBlock() instanceof WallSignBlock)) {
            return;
        }

        // Vérifier si la partie est active (sinon les admins peuvent configurer librement)
        GameManager.GameState gameState = GameManager.getGameState();
        if (gameState != GameManager.GameState.WAVE_ACTIVE && gameState != GameManager.GameState.WAVE_COOLDOWN) {
            // Partie non active, ne pas bloquer l'interaction normale
            return;
        }

        // Chercher si ce panneau fait partie d'une porte
        MapConfig map = MapManager.getSelectedMap();
        if (map == null) {
            return;
        }

        DoorConfig door = null;
        for (DoorConfig d : map.getDoors().values()) {
            if (pos.equals(d.getSignPosition())) {
                door = d;
                break;
            }
        }

        // Si ce n'est pas une porte, laisser l'interaction normale
        if (door == null) {
            return;
        }

        // C'est une porte ! Annuler l'ouverture de l'interface du panneau
        event.setCanceled(true);

        if (!level.isClientSide) {
            // Vérifier si la porte est déjà ouverte
            if (door.isOpen()) {
                player.sendSystemMessage(Component.literal("§eCette porte est déjà ouverte !"));
                return;
            }

            // Vérifier si le joueur est actif
            if (!GameManager.isPlayerActive(player.getUUID())) {
                player.sendSystemMessage(Component.literal("§cVous devez être dans la partie pour ouvrir cette porte ! §7(/zombiejoin)"));
                return;
            }

            int cost = door.getCost();
            int playerPoints = PointsManager.getPoints(player.getUUID());

            if (playerPoints < cost) {
                player.sendSystemMessage(Component.literal("§c✖ Pas assez de points ! §7(§e" + cost + " §7requis)"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0f, 0.8f);
                return;
            }

            // Retirer les points
            PointsManager.removePoints(player.getUUID(), cost);

            // Ouvrir physiquement la porte
            ServerLevel serverLevel = (ServerLevel) level;
            openDoorPhysically(serverLevel, door);

            // Marquer comme ouverte dans la config
            map.openDoor(door.getDoorNumber());
            MapManager.save();

            // Synchroniser avec tous les clients
            ServerDoorTracker.syncToAllPlayers();

            // Messages et effets
            player.sendSystemMessage(Component.literal("§a§l✓ Porte #" + door.getDoorNumber() + " ouverte !"));
            player.sendSystemMessage(Component.literal("§7Points restants: §e" + PointsManager.getPoints(player.getUUID())));

            level.playSound(null, pos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 1.0f, 0.8f);
            spawnParticles(serverLevel, pos);

            // Broadcast à tous les joueurs
            GameManager.broadcastToAll(serverLevel, "§6" + player.getName().getString() + " §ea ouvert la §6Porte #" + door.getDoorNumber() + " §e!");
        }
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

    private static void spawnParticles(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 30; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;
            double y = pos.getY() + 0.5 + level.random.nextDouble() * 2.0;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;

            level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0.1, 0, 0);
        }
    }
}

package com.zombiemod.system;

import com.zombiemod.ModSounds;
import com.zombiemod.manager.PointsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

public class JukeboxManager {

    /**
     * Vérifie si un jukebox est configuré comme jukebox zombie
     */
    public static boolean isZombieJukebox(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof JukeboxBlockEntity jukebox) {
            CompoundTag data = jukebox.getPersistentData();
            return data.getBoolean("IsZombieJukebox");
        }
        return false;
    }

    /**
     * Configure un jukebox comme jukebox zombie avec un prix
     */
    public static void setZombieJukebox(Level level, BlockPos pos, int cost) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof JukeboxBlockEntity jukebox) {
            CompoundTag data = jukebox.getPersistentData();
            data.putBoolean("IsZombieJukebox", true);
            data.putInt("JukeboxCost", cost);
            jukebox.setChanged();

            // Synchroniser avec le tracker serveur si on est côté serveur
            if (!level.isClientSide()) {
                ServerJukeboxTracker.addJukebox(pos, cost);
            }
        }
    }

    /**
     * Récupère le coût du jukebox
     */
    public static int getJukeboxCost(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof JukeboxBlockEntity jukebox) {
            CompoundTag data = jukebox.getPersistentData();
            return data.getInt("JukeboxCost");
        }
        return 0;
    }

    /**
     * Vérifie si le jukebox est en train de jouer
     */
    public static boolean isPlaying(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof JukeboxBlockEntity jukebox) {
            CompoundTag data = jukebox.getPersistentData();
            return data.getBoolean("IsPlaying");
        }
        return false;
    }

    /**
     * Définit l'état de lecture du jukebox
     */
    private static void setPlaying(Level level, BlockPos pos, boolean playing) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof JukeboxBlockEntity jukebox) {
            CompoundTag data = jukebox.getPersistentData();
            data.putBoolean("IsPlaying", playing);
            jukebox.setChanged();
        }
    }

    /**
     * Gère l'interaction d'un joueur avec un jukebox zombie
     * Retourne true si l'interaction doit être annulée
     */
    public static boolean handleJukeboxInteraction(ServerPlayer player, BlockPos pos) {
        Level level = player.level();

        if (!isZombieJukebox(level, pos)) {
            return false; // Pas un jukebox zombie, laisser le comportement normal
        }

        // Vérifier si le jukebox est déjà en train de jouer
        if (isPlaying(level, pos)) {
            // Arrêter la musique pour tous les joueurs proches (dans un rayon de 64 blocs)
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                if (p.level() == level && p.blockPosition().distSqr(pos) < 64 * 64) {
                    p.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                        ModSounds.SONG_115.get().getLocation(), SoundSource.RECORDS));
                }
            }

            setPlaying(level, pos, false);

            // Message de confirmation
            player.displayClientMessage(Component.literal(
                "§c♪ Musique arrêtée !"), false);

            System.out.println("[JukeboxManager] Joueur " + player.getName().getString() +
                " a arrêté la musique du jukebox à " + pos);

            return true; // Annuler l'interaction normale du jukebox
        }

        // La musique n'est pas en train de jouer, vérifier les points
        int cost = getJukeboxCost(level, pos);
        int playerPoints = PointsManager.getPoints(player.getUUID());

        if (playerPoints < cost) {
            player.displayClientMessage(Component.literal(
                "§cPoints insuffisants ! §7(Coût: §e" + cost + "§7, Vous avez: §e" + playerPoints + "§7)"), true);
            return true; // Annuler l'interaction
        }

        // Déduire les points
        PointsManager.removePoints(player.getUUID(), cost);

        // Jouer le son 115
        level.playSound(null, pos, ModSounds.SONG_115.get(), SoundSource.RECORDS, 1.0f, 1.0f);

        // Marquer comme en train de jouer
        setPlaying(level, pos, true);

        // Message de confirmation
        player.displayClientMessage(Component.literal(
            "§a♪ Musique lancée ! §7(-" + cost + " points)"), false);

        System.out.println("[JukeboxManager] Joueur " + player.getName().getString() +
            " a activé le jukebox à " + pos + " pour " + cost + " points");

        return true; // Annuler l'interaction normale du jukebox
    }

    /**
     * Supprime la configuration zombie d'un jukebox
     */
    public static void removeZombieJukebox(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof JukeboxBlockEntity jukebox) {
            CompoundTag data = jukebox.getPersistentData();
            data.remove("IsZombieJukebox");
            data.remove("JukeboxCost");
            data.remove("IsPlaying");
            jukebox.setChanged();

            // Synchroniser avec le tracker serveur si on est côté serveur
            if (!level.isClientSide()) {
                ServerJukeboxTracker.removeJukebox(pos);
            }
        }
    }
}

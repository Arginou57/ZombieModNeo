package com.zombiemod.event;

import com.zombiemod.system.JukeboxManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.JukeboxBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class JukeboxInteractionHandler {

    @SubscribeEvent
    public static void onJukeboxInteract(PlayerInteractEvent.RightClickBlock event) {
        // Vérifier que c'est côté serveur et que le joueur est un ServerPlayer
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Vérifier que c'est un jukebox
        BlockPos pos = event.getPos();
        if (!(event.getLevel().getBlockState(pos).getBlock() instanceof JukeboxBlock)) {
            return;
        }

        // Vérifier que c'est la main principale pour éviter le double événement
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        // Vérifier si c'est un jukebox zombie et gérer l'interaction
        if (JukeboxManager.isZombieJukebox(event.getLevel(), pos)) {
            boolean cancelled = JukeboxManager.handleJukeboxInteraction(player, pos);
            if (cancelled) {
                event.setCanceled(true); // Annuler l'interaction normale du jukebox
            }
        }
    }
}

package com.zombiemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zombiemod.system.JukeboxManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ZombieJukeboxCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /zombiejukebox <cost>
        dispatcher.register(Commands.literal("zombiejukebox")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("cost", IntegerArgumentType.integer(0))
                        .executes(ZombieJukeboxCommand::createJukebox)));

        // /zombiejukebox remove
        dispatcher.register(Commands.literal("zombiejukebox")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("remove")
                        .executes(ZombieJukeboxCommand::removeJukebox)));
    }

    private static int createJukebox(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        int cost = IntegerArgumentType.getInteger(context, "cost");
        BlockPos jukeboxPos = getTargetedJukebox(player);

        if (jukeboxPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un jukebox !"));
            return 0;
        }

        JukeboxManager.setZombieJukebox(player.level(), jukeboxPos, cost);
        player.sendSystemMessage(Component.literal("§aJukebox zombie créé avec un coût de §e" + cost + " points§a."));
        player.sendSystemMessage(Component.literal("§7Les joueurs pourront activer la musique en cliquant dessus."));

        return 1;
    }

    private static int removeJukebox(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        BlockPos jukeboxPos = getTargetedJukebox(player);

        if (jukeboxPos == null) {
            player.sendSystemMessage(Component.literal("§cVous devez regarder un jukebox !"));
            return 0;
        }

        if (!JukeboxManager.isZombieJukebox(player.level(), jukeboxPos)) {
            player.sendSystemMessage(Component.literal("§cCe jukebox n'est pas un jukebox zombie !"));
            return 0;
        }

        JukeboxManager.removeZombieJukebox(player.level(), jukeboxPos);
        player.sendSystemMessage(Component.literal("§aJukebox zombie supprimé."));

        return 1;
    }

    private static BlockPos getTargetedJukebox(ServerPlayer player) {
        HitResult hit = player.pick(5.0, 0, false);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();

            if (player.level().getBlockState(pos).getBlock() instanceof JukeboxBlock) {
                return pos;
            }
        }

        return null;
    }
}

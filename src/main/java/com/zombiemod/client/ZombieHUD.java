package com.zombiemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zombiemod.manager.GameManager;
import com.zombiemod.network.packet.GameSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

public class ZombieHUD {

    @SubscribeEvent
    public static void onRenderHUD(RenderGuiEvent.Post event) {
        GuiGraphics graphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        Font font = mc.font;
        GameManager.GameState state = ClientGameData.getGameState();

        // Countdown démarrage (60s)
        if (state == GameManager.GameState.STARTING) {
            int seconds = ClientGameData.getStartCountdownSeconds();
            String text = "§6§lPartie dans " + seconds + "s";
            int x = (graphics.guiWidth() - font.width(text)) / 2;
            graphics.drawString(font, text, x, 50, 0xFFFFFF);

            String joinText = "§eTapez §6/zombiejoin §epour rejoindre";
            int x2 = (graphics.guiWidth() - font.width(joinText)) / 2;
            graphics.drawString(font, joinText, x2, 70, 0xFFFFFF);
            return;
        }

        // Si le joueur n'est pas dans la partie, ne rien afficher
        if (!ClientGameData.isLocalPlayerActive() && !ClientGameData.isLocalPlayerWaiting()) {
            return;
        }

        // Vague
        graphics.drawString(font, "§6Vague: §e" + ClientGameData.getCurrentWave(), 10, 10, 0xFFFFFF);

        // Zombies
        graphics.drawString(font, "§cZombies: §e" + ClientGameData.getZombiesRemaining(), 10, 20, 0xFFFFFF);

        // Points du joueur local (seulement si actif)
        if (ClientGameData.isLocalPlayerActive()) {
            graphics.drawString(font, "§6Points: §e" + ClientGameData.getLocalPlayerPoints(), 10, 30, 0xFFFFFF);

            // Afficher les animations de points flottants
            renderPointsAnimations(graphics, font);
        }

        // Countdown entre vagues (10s)
        if (state == GameManager.GameState.WAVE_COOLDOWN) {
            int seconds = ClientGameData.getWaveCountdownSeconds();
            String text = "§eProchaine vague dans: §6§l" + seconds;
            int x = (graphics.guiWidth() - font.width(text)) / 2;
            graphics.drawString(font, text, x, 50, 0xFFFFFF);
        }

        // Message pour joueurs en attente
        if (ClientGameData.isLocalPlayerWaiting()) {
            String text = "§eEn attente... §7(§c" + ClientGameData.getZombiesRemaining() + " §7zombies)";
            int x = (graphics.guiWidth() - font.width(text)) / 2;
            graphics.drawString(font, text, x, 90, 0xFFFFFF);
        }

        // Scoreboard - Afficher tous les joueurs et leurs points
        if (state == GameManager.GameState.WAVE_ACTIVE || state == GameManager.GameState.WAVE_COOLDOWN) {
            List<GameSyncPacket.PlayerData> players = ClientGameData.getActivePlayers();
            if (!players.isEmpty()) {
                // Trier par points (décroissant)
                players.sort((a, b) -> Integer.compare(b.points(), a.points()));

                int startY = 10;
                int rightX = graphics.guiWidth() - 10;

                // Titre
                String title = "§6§lJOUEURS";
                int titleWidth = font.width(title);
                graphics.drawString(font, title, rightX - titleWidth, startY, 0xFFFFFF);

                // Liste des joueurs
                int y = startY + 12;
                for (GameSyncPacket.PlayerData playerData : players) {
                    String playerText = "§f" + playerData.name() + ": §e" + playerData.points();
                    int textWidth = font.width(playerText);
                    graphics.drawString(font, playerText, rightX - textWidth, y, 0xFFFFFF);
                    y += 10;
                }
            }
        }

        // Afficher le prix de la caisse si le joueur la regarde (seulement si actif)
        if (ClientGameData.isLocalPlayerActive()) {
            renderWeaponCrateInfo(graphics, font, mc, player);
        }
    }

    private static void renderPointsAnimations(GuiGraphics graphics, Font font) {
        PoseStack poseStack = graphics.pose();
        List<PointsAnimationManager.FloatingPoints> animations = PointsAnimationManager.getAnimations();

        for (PointsAnimationManager.FloatingPoints anim : animations) {
            poseStack.pushPose();

            // Position de base : à côté du compteur de points (10, 30)
            float baseX = 80.0f;
            float baseY = 30.0f;

            // Appliquer le déplacement
            float x = baseX + anim.getX();
            float y = baseY + anim.getY();

            // Appliquer le scale
            float scale = anim.getScale();
            poseStack.translate(x, y, 0);
            poseStack.scale(scale, scale, 1.0f);

            // Calculer la couleur avec alpha
            float alpha = anim.getAlpha();
            int alphaInt = (int) (alpha * 255);
            int color = (alphaInt << 24) | 0xFFFFFF;

            // Afficher le texte
            String text = anim.getColor() + "+" + anim.getPoints();
            graphics.drawString(font, text, 0, 0, color);

            poseStack.popPose();
        }
    }

    private static void renderWeaponCrateInfo(GuiGraphics graphics, Font font, Minecraft mc, Player player) {
        // Raycasting pour détecter le bloc regardé
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(5.0)); // Distance de 5 blocs

        ClipContext context = new ClipContext(
            eyePos,
            endPos,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            player
        );

        BlockHitResult hitResult = player.level().clip(context);

        // Vérifier si on regarde un bloc
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos lookingAt = hitResult.getBlockPos();

        // Vérifier si c'est un coffre
        if (!(player.level().getBlockState(lookingAt).getBlock() instanceof ChestBlock)) {
            return;
        }

        // Vérifier si c'est une weapon crate (utiliser le cache client)
        if (!ClientWeaponCrateData.isWeaponCrate(lookingAt)) {
            return;
        }

        // Récupérer le coût (depuis le cache client)
        int cost = ClientWeaponCrateData.getCost(lookingAt);

        // Position au-dessus de la hotbar (centré)
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // Position Y : au-dessus de la hotbar (hotbar est à screenHeight - 40 environ)
        int baseY = screenHeight - 70;

        // Afficher le prix
        String priceText = "§6§l" + cost + " Points";
        int priceWidth = font.width(priceText);
        int priceX = (screenWidth - priceWidth) / 2;
        graphics.drawString(font, priceText, priceX, baseY, 0xFFFFFF);

        // Afficher "Ouvrir caisse" en dessous
        String actionText = "§eOuvrir Caisse";
        int actionWidth = font.width(actionText);
        int actionX = (screenWidth - actionWidth) / 2;
        graphics.drawString(font, actionText, actionX, baseY + 12, 0xFFFFFF);
    }
}

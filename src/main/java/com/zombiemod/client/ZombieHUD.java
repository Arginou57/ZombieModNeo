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
import net.minecraft.world.level.block.JukeboxBlock;
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
        }

        // IMPORTANT: Afficher l'info des weapon crates, jukeboxes et portes AVANT de vérifier si le joueur est dans la partie
        // (permet aux admins de voir le prix même hors partie)
        renderWeaponCrateInfo(graphics, font, mc, player);
        renderJukeboxInfo(graphics, font, mc, player);
        renderDoorInfo(graphics, font, mc, player);

        // Si le joueur n'est pas dans la partie, ne rien afficher d'autre
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

        // DEBUG: Log quand on regarde un coffre
        System.out.println("[ZombieHUD] Regardant coffre à: " + lookingAt);

        // Vérifier si c'est une weapon crate (utiliser le cache client)
        boolean isWeaponCrate = ClientWeaponCrateData.isWeaponCrate(lookingAt);
        System.out.println("[ZombieHUD] isWeaponCrate: " + isWeaponCrate);

        if (!isWeaponCrate) {
            return;
        }

        // Récupérer le coût (depuis le cache client)
        int cost = ClientWeaponCrateData.getCost(lookingAt);
        System.out.println("[ZombieHUD] Coût: " + cost);

        // Récupérer les munitions
        net.minecraft.nbt.ListTag ammo = ClientWeaponCrateData.getAmmo(lookingAt);
        System.out.println("[ZombieHUD] Munitions: " + ammo.size());

        // Position au-dessus de la hotbar (centré)
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // Position Y : bien au-dessus de la hotbar et de la barre d'expérience
        // (hotbar est à screenHeight - 40, barre d'expérience à screenHeight - 32)
        int baseY = screenHeight - 110;

        // TOUJOURS afficher la section arme en premier
        String weaponLine = "§6§lArme §7- §ePrix: §6" + cost + " points";
        int weaponLineWidth = font.width(weaponLine);
        int weaponLineX = (screenWidth - weaponLineWidth) / 2;
        graphics.drawString(font, weaponLine, weaponLineX, baseY, 0xFFFFFF);
        baseY += 10;

        String weaponAction = "§7Clique §edroit";
        int weaponActionWidth = font.width(weaponAction);
        int weaponActionX = (screenWidth - weaponActionWidth) / 2;
        graphics.drawString(font, weaponAction, weaponActionX, baseY, 0xFFFFFF);
        baseY += 15; // Espace entre les sections

        // Si des munitions sont disponibles, afficher la section munitions
        if (!ammo.isEmpty()) {
            // Calculer quantité totale et prix total
            int totalQuantity = 0;
            int totalCost = 0;
            for (int i = 0; i < ammo.size(); i++) {
                net.minecraft.nbt.CompoundTag ammoTag = ammo.getCompound(i);
                int count = ammoTag.getInt("Count");
                int prix = ammoTag.getInt("Prix");
                totalQuantity += count;
                totalCost += prix;
            }

            String ammoLine = "§6§lMunitions §fx" + totalQuantity + " §7- §ePrix: §6" + totalCost + " points";
            int ammoLineWidth = font.width(ammoLine);
            int ammoLineX = (screenWidth - ammoLineWidth) / 2;
            graphics.drawString(font, ammoLine, ammoLineX, baseY, 0xFFFFFF);
            baseY += 10;

            String ammoAction = "§7Clique §agauche";
            int ammoActionWidth = font.width(ammoAction);
            int ammoActionX = (screenWidth - ammoActionWidth) / 2;
            graphics.drawString(font, ammoAction, ammoActionX, baseY, 0xFFFFFF);
        }
    }

    private static void renderJukeboxInfo(GuiGraphics graphics, Font font, Minecraft mc, Player player) {
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

        // Vérifier si c'est un jukebox
        if (!(player.level().getBlockState(lookingAt).getBlock() instanceof JukeboxBlock)) {
            return;
        }

        // DEBUG: Log quand on regarde un jukebox
        System.out.println("[ZombieHUD] Regardant jukebox à: " + lookingAt);

        // Vérifier si c'est un jukebox zombie (utiliser le cache client)
        boolean isZombieJukebox = ClientJukeboxData.isJukebox(lookingAt);
        System.out.println("[ZombieHUD] isZombieJukebox: " + isZombieJukebox);

        if (!isZombieJukebox) {
            return;
        }

        // Récupérer le coût (depuis le cache client)
        int cost = ClientJukeboxData.getCost(lookingAt);
        System.out.println("[ZombieHUD] Coût: " + cost);

        // Position au-dessus de la hotbar (centré)
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // Position Y : bien au-dessus de la hotbar et de la barre d'expérience
        // (hotbar est à screenHeight - 40, barre d'expérience à screenHeight - 32)
        int baseY = screenHeight - 90;

        // Afficher le prix
        String priceText = "§6§l" + cost + " Points";
        int priceWidth = font.width(priceText);
        int priceX = (screenWidth - priceWidth) / 2;
        graphics.drawString(font, priceText, priceX, baseY, 0xFFFFFF);

        // Afficher "♪ Activer Musique" en dessous
        String actionText = "§e♪ Activer Musique";
        int actionWidth = font.width(actionText);
        int actionX = (screenWidth - actionWidth) / 2;
        graphics.drawString(font, actionText, actionX, baseY + 12, 0xFFFFFF);
    }

    private static void renderDoorInfo(GuiGraphics graphics, Font font, Minecraft mc, Player player) {
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

        // Vérifier si c'est un panneau mural
        if (!(player.level().getBlockState(lookingAt).getBlock() instanceof net.minecraft.world.level.block.WallSignBlock)) {
            return;
        }

        // DEBUG: Log quand on regarde un panneau
        System.out.println("[ZombieHUD] Regardant panneau à: " + lookingAt);

        // Vérifier si c'est une porte (utiliser le cache client)
        boolean isDoor = ClientDoorData.isDoor(lookingAt);
        System.out.println("[ZombieHUD] isDoor: " + isDoor);

        if (!isDoor) {
            return;
        }

        // Récupérer les données de la porte
        int doorNumber = ClientDoorData.getDoorNumber(lookingAt);
        int cost = ClientDoorData.getCost(lookingAt);
        boolean isOpen = ClientDoorData.isOpen(lookingAt);
        System.out.println("[ZombieHUD] Porte #" + doorNumber + ", Coût: " + cost + ", Ouverte: " + isOpen);

        // Position au-dessus de la hotbar (centré)
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // Position Y : bien au-dessus de la hotbar et de la barre d'expérience
        int baseY = screenHeight - 100;

        if (isOpen) {
            // Porte déjà ouverte
            String statusText = "§a§lPorte #" + doorNumber + " - OUVERTE";
            int statusWidth = font.width(statusText);
            int statusX = (screenWidth - statusWidth) / 2;
            graphics.drawString(font, statusText, statusX, baseY, 0xFFFFFF);
        } else {
            // Porte fermée - afficher le prix et l'action
            String priceText = "§6§lPorte #" + doorNumber + " §7- §ePrix: §6" + cost + " points";
            int priceWidth = font.width(priceText);
            int priceX = (screenWidth - priceWidth) / 2;
            graphics.drawString(font, priceText, priceX, baseY, 0xFFFFFF);

            String actionText = "§7Clique §edroit §7pour ouvrir";
            int actionWidth = font.width(actionText);
            int actionX = (screenWidth - actionWidth) / 2;
            graphics.drawString(font, actionText, actionX, baseY + 12, 0xFFFFFF);
        }
    }
}

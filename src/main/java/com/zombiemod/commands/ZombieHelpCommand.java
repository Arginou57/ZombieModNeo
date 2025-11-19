package com.zombiemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ZombieHelpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zombiehelp")
                .requires(source -> source.hasPermission(2))
                .executes(ZombieHelpCommand::showHelp));
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("§6§l============ ZOMBIE MODE - AIDE ============"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // GESTION DE PARTIE
        source.sendSuccess(() -> Component.literal("§e§l► GESTION DE PARTIE:"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiestart [mapName] §7- Démarre une partie"), false);
        source.sendSuccess(() -> Component.literal("  §7Compte à rebours de 60s, optionnel: spécifier une map"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/zombiestart §7ou §f/zombiestart arena1"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiestop §7- Arrête la partie en cours"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiejoin §7- Rejoindre la partie"), false);
        source.sendSuccess(() -> Component.literal("§a/zombieleave §7- Quitter la partie"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiestatus §7- Voir le statut de la partie"), false);
        source.sendSuccess(() -> Component.literal("§a/zombieskip §7- Passer à la vague suivante (admin)"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // GESTION DES MAPS
        source.sendSuccess(() -> Component.literal("§e§l► GESTION DES MAPS:"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap create <nom> §7- Créer une nouvelle map"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap delete <nom> §7- Supprimer une map"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap select <nom> §7- Sélectionner une map active"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap list §7- Lister toutes les maps"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap info [nom] §7- Infos sur une map"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // CONFIGURATION: SPAWN POINTS
        source.sendSuccess(() -> Component.literal("§e§l► POINTS DE SPAWN:"), false);
        source.sendSuccess(() -> Component.literal("§a/zombierespawn set <mapname> §7- Définir le respawn joueurs"), false);
        source.sendSuccess(() -> Component.literal("  §7À votre position actuelle"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiespawn add <mapname> [doorNumber] §7- Ajouter spawn zombie"), false);
        source.sendSuccess(() -> Component.literal("  §7Sans doorNumber: spawn toujours actif"), false);
        source.sendSuccess(() -> Component.literal("  §7Avec doorNumber: actif seulement si porte ouverte"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/zombiespawn add arena1 §7ou §f/zombiespawn add arena1 1"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiespawn clear <mapname> §7- Effacer tous les spawns zombies"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiespawn list <mapname> §7- Lister les spawns zombies"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // CONFIGURATION: PORTES
        source.sendSuccess(() -> Component.literal("§e§l► PORTES:"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiedoor add <mapname> <numéro> <coût>"), false);
        source.sendSuccess(() -> Component.literal("  §7Regardez une pancarte murale, puis tapez la commande"), false);
        source.sendSuccess(() -> Component.literal("  §7Sauvegarde la pancarte + mur 3x3 derrière"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/zombiedoor add arena1 1 750"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiedoor remove <mapname> <numéro> §7- Supprimer une porte"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiedoor list <mapname> §7- Lister toutes les portes"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiedoor open <mapname> <numéro> §7- Ouvrir (détruit blocs)"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiedoor close <mapname> <numéro> §7- Fermer (remet blocs)"), false);
        source.sendSuccess(() -> Component.literal("  §7Les portes se ferment automatiquement en fin de partie"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // CAISSES D'ARMES
        source.sendSuccess(() -> Component.literal("§e§l► CAISSES D'ARMES (Mystery Box):"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate add <coût> <itemId> §7- Créer une caisse"), false);
        source.sendSuccess(() -> Component.literal("  §7Regardez un coffre, puis tapez la commande"), false);
        source.sendSuccess(() -> Component.literal("  §7itemId: l'arme dans votre main (§ftacz:xx§7 ou §fminecraft:xx§7)"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/weaponcrate add 500 tacz:ak47"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate addammo <itemId> <qté> <coût>"), false);
        source.sendSuccess(() -> Component.literal("  §7Ajouter munitions (achat clic gauche)"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/weaponcrate addammo tacz:ammo_9mm 30 100"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate remove §7- Supprimer une caisse"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate scan §7- Scanner toutes les caisses du monde"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate reload §7- Recharger affichages des caisses"), false);
        source.sendSuccess(() -> Component.literal("  §7Utile après avoir supprimé toutes les entités"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // JUKEBOXES
        source.sendSuccess(() -> Component.literal("§e§l► JUKEBOXES (Musique):"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiejukebox add <coût> §7- Créer un jukebox zombie"), false);
        source.sendSuccess(() -> Component.literal("  §7Regardez un jukebox avec un disque, puis tapez"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/zombiejukebox add 1000"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiejukebox remove §7- Supprimer un jukebox zombie"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiejukebox list §7- Lister tous les jukeboxes"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // INFORMATIONS
        source.sendSuccess(() -> Component.literal("§e§l► MÉCANIQUES DU JEU:"), false);
        source.sendSuccess(() -> Component.literal("§7• Points de départ: §e500 points"), false);
        source.sendSuccess(() -> Component.literal("§7• Kill zombie: §e+100 points"), false);
        source.sendSuccess(() -> Component.literal("§7• Zombies par vague: §c6 + (vague × 6)"), false);
        source.sendSuccess(() -> Component.literal("§7• HP zombies: §c1 cœur + 0.5 cœur/vague"), false);
        source.sendSuccess(() -> Component.literal("§7• 15% chance zombie avec armure"), false);
        source.sendSuccess(() -> Component.literal("§7• Cooldown entre vagues: §610 secondes"), false);
        source.sendSuccess(() -> Component.literal("§7• Joueurs morts: §erespawn fin de vague"), false);
        source.sendSuccess(() -> Component.literal("§7• Portes: §eactivent nouveaux spawns zombies"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // FICHIERS
        source.sendSuccess(() -> Component.literal("§e§l► FICHIERS DE CONFIG:"), false);
        source.sendSuccess(() -> Component.literal("§7Dossier: §fconfig/"), false);
        source.sendSuccess(() -> Component.literal("  §f• zombiemod.json §7- Configuration gameplay"), false);
        source.sendSuccess(() -> Component.literal("  §f• zombiemod-maps.json §7- Sauvegarde des maps"), false);
        source.sendSuccess(() -> Component.literal("  §f• zombiemod-drops.json §7- Configuration des drops"), false);
        source.sendSuccess(() -> Component.literal("  §f• zombiemod-mobs.json §7- Configuration des mobs"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        source.sendSuccess(() -> Component.literal("§6§l============================================"), false);

        return 1;
    }
}

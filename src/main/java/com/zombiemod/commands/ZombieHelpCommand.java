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

        source.sendSuccess(() -> Component.literal("§6§l========== ZOMBIE MODE - AIDE =========="), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // GESTION DE PARTIE
        source.sendSuccess(() -> Component.literal("§e§lGESTION DE PARTIE:"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiestart §7- Démarre une partie (compte à rebours de 60s)"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiestop §7- Arrête la partie en cours"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiejoin §7- Rejoindre la partie"), false);
        source.sendSuccess(() -> Component.literal("§a/zombieleave §7- Quitter la partie"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // GESTION DES MAPS
        source.sendSuccess(() -> Component.literal("§e§lGESTION DES MAPS:"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap create <nom> §7- Créer une nouvelle map"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/zombiemap create arena1"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap list §7- Liste toutes les maps"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap select <nom> §7- Sélectionner une map active"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/zombiemap select arena1"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap delete <nom> §7- Supprimer une map"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiemap info [nom] §7- Infos sur une map (ou la map active)"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // CONFIGURATION DES MAPS
        source.sendSuccess(() -> Component.literal("§e§lCONFIGURATION DES MAPS:"), false);
        source.sendSuccess(() -> Component.literal("§a/respawnpoint <map> §7- Définir le point de respawn pour une map"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/respawnpoint arena1"), false);
        source.sendSuccess(() -> Component.literal("  §7Vous serez téléporté à la position définie"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiespawn <map> §7- Ajouter un point de spawn zombie pour une map"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/zombiespawn arena1"), false);
        source.sendSuccess(() -> Component.literal("  §7Utilisez plusieurs fois pour créer plusieurs points"), false);
        source.sendSuccess(() -> Component.literal("§a/zombiespawn clear <map> §7- Effacer tous les points de spawn d'une map"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // CAISSES D'ARMES
        source.sendSuccess(() -> Component.literal("§e§lCAISSES D'ARMES:"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate create §7- Créer une caisse vide"), false);
        source.sendSuccess(() -> Component.literal("  §7Clic droit sur un double coffre puis tapez la commande"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate preset <type> <prix> §7- Créer une caisse prédéfinie"), false);
        source.sendSuccess(() -> Component.literal("  §7Types: §fstarter§7, §fadvanced§7, §flegendary"), false);
        source.sendSuccess(() -> Component.literal("  §7Exemple: §f/weaponcrate preset starter 500"), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate addweapon <item> [count] [weight] [name]"), false);
        source.sendSuccess(() -> Component.literal("  §7Formats simples:"), false);
        source.sendSuccess(() -> Component.literal("  §f/weaponcrate addweapon minecraft:wooden_sword"), false);
        source.sendSuccess(() -> Component.literal("  §f/weaponcrate addweapon minecraft:arrow 64"), false);
        source.sendSuccess(() -> Component.literal("  §f/weaponcrate addweapon minecraft:diamond_sword 1 50"), false);
        source.sendSuccess(() -> Component.literal("  §f/weaponcrate addweapon minecraft:bow 1 30 \"§6Arc Magique\""), false);
        source.sendSuccess(() -> Component.literal("§a/weaponcrate info §7- Voir le contenu de la caisse"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // SYSTÈME DE POINTS
        source.sendSuccess(() -> Component.literal("§e§lPOINTS:"), false);
        source.sendSuccess(() -> Component.literal("§a/points add <joueur> <montant> §7- Ajouter des points"), false);
        source.sendSuccess(() -> Component.literal("§a/points remove <joueur> <montant> §7- Retirer des points"), false);
        source.sendSuccess(() -> Component.literal("§a/points set <joueur> <montant> §7- Définir les points"), false);
        source.sendSuccess(() -> Component.literal("§a/points get <joueur> §7- Voir les points d'un joueur"), false);
        source.sendSuccess(() -> Component.literal("§7Info: Chaque kill de zombie donne §e100 points"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // INFORMATIONS
        source.sendSuccess(() -> Component.literal("§e§lINFORMATIONS:"), false);
        source.sendSuccess(() -> Component.literal("§7• Les joueurs démarrent avec §e500 points"), false);
        source.sendSuccess(() -> Component.literal("§7• Chaque vague contient §c6 + (vague × 6) zombies"), false);
        source.sendSuccess(() -> Component.literal("§7• HP des zombies: §c1 cœur + 0.5 cœur/vague"), false);
        source.sendSuccess(() -> Component.literal("§7• 15% de chance de zombie avec armure"), false);
        source.sendSuccess(() -> Component.literal("§7• Cooldown de §610s §7entre les vagues"), false);
        source.sendSuccess(() -> Component.literal("§7• Les joueurs morts respawn à la fin de la vague"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        // FICHIERS DE CONFIG
        source.sendSuccess(() -> Component.literal("§e§lCONFIGURATION:"), false);
        source.sendSuccess(() -> Component.literal("§7Fichiers dans §fconfig/§7:"), false);
        source.sendSuccess(() -> Component.literal("  §f• zombiemod.json §7- Config du gameplay"), false);
        source.sendSuccess(() -> Component.literal("  §f• zombiemod-maps.json §7- Sauvegarde des maps"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        source.sendSuccess(() -> Component.literal("§6§l========================================"), false);

        return 1;
    }
}

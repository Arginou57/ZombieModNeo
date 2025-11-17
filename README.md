# Zombie Mode - Mod Minecraft NeoForge 1.21.1

Un mod de survie zombie pour Minecraft avec système de vagues, points, caisses d'armes et respawn d'équipe.

## Fonctionnalités

### 🎮 Système de Jeu
- **Countdown de démarrage** : 60 secondes pour rejoindre avant le début
- **Vagues de zombies** : Difficulté croissante (6 + vague × 6 zombies)
- **Points** : 100 points par zombie tué, conservés après la mort
- **Système de respawn** : Respawn automatique à la fin de chaque vague
- **Game Over** : Quand tous les joueurs actifs sont morts

### 💀 Zombies
- Les 32 derniers zombies de chaque vague sont lumineux (effet Glowing)
- Spawn aléatoire sur des points configurables
- Compteur en temps réel des zombies restants

### 🎁 Caisses d'Armes
- **3 presets prédéfinis** : Starter (500pts), Advanced (1500pts), Legendary (5000pts)
- **Système de loot pondéré** : Armes rares plus difficiles à obtenir
- **Armes enchantées** : Du simple arc aux armes légendaires
- **Coffres vanilla** : Utilisez des double coffres pour les caisses

### 👥 Système de Joueurs
- **Joueurs actifs** : En jeu, peuvent gagner des points et acheter
- **Joueurs en attente** : Rejoignent à la fin de la vague en cours
- **Spectateurs** : Les morts attendent la fin de vague en mode spectateur

## Commandes

### Commandes de Jeu
- `/zombiestart` - **(Admin)** Démarre une partie avec countdown 60s
- `/zombiejoin` - Rejoindre la partie en cours
- `/zombieleave` - Quitter la partie
- `/zombiestatus` - Afficher les infos (vague, joueurs, points)

### Configuration
- `/zombiespawn` - **(Admin)** Ajouter un point de spawn de zombies
- `/zombiespawn list` - **(Admin)** Lister tous les points de spawn
- `/zombiespawn clear` - **(Admin)** Supprimer tous les points de spawn
- `/zombierespawn` - **(Admin)** Définir le point de respawn d'équipe
- `/zombierespawn show` - **(Admin)** Afficher le point de respawn

### Caisses d'Armes
- `/weaponcrate create <cost>` - **(Admin)** Créer une caisse (regarder un coffre)
- `/weaponcrate addweapon <item> <count> <weight> <name>` - **(Admin)** Ajouter une arme custom
- `/weaponcrate preset starter` - **(Admin)** Créer caisse Starter (500pts)
- `/weaponcrate preset advanced` - **(Admin)** Créer caisse Advanced (1500pts)
- `/weaponcrate preset legendary` - **(Admin)** Créer caisse Legendary (5000pts)

## Installation

1. **Pré-requis** :
   - Minecraft 1.21.1
   - NeoForge 21.1.77+
   - Java 21

2. **Build le mod** :
   ```bash
   ./gradlew build
   ```
   Le fichier JAR sera dans `build/libs/`

3. **Installation** :
   - Placer le JAR dans le dossier `mods` de votre serveur/client NeoForge
   - Redémarrer le serveur/client

## Configuration d'une Partie

### 1. Préparer l'arène
1. Définir le point de respawn : `/zombierespawn`
2. Placer des points de spawn de zombies : `/zombiespawn` (minimum 3-4 points recommandés)
3. Placer des coffres et les transformer en caisses :
   ```
   /weaponcrate preset starter
   /weaponcrate preset advanced
   /weaponcrate preset legendary
   ```

### 2. Lancer la partie
1. Un admin lance : `/zombiestart`
2. Countdown de 60 secondes démarre
3. Les joueurs rejoignent : `/zombiejoin`
4. La vague 1 démarre automatiquement

### 3. Pendant la partie
- Tuez des zombies pour gagner des points
- Achetez des armes dans les caisses en cliquant dessus
- Survivez le plus longtemps possible !

## Presets de Caisses

### 🟢 Starter (500 points)
- Couteau de Survie (40%)
- Épée Basique (30%)
- Lame de Fer (20%)
- Arc Simple + Infinity (10%)

### 🔵 Advanced (1500 points)
- Épée Diamant + Sharpness III (30%)
- Arc Puissant + Power IV + Infinity (25%)
- Arbalète Rapide + Quick Charge III (25%)
- Trident + Loyalty III (20%)

### 🔴 Legendary (5000 points)
- **LAME INFERNALE** - Netherite Sword + Sharpness V + Fire Aspect II + Looting III (50%)
- **ARC DIVIN** - Bow + Power V + Flame + Infinity (30%)
- **TRIDENT DE POSÉIDON** - Trident + Loyalty III + Impaling V + Channeling (20%)

## HUD

Le HUD affiche en temps réel :
- **Vague actuelle** (haut gauche)
- **Zombies restants** (haut gauche)
- **Points du joueur** (haut gauche, si actif)
- **Countdown** entre vagues (centre écran)
- **Statut** (en attente, etc.)

## Comportements Importants

### Rejoindre en cours de partie
- **Pendant le countdown (60s)** : Rejoignez immédiatement en mode aventure
- **Pendant une vague** : Mode spectateur, rejoignez à la fin de la vague
- **Entre les vagues** : Rejoignez immédiatement en mode survie

### Mort et Respawn
- **Mort pendant une vague** : Mode spectateur jusqu'à la fin
- **Fin de vague** : Respawn automatique avec santé/faim restaurées
- **Conservation des points** : Les points ne sont jamais perdus

### Game Over
- Tous les joueurs actifs sont morts
- Affiche la vague atteinte
- Tous passent en spectateur
- Possibilité de recommencer avec `/zombiestart`

## Structure du Projet

```
src/main/java/com/zombiemod/
├── ZombieMod.java              # Classe principale
├── manager/
│   ├── GameManager.java        # Gestion états et joueurs
│   ├── WaveManager.java        # Gestion vagues et zombies
│   ├── PointsManager.java      # Gestion points
│   └── RespawnManager.java     # Gestion respawn
├── system/
│   └── WeaponCrateManager.java # Système de caisses
├── event/
│   ├── ZombieEventHandler.java
│   ├── ChestInteractionHandler.java
│   └── PlayerDeathHandler.java
├── command/
│   ├── GameCommands.java
│   ├── SpawnCommand.java
│   ├── RespawnCommand.java
│   └── WeaponCrateCommand.java
└── client/
    └── ZombieHUD.java          # Affichage HUD
```

## Développement

- **Minecraft** : 1.21.1
- **NeoForge** : 21.1.77
- **Java** : 21
- **Gradle** : 8.x

## Licence

All Rights Reserved

## Auteurs

ZombieMod Team

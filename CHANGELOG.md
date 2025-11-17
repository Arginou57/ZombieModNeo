# Changelog - Zombie Mode

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

---

## [1.0.1] - 2025-01-17

### ✨ Ajouté
- **Affichage HUD des caisses d'armes** : Quand un joueur regarde une weapon crate, le prix et l'action "Ouvrir Caisse" s'affichent au-dessus de la hotbar
- Détection par raycasting du bloc regardé (distance : 5 blocs)
- Interface utilisateur améliorée pour une meilleure expérience de jeu

### 🔧 Technique
- Ajout de la méthode `renderWeaponCrateInfo()` dans `ZombieHUD.java`
- Utilisation de `ClipContext` pour le raycasting côté client
- Affichage centré au-dessus de la hotbar (Y: screenHeight - 70)

---

## [1.0.0] - 2025-01-16

### 🎉 Release Initiale

#### ✨ Fonctionnalités Principales
- **Système de vagues** avec difficulté progressive infinie
- **Système de points** : 100 points par zombie tué
- **3 caisses d'armes** : Starter (500pts), Advanced (1500pts), Legendary (5000pts)
- **Respawn automatique** à la fin de chaque vague
- **Mode spectateur** pour les joueurs morts
- **HUD en temps réel** : vague, zombies restants, points, countdown

#### 🧟 Système de Zombies
- Spawn progressif optimisé (2s entre chaque)
- Limite de 32 zombies simultanés (configurable)
- HP progressifs par vague
- Vitesse augmentée selon la vague
- 15% chance de zombie avec armure
- Les 32 derniers zombies ont l'effet Glowing
- Support de différents types de mobs (zombies, squelettes, etc.)

#### ⚙️ Configuration
- `zombiemod.json` - Configuration générale
- `zombiedrops.json` - Configuration des drops
- `zombiemobs.json` - Types de mobs et vitesses
- Système de maps multiples

#### 🎮 Commandes
- `/zombiestart` - Démarrer une partie
- `/zombiejoin` - Rejoindre la partie
- `/zombieleave` - Quitter la partie
- `/zombiestatus` - Afficher les infos
- `/zombiespawn` - Gérer les points de spawn
- `/zombierespawn` - Définir le point de respawn
- `/weaponcrate` - Gérer les caisses d'armes
- `/zombiemap` - Gérer les maps

#### 🔧 Technique
- Minecraft 1.21.1
- NeoForge 21.1.77+
- Java 21
- Architecture modulaire (managers, events, commands, client)
- Synchronisation réseau client-serveur
- Système de packets custom

---

## Format

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/lang/fr/).

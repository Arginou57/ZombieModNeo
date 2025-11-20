# Zombie Mod - Survival Mode

## Description

**Zombie Mod** transforme votre serveur Minecraft en une expérience de survie intense inspirée des classiques du genre zombie. Affrontez des vagues infinies de zombies qui deviennent de plus en plus difficiles, gérez vos points pour acheter des armes et ouvrir de nouvelles zones, et survivez le plus longtemps possible avec vos amis !

Ce mod apporte une expérience complète de survie zombie avec un système de vagues, de points, de caisses d'armes mystérieuses (Mystery Box), de portes à acheter pour débloquer de nouvelles zones, et bien plus encore.

---

## Caractéristiques Principales

### 🧟 Système de Vagues
- Vagues infinies avec difficulté croissante
- Nombre de zombies : **6 + (vague × 6)**
- HP des zombies : **1 cœur + 0.5 cœur par vague**
- 15% de chance que les zombies portent une armure
- Cooldown de 10 secondes entre chaque vague

### 💰 Système de Points
- Points de départ : **500 points**
- Kill zombie : **+100 points**
- Utilisez vos points pour acheter des armes, des munitions, ouvrir des portes et activer la musique

### 🎁 Mystery Box (Caisses d'Armes)
- Caisses d'armes personnalisables avec animation de roulette
- Support des armes vanilla et moddées (compatible avec TACZ et autres mods d'armes)
- Achat de munitions au clique gauche
- Affichage 3D de l'arme au-dessus du coffre avec rotation automatique selon l'orientation
- Animation fluide lors de l'ouverture avec son immersif

### 🚪 Système de Portes
- Créez des portes à acheter pour débloquer de nouvelles zones
- Système de panneaux muraux avec HUD informatif
- Sauvegarde automatique des blocs (pancarte + mur 3×3 derrière)
- Restauration automatique à la fin de la partie
- Liez des points de spawn de zombies à des portes spécifiques

### 🎵 Jukeboxes Personnalisés
- Placez des jukeboxes payants pour activer la musique d'ambiance
- Personnalisez le coût d'activation
- Ajoutez vos propres disques musicaux

### 🗺️ Système de Maps
- Créez et gérez plusieurs maps de jeu
- Chaque map conserve ses propres configurations :
  - Points de spawn des joueurs
  - Points de spawn des zombies (liés ou non aux portes)
  - Portes avec leurs prix et positions
  - Caisses d'armes avec leurs configurations

### 👥 Mode Multijoueur
- Support complet du multijoueur
- Scoreboard en temps réel des joueurs et de leurs points
- Système de respawn : les joueurs morts reviennent à la fin de la vague
- Synchronisation client-serveur optimisée

---

## 📋 Commandes

### Gestion de Partie

| Commande | Description | Permission |
|----------|-------------|------------|
| `/zombiestart [mapName]` | Démarre une partie avec compte à rebours de 60s. Vous pouvez optionnellement spécifier une map. | OP (niveau 2) |
| `/zombiestop` | Arrête la partie en cours et réinitialise tout (portes fermées, animations arrêtées) | OP (niveau 2) |
| `/zombiejoin` | Rejoindre la partie en cours | Tous |
| `/zombieleave` | Quitter la partie en cours | Tous |
| `/zombiestatus` | Affiche le statut de la partie (vague, zombies, joueurs) | Tous |
| `/zombieskip` | Passe à la vague suivante immédiatement (pour les tests) | OP (niveau 2) |

**Exemples :**
```
/zombiestart
/zombiestart nacht_der_untoten
/zombiejoin
```

---

### Gestion des Maps

| Commande | Description |
|----------|-------------|
| `/zombiemap create <nom>` | Crée une nouvelle map |
| `/zombiemap delete <nom>` | Supprime une map existante |
| `/zombiemap select <nom>` | Sélectionne la map active pour la prochaine partie |
| `/zombiemap list` | Liste toutes les maps disponibles |
| `/zombiemap info [nom]` | Affiche les informations détaillées d'une map |

**Exemples :**
```
/zombiemap create nacht_der_untoten
/zombiemap select nacht_der_untoten
/zombiemap info nacht_der_untoten
```

---

### Configuration des Points de Spawn

#### Spawn des Joueurs
| Commande | Description |
|----------|-------------|
| `/zombierespawn set <mapname>` | Définit le point de respawn des joueurs à votre position actuelle |

#### Spawn des Zombies
| Commande | Description |
|----------|-------------|
| `/zombiespawn add <mapname> [doorNumber]` | Ajoute un point de spawn zombie à votre position.<br>**Sans doorNumber** : spawn toujours actif<br>**Avec doorNumber** : spawn actif uniquement si la porte est ouverte |
| `/zombiespawn clear <mapname>` | Efface tous les spawns zombies de la map |
| `/zombiespawn list <mapname>` | Liste tous les points de spawn avec leur statut |

**Exemples :**
```
/zombiespawn add nacht_der_untoten
/zombiespawn add nacht_der_untoten 1
/zombiespawn add nacht_der_untoten 2
```

---

### Système de Portes

| Commande | Description |
|----------|-------------|
| `/zombiedoor add <mapname> <numéro> <coût>` | **Regardez une pancarte murale**, puis tapez la commande. Sauvegarde la pancarte + le mur 3×3 derrière |
| `/zombiedoor remove <mapname> <numéro>` | Supprime une porte de la map |
| `/zombiedoor list <mapname>` | Liste toutes les portes de la map |
| `/zombiedoor open <mapname> <numéro>` | Ouvre une porte (détruit les blocs) |
| `/zombiedoor close <mapname> <numéro>` | Ferme une porte (remet les blocs sauvegardés) |

**Exemples :**
```
/zombiedoor add nacht_der_untoten 1 750
/zombiedoor add nacht_der_untoten 2 1250
/zombiedoor list nacht_der_untoten
```

**Note importante :** Les portes se ferment automatiquement à la fin de chaque partie !

---

### Caisses d'Armes (Mystery Box)

| Commande | Description |
|----------|-------------|
| `/weaponcrate add <coût> <itemId>` | **Regardez un coffre**, puis tapez la commande. Crée une caisse d'armes avec l'item spécifié.<br>L'itemId peut être vanilla (`minecraft:diamond_sword`) ou moddé (`tacz:ak47`) |
| `/weaponcrate addammo <itemId> <quantité> <coût>` | Ajoute des munitions achetables au clique gauche sur la caisse |
| `/weaponcrate remove` | **Regardez une caisse**, puis tapez la commande pour la supprimer |
| `/weaponcrate scan` | Scanne toutes les caisses du monde et affiche leur position |
| `/weaponcrate reload` | Recharge tous les affichages 3D des caisses (utile après avoir supprimé toutes les entités) |

**Exemples :**
```
/weaponcrate add 500 tacz:ak47
/weaponcrate add 950 tacz:m4a1
/weaponcrate addammo tacz:ammo_762x39 30 100
/weaponcrate addammo tacz:ammo_556x45 30 150
```

**Utilisation en jeu :**
- **Clique droit** : Acheter une arme aléatoire (animation de roulette si plusieurs armes)
- **Clique gauche** : Acheter toutes les munitions configurées

---

### Jukeboxes Musicaux

| Commande | Description |
|----------|-------------|
| `/zombiejukebox add <coût>` | **Regardez un jukebox contenant un disque**, puis tapez la commande |
| `/zombiejukebox remove` | **Regardez un jukebox zombie**, puis tapez la commande pour le supprimer |
| `/zombiejukebox list` | Liste tous les jukeboxes configurés dans le monde |

**Exemple :**
```
/zombiejukebox add 1000
```

---

### Aide

| Commande | Description |
|----------|-------------|
| `/zombiehelp` | Affiche l'aide complète avec toutes les commandes et mécaniques |

---

## 🎮 Mécaniques du Jeu

### Système de Vagues
1. La partie démarre après un compte à rebours de 60 secondes
2. Les joueurs commencent avec **500 points**
3. Chaque vague spawn **6 + (vague × 6)** zombies
4. Les zombies ont **1 cœur + 0.5 cœur par vague**
5. 15% de chance que les zombies portent une armure aléatoire
6. Entre chaque vague : cooldown de **10 secondes**

### Système de Points
- **+100 points** par zombie tué
- Les points sont utilisés pour :
  - Acheter des armes (coût variable selon la caisse)
  - Acheter des munitions (coût configuré par type)
  - Ouvrir des portes (coût configuré par porte)
  - Activer des jukeboxes (coût configuré par jukebox)

### Système de Mort et Respawn
- Quand un joueur meurt, il entre en mode **"En attente"**
- Il respawn automatiquement à la **fin de la vague en cours**
- Ses points sont conservés
- S'il n'y a plus aucun joueur vivant : **GAME OVER**

### Portes et Zones
- Les portes bloquent l'accès à de nouvelles zones
- En ouvrant une porte, vous **activez de nouveaux points de spawn de zombies**
- Cela augmente la difficulté mais offre plus d'espace pour manœuvrer
- Les portes se ferment automatiquement à la fin de la partie

### Mystery Box (Caisses d'Armes)
- Chaque coffre peut contenir **plusieurs armes**
- L'arme obtenue est **aléatoire** (roulette animée)
- Si une seule arme est configurée, pas d'animation (affichage statique)
- Les arcs et arbalètes donnent automatiquement **64 flèches**
- Affichage 3D de l'arme avec rotation adaptée à l'orientation du coffre

---

## 📁 Fichiers de Configuration

Tous les fichiers de configuration se trouvent dans le dossier **`config/`** :

| Fichier | Description |
|---------|-------------|
| `zombiemod.json` | Configuration générale du gameplay (points de départ, cooldowns, etc.) |
| `zombiemod-maps.json` | Sauvegarde de toutes les maps avec leurs configurations (spawns, portes, etc.) |
| `zombiemod-drops.json` | Configuration des drops des zombies |
| `zombiemod-mobs.json` | Configuration des mobs (HP, armure, etc.) |

---

## 🔧 Installation

1. **Installez NeoForge 1.21** sur votre serveur ou client
2. Téléchargez le mod **Zombie Mod** depuis CurseForge
3. Placez le fichier `.jar` dans le dossier **`mods/`**
4. Démarrez le serveur/client
5. Les fichiers de configuration seront générés automatiquement dans **`config/`**

### Mods Compatibles
- **TACZ (Timeless and Classics Zero)** : Support complet des armes
- Tout mod ajoutant des items personnalisés
- Plugins de protection (WorldGuard, GriefPrevention, etc.)

---

## 🎯 Guide de Démarrage Rapide

### Pour les Administrateurs

1. **Créez votre première map :**
   ```
   /zombiemap create ma_premiere_map
   /zombiemap select ma_premiere_map
   ```

2. **Configurez le spawn des joueurs :**
   ```
   (Placez-vous à l'endroit désiré)
   /zombierespawn set ma_premiere_map
   ```

3. **Ajoutez des spawns zombies :**
   ```
   (Placez-vous à chaque endroit)
   /zombiespawn add ma_premiere_map
   /zombiespawn add ma_premiere_map
   /zombiespawn add ma_premiere_map
   ```

4. **Créez une porte (optionnel) :**
   ```
   (Placez une pancarte murale devant un mur)
   (Regardez la pancarte)
   /zombiedoor add ma_premiere_map 1 750
   ```

5. **Ajoutez un spawn lié à la porte :**
   ```
   (Placez-vous dans la zone derrière la porte)
   /zombiespawn add ma_premiere_map 1
   ```

6. **Créez une caisse d'armes :**
   ```
   (Placez un coffre)
   (Regardez le coffre)
   /weaponcrate add 500 tacz:ak47
   /weaponcrate add 950 tacz:m4a1
   /weaponcrate addammo tacz:ammo_762x39 30 100
   ```

7. **Démarrez la partie :**
   ```
   /zombiestart ma_premiere_map
   ```

### Pour les Joueurs

1. Attendez qu'un administrateur démarre une partie avec `/zombiestart`
2. Rejoignez la partie avec `/zombiejoin`
3. Tuez des zombies pour gagner des points (+100 par kill)
4. Achetez des armes aux Mystery Box (clique droit sur les coffres)
5. Achetez des munitions (clique gauche sur les coffres)
6. Ouvrez des portes pour accéder à de nouvelles zones (clique droit sur les panneaux)
7. Survivez le plus longtemps possible !

---

## 🎨 HUD et Interface

### Affichage Principal (En Partie)
- **Vague actuelle** (coin supérieur gauche)
- **Zombies restants** (coin supérieur gauche)
- **Vos points** (coin supérieur gauche)
- **Scoreboard des joueurs** (coin supérieur droit)

### HUD Contextuel
Quand vous regardez un élément interactif, un HUD s'affiche au-dessus de votre hotbar :

- **Caisse d'armes** : Prix de l'arme (clique droit) + Prix des munitions (clique gauche)
- **Porte** : Numéro de la porte + Prix + "Clique droit pour ouvrir" (ou "OUVERTE" si déjà ouverte)
- **Jukebox** : Prix + "♪ Activer Musique"

### Animations
- **Mystery Box** : Animation de roulette avec son immersif (3 secondes)
- **Points flottants** : +100 apparaît à côté de votre compteur de points quand vous tuez un zombie
- **Particules** : Effets visuels lors des achats (HAPPY_VILLAGER + ENCHANT)

---

## 🛠️ Support et Contributions

**Bugs et Suggestions :** Veuillez signaler les bugs ou suggérer des améliorations sur la page GitHub ou CurseForge.

**Compatibilité :** Ce mod est conçu pour NeoForge 1.21 et est compatible avec la plupart des mods de contenu (armes, armures, etc.)

---

## 📜 Crédits

**Développé pour NeoForge 1.21**

Sons utilisés :
- Mystery Box sound (Call of Duty: Zombies)
- Round Start/End sounds (Call of Duty: Zombies)

---

## ⚖️ Licence

Ce mod est fourni tel quel pour un usage personnel et sur serveurs privés. Les sons appartiennent à leurs créateurs respectifs.

---

**Amusez-vous bien et survivez le plus longtemps possible ! 🧟‍♂️🔫**

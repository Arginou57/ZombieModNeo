# Architecture du Mod Zombie

## Vue d'Ensemble

Le mod est organisé en plusieurs couches :
1. **Managers** : Logique métier centrale
2. **Systems** : Systèmes spécialisés (caisses d'armes)
3. **Events** : Réaction aux événements du jeu
4. **Commands** : Interface admin/joueur
5. **Client** : Affichage HUD

## Flux de Données

### Démarrage d'une Partie

```
Admin → /zombiestart
    ↓
GameManager.startGame()
    ↓
GameState = STARTING
startCountdownTicks = 1200 (60s)
    ↓
Tick chaque frame (GameManager.tick())
    ↓
Messages broadcast toutes les 10s
    ↓
Countdown atteint 0
    ↓
GameManager.activateAllPlayers()
    ↓
WaveManager.startWave()
    ↓
Spawn zombies aux points configurés
```

### Cycle d'une Vague

```
WaveManager.startWave()
    ↓
zombiesRemaining = 6 + (vague × 6)
    ↓
Spawn tous les zombies
    ↓
Joueurs tuent zombies
    ↓
ZombieEventHandler.onEntityDeath()
    ↓
PointsManager.addPoints(+100)
WaveManager.onZombieKilled()
    ↓
zombiesRemaining--
    ↓
zombiesRemaining == 0 ?
    ↓
WaveManager.onWaveComplete()
    ↓
GameState = WAVE_COOLDOWN
RespawnManager.respawnAllDeadPlayers()
GameManager.activateWaitingPlayers()
waveCountdownTicks = 200 (10s)
    ↓
WaveManager.tick() countdown
    ↓
Countdown atteint 0
    ↓
WaveManager.startWave() (vague suivante)
```

### Mort d'un Joueur

```
Joueur meurt
    ↓
PlayerDeathHandler.onPlayerDeath()
    ↓
Joueur actif ?
    ↓ Oui
RespawnManager.onPlayerDeath()
    ↓
Ajout à deadPlayers
gameMode = SPECTATOR
    ↓
Vérifier GameOver
    ↓
Tous actifs morts ?
    ↓ Oui
gameOver()
    ↓
Broadcast message
Tous en spectateur
Reset managers
```

### Système de Rejoin

```
Joueur → /zombiejoin
    ↓
GameManager.joinGame()
    ↓
État du jeu ?
    ├─ WAITING → "Aucune partie"
    ├─ STARTING → waitingPlayers + ADVENTURE
    ├─ WAVE_ACTIVE → waitingPlayers + SPECTATOR
    └─ WAVE_COOLDOWN → activePlayers + SURVIVAL
```

### Achat dans une Caisse

```
Joueur clique coffre
    ↓
ChestInteractionHandler.onChestInteract()
    ↓
event.setCanceled(true) (empêcher ouverture)
    ↓
WeaponCrateManager.isWeaponCrate() ?
    ↓ Oui
Joueur actif ?
    ↓ Oui
Points suffisants ?
    ↓ Oui
PointsManager.removePoints(cost)
    ↓
WeaponCrateManager.getRandomWeapon()
    ↓
Sélection pondérée
    ↓
WeaponConfig.toItemStack()
    ↓
Ajouter à l'inventaire
Sons + Particules
```

## Managers Détaillés

### GameManager
**Responsabilités** :
- Gérer l'état du jeu (WAITING, STARTING, WAVE_ACTIVE, WAVE_COOLDOWN)
- Maintenir les listes de joueurs (actifs, en attente)
- Gérer le countdown de démarrage (60s)
- Broadcast des messages
- Sons globaux

**Variables clés** :
- `currentState : GameState`
- `activePlayers : Set<UUID>`
- `waitingPlayers : Set<UUID>`
- `startCountdownTicks : int`

### WaveManager
**Responsabilités** :
- Gérer les vagues (numéro, zombies)
- Spawner les zombies
- Countdown entre vagues (10s)
- Effet glowing sur derniers zombies

**Variables clés** :
- `currentWave : int`
- `zombiesRemaining : int`
- `spawnPoints : List<BlockPos>`
- `waveCountdownTicks : int`

### PointsManager
**Responsabilités** :
- Stocker les points par joueur
- Opérations : get, set, add, remove

**Variables clés** :
- `playerPoints : Map<UUID, Integer>`

### RespawnManager
**Responsabilités** :
- Gérer le point de respawn unique
- Tracker les joueurs morts
- Respawn à la fin des vagues

**Variables clés** :
- `respawnPoint : BlockPos`
- `deadPlayers : Set<UUID>`

## Systems Détaillés

### WeaponCrateManager
**Responsabilités** :
- Transformer coffres en caisses d'armes
- Stocker config dans NBT du coffre
- Sélection aléatoire pondérée
- Créer ItemStack avec enchantements

**Stockage NBT** :
```
PersistentData:
  IsWeaponCrate: boolean
  Cost: int
  Weapons: ListTag
    - Item: string
    - Count: int
    - Weight: int
    - Name: string
    - Enchantments: ListTag
      - Id: string
      - Level: int
```

**Algorithme de sélection** :
1. Calculer poids total
2. Nombre aléatoire [0, total)
3. Parcourir armes jusqu'à atteindre le nombre
4. Retourner arme sélectionnée

## Event Handlers

### ZombieEventHandler
**Écoute** : `LivingDeathEvent`
**Déclenche si** : Zombie tué par joueur actif
**Actions** :
- +100 points au joueur
- WaveManager.onZombieKilled()

### ChestInteractionHandler
**Écoute** : `PlayerInteractEvent.RightClickBlock`
**Déclenche si** : Coffre + Caisse d'armes
**Actions** :
- Annuler ouverture normale
- Vérifier joueur actif
- Vérifier points
- Retirer points
- Donner arme aléatoire

### PlayerDeathHandler
**Écoute** : `LivingDeathEvent`
**Déclenche si** : Joueur actif mort
**Actions** :
- RespawnManager.onPlayerDeath()
- Vérifier Game Over

## Commands

### GameCommands
- `/zombiestart` : GameManager.startGame()
- `/zombiejoin` : GameManager.joinGame()
- `/zombieleave` : GameManager.leaveGame()
- `/zombiestatus` : Afficher infos

### SpawnCommand
- `/zombiespawn` : WaveManager.addSpawnPoint()
- `/zombiespawn list` : Lister points
- `/zombiespawn clear` : Clear points

### RespawnCommand
- `/zombierespawn` : RespawnManager.setRespawnPoint()
- `/zombierespawn show` : Afficher point

### WeaponCrateCommand
- `/weaponcrate create <cost>` : Créer caisse
- `/weaponcrate addweapon ...` : Ajouter arme
- `/weaponcrate preset <type>` : Créer preset

## Client (HUD)

### ZombieHUD
**Écoute** : `RenderGuiEvent.Post`
**Affiche** :
- Vague actuelle (si dans partie)
- Zombies restants (si dans partie)
- Points (si actif)
- Countdown (STARTING ou WAVE_COOLDOWN)
- Statut attente (si en attente)

**Position** :
- Haut gauche : Infos jeu
- Centre : Countdowns et gros messages
- Centre bas : Statut attente

## Tick System

Le mod utilise `ServerTickEvent.Post` pour :
1. GameManager.tick()
   - Gérer countdown démarrage (60s)
   - Déléguer à WaveManager si WAVE_COOLDOWN

2. WaveManager.tick()
   - Gérer countdown entre vagues (10s)
   - Démarrer vague suivante

**Fréquence** : 20 ticks/seconde (Minecraft standard)

## Synchronisation

**Actuel** : Managers statiques, pas de synchronisation réseau

**Impact** :
- Fonctionne en solo et serveur local
- En multijoueur distant, HUD peut être désynchronisé

**Solution future** :
- Créer packets de synchronisation
- Envoyer updates réguliers aux clients
- Voir NOTES.md pour détails

## États du Jeu

```
WAITING
  ↓ /zombiestart
STARTING (60s countdown)
  ↓ countdown terminé
WAVE_ACTIVE (zombies spawés)
  ↓ tous zombies tués
WAVE_COOLDOWN (10s countdown)
  ↓ countdown terminé
WAVE_ACTIVE (vague suivante)
  ...
  ↓ tous joueurs morts
WAITING (game over)
```

## Gestion des Joueurs

**3 états possibles** :
1. **Hors partie** : Ni actif ni en attente
2. **En attente** : Dans waitingPlayers, mode spectateur/aventure
3. **Actif** : Dans activePlayers, mode survie, peut gagner points

**Transitions** :
```
Hors partie
  ↓ /zombiejoin (STARTING)
En attente (ADVENTURE)
  ↓ countdown terminé
Actif (SURVIVAL)

Hors partie
  ↓ /zombiejoin (WAVE_ACTIVE)
En attente (SPECTATOR)
  ↓ fin vague
Actif (SURVIVAL)

Actif
  ↓ mort
Actif (SPECTATOR, dans deadPlayers)
  ↓ fin vague
Actif (SURVIVAL, retiré de deadPlayers)
```

# Changelog - Version 1.0.2

## 🆕 Nouvelles fonctionnalités

### 1. **Système de dégâts configurables par mob**
- Ajout de 3 nouveaux paramètres dans `zombiemobs.json` pour chaque type de mob :
  - `startingDamage` : Dégâts de départ (en cœurs)
  - `damagePerWave` : Augmentation des dégâts par vague
  - `maxDamage` : Plafond de dégâts maximum
- Chaque mob peut maintenant avoir sa propre progression de dégâts indépendante

### 2. **Configuration de santé par mob**
- Déplacement des paramètres `startingHearts` et `heartsPerWave` de `zombiemod.json` vers `zombiemobs.json`
- Chaque type de mob a maintenant sa propre configuration de santé
- Permet de créer des mobs tanks (beaucoup de HP) ou fragiles (peu de HP)

### 3. **Configuration par défaut améliorée**
- Ajout des **Husks** dans la configuration par défaut (30% de spawn)
  - Plus lents mais plus résistants que les zombies normaux
  - HP de base plus élevés (2 cœurs vs 1 cœur)
  - Dégâts augmentent plus rapidement
- **Zombies normaux** : 70% de spawn
- Exemples de configuration multi-mobs prêts à l'emploi

### 4. **Arrêt automatique de la partie**
- La partie s'arrête automatiquement quand **tous les joueurs se déconnectent**
- Double vérification :
  - À la déconnexion d'un joueur
  - Au début de chaque nouvelle vague
- Nettoie proprement les mobs, réinitialise les managers et ferme les portes
- Log console : `[ZombieMod] Tous les joueurs sont déconnectés - Arrêt automatique`

## 🐛 Corrections de bugs

### 5. **Fix du crash au chargement des maps**
- **Problème** : Crash au démarrage du serveur avec l'erreur `Interfaces can't be instantiated! Register an InstanceCreator for net.minecraft.nbt.Tag`
- **Solution** : Les données NBT des portes (texte des pancartes) sont maintenant sauvegardées en format SNBT (String NBT) dans le JSON
- Les fichiers `zombiemod-maps.json` sont maintenant compatibles avec la sérialisation JSON standard

## 🧹 Nettoyage et optimisations

### 6. **Simplification de zombiemod.json**
- Suppression des paramètres dupliqués :
  - ❌ `heartsPerWave` (maintenant dans `zombiemobs.json`)
  - ❌ `startingHearts` (maintenant dans `zombiemobs.json`)
- `zombiemod.json` ne contient plus que les paramètres globaux du gameplay :
  - `maxZombiesOnMap` : Limite de zombies simultanés
  - `spawnDelaySeconds` : Délai entre chaque spawn
  - `zombieFollowRange` : Portée de détection
  - `armoredZombieChance` : Chance d'armure
  - `waveTimeoutSeconds` : Timeout de vague
  - `glowingZombiesCount` : Derniers zombies avec effet glowing

### 7. **Séparation claire des configurations**
- **`zombiemod.json`** → Paramètres globaux du gameplay
- **`zombiemobs.json`** → Configuration individuelle par type de mob
- **`zombiemod-maps.json`** → Configuration des maps (spawns, portes, etc.)
- Architecture plus claire et maintenable

### 8. **Mise à jour de version**
- Passage à la version **1.0.2**
- Recompilation complète avec toutes les nouvelles fonctionnalités

---

## 📋 Structure des fichiers de configuration

### **zombiemod.json** (paramètres globaux)
```json
{
  "maxZombiesOnMap": 32,
  "spawnDelaySeconds": 2.0,
  "zombieFollowRange": 32.0,
  "armoredZombieChance": 0.15,
  "waveTimeoutSeconds": 50,
  "glowingZombiesCount": 5
}
```

### **zombiemobs.json** (configuration par mob)
```json
{
  "mobs": [
    {
      "mobType": "minecraft:zombie",
      "chance": 0.7,
      "baseSpeed": 0.23,
      "speedPerWave": 0.01,
      "maxSpeed": 0.5,
      "startingHearts": 1,
      "heartsPerWave": 0.5,
      "startingDamage": 1.5,
      "damagePerWave": 0.25,
      "maxDamage": 5.0
    },
    {
      "mobType": "minecraft:husk",
      "chance": 0.3,
      "baseSpeed": 0.2,
      "speedPerWave": 0.008,
      "maxSpeed": 0.4,
      "startingHearts": 2,
      "heartsPerWave": 0.6,
      "startingDamage": 1.5,
      "damagePerWave": 0.3,
      "maxDamage": 6.0
    }
  ]
}
```

---

## ⚠️ Notes d'installation

### **IMPORTANT** : Si vous migrez depuis la version 1.0.1

Avant de démarrer le serveur avec la version 1.0.2, **supprimez ou renommez** l'ancien fichier de configuration des maps pour éviter les erreurs de compatibilité :

```bash
mv config/zombiemod-maps.json config/zombiemod-maps.json.backup
```

Le mod recréera automatiquement un nouveau fichier compatible au démarrage.

---

## 📦 Fichier compilé

**`zombiemod-1.0.2.jar`** (3.6 Mo)
- Compatible NeoForge 1.21.1
- Toutes les fonctionnalités testées et validées

# 🎉 Mise à Jour v1.1.0 - Système de Spawn Amélioré

## ✨ Nouvelles Fonctionnalités

### 1. 🧟‍♂️ Spawn Progressif
- Les zombies ne spawnent **plus tous d'un coup**
- **Délai de 2 secondes** entre chaque spawn (configurable)
- Crée une montée en intensité progressive
- Réduit les pics de lag

**Avant** :
```
Vague démarre → 12 zombies apparaissent instantanément → Chaos !
```

**Maintenant** :
```
Vague démarre → 1 zombie toutes les 2s → 32 max sur la map → Gameplay fluide
```

---

### 2. 📊 Limite de Zombies Simultanés
- **Maximum 32 zombies vivants** sur la map en même temps
- Les zombies suivants attendent que des places se libèrent
- Maintient les performances même en vague 50+

**Exemple** :
```
Vague 10 = 66 zombies au total
→ Seulement 32 présents à la fois
→ Les 34 autres spawnent progressivement
→ Pas de lag !
```

---

### 3. 💪 HP Progressifs par Vague
- Les zombies démarrent **faibles** et deviennent progressivement plus forts
- **Formule** : HP = HP de base + (Vague - 1) × 0.5 cœurs × 2

**Progression par défaut** :
| Vague | HP | Cœurs | Difficulté |
|-------|-----|-------|------------|
| 1     | 2   | 1.0   | Très facile |
| 5     | 6   | 3.0   | Facile |
| 10    | 11  | 5.5   | Modéré |
| 20    | 21  | 10.5  | Difficile |
| 30    | 31  | 15.5  | Très difficile |
| 50    | 51  | 25.5  | Extrême |

---

### 4. 🚫 Pas de Drops
- Les zombies **ne dropent plus d'XP**
- Les zombies **ne dropent plus d'items** (chair putréfiée, armures, etc.)
- Garde le focus sur les points du mode zombie
- Évite l'accumulation d'items au sol

---

### 5. ⚙️ Fichier de Configuration
- **Création automatique** au premier lancement
- **Emplacement** : `serveur/config/zombiemod.json`
- **Modifiable** à tout moment (redémarrage requis)

**Contenu par défaut** :
```json
{
  "maxZombiesOnMap": 32,
  "spawnDelaySeconds": 2.0,
  "heartsPerWave": 0.5,
  "startingHearts": 1
}
```

---

## 📝 Configuration Détaillée

### `maxZombiesOnMap` (défaut: 32)
Nombre maximum de zombies vivants simultanément

**Exemples** :
- `16` : Serveur faible ou peu de joueurs
- `32` : Configuration équilibrée (recommandé)
- `48` : Serveur puissant avec beaucoup de joueurs
- `64` : Mode hardcore

### `spawnDelaySeconds` (défaut: 2.0)
Délai en secondes entre chaque spawn

**Exemples** :
- `0.5` : Très rapide, difficile
- `1.0` : Rapide
- `2.0` : Modéré (recommandé)
- `3.0` : Lent, plus facile

### `heartsPerWave` (défaut: 0.5)
Cœurs ajoutés aux zombies à chaque vague

**Exemples** :
- `0.25` : Progression lente (parties longues)
- `0.5` : Progression normale (recommandé)
- `1.0` : Progression rapide (challenge)
- `2.0` : Progression explosive (extrême)

### `startingHearts` (défaut: 1)
Cœurs des zombies à la vague 1

**Exemples** :
- `1` : Facile (1 coup d'épée en bois)
- `2` : Modéré (2-3 coups)
- `5` : Difficile dès le début

---

## 🎮 Exemples de Configurations

### Mode Facile (Survie Longue)
```json
{
  "maxZombiesOnMap": 24,
  "spawnDelaySeconds": 3.0,
  "heartsPerWave": 0.25,
  "startingHearts": 1
}
```
→ Peu de zombies, spawn lent, HP augmentent doucement

### Mode Normal (Par Défaut)
```json
{
  "maxZombiesOnMap": 32,
  "spawnDelaySeconds": 2.0,
  "heartsPerWave": 0.5,
  "startingHearts": 1
}
```
→ Équilibré pour 2-4 joueurs

### Mode Difficile (Challenge)
```json
{
  "maxZombiesOnMap": 48,
  "spawnDelaySeconds": 1.0,
  "heartsPerWave": 1.0,
  "startingHearts": 2
}
```
→ Beaucoup de zombies, spawn rapide, HP explosifs

### Mode Hardcore (Extrême)
```json
{
  "maxZombiesOnMap": 64,
  "spawnDelaySeconds": 0.5,
  "heartsPerWave": 2.0,
  "startingHearts": 3
}
```
→ Pour les pros uniquement !

---

## 📦 Installation

### 1. Télécharger le Nouveau JAR
**Fichier** : `build/libs/zombiemod-1.0.0.jar` (45 KB)

### 2. Remplacer l'Ancien
1. Arrêtez le serveur/client
2. Supprimez l'ancien JAR du dossier `mods/`
3. Copiez le nouveau JAR dans `mods/`

### 3. Premier Lancement
Le fichier de config sera créé automatiquement :
```
serveur/config/zombiemod.json
```

### 4. Configuration (Optionnel)
Modifiez `zombiemod.json` selon vos préférences et redémarrez

---

## 🔄 Changements Techniques

### Nouveaux Fichiers
- `ZombieConfig.java` - Système de configuration
- `ZombieDropHandler.java` - Désactivation des drops
- `zombiemod.json` - Fichier de configuration auto-généré

### Fichiers Modifiés
- `WaveManager.java` - Spawn progressif + HP dynamiques
- `GameManager.java` - Tick pour spawn progressif
- `ZombieMod.java` - Initialisation config + handler

### Nouvelles Dépendances
- `com.google.gson` - Gestion du fichier JSON (déjà inclus)

---

## 🐛 Corrections et Optimisations

- ✅ Suppression du lag au démarrage des vagues
- ✅ Meilleure gestion de la mémoire (zombies nettoyés)
- ✅ Pas de drops inutiles qui traînent par terre
- ✅ Performance stable même en vague 50+

---

## 📊 Comparaison Avant/Après

### Avant (v1.0.0)
- ❌ Tous les zombies spawn d'un coup → Lag
- ❌ Pas de limite → 100+ zombies en vague 15 → Crash potentiel
- ❌ HP fixes à 20 → Trop facile en début, trop dur plus tard
- ❌ Drops d'XP et items → Accumulation au sol

### Après (v1.1.0)
- ✅ Spawn progressif → Fluide
- ✅ Limite de 32 → Performance stable
- ✅ HP progressifs → Difficulté équilibrée
- ✅ Pas de drops → Gameplay épuré

---

## 📖 Documentation Complète

Consultez **ZOMBIE_SPAWNING_GUIDE.md** pour :
- Guide détaillé de chaque paramètre
- Tableaux de HP par vague
- Exemples de configurations avancées
- Dépannage

---

## 🎯 Rétrocompatibilité

✅ **100% compatible** avec les parties existantes
✅ Les caisses d'armes fonctionnent normalement
✅ Les commandes restent identiques
✅ Pas besoin de reconfigurer le serveur

---

## 🚀 Prochaines Mises à Jour

Fonctionnalités prévues :
- [ ] Power-ups (speed, damage boost, etc.)
- [ ] Boss zombies toutes les 5 vagues
- [ ] Système de classes (Tank, DPS, Support)
- [ ] Statistiques et leaderboard
- [ ] Shop permanent pour achats divers

---

## ✨ Remerciements

Merci d'utiliser le Zombie Mode Mod !

**Version** : 1.1.0
**Date** : 16 novembre 2025
**Build** : zombiemod-1.0.0.jar (45 KB)
**Temps de compilation** : 49s
**Statut** : ✅ BUILD SUCCESSFUL

---

**Fichiers de Documentation** :
- `ZOMBIE_SPAWNING_GUIDE.md` - Guide complet du système de spawn
- `zombiemod-config-example.json` - Exemple de configuration
- `README.md` - Documentation générale du mod
- `COMMAND_IMPROVEMENTS.md` - Guide des commandes améliorées

# 🧟 Guide du Système de Spawn des Zombies

## ✨ Nouvelles Fonctionnalités (v1.1.0)

Le système de spawn des zombies a été complètement refait avec les fonctionnalités suivantes :

### 1. ⏱️ Spawn Progressif avec Délai
- Les zombies ne spawnent **plus tous en même temps**
- Délai configurable entre chaque spawn (par défaut : **2 secondes**)
- Crée une montée en intensité progressive
- Plus réaliste et moins de lag

### 2. 📊 Limite de Zombies Simultanés
- Maximum de **32 zombies** vivants sur la map en même temps
- Les zombies suivants spawnent au fur et à mesure que les autres meurent
- Évite les problèmes de performance
- Maintient le gameplay équilibré

### 3. 💪 HP Progressifs par Vague
- Les zombies démarrent faibles et deviennent plus forts
- **Formule** : HP = Starting HP + (Vague - 1) × Cœurs par vague × 2
- **Par défaut** :
  * Vague 1 : 2 HP (1 cœur)
  * Vague 2 : 3 HP (1.5 cœurs)
  * Vague 3 : 4 HP (2 cœurs)
  * Vague 10 : 11 HP (5.5 cœurs)
  * Vague 20 : 21 HP (10.5 cœurs)

### 4. 🚫 Pas de Drops
- Les zombies ne dropent **aucun XP**
- Les zombies ne dropent **aucun item** (chair putréfiée, armures, etc.)
- Garde le focus sur les points du mode zombie

### 5. ⚙️ Configuration Complète
- Fichier de configuration JSON auto-généré
- Modifiable à chaud (redémarrage serveur requis)
- Toutes les valeurs personnalisables

---

## 📝 Fichier de Configuration

### Emplacement
Le fichier de configuration est automatiquement créé au premier lancement :

```
serveur/config/zombiemod.json
```

### Exemple de Configuration

```json
{
  "maxZombiesOnMap": 32,
  "spawnDelaySeconds": 2.0,
  "heartsPerWave": 0.5,
  "startingHearts": 1
}
```

### Paramètres Détaillés

#### `maxZombiesOnMap`
- **Type** : Nombre entier
- **Défaut** : `32`
- **Description** : Nombre maximum de zombies vivants simultanément sur la map
- **Recommandations** :
  * 16-24 : Serveur avec peu de joueurs ou faible performance
  * 32-48 : Configuration standard
  * 64+ : Serveur puissant avec beaucoup de joueurs

**Exemple** :
```json
"maxZombiesOnMap": 48
```

#### `spawnDelaySeconds`
- **Type** : Nombre décimal
- **Défaut** : `2.0`
- **Description** : Délai en secondes entre chaque spawn de zombie
- **Recommandations** :
  * 0.5-1.0 : Spawn rapide, plus difficile
  * 2.0-3.0 : Spawn modéré (recommandé)
  * 4.0+ : Spawn lent, plus facile

**Exemple** :
```json
"spawnDelaySeconds": 1.5
```

#### `heartsPerWave`
- **Type** : Nombre décimal
- **Défaut** : `0.5`
- **Description** : Nombre de cœurs ajoutés aux zombies à chaque nouvelle vague
- **Note** : 1 cœur = 2 HP
- **Recommandations** :
  * 0.25 : Progression lente (survie longue)
  * 0.5 : Progression normale (recommandé)
  * 1.0 : Progression rapide (challenge)
  * 2.0+ : Progression très rapide (extrême)

**Formule** : HP = `startingHearts` × 2 + (Vague - 1) × `heartsPerWave` × 2

**Exemples** :
```json
"heartsPerWave": 1.0
```
→ Vague 1: 2 HP, Vague 2: 4 HP, Vague 3: 6 HP, etc.

```json
"heartsPerWave": 0.25
```
→ Vague 1: 2 HP, Vague 2: 2.5 HP, Vague 3: 3 HP, etc.

#### `startingHearts`
- **Type** : Nombre entier
- **Défaut** : `1`
- **Description** : Nombre de cœurs des zombies à la vague 1
- **Note** : 1 cœur = 2 HP
- **Recommandations** :
  * 1 : Facile au début (1 coup d'épée en bois)
  * 2-3 : Modéré
  * 5+ : Difficile dès le début

**Exemple** :
```json
"startingHearts": 2
```
→ Les zombies démarrent avec 4 HP (2 cœurs)

---

## 📊 Exemples de Configurations

### Configuration Facile (Longue Survie)
```json
{
  "maxZombiesOnMap": 24,
  "spawnDelaySeconds": 3.0,
  "heartsPerWave": 0.25,
  "startingHearts": 1
}
```
**Résultat** :
- Peu de zombies à la fois
- Spawn lent
- HP augmentent doucement
- Progression : Vague 1: 2 HP → Vague 10: 4.5 HP → Vague 20: 7 HP

### Configuration Normale (Équilibrée)
```json
{
  "maxZombiesOnMap": 32,
  "spawnDelaySeconds": 2.0,
  "heartsPerWave": 0.5,
  "startingHearts": 1
}
```
**Résultat** :
- Configuration par défaut
- Équilibre parfait
- Progression : Vague 1: 2 HP → Vague 10: 11 HP → Vague 20: 21 HP

### Configuration Difficile (Challenge)
```json
{
  "maxZombiesOnMap": 48,
  "spawnDelaySeconds": 1.0,
  "heartsPerWave": 1.0,
  "startingHearts": 2
}
```
**Résultat** :
- Beaucoup de zombies à la fois
- Spawn rapide
- HP augmentent vite
- Progression : Vague 1: 4 HP → Vague 10: 22 HP → Vague 20: 42 HP

### Configuration Extrême (Hardcore)
```json
{
  "maxZombiesOnMap": 64,
  "spawnDelaySeconds": 0.5,
  "heartsPerWave": 2.0,
  "startingHearts": 3
}
```
**Résultat** :
- Énormément de zombies
- Spawn très rapide
- HP explosifs
- Progression : Vague 1: 6 HP → Vague 10: 42 HP → Vague 20: 82 HP

---

## 🎮 Comportement en Jeu

### Démarrage de Vague
1. Message : "=== VAGUE X ==="
2. Message : "X zombies à éliminer !"
3. Son : ENDER_DRAGON_GROWL
4. Les zombies commencent à spawner **progressivement**

### Pendant la Vague
- Les zombies spawnent toutes les X secondes (config)
- Maximum de Y zombies vivants à la fois (config)
- Les 32 derniers zombies ont l'effet **Glowing** (lumineux)
- Chaque kill donne +100 points

### HP des Zombies
- Les HP sont calculés à la création du zombie
- Affichage dans le jeu : barre de vie au-dessus du zombie
- Plus la vague est haute, plus ils ont de HP
- Les zombies ne régénèrent **jamais** leurs HP

### Fin de Vague
- Tous les zombies éliminés
- Respawn des joueurs morts
- Joueurs en attente rejoignent
- Countdown de 10 secondes avant la prochaine vague

---

## 🔧 Modifier la Configuration

### 1. Localiser le Fichier
```
serveur/config/zombiemod.json
```

### 2. Éditer avec un Éditeur de Texte
- Notepad++
- Visual Studio Code
- Bloc-notes

### 3. Modifier les Valeurs
```json
{
  "maxZombiesOnMap": 40,      ← Modifier ici
  "spawnDelaySeconds": 1.5,   ← Modifier ici
  "heartsPerWave": 0.75,      ← Modifier ici
  "startingHearts": 2         ← Modifier ici
}
```

### 4. Sauvegarder

### 5. Redémarrer le Serveur
Les changements prennent effet au redémarrage

---

## 📈 Calcul des HP par Vague

### Formule
```
HP = (startingHearts × 2) + ((vague - 1) × heartsPerWave × 2)
```

### Tableau avec Config Par Défaut

| Vague | HP | Cœurs | Coups (Épée Bois) | Coups (Épée Fer) | Coups (Épée Diamant) |
|-------|----|----- |-------------------|------------------|---------------------|
| 1     | 2  | 1.0  | 1                 | 1                | 1                   |
| 2     | 3  | 1.5  | 1                 | 1                | 1                   |
| 5     | 8  | 4.0  | 2                 | 2                | 2                   |
| 10    | 11 | 5.5  | 3                 | 2                | 2                   |
| 15    | 16 | 8.0  | 4                 | 3                | 3                   |
| 20    | 21 | 10.5 | 5                 | 3                | 3                   |
| 30    | 31 | 15.5 | 7                 | 5                | 4                   |
| 50    | 51 | 25.5 | 11                | 8                | 7                   |

**Dégâts des épées** :
- Bois : 4 dégâts
- Fer : 6 dégâts
- Diamant : 7 dégâts
- Netherite : 8 dégâts

---

## ⚠️ Notes Importantes

### Performance
- Si vous avez du lag, **réduisez** `maxZombiesOnMap`
- Un délai plus long (`spawnDelaySeconds`) réduit aussi le lag
- Plus de zombies = plus de calculs d'IA

### Équilibrage
- Testez différentes configs pour trouver celle qui vous plaît
- La config par défaut est équilibrée pour 2-4 joueurs
- Ajustez selon le nombre de joueurs sur votre serveur

### Changements en Cours de Partie
- Les changements de config nécessitent un **redémarrage**
- Les parties en cours ne sont **pas affectées**
- La config est rechargée au démarrage du serveur

---

## 🐛 Dépannage

### Les zombies spawnent trop lentement
→ Réduisez `spawnDelaySeconds` (exemple : 1.0)

### Trop de zombies, le serveur lag
→ Réduisez `maxZombiesOnMap` (exemple : 24)

### Les zombies meurent trop facilement
→ Augmentez `heartsPerWave` ou `startingHearts`

### Les zombies deviennent trop forts trop vite
→ Réduisez `heartsPerWave` (exemple : 0.25)

### Le fichier de config n'existe pas
→ Lancez le serveur une fois, il sera créé automatiquement

### Les changements ne s'appliquent pas
→ Vérifiez que vous avez bien **redémarré le serveur**

---

## 📋 Résumé des Changements

✅ **Spawn progressif** au lieu d'instantané
✅ **Limite de 32 zombies** max simultanés
✅ **Délai de 2s** entre spawns (configurable)
✅ **HP progressifs** basés sur la vague
✅ **Aucun drop** d'XP ou items
✅ **Fichier de config** JSON complet

**Fichier JAR mis à jour** : `build/libs/zombiemod-1.0.0.jar`

---

**Version** : 1.1.0
**Date** : 16 novembre 2025
**Fichier** : `zombiemod-1.0.0.jar` (44 KB)

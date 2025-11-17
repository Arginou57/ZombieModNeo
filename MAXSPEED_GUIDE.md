# 🏃 Guide de la Vitesse Maximale des Mobs

## 📋 Vue d'ensemble

La fonctionnalité **maxSpeed** permet de plafonner la vitesse des monstres pour éviter qu'ils deviennent trop rapides aux vagues élevées.

---

## 🎯 Problème Résolu

**Avant :**
- Vitesse = `baseSpeed + (speedPerWave × vague)`
- À la vague 50 : `0.23 + (0.01 × 50) = 0.73` ⚡ (trop rapide !)
- Les zombies finissaient par courir plus vite que les joueurs

**Après :**
- Vitesse plafonnée à `maxSpeed`
- À la vague 50 : `min(0.73, 0.50) = 0.50` ✅ (équilibré)
- Difficulté maîtrisée même aux vagues très élevées

---

## ⚙️ Configuration

### Fichier : `config/zombiemobs.json`

```json
{
  "mobs": [
    {
      "mobType": "minecraft:zombie",
      "chance": 1.0,
      "baseSpeed": 0.23,
      "speedPerWave": 0.01,
      "maxSpeed": 0.50
    }
  ]
}
```

### Paramètres

| Paramètre | Description | Valeur par défaut |
|-----------|-------------|-------------------|
| `baseSpeed` | Vitesse de départ (vague 1) | 0.23 |
| `speedPerWave` | Vitesse ajoutée par vague | 0.01 |
| `maxSpeed` | Vitesse maximale (plafond) | 0.50 |

---

## 📊 Exemples de Configuration

### Configuration Par Défaut (Équilibrée)
```json
{
  "baseSpeed": 0.23,
  "speedPerWave": 0.01,
  "maxSpeed": 0.50
}
```
- **Vague 1** : 0.23 (vitesse normale)
- **Vague 10** : 0.33
- **Vague 20** : 0.43
- **Vague 27** : 0.50 (plafond atteint)
- **Vague 50+** : 0.50 (reste plafonné)

### Configuration Facile (Zombies Lents)
```json
{
  "baseSpeed": 0.20,
  "speedPerWave": 0.005,
  "maxSpeed": 0.35
}
```
- Progression très lente
- Plafond bas pour rester accessible

### Configuration Difficile (Zombies Rapides)
```json
{
  "baseSpeed": 0.25,
  "speedPerWave": 0.02,
  "maxSpeed": 0.60
}
```
- Progression rapide
- Plafond élevé pour challenge

### Configuration Sans Limite
```json
{
  "baseSpeed": 0.23,
  "speedPerWave": 0.01,
  "maxSpeed": 0
}
```
⚠️ **Note** : Si `maxSpeed = 0`, aucune limite n'est appliquée (rétrocompatibilité)

---

## 🧮 Calcul de la Vitesse

### Formule
```
vitesse_calculée = baseSpeed + (speedPerWave × vague_actuelle)
vitesse_finale = min(vitesse_calculée, maxSpeed)
```

### Exemple avec Config Par Défaut

| Vague | Calcul | Vitesse Calculée | Vitesse Finale |
|-------|--------|------------------|----------------|
| 1 | 0.23 + (0.01 × 1) | 0.24 | 0.24 |
| 10 | 0.23 + (0.01 × 10) | 0.33 | 0.33 |
| 20 | 0.23 + (0.01 × 20) | 0.43 | 0.43 |
| 27 | 0.23 + (0.01 × 27) | 0.50 | **0.50** ⬅️ Plafond |
| 50 | 0.23 + (0.01 × 50) | 0.73 | **0.50** ⬅️ Plafonné |
| 100 | 0.23 + (0.01 × 100) | 1.23 | **0.50** ⬅️ Plafonné |

---

## 🎮 Vitesses de Référence Minecraft

Pour vous aider à choisir des valeurs :

| Entité | Vitesse | Commentaire |
|--------|---------|-------------|
| Zombie | 0.23 | Vitesse normale |
| Joueur (marche) | 0.10 | Lent |
| Joueur (sprint) | ~0.28 | Rapide |
| Creeper | 0.25 | Légèrement plus rapide qu'un zombie |
| Enderman | 0.30 | Rapide |
| Spider | 0.30 | Rapide |
| Baby Zombie | 0.46 | Très rapide |

**Recommandation** : Garder `maxSpeed` entre 0.40 et 0.60 pour un gameplay équilibré.

---

## 🔧 Rétrocompatibilité

### Anciens Fichiers de Configuration
Si votre fichier `zombiemobs.json` ne contient pas le champ `maxSpeed` :
- **Comportement** : Aucune limite n'est appliquée (comme avant)
- **Solution** : Ajoutez manuellement `"maxSpeed": 0.50` à vos entrées de mobs

### Migration Automatique
Au prochain lancement après la mise à jour :
- Si le fichier n'existe pas → Création avec `maxSpeed = 0.50`
- Si le fichier existe sans `maxSpeed` → Fonctionnera sans limite (rétrocompatibilité)

---

## 📝 Exemples Avancés

### Multi-Mobs avec Vitesses Différentes
```json
{
  "mobs": [
    {
      "mobType": "minecraft:zombie",
      "chance": 0.7,
      "baseSpeed": 0.23,
      "speedPerWave": 0.01,
      "maxSpeed": 0.50
    },
    {
      "mobType": "minecraft:skeleton",
      "chance": 0.2,
      "baseSpeed": 0.25,
      "speedPerWave": 0.008,
      "maxSpeed": 0.45
    },
    {
      "mobType": "minecraft:spider",
      "chance": 0.1,
      "baseSpeed": 0.30,
      "speedPerWave": 0.005,
      "maxSpeed": 0.55
    }
  ]
}
```

---

## ⚠️ Notes Importantes

1. **Valeurs Extrêmes**
   - ❌ `maxSpeed < baseSpeed` : Les mobs seront toujours à maxSpeed
   - ❌ `maxSpeed > 1.0` : Peut causer des bugs de mouvement
   - ✅ Recommandé : `0.30 ≤ maxSpeed ≤ 0.70`

2. **Impact sur le Gameplay**
   - Plus `maxSpeed` est élevé, plus les vagues tardives sont difficiles
   - Plus `maxSpeed` est bas, plus le jeu stagne en difficulté

3. **Performance**
   - Le plafonnement n'a aucun impact sur les performances
   - C'est simplement un `Math.min()` lors du spawn

---

## 🧪 Tester vos Paramètres

1. Lancez le jeu et démarrez une partie
2. Utilisez `/zombieskip` (si disponible) pour avancer rapidement
3. Observez la vitesse des zombies à différentes vagues
4. Ajustez `maxSpeed` dans le fichier de config
5. Redémarrez le serveur pour appliquer les changements

---

## 💡 Conseils

- **Pour débutants** : `maxSpeed = 0.40` (facile)
- **Pour joueurs normaux** : `maxSpeed = 0.50` (équilibré)
- **Pour experts** : `maxSpeed = 0.60` (difficile)
- **Pour hardcore** : `maxSpeed = 0.70` (extrême)

---

## 🐛 Dépannage

**Q : Les zombies ne deviennent pas plus rapides**
- Vérifiez que `speedPerWave > 0`
- Vérifiez que vous n'avez pas déjà atteint `maxSpeed`

**Q : Les zombies sont trop rapides dès la vague 1**
- Vérifiez que `baseSpeed` n'est pas trop élevé
- Valeur recommandée : `0.23` (vitesse normale zombie)

**Q : Mes modifications ne s'appliquent pas**
- Redémarrez complètement le serveur
- Vérifiez que le fichier JSON est bien formé (pas d'erreurs de syntaxe)
- Consultez les logs du serveur pour des erreurs

---

**Amusez-vous bien et bon courage contre les hordes !** 🧟‍♂️

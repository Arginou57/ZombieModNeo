# Notes de Développement

## Améliorations Possibles

### 1. Synchronisation Client-Serveur
**Problème actuel** : Le HUD accède directement aux managers statiques côté serveur. En multijoueur distant, le client pourrait ne pas avoir les bonnes données.

**Solution** :
- Créer des packets de synchronisation pour envoyer les données du serveur au client
- Utiliser `SimpleChannel` de NeoForge pour la communication réseau
- Synchroniser : vague actuelle, zombies restants, points du joueur, état du jeu

### 2. Persistance des Données
**Problème actuel** : Les données sont perdues au redémarrage du serveur.

**Solution** :
- Sauvegarder les points des joueurs dans un fichier JSON ou NBT
- Sauvegarder les points de spawn et le point de respawn
- Charger automatiquement au démarrage du serveur

### 3. Configuration
**Amélioration** : Ajouter un fichier de configuration pour :
- Points par kill (actuellement 100)
- Formule de zombies par vague (actuellement 6 + vague × 6)
- Durée des countdowns (60s démarrage, 10s entre vagues)
- Points de départ (500)
- Nombre de derniers zombies glowing (32)

### 4. Système de Classes
**Nouvelle feature** :
- Choisir une classe au démarrage (Tank, DPS, Support, etc.)
- Chaque classe a des avantages (santé bonus, dégâts bonus, etc.)
- Compétences spéciales activables

### 5. Boss Waves
**Nouvelle feature** :
- Toutes les 5 vagues, spawn d'un boss zombie
- Boss avec beaucoup de vie et dégâts
- Récompense bonus de points

### 6. Power-ups
**Nouvelle feature** :
- Drop aléatoire de power-ups lors de kills
- Speed boost, damage boost, santé, munitions infinies temporaires
- Système de ramassage avec effets temporaires

### 7. Système de Shop Amélioré
**Amélioration** :
- Shop permanent pour acheter des objets (nourriture, armures, etc.)
- Système d'upgrade (améliorer dégâts, vie, vitesse)
- Shop de munitions pour arcs/arbalètes

### 8. Statistiques
**Nouvelle feature** :
- Tracker les kills totaux, vagues complétées, meilleur score
- Afficher un scoreboard en fin de partie
- Sauvegarder les records

### 9. Maps Personnalisables
**Amélioration** :
- Système de zones (fermer/ouvrir des portes à acheter)
- Téléporteurs entre zones
- Pièges à acheter (lave, TNT, etc.)

### 10. Balancing
**À tester et ajuster** :
- Prix des caisses
- Probabilités des armes
- Nombre de zombies par vague
- Points par kill

## Bugs Potentiels à Surveiller

### 1. Zombies qui ne spawent pas
- Vérifier que les points de spawn sont valides
- S'assurer qu'il n'y a pas d'obstruction

### 2. Joueurs coincés en spectateur
- Vérifier les conditions de respawn
- Ajouter une commande de debug pour forcer le mode de jeu

### 3. Points négatifs
- Ajouter une vérification pour empêcher les points < 0
- Bloquer les achats si pas assez de points

### 4. Synchronisation HUD
- En multijoueur distant, le HUD peut afficher de mauvaises valeurs
- Nécessite packets de synchronisation (voir amélioration #1)

### 5. Game Over non déclenché
- Vérifier la logique de détection "tous morts"
- S'assurer que les joueurs en attente ne comptent pas

## Tests à Effectuer

1. **Solo** : Tester toutes les fonctionnalités en solo
2. **Multijoueur** : Tester avec 2-4 joueurs
3. **Rejoin** : Tester rejoin pendant countdown, vague, entre vagues
4. **Death** : Tester mort et respawn
5. **Caisses** : Tester tous les presets
6. **Game Over** : Tester que le game over fonctionne correctement
7. **Commandes** : Tester toutes les commandes admin

## Optimisations

1. **Zombies** : Limiter le nombre de zombies actifs en même temps
2. **Particles** : Réduire les particules si lag
3. **Sounds** : Éviter trop de sons simultanés

## Code Cleanup

1. Ajouter des commentaires JavaDoc
2. Extraire les constantes magiques (100 points, 500 départ, etc.)
3. Créer une classe Config pour les valeurs configurables
4. Ajouter des logs pour le debugging

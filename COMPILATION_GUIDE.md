# 🔨 Guide de Compilation - ZombieMod v1.0.1

## 📋 Pré-requis

- **Java 21** installé
- **Git** installé
- Connexion internet (pour télécharger les dépendances Gradle)

---

## 🚀 Compilation Rapide

### Méthode 1 : Compilation Simple

```bash
# 1. Naviguer dans le dossier du projet
cd ZombieModNeo

# 2. Compiler le mod (Windows)
gradlew.bat build

# OU (Linux/Mac)
./gradlew build
```

### Méthode 2 : Nettoyage + Compilation

```bash
# Nettoyer avant de compiler (recommandé)
# Windows:
gradlew.bat clean build

# Linux/Mac:
./gradlew clean build
```

---

## 📦 Résultat de la Compilation

Après une compilation réussie, vous trouverez le fichier JAR dans :

```
build/libs/zombiemod-1.0.1.jar
```

**Taille attendue** : ~45-50 KB

---

## 🔍 Vérifier la Version

Pour vérifier que la version est bien 1.0.1 :

```bash
# Lire le fichier de propriétés
cat gradle.properties | grep mod_version
```

Devrait afficher : `mod_version=1.0.1`

---

## ❌ Problèmes Courants

### Erreur : "Permission denied" (Linux/Mac)
```bash
chmod +x gradlew
./gradlew build
```

### Erreur : Java version incorrecte
```bash
# Vérifier votre version de Java
java -version

# Devrait afficher Java 21
```

### Erreur : "Could not find or load main class"
```bash
# Télécharger à nouveau le wrapper Gradle
./gradlew wrapper --gradle-version=8.8
```

### Gradle très lent / bloqué
```bash
# Utiliser le mode offline si vous avez déjà compilé
./gradlew build --offline
```

---

## 🎯 Commandes Utiles

### Compiler sans tests
```bash
./gradlew build -x test
```

### Voir toutes les tâches disponibles
```bash
./gradlew tasks
```

### Nettoyer les fichiers de build
```bash
./gradlew clean
```

### Compiler avec plus d'informations
```bash
./gradlew build --info
```

### Compiler en mode debug
```bash
./gradlew build --debug
```

---

## 📂 Structure après Compilation

```
ZombieModNeo/
├── build/
│   ├── libs/
│   │   └── zombiemod-1.0.1.jar  ← VOTRE MOD ICI
│   ├── classes/
│   ├── resources/
│   └── ...
├── src/
├── gradle.properties
└── build.gradle
```

---

## ✅ Vérification Finale

Après compilation, vérifiez :

1. **Le fichier existe** : `build/libs/zombiemod-1.0.1.jar`
2. **La taille est correcte** : ~45-50 KB
3. **Le nom contient 1.0.1** : confirme la version

---

## 🚀 Installation du Mod Compilé

1. **Copiez** `build/libs/zombiemod-1.0.1.jar`
2. **Collez** dans votre dossier `mods/` (serveur ou client)
3. **Lancez** Minecraft avec NeoForge 21.1.77+

---

## 📝 Notes

- La première compilation peut prendre 2-5 minutes (téléchargement des dépendances)
- Les compilations suivantes seront plus rapides (~30 secondes)
- Gradle met en cache les dépendances dans `~/.gradle/`

---

## 🐛 Toujours des Problèmes ?

Si la compilation échoue, partagez le message d'erreur complet pour obtenir de l'aide.

**Bon build !** 🎉

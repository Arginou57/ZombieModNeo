@echo off
echo ==========================================
echo Conversion MP3 vers OGG pour Minecraft
echo ==========================================
echo.

:: Vérifier si ffmpeg est installé
where ffmpeg >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo FFmpeg trouvé! Conversion en cours...
    goto :convert
)

echo FFmpeg n'est pas installé.
echo.
echo INSTRUCTIONS:
echo 1. Téléchargez FFmpeg depuis: https://www.gyan.dev/ffmpeg/builds/
echo 2. Extrayez l'archive
echo 3. Ajoutez le dossier 'bin' au PATH ou copiez ffmpeg.exe ici
echo.
echo OU
echo.
echo Utilisez un convertisseur en ligne:
echo https://cloudconvert.com/mp3-to-ogg
echo.
echo Fichiers à convertir:
echo - sounds\003 Nacht Der Untoten - Round Start.mp3
echo - sounds\005 Nacht Der Untoten - Round End.mp3
echo - sounds\006 Nacht Der Untoten - Mystery Box.mp3
echo - sounds\05. 115 [Kino der Toten].mp3
echo.
echo Une fois convertis en .ogg, placez-les dans:
echo src\main\resources\assets\zombiemod\sounds\
echo.
echo Noms des fichiers de sortie:
echo - round_start.ogg
echo - round_end.ogg
echo - mystery_box.ogg
echo - 115.ogg
echo.
pause
exit /b 1

:convert
echo Conversion des fichiers MP3 en OGG...
echo.

cd /d "%~dp0"

:: Créer le dossier de sortie s'il n'existe pas
if not exist "src\main\resources\assets\zombiemod\sounds" (
    mkdir "src\main\resources\assets\zombiemod\sounds"
)

:: Convertir round_start
if exist "sounds\003 Nacht Der Untoten - Round Start.mp3" (
    echo Conversion de round_start.mp3...
    ffmpeg -i "sounds\003 Nacht Der Untoten - Round Start.mp3" -codec:a libvorbis -qscale:a 5 "src\main\resources\assets\zombiemod\sounds\round_start.ogg" -y
    echo [OK] round_start.ogg
    echo.
)

:: Convertir round_end
if exist "sounds\005 Nacht Der Untoten - Round End.mp3" (
    echo Conversion de round_end.mp3...
    ffmpeg -i "sounds\005 Nacht Der Untoten - Round End.mp3" -codec:a libvorbis -qscale:a 5 "src\main\resources\assets\zombiemod\sounds\round_end.ogg" -y
    echo [OK] round_end.ogg
    echo.
)

:: Convertir mystery_box
if exist "sounds\006 Nacht Der Untoten - Mystery Box.mp3" (
    echo Conversion de mystery_box.mp3...
    ffmpeg -i "sounds\006 Nacht Der Untoten - Mystery Box.mp3" -codec:a libvorbis -qscale:a 5 "src\main\resources\assets\zombiemod\sounds\mystery_box.ogg" -y
    echo [OK] mystery_box.ogg
    echo.
)

:: Convertir 115
if exist "sounds\05. 115 [Kino der Toten].mp3" (
    echo Conversion de 115.mp3...
    ffmpeg -i "sounds\05. 115 [Kino der Toten].mp3" -codec:a libvorbis -qscale:a 5 "src\main\resources\assets\zombiemod\sounds\115.ogg" -y
    echo [OK] 115.ogg
    echo.
)

echo ==========================================
echo Conversion terminée!
echo ==========================================
echo.
echo Les fichiers OGG sont dans:
echo src\main\resources\assets\zombiemod\sounds\
echo.
echo Vous pouvez maintenant compiler le mod avec:
echo gradlew build
echo.
pause

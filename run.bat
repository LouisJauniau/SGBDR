@echo off
setlocal enabledelayedexpansion

:: Chemin vers le répertoire contenant les fichiers .java
set "src_dir=%~dp0src"

:: Chemin vers le répertoire où les .class seront placés
set "bin_dir=%~dp0target\classes"

:: Chemin vers le fichier JAR directement à la racine
set "lib_dir=%~dp0json-20240303.jar"

:: Chemin vers le fichier de configuration
set "config_file=%~dp0config.json"

:: Vérifier si le fichier JAR existe
if not exist "%lib_dir%" (
    echo Erreur : le fichier JAR 'json-20240303.jar' est introuvable à l'emplacement "%lib_dir%".
    exit /b 1
)

:: Création du dossier pour les .class si nécessaire
if not exist "%bin_dir%" (
    mkdir "%bin_dir%"
)

:: Initialisation de la variable pour stocker la liste des fichiers .java
set "java_files="
for /r "%src_dir%" %%f in (*.java) do (
    set "file=%%f"
    set "java_files=!java_files! "!file!""
)

:: Vérification qu'il y a bien des fichiers à compiler
if "%java_files%"=="" (
    echo Erreur : Aucun fichier source .java trouvé dans "%src_dir%".
    exit /b 1
)

:: Compilation des fichiers .java avec le JAR dans le classpath
echo Compilation en cours...
javac -d "%bin_dir%" -classpath "%lib_dir%" %java_files%

:: Vérification que la compilation a réussi
if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation.
    exit /b 1
)

:: Exécution du programme avec le JAR dans le classpath
echo Exécution du programme...
java -cp "%bin_dir%;%lib_dir%" up.mi.jgm.bdda.SGBD "%config_file%"

pause
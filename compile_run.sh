#!/bin/bash

# Chemin vers le répertoire contenant les fichiers .java
src_dir="$(dirname "$0")/src"

# Chemin vers le répertoire où les .class seront placés
bin_dir="$(dirname "$0")/src/target/classes"

# Chemin vers le fichier JAR directement à la racine
lib_dir="$(dirname "$0")/json-20240303.jar"

# Vérifier si le fichier JAR existe
if [ ! -f "$lib_dir" ]; then
    echo "Erreur : le fichier JAR 'json-20240303.jar' est introuvable à l'emplacement '$lib_dir'."
    exit 1
fi

# Création du dossier pour les .class si nécessaire
if [ ! -d "$bin_dir" ]; then
    mkdir -p "$bin_dir"
fi

# Compilation des fichiers .java avec le JAR dans le classpath
echo "Compilation en cours..."

# Trouver tous les fichiers .java et les compiler
java_files=$(find "$src_dir" -name "*.java")
javac -d "$bin_dir" -classpath "$lib_dir" $java_files

# Vérification que la compilation a réussi
if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation."
    exit 1
fi

# Exécution du programme avec le JAR dans le classpath
echo "Exécution du programme..."
java -cp "$bin_dir:$lib_dir" up.mi.jgm.bdda.SGBD "$(dirname "$0")/config.json"

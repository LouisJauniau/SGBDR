#!/bin/bash

# Définir le répertoire du projet à partir du script (répertoire où se trouve le script)
SCRIPT_DIR=$(dirname "$0")
PROJECT_DIR=$(realpath "$SCRIPT_DIR/..")  # Chemin de base
# Chemins par défaut
LIB_DIR="${LIB_DIR:-$PROJECT_DIR/SGBDR}"
SRC_DIR="${SRC_DIR:-$PROJECT_DIR/SGBDR/src}" 
CONFIG_FILE="${CONFIG_FILE:-$PROJECT_DIR/SGBDR/config.json}"

# Compilation
javac -cp "$LIB_DIR/json-20240303.jar" $(find "$SRC_DIR" -name "*.java")

# Exécution
java -cp "$SRC_DIR:$LIB_DIR/json-20240303.jar" up.mi.jgm.bdda.SGBD "$CONFIG_FILE"
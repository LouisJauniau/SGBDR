#!/bin/bash
# Compilation des classes Java en incluant le chemin vers le JAR JSON
javac -cp .:json-20210307.jar DBConfig.java PageId.java DiskManager.java BufferManager.java Value.java ColInfo.java Record.java RecordId.java Relation.java RelationDataTest.java

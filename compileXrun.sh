#!/bin/bash

javac -cp "/Users/louisjauniau/Downloads/json-20240303.jar" $(find /Users/louisjauniau/eclipse-workspace/BDDA/SGBDR/src/up/mi/jgm/bdda/ -name "*.java")
java -cp "/Users/louisjauniau/eclipse-workspace/BDDA/SGBDR/src:/Users/louisjauniau/Downloads/json-20240303.jar" up.mi.jgm.bdda.SGBD "/Users/louisjauniau/eclipse-workspace/BDDA/SGBDR/config.json"
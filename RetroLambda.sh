#!/bin/bash
classesDir=$1
java -Dretrolambda.inputDir=$classesDir -Dretrolambda.classpath=$classesDir -jar ./retrolambda-2.0.5.jar

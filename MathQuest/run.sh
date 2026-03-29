#!/bin/zsh

mkdir -p out
javac -cp "lib/*" -d out src/comp2800_project/*.java
java -cp "out:lib/*" comp2800_project.Main

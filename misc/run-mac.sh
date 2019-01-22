#!/bin/bash

#clean
rm -rf ./com
rm -rf ./*.class

#compile CVSS
javac -Xlint:unchecked -classpath ./lib/*:. -d .  ./src/cvss/*

#compile & run tool
javac -Xlint:unchecked -classpath ./lib/*:. -d .  ./src/graph/*
javac -Xlint:unchecked -classpath ./lib/*:. -d .  ./src/modelMain.java && java -classpath ./lib/*:. modelMain ./res/ERTMSDetail.vsdx 

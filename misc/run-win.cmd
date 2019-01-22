@echo off
SET PATH=%PATH%;C:\Program Files\Java\jdk1.8.0_161\bin

rd /S /Q "./com"
del /S /F "*.class" >nul 2>&1

javac -Xlint:unchecked -classpath ./lib/*;. -d . ./src/cvss/*.java

javac -Xlint:unchecked -classpath ./lib/*;. -d . ./src/graph/*.java
javac -Xlint:unchecked -classpath ./lib/*;. -d . ./src/modelMain.java && java -Xmx10g -Xms10g -XX:+UseCompressedOops -classpath ./lib/*;. modelMain ./res/ERTMSDetail.vsdx "KMC" "Car BUS"

::java -Xmx10g -Xms10g -XX:+UseCompressedOops -classpath ./lib/*;. modelMain ./res/PowerDetail.vsdx "Primary equipment S1" "Grid control"
::java -classpath ./lib/*;. modelMain ./res/graphModel.xml ./res/assets.xml ./res/adversary.xml

::test
::javac -Xlint:unchecked -classpath ./lib/*;. -d . ./src/test/*
::java -Xmx10g -Xms10g -XX:+UseCompressedOops -classpath ./lib/*;. Permutations
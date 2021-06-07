#!/bin/bash

bugDataPath=$1                  #root folder with all bugs - D4J/projects/ (folderpath)
bugID=$2                        #folder name - Bears-56 (foldername)
targetClasses=$3                #classes  - /target/classes/ (folderpath)
targetTestClasses=$4            #test classes  -   /target/test-classes/ (folderpath)
targetMainJava=$5               #main java                  /src/main/java/ (folderpath)
targetTestJava=$6               #test java                  /src/test/java/ (folderpath)
suspiciousCodeLoc=$7            #suspicious code location - SuspiciousCodePositions/BearsPFL/B56.txt (filepath, gzoltar:all,fail)
projectWithoutGit=$8            #git or nogit  (git,nogit)
bearsord4j=$9                   #bears or defects4j (bears,d4j)
incomingDeps=${10}              #/home/gynther/.m2/repository/org/eclipse/,D4J/projects/Bears-56/target/ (none or folderpaths , separated)
readTests=${11}                 #how to read tests - bears or bears2 (bears,bears2,d4j)
runtestLocal=${12}              #testlocal or no_local_test (no_local_test,testlocal)
additionalClassPathsFile=${13}  #file with additional classpaths (noneclasspath,filepath)
mvnpath=${14}                   #defaultmaven or folder to mvn (folderpath,defaultmaven)
measure="all"                   #gzoltar or none, all (none,gzoltar,all)
verboseshell="false"            #shell output (true,false)
verbosetest="false"             #test output (true,false)



java -Xmx1g -cp "target/dependency/*" edu.lu.uni.serval.tbar.main.Main $bugDataPath $bugID $targetClasses $targetTestClasses $targetMainJava $targetTestJava $suspiciousCodeLoc $projectWithoutGit $bearsord4j $incomingDeps $readTests $runtestLocal $additionalClassPathsFile $mvnpath $measure $verboseshell $verbosetest
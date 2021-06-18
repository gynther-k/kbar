# kBar (TBar modification)
kBar is a modification of TBar, Template-based automated program repair, for standalone usage with Fault Localization integrated, reading tests from Bears, manual input of project setup and more.


**Prerequisities:**

Java 1.8 for Defects4j and Bears bugs.

**Previous test results:**

Previous test results with Bears dataset is located in Results folder.

Test scripts for Bears bugs is located in testScript.sh

**Todo:**

Add CI to repo

------


------------------------





I. Example on initialization of bug Bears-133,Bears-15 and Defects4J Lang_33
--------------
```
git submodule update --init

python bears-benchmark/scripts/checkout_bug.py --bugId Bears-133 --workspace $(pwd)/D4J/projects/

python bears-benchmark/scripts/checkout_bug.py --bugId Bears-5 --workspace $(pwd)/D4J/projects/

unzip D4J/projects/Lang_33.zip -d D4J/projects/

```

II. Modify or create the additional classpathfile
--------------
```
Modify the additional classpathfile, for example BearsClasspaths/B125.txt or generate your own:

cd D4J/projects/Bears-125

mvn -V -B -DskipTests=true -Denforcer.skip=true -Dcheckstyle.skip=true -Dcobertura.skip=true -DskipITs=true -Drat.skip=true -Dlicense.skip=true -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true clean install

mvn dependency:build-classpath -B | egrep -v "(^\[INFO\]|^\[WARNING\])" | tee BearsClasspaths/B125.txt

replace the old B125.txt with the newly generated.

```


III. Example on bugs repair with GZoltar Fault Localization (this could take a while)
--------------
```

./NormalFLTBarRunner.sh D4J/projects/ Bears-133 /target/classes/ /target/test-classes/ /src/ /test/ gzoltar:fail withgit bears none normal no_local_test BearsClasspaths/B133.txt $(pwd)/apache-maven-3.6.3/bin/mvn

./NormalFLTBarRunner.sh D4J/projects/ Lang_33 /target/classes/ /target/test-classes/ /src/main/java/ /src/test/java/ gzoltar:fail withgit d4j none normal no_local_test noneclasspath $(pwd)/apache-maven-3.6.3/bin/mvn

```



IIII. Examples with bugs and repair with Perfect Fault Localization
--------------
```

./NormalFLTBarRunner.sh D4J/projects/ Bears-133 /target/classes/ /target/test-classes/ /src/ /test/ SuspiciousCodePositions/BearsPFL/B133.txt withgit bears none normal no_local_test BearsClasspaths/B133.txt $(pwd)/apache-maven-3.6.3/bin/mvn

./NormalFLTBarRunner.sh D4J/projects/ Bears-5 /target/classes/ /target/test-classes/ /src/main/java/ /src/test/java/ SuspiciousCodePositions/BearsPFL/B5.txt withgit bears none bears2 no_local_test BearsClasspaths/B5.txt $(pwd)/apache-maven-3.6.3/bin/mvn

./NormalFLTBarRunner.sh D4J/projects/ Lang_33 /target/classes/ /target/test-classes/ /src/main/java/ /src/test/java/ SuspiciousCodePositions/Lang_33/Ochiai.txt withgit d4j none normal no_local_test noneclasspath $(pwd)/apache-maven-3.6.3/bin/mvn

```


V. Instructions
--------------
```
The following arguments could be set to TBar:

bugDataPath - root folder with all bugs - D4J/projects/ (folderpath)
bugID - folder name which should correspond to the Bug-Id - Bears-56 (foldername)
targetClasses - classes folder - /target/classes/ (folderpath)
targetTestClasses - test-classes folder -  /target/test-classes/ (folderpath)
targetMainJava - /src/main/java/ folder (folderpath)
targetTestJava - /src/test/java/ folder (folderpath)
suspiciousCodeLoc - text file with one or many suspicious code locations OR enable gzoltar - SuspiciousCodePositions/BearsPFL/B56.txt - (filepath or gzoltar:all,fail)
projectWithoutGit - with git or nogit to run TBar with projects without git (git,nogit)
bearsord4j - compile and run for bears or Defects4j bugs (bears,d4j)
incomingDeps - dependencies root folders separated with , which should be scanned for .jar files (none or folderpaths , separated)
readTests - how to read test output bears,bears2 or d4j (bears,bears2,d4j)
runtestLocal - run tests from root folder or test folder (no_local_test,testlocal)
additionalClassPathsFile - text file with classpaths built with mvn dependency:build-classpath or manually (noneclasspath,filepath)
mvnpath - path to maven for using same version when running tests (folderpath,defaultmaven)

Arguments set in NormalFLTBarRunner.sh:
measure - measure (gzoltar,none or all)
verboseshell - see what happens in the shell, (true or false)
verbosetest - see what happens when excecuting tests, (true or false)

```


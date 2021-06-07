package edu.lu.uni.serval.tbar.utils;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.ArrayList; 
import java.util.List;
import java.io.FileReader;
import edu.lu.uni.serval.tbar.config.Configuration;
import java.io.*;  



public class TestUtils {

/*

    This class contains two methods for specific test output. 
    Add additional methods for project specific output:

    getFailTestNumInProject()
    getFailTestNumInProjectBears2()
    */

    

public static int getFailTestNumInProjectBears2(String projectName, List<String> failedTests){
/*
Test output example in the following format (do not read Results):

READ ERRORS
1. if row.contains Tests run: && Failures: && Errors: && !Errors: 0   
2. startREAD error
3. if ERROR! && !str.contains(in)    Collect

READ FAILURES
1. if row.contains Tests run: && Failures: && Errors: && Errors: 0  && !Failures: 0 
2. startREAD faiure
3. if contains FAILURE! && !contains in Collect

Tests run: 31, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.025 sec <<< FAILURE! - in com.fasterxml.jackson.databind.deser.TestMapDeserialization
testcharSequenceKeyMap(com.fasterxml.jackson.databind.deser.TestMapDeserialization)  Time elapsed: 0.005 sec  <<< ERROR!
com.fasterxml.jackson.databind.JsonMappingException: Can not find a (Map) Key deserializer for type [simple type, class java.lang.CharSequence]
 at [Source: {"a":"b"}; line: 1, column: 1]
	at com.fasterxml.jackson.databind.JsonMappingException.from(JsonMappingException.java:284)
	at com.fasterxml.jackson.databind.deser.DeserializerCache._handleUnknownKeyDeserializer(DeserializerCache.java:587)
	at com.fasterxml.jackson.databind.deser.DeserializerCache.findKeyDeserializer(DeserializerCache.java:168)
	at com.fasterxml.jackson.databind.DeserializationContext.findKeyDeserializer(DeserializationContext.java:500)
	at com.fasterxml.jackson.databind.deser.std.MapDeserializer.createContextual(MapDeserializer.java:231)
	at com.fasterxml.jackson.databind.DeserializationContext.handleSecondaryContextualization(DeserializationContext.java:685)
	at com.fasterxml.jackson.databind.DeserializationContext.findRootValueDeserializer(DeserializationContext.java:482)
	at com.fasterxml.jackson.databind.ObjectMapper._findRootDeserializer(ObjectMapper.java:3908)
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:3803)
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:2816)
	at com.fasterxml.jackson.databind.deser.TestMapDeserialization.testcharSequenceKeyMap(TestMapDeserialization.java:507)


(Do not read here because all the information org.*.* is not avaiable)
Results :

Tests in error: 
  TestMapDeserialization.testcharSequenceKeyMap:507 » JsonMapping Can not find a...

  TRUE SKA VA:

  com.fasterxml.jackson.databind.deser.TestMapDeserialization.testcharSequenceKeyMap
  com.fasterxml.jackson.databind.JsonMappingException: Can not find a (Map) Key deserializer for type [simple type, class java.lang.CharSequence] at [Source: {"a":"b"}; line: 1, column: 1]

*/    
    String testResult = getProjectResultTest(projectName,"test");



    try {
    //File fileTestOutput = new File("/home/gynther/Desktop/thesisJan28LocalStartChange/TBar/D4J/projects/Lang_33/testOutput.txt"); 
    File fileTestOutput = new File(Configuration.GLOBAL_TEMP_FILES_PATH+"testOutPut.txt"); 

    //BufferedReader br = new BufferedReader(new FileReader(fileTestOutput)); //innan med bears

    Reader inputString = new StringReader(testResult);
    BufferedReader br = new BufferedReader(inputString);


    String st; 
    List<String> failedTestCmdLine = new ArrayList<>();
    List<String> errorTestCmdLine = new ArrayList<>();
    boolean readFailedtests = false;
    boolean readErrortests = false;
    boolean readBoth = false;
    boolean buildSuccess = false;
    int lineCount = 0;

    while ((st = br.readLine()) != null)
    {
        lineCount++;
        if(Configuration.testVerbose)
        {
            System.out.println(st); 
        }
        

        //*************************** Failed tests ***********************************************************************/

        //2. Read the lines
        if(readFailedtests && !st.trim().isEmpty() && !st.contains("- in") && st.contains("FAILURE!")) 
        {
            String[] splittedStrings = st.trim().split("\\(");
            String[] splittedStringsClass = splittedStrings[1].trim().split("\\)");

            
            String testIndividual = splittedStrings[0];
            String testClass = splittedStringsClass[0].trim();

            if(Configuration.testVerbose)
            {
            System.out.println(testClass);
            System.out.println(testIndividual);
            }
    
            failedTestCmdLine.add(testClass+"::"+testIndividual);
        }

        //1. Read failures 
        if(st.contains("Tests run:") && st.contains("Failures:") && st.contains("Errors:") && st.contains("Errors: 0") && !st.contains("Failures: 0")) 
        {
            if(Configuration.testVerbose)
            {
            System.out.println("XXXXXX Read failures");
            }
            readFailedtests=true;
            //failedTestCmdLine.clear();
        }

        //3. Stop reading
        if(readFailedtests && st.trim().isEmpty()){ 
            readFailedtests=false;
        }

        //*************************** Tests in error ***********************************************************************/

        //2. Read the lines
        if(readErrortests && !st.trim().isEmpty() && !st.contains("- in") && st.contains("ERROR!")) 
        {

            String[] splittedStringsE = st.trim().split("\\(");
            String[] splittedStringsClassE = splittedStringsE[1].trim().split("\\)");
            String testIndividualE = splittedStringsE[0];
            String testClassE = splittedStringsClassE[0].trim();

            if(Configuration.testVerbose)
            {
                System.out.println(testIndividualE);
                System.out.println(testClassE);
            }

            errorTestCmdLine.add(testClassE+"::"+testIndividualE);
        }

        // 1. START READING ERRORS
        if(st.contains("Tests run:") && st.contains("Failures:") && st.contains("Errors:") && !st.contains("Errors: 0")) 
        {
            readErrortests=true;
        }

        //3. Stop reading
        if(readErrortests && st.trim().isEmpty()){ 
            readErrortests=false;
        }

        /* ********************************** Test with both Failure and Error: ***************************************************/

        //2. Read the lines Error
        if(readBoth && !st.trim().isEmpty() && !st.contains("- in") && st.contains("ERROR!")) 
        {
            String[] splittedStringsE = st.trim().split("\\(");
            String[] splittedStringsClassE = splittedStringsE[1].trim().split("\\)");
            
            String testIndividualE = splittedStringsE[0];
            String testClassE = splittedStringsClassE[0].trim();

            if(Configuration.testVerbose)
            {
                System.out.println(testIndividualE);
                System.out.println(testClassE);
            }

            errorTestCmdLine.add(testClassE+"::"+testIndividualE);
        }

        //2. Read the lines Failure
        if(readBoth && !st.trim().isEmpty() && !st.contains("- in") && st.contains("FAILURE!")) 
        {
            String[] splittedStrings = st.trim().split("\\(");
            String[] splittedStringsClass = splittedStrings[1].trim().split("\\)");
            
            String testIndividual = splittedStrings[0];
            String testClass = splittedStringsClass[0].trim();


            if(Configuration.testVerbose)
            {
            System.out.println(testClass);
            System.out.println(testIndividual);
            }


            failedTestCmdLine.add(testClass+"::"+testIndividual);
        }

        // START READING ERRORS
        if(st.contains("Tests run:") && st.contains("Failures:") && st.contains("Errors:") && !st.contains("Errors: 0") && !st.contains("Failures: 0")) 
        {
            readBoth=true;
        }

        /*if(readBoth && st.trim().isEmpty()){ //3. Stop reading Results || Running
            readBoth=false;
        }*/
        if(readBoth && (st.contains("Results") || st.contains("Running"))){ //3. Stop reading Results || Running
            readBoth=false;
        }


        if(st.contains("BUILD SUCCESS"))
        {
            buildSuccess=true;
            if(Configuration.testVerbose)
            {
    
            System.out.println("BUILD SUCESS - XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            }
        }
        if(st.contains("BUILD FAILURE"))
        {
            if(Configuration.testVerbose)
            {

            System.out.println("BUILD FAILURE - XXXXXXXXXXXXXXXXXXXXXXXXXX");
            }
        }
    }
    
    if(Configuration.testVerbose)
    {

    System.out.println("xx - sizeoferrortest: "+errorTestCmdLine.size());
    System.out.println("xx - sizeoffailtest: "+failedTestCmdLine.size());
    }
    fileTestOutput.delete();

    int errorNum=0;

    if (lineCount<2){//error occurs in run
        return Integer.MAX_VALUE;
    }
    if(buildSuccess)
    {
        return 0;
    }

    failedTests.addAll(errorTestCmdLine);
    errorNum=errorTestCmdLine.size();

    failedTests.addAll(failedTestCmdLine);
    errorNum=errorNum+failedTestCmdLine.size();

    return errorNum;


    }
    catch(IOException e){
        System.out.println("xx - Fail to create file");
        return Integer.MAX_VALUE;
    }

    //return Integer.MAX_VALUE;

}

    //Normal Bears
	public static int getFailTestNumInProject(String projectName, List<String> failedTests){

        /* 
Modified for the following test output:
        
Mvn build success:
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 sec
Running org.apache.commons.lang3.mutable.MutableByteTest
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 sec

Results :

Tests run: 1670, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  9.793 s
[INFO] Finished at: 2021-01-28T17:53:52+01:00
[INFO] ------------------------------------------------------------------------
*/


/* Mvn build failure
Results :

Failed tests: 
  testReflectionLongArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionIntArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionShortArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionyteArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionCharArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionDoubleArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionFloatArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionBooleanArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionFloatArrayArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionLongArrayArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionIntArrayArray(org.apache.commons.lang3.builder.ToStringBuilderTest)
  testReflectionhortArrayArray(org.apache.commons.lang3.builder.ToStringBuilderTest)

Tests in error: 
  testToClass_object(org.apache.commons.lang3.ClassUtilsTest)

Tests run: 1670, Failures: 12, Errors: 1, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE


*/

       
        String testResult = getProjectResultTest(projectName,"test");

        try {
        File fileTestOutput = new File(Configuration.GLOBAL_TEMP_FILES_PATH+"testOutPut.txt"); 

        Reader inputString = new StringReader(testResult);
        BufferedReader br = new BufferedReader(inputString);


        String st; 
        List<String> failedTestCmdLine = new ArrayList<>();
        List<String> errorTestCmdLine = new ArrayList<>();
        boolean readFailedtests = false;
        boolean readErrortests = false;
        boolean buildSuccess = false;
        int lineCount = 0;

        while ((st = br.readLine()) != null)
        {
            lineCount++;

            if(Configuration.testVerbose)
            {
                System.out.println(st); 
            }
    

            //**************************************Failed tests ***************************/

            //2. Read the lines
            if(readFailedtests && !st.trim().isEmpty()) 
            {
                String[] splittedStrings = st.trim().split("\\(");

                if(Configuration.testVerbose)
                {
                    System.out.println(splittedStrings[0]);
                    System.out.println(splittedStrings[1]);
                }
    
                String[] splittedStringsClass = splittedStrings[1].trim().split("\\)");

                
                String testIndividual = splittedStrings[0];
                String testClass = splittedStringsClass[0].trim();

                failedTestCmdLine.add(testClass+"::"+testIndividual);
            }

            //1. Get subject
            if(st.contains("Failed tests:")) 
            {
                if(Configuration.testVerbose)
                {
                    System.out.println("XX - FAILED TEST GET SUBJECT");
                }

                readFailedtests=true;
                failedTestCmdLine.clear();

                //if test result is located on the same line, otherwise continue to 2.
                if(st.contains("(") && st.contains(")"))
                {

                    st = st.replace("Failed tests:","");


                    String[] splittedStrings = st.trim().split("\\(");

        
                    String[] splittedStringsClass = splittedStrings[1].trim().split("\\)");
    
                    
                    String testIndividual = splittedStrings[0];
                    String testClass = splittedStringsClass[0].trim();
                    if(Configuration.testVerbose)
                    {
                    System.out.println(testClass+"::"+testIndividual);
                    }

    
                    failedTestCmdLine.add(testClass+"::"+testIndividual);   

                }

            }

            //3. Stop reading
            if(readFailedtests && st.trim().isEmpty()){ 
                readFailedtests=false;
            }

            /********************** Read error tests ************************************/

            //2. Read the lines:
            if(readErrortests && !st.trim().isEmpty()) 
            {
                String[] splittedStringsE = st.trim().split("\\(");

                if(Configuration.testVerbose)
                {
                    System.out.println(splittedStringsE[0]);
                    System.out.println(splittedStringsE[1]);
                }


                String[] splittedStringsClassE = splittedStringsE[1].trim().split("\\)");

                String testIndividualE = splittedStringsE[0];
                String testClassE = splittedStringsClassE[0].trim();


                if(Configuration.testVerbose)
                {
                    System.out.println(testIndividualE);
                    System.out.println(testClassE);
                }


                errorTestCmdLine.add(testClassE+"::"+testIndividualE);
            }

            //1. Get subject
            if(st.contains("Tests in error:")) 
            {
                readErrortests=true;
                errorTestCmdLine.clear();


            //if test result is located on the same line, otherwise continue to 2.
            if(st.contains("(") && st.contains(")"))
            {

                st = st.replace("Tests in error:","");

                String[] splittedStringsE = st.trim().split("\\(");
                String[] splittedStringsClassE = splittedStringsE[1].trim().split("\\)");

                String testIndividualE = splittedStringsE[0];
                String testClassE = splittedStringsClassE[0].trim();

                if(Configuration.testVerbose)
                {
                System.out.println(testIndividualE+"::"+testClassE);
                }

                errorTestCmdLine.add(testClassE+"::"+testIndividualE);

            }

            }

            //3. Stop reading
            if(readErrortests && st.trim().isEmpty()){ 
                readErrortests=false;
            }

            if(st.contains("BUILD SUCCESS"))
            {
                buildSuccess=true;
                System.out.println("BUILD SUCESS - XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            }
            if(st.contains("BUILD FAILURE"))
            {
                System.out.println("BUILD FAILURE - XXXXXXXXXXXXXXXXXXXXXXXXXX");
            }
        }

        System.out.println("xx - sizeoferrortest:"+errorTestCmdLine.size());
        System.out.println("xx - sizeoffailtest:"+failedTestCmdLine.size());
        fileTestOutput.delete();

        int errorNum=0;

        if (lineCount<2){//error occurs in run
            return Integer.MAX_VALUE;
        }
        if(buildSuccess)
        {
            return 0;
        }

        failedTests.addAll(errorTestCmdLine);
        errorNum=errorTestCmdLine.size();
        
        failedTests.addAll(failedTestCmdLine);
        errorNum=errorNum+failedTestCmdLine.size();

        return errorNum;


        }
        catch(IOException e){
            System.out.println("FAILFILECREATION");
            return Integer.MAX_VALUE;
        }


	}
	
//	public static int getFailTestNumInProject(String buggyProject, List<String> failedTests, String classPath,
//			String testClassPath, String[] testCasesArray){
//		StringBuilder builder = new StringBuilder();
//		for (String testCase : testCasesArray) {
//			builder.append(testCase).append(" ");
//		}
//		String testCases = builder.toString();
//		
//		String testResult = "";
//		try {
//			testResult = ShellUtils.shellRun(Arrays.asList("java -cp " + PathUtils.buildClassPath(classPath, testClassPath)
//					+ " org.junit.runner.JUnitCore " + testCases), buggyProject);
//		} catch (IOException e) {
////			e.printStackTrace();
//		}
//		
//        if (testResult.equals("")){//error occurs in run
//            return Integer.MAX_VALUE;
//        }
//        if (!testResult.contains("Failing tests:")){
//            return Integer.MAX_VALUE;
//        }
//        int errorNum = 0;
//        String[] lines = testResult.trim().split("\n");
//        for (String lineString: lines){
//            if (lineString.startsWith("Failing tests:")){
//                errorNum =  Integer.valueOf(lineString.split(":")[1].trim());
//                if (errorNum == 0) break;
//            } else if (lineString.startsWith("Running ")) {
//            	break;
//            } else {
//            	failedTests.add(lineString);
//            }
//        }
//        return errorNum;
//	}
	
	/*public static int compileProjectWithDefects4j(String projectName, String defects4jPath) {
		String compileResults = getDefects4jResult(projectName, defects4jPath, "compile");
		String[] lines = compileResults.split("\n");
		if (lines.length != 2) return 1;
        for (String lineString: lines){
        	if (!lineString.endsWith("OK")) return 1;
        }
		return 0;
    }*/
    
	public static int compileProject(String projectName) {
		String compileResults = getProjectResultCompile(projectName,"compile");
		String[] lines = compileResults.split("\n");
		/*if (lines.length != 2) return 1;
        for (String lineString: lines){
        	if (!lineString.endsWith("OK")) return 1;
        }*/
		return 0;
	}

	/*private static String getDefects4jResult(String projectName, String defects4jPath, String cmdType) {
		try {
			String buggyProject = projectName.substring(projectName.lastIndexOf("/") + 1);
			//which java\njava -version\n
            String result = ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", defects4jPath + "framework/bin/defects4j " + cmdType + "\n"), buggyProject, cmdType.equals("test") ? 2 : 1);//"defects4j " + cmdType + "\n"));//
            return result.trim();
        } catch (IOException e){
        	e.printStackTrace();
            return "";
        }
    }*/

    private static String getProjectResultCompile(String projectName, String cmdType) {
		try {
            String buggyProject = projectName.substring(projectName.lastIndexOf("/") + 1);
            String result=null;
            if(Configuration.bugDataSet.equals("d4j"))
            {
            
//          String result = ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", defects4jPath + "framework/bin/defects4j " + cmdType + "\n"), buggyProject, cmdType.equals("test") ? 2 : 1);//"defects4j " + cmdType + "\n"));//
            result = ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", Configuration.mvnPathfromCmdLine+" -Dmaven.test.skip clean install"), buggyProject, cmdType.equals("test") ? 2 : 1);//"defects4j " + cmdType + "\n"));//
            //System.exit(0);
            }
            if(Configuration.bugDataSet.equals("bears"))
            {
            result = ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", Configuration.mvnPathfromCmdLine+" -V -B -DskipTests=true -Denforcer.skip=true -Dcheckstyle.skip=true -Dcobertura.skip=true -DskipITs=true -Drat.skip=true -Dlicense.skip=true -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true clean install"), buggyProject, cmdType.equals("test") ? 2 : 1);//"defects4j " + cmdType + "\n"));//
            }

            if(Configuration.ShellVerbose)
            {
                System.out.println("XX javac output:");
                System.out.println(result);
            }

            return result.trim();
        } catch (IOException e){
        	e.printStackTrace();
            return "";
        }
    }

    private static String getProjectResultTest(String projectName, String cmdType) {
		try {
			String buggyProject = projectName.substring(projectName.lastIndexOf("/") + 1);

            //String result = ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", defects4jPath + "framework/bin/defects4j " + cmdType + "\n"), buggyProject, cmdType.equals("test") ? 2 : 1);//"defects4j " + cmdType + "\n"));//
            String result=null;
            if(Configuration.bugDataSet.equals("d4j"))
            {
            result = ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", Configuration.mvnPathfromCmdLine+" -Dsurefire.junit4.upgradecheck -Dsurefire.rerunFailingTestsCount=3 test | tee "+Configuration.GLOBAL_TEMP_FILES_PATH+"testOutPut.txt"+" -a"), buggyProject, cmdType.equals("test") ? 2 : 1);//"defects4j " + cmdType + "\n"));//
            }

            if(Configuration.bugDataSet.equals("bears"))
            {
            result = ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", Configuration.mvnPathfromCmdLine+" -V -B -Denforcer.skip=true -Dcheckstyle.skip=true -Dcobertura.skip=true -DskipITs=true -Drat.skip=true -Dlicense.skip=true -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true test | tee "+Configuration.GLOBAL_TEMP_FILES_PATH+"testOutPut.txt"), buggyProject, cmdType.equals("test") ? 2 : 1);//"defects4j " + cmdType + "\n"));//
            }
            
            return result.trim();
        } catch (IOException e){
        	e.printStackTrace();
            return "";
        }
    }


	public static String recoverWithGitCmd(String projectName) {
		try {
			String buggyProject = projectName.substring(projectName.lastIndexOf("/") + 1);
            ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", "git checkout -- ."), buggyProject, 1);
            return "";
        } catch (IOException e){
            return "Failed to recover.";
        }
	}

	public static String readPatch(String projectName) {
		try {
			String buggyProject = projectName.substring(projectName.lastIndexOf("/") + 1);
            //return ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", "git diff"), buggyProject, 1).trim();
            if(!Configuration.NO_GIT)
            {
                return ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", "git diff"), buggyProject, 1).trim();
            }
            else{
                return null;
            }
        } catch (IOException e){
            return null;
        }
	}

	public static String checkout(String projectName) {
		try {
			String buggyProject = projectName.substring(projectName.lastIndexOf("/") + 1);
            //return ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", "git checkout -- ."), buggyProject, 1).trim();

            if (Configuration.NO_GIT)
            {
                return ShellUtils.shellRun(Arrays.asList("cd " + projectName), buggyProject, 1).trim();

            }
            else{
                return ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", "git checkout -- ."), buggyProject, 1).trim();

            }

        } catch (IOException e){
            return null;
        }
	}

}

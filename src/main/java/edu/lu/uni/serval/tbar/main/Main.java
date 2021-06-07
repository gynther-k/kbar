package edu.lu.uni.serval.tbar.main;

import java.io.File;

import edu.lu.uni.serval.tbar.AbstractFixer;
import edu.lu.uni.serval.tbar.TBarFixer;
import edu.lu.uni.serval.tbar.config.Configuration;
import java.util.ArrayList;
import java.util.Map;
import java.io.*;

/**
 * Fix bugs with Fault Localization results.
 * 
 * @author kui.liu
 * modified by Gunnar Applelid - gynther-k
 *
 */

public class Main {

	public static long totalstartTime;
	
	public static void main(String[] args) {

		totalstartTime = System.nanoTime();    //Measurements
		
				

		if (args.length < 3) {
			System.out.println("Arguments: \n" 
					+ "\t<Bug_Data_Path>: the directory of checking out Defects4J bugs. \n"
					+ "\t<Bug_ID>: bug id of each Defects4J bug, such as Chart_1. \n"
//					+ "\t<Suspicious_Code_Positions_File_Path>: \n"
//					 +"\t<Failed_Test_Cases_File_Path>: \n"
					+ "\t<defects4j_Home>: the directory of defects4j git repository.\n");
			System.exit(0);
		}
		String bugDataPath = args[0]; // root folder with all bugs - D4J/projects/ (folderpath)
		String bugId = args[1]; // folder name - Bears-56 (foldername)

		ArrayList<String> pathsFromCmdLine = new ArrayList<String>(); //add paths manually
		pathsFromCmdLine.add(args[2]); // /target/classes/	(folderpath)
		pathsFromCmdLine.add(args[3]); // /target/test-classes/ (folderpath)
		pathsFromCmdLine.add(args[4]); // /src/main/java/ (folderpath)
		pathsFromCmdLine.add(args[5]); // /src/test/java/ (folderpath)

		String pathToSuspCodeCmdLine = args[6]; // suspicious code location SuspiciousCodePositions/BearsPFL/B56.txt (filepath, gzoltar:all,fail)
		String projecWithoutGit = args[7]; // (git,nogit)
		String bearsord4j = args[8]; // bears or defects4j (bears,d4j)
		String incomingDeps = args[9]; //  folder scanned for .jar files (none or folderpaths , separated)
		String readTests = args[10]; //	how to read tests - (bears,bears2,d4j)
		String runTestLocally = args[11]; // run tests locally (no_local_test,testlocal)
		String additionclasspaths = args[12]; //file with additional classpaths (noneclasspath, filepath)
		String mavenpath = args[13]; //defaultmaven or folder to mvn (folderpath,defaultmaven)
		String measureIn = args[14]; //measure none, gzoltar or all (none,gzoltar,all)
		String verbshell = args[15]; //shell ouput true or false (true,false)
		String verbtest = args[16]; //test output true or false  (true,false)


		if(projecWithoutGit.equals("nogit"))
		{
			Configuration.NO_GIT=true;
		}

		if(bearsord4j.equals("d4j")) // || bears (default)
		{
			Configuration.bugDataSet="d4j";
		}


		Configuration.measurements=measureIn;

		if(!mavenpath.equals("defaultmaven"))
		{
			Configuration.mvnPathfromCmdLine=mavenpath;
		}

		if(runTestLocally.equals("testlocal"))
		{
			Configuration.run_tests_locally=true;
		}

		if(!additionclasspaths.equals("noneclasspath"))
		{

			try (BufferedReader br = new BufferedReader(new FileReader(additionclasspaths))) {
				String line;
				while ((line = br.readLine()) != null) {
				   Configuration.additionalClasspathsFromCmdLine=line;
				}
			}
			catch(Exception e)
			{
				System.out.println("Could not find additional classpath file:"+additionclasspaths);
				System.exit(0);
			}
			
		}

		String[] arrSplit = incomingDeps.split(",");
		for (int i=0; i < arrSplit.length; i++)
		{
			Configuration.additionalDepsFromCmdLine.add(arrSplit[i]);
		}

		if(readTests.equals("bears") || readTests.equals("bears2") || readTests.equals("d4j"))
		{
			Configuration.testOutputAdapter_for=readTests;
		}


		//print shell output
		if(verbshell.equals("true"))
		{
		Configuration.ShellVerbose=true;
		}
		if(verbtest.equals("true"))
		{
		Configuration.testVerbose=true;
		}
	
		fixBug(bugDataPath, bugId,pathsFromCmdLine,pathToSuspCodeCmdLine);
	}

	public static void fixBug(String bugDataPath,String bugIdStr,ArrayList<String> pathsFromCmdLine,String pathToSuspCodeCmdLine) {
		Configuration.outputPath += "NormalFL/";
		String suspiciousFileStr = Configuration.suspPositionsFilePath;
		Configuration.bugId=bugIdStr;
		
		//String[] elements = bugIdStr.split("_");
		//String projectName = elements[0];
		//int bugId;
		//try {
		//	bugId = Integer.valueOf(elements[1]);
		//} catch (NumberFormatException e) {
		//	System.out.println("Please input correct buggy project ID, such as \"Chart_1\".");
		//	return;
		//}
		
		AbstractFixer fixer = new TBarFixer(bugDataPath, bugIdStr,pathsFromCmdLine);
		fixer.dataType = "TBar";
		fixer.metric = Configuration.faultLocalizationMetric;
		Configuration.gzoltarArguments=pathToSuspCodeCmdLine;
		fixer.suspCodePosFile = new File(pathToSuspCodeCmdLine); //new File("SuspiciousCodePositions/Lang_33/Ochiai.txt");

		if (Integer.MAX_VALUE == fixer.minErrorTest) {
			if(Configuration.testVerbose)
			{ 
			System.out.println("Failed to defects4j compile bug " + bugIdStr);
			}
			return;
		}
		
		fixer.fixProcess();


		//Measurements

		if(Configuration.measurements.contains("all"))
		{
		int totalPatchCandidates = 0;
		for(Map.Entry<String, Integer> entry : Configuration.freq.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			System.out.println("FixPattern: "+key+" PatchCandidates: "+value); //key: org.apache.commons.lang3.ClassUtils
			totalPatchCandidates+=value;
			
		}
		System.out.println("Last fixpattern: "+Configuration.lastFixPattern);
		System.out.println("Total patch candidates: "+totalPatchCandidates);
		System.out.println("Dictionary size: "+Configuration.dictionarysize);
		System.out.println("Dictionary time: "+((double) Configuration.dicttime / 1_000_000_000));
		long totalEndTime = System.nanoTime() - totalstartTime;
		System.out.println("Total time: "+((double) totalEndTime / 1_000_000_000)); 
		System.out.println("generatePatchTime time: "+((double) Configuration.generatePatchTime / 1_000_000_000)); 
		System.out.println("validatePatchTime time: "+((double) Configuration.validatePatchTime / 1_000_000_000)); 
		}
			
		int fixedStatus = fixer.fixedStatus;
		switch (fixedStatus) {
		case 0:
			System.out.println("Failed to fix bug " + bugIdStr);
			break;
		case 1:
			System.out.println("Succeeded to fix bug " + bugIdStr);
			break;
		case 2:
			System.out.println("Partial succeeded to fix bug " + bugIdStr);
			break;
		}
	}

}

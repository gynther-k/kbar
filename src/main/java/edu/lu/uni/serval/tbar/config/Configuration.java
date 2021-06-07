package edu.lu.uni.serval.tbar.config;

import java.util.List;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Map;


public class Configuration {


	public static String measurements = "none";
	public static String knownBugPositions = "BugPositions.txt";
	public static String suspPositionsFilePath = "SuspiciousCodePositions/";
	public static String suspPositionsFilePathGZoltar = "gzoltarOutput/";
	public static String gzoltarArguments = "none";
	//public static String failedTestCasesFilePath = "FailedTestCases/";
	public static String faultLocalizationMetric = "Ochiai";
	public static String outputPath = "OUTPUT/";
	public static boolean NO_GIT = false;
	public static boolean ShellVerbose = false;
	public static boolean testVerbose = false;
	public static String testOutputAdapter_for="bears";
	public static String bugDataSet = "bears"; // d4j or bears
	public static boolean run_tests_locally = false;

	public static List<String> additionalDepsFromCmdLine = new ArrayList<>();
	public static String additionalClasspathsFromCmdLine = "";
	public static String mvnPathfromCmdLine = "mvn";

	//public static final String TEMP_FILES_PATH = ".temp/";
	public static final String TEMP_FILES_PATH = "/tmp/";
	public static final String GLOBAL_TEMP_FILES_PATH = "/tmp/";

	public static final long SHELL_RUN_TIMEOUT = 300L;
	public static final long TEST_SHELL_RUN_TIMEOUT = 600L;

	public static int dictionarysize = 0;
	public static Map<String, Integer> freq = new HashMap<String, Integer>();
	public static long dicttime = 0;
	public static String lastFixPattern = "";
	public static long generatePatchTime = 0;
	public static long validatePatchTime = 0;
	public static String bugId = "";

}

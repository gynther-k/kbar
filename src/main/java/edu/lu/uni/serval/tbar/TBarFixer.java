package edu.lu.uni.serval.tbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.config.Configuration;
import edu.lu.uni.serval.tbar.context.ContextReader;
import edu.lu.uni.serval.tbar.fixpatterns.CNIdiomNoSuperCall;
import edu.lu.uni.serval.tbar.fixpatterns.ClassCastChecker;
import edu.lu.uni.serval.tbar.fixpatterns.ConditionalExpressionMutator;
import edu.lu.uni.serval.tbar.fixpatterns.DataTypeReplacer;
import edu.lu.uni.serval.tbar.fixpatterns.ICASTIdivCastToDouble;
import edu.lu.uni.serval.tbar.fixpatterns.LiteralExpressionMutator;
import edu.lu.uni.serval.tbar.fixpatterns.MethodInvocationMutator;
import edu.lu.uni.serval.tbar.fixpatterns.NPEqualsShouldHandleNullArgument;
import edu.lu.uni.serval.tbar.fixpatterns.NullPointerChecker;
import edu.lu.uni.serval.tbar.fixpatterns.OperatorMutator;
import edu.lu.uni.serval.tbar.fixpatterns.RangeChecker;
import edu.lu.uni.serval.tbar.fixpatterns.ReturnStatementMutator;
import edu.lu.uni.serval.tbar.fixpatterns.StatementInserter;
import edu.lu.uni.serval.tbar.fixpatterns.StatementMover;
import edu.lu.uni.serval.tbar.fixpatterns.StatementRemover;
import edu.lu.uni.serval.tbar.fixpatterns.VariableReplacer;
import edu.lu.uni.serval.tbar.fixtemplate.FixTemplate;
import edu.lu.uni.serval.tbar.info.Patch;
import edu.lu.uni.serval.tbar.utils.Checker;
import edu.lu.uni.serval.tbar.utils.FileHelper;
import edu.lu.uni.serval.tbar.utils.SuspiciousPosition;
import edu.lu.uni.serval.tbar.utils.PathUtils;
import edu.lu.uni.serval.tbar.utils.ShellUtils;
import java.io.*;


/**
 * 
 * @author kui.liu
 *
 */
@SuppressWarnings("unused")
public class TBarFixer extends AbstractFixer {
	
	public Granularity granularity = Granularity.FL;
	
	public enum Granularity {
		Line,
		File,
		FL
	}
	
	private static Logger log = LoggerFactory.getLogger(TBarFixer.class);
	
	public TBarFixer(String path, String bugIdStr, ArrayList<String> pathsFromCmdLine) {
		super(path,bugIdStr, pathsFromCmdLine);
	}
	
	/*public TBarFixer(String path, String metric, String projectName, int bugId, String defects4jPath) {
		super(path, metric, projectName, bugId, defects4jPath);
	}*/

	@Override
	public void fixProcess() {
		// Read paths of the buggy project.
		if (!dp.validPaths) return;
		
		// Read suspicious positions.
		List<SuspiciousPosition> suspiciousCodeList = null;
		if (granularity == Granularity.Line) {
			// It assumes that the line-level bug positions are known.
			suspiciousCodeList = readKnownBugPositionsFromFile();
		} else if (granularity == Granularity.File) {
			// It assumes that the file-level bug positions are known.
			List<String> buggyFileList = readKnownFileLevelBugPositions();
			suspiciousCodeList = readSuspiciousCodeFromFile(buggyFileList);
		} else {//Default with kbar

			String argumentsIn = Configuration.gzoltarArguments;
			
			//GZOLTAR Fault Localization arguments: gzoltar:all,fail
			if(argumentsIn.contains("gzoltar"))
			{
		
			long totalstartTimeGzoltar = System.nanoTime();	

			String gzoltarargs[] = argumentsIn.split(":"); 
			String arg1 = gzoltarargs[0]; //gzoltar
			String modeAll = gzoltarargs[1]; //all or fail

			String testsToGzoltar = ""; 
			int counterG=0;

			for (String failedTest : failedTestStrList) {

				int index=failedTest.lastIndexOf('.');
				String testlocal=failedTest.substring(0,index)+"."+"*";

				if (counterG>0)
				{
					testsToGzoltar+=":"; 
				}
				

				testsToGzoltar+=testlocal; 

				counterG++;
			}

			if(modeAll.equals("all"))
			{
				testsToGzoltar="*";
			}
			
			String GZOLTAR_CLI_JAR="lib/com.gzoltar.cli-1.7.3-SNAPSHOT-jar-with-dependencies.jar";
			String GZOLTAR_AGENT_JAR="lib/com.gzoltar.agent.rt-1.7.3-SNAPSHOT-all.jar";

			String classpathGzoltar = PathUtils.buildCompileClassPath(Arrays.asList(PathUtils.getJunitPath()), dp.classPath, dp.testClassPath, dp.libPaths);
			String list_of_tests_to_run_file="gzoltarOutput/"+this.buggyProject+"/tests_to_run.txt";
			String list_of_tests_to_run_file_temp="gzoltarOutput/"+this.buggyProject+"/tests_to_runtempfile.txt";
			String ser_file = "gzoltarOutput/"+this.buggyProject+"/gzoltar.ser";

			try{
			String gzoltarResult = ShellUtils.shellRun(Arrays.asList(
			"rm -rf gzoltarOutput/"+this.buggyProject + "\n", 
			"mkdir -p gzoltarOutput/"+this.buggyProject + "\n", 
			"rm -f "+list_of_tests_to_run_file + "\n", 
			"rm -f "+list_of_tests_to_run_file_temp + "\n", 
			"java -cp "+classpathGzoltar+GZOLTAR_CLI_JAR+" com.gzoltar.cli.Main listTestMethods "+dp.testClassPath+" --outputFile "+list_of_tests_to_run_file+" --includes \""+testsToGzoltar+"\""+"\n" 
			), buggyProject, "test".equals("test") ? 2 : 1);



			String gzoltarResult2 = ShellUtils.shellRun(Arrays.asList(
				"cat " + list_of_tests_to_run_file + "\n",
				"rm -f " + ser_file + "\n", 
				"java -javaagent:"+GZOLTAR_AGENT_JAR+"=destfile="+ser_file+",buildlocation="+dp.classPath+",inclnolocationclasses=false,output=\"FILE\" -cp "+classpathGzoltar+GZOLTAR_CLI_JAR+" com.gzoltar.cli.Main runTestMethods --initTestClass --testMethods \""+list_of_tests_to_run_file+"\" --collectCoverage"+"\n", 
				"java -cp "+classpathGzoltar+GZOLTAR_CLI_JAR+" com.gzoltar.cli.Main faultLocalizationReport --buildLocation "+dp.classPath+" --granularity \"line\" --inclPublicMethods --inclStaticConstructors --inclDeprecatedMethods --dataFile "+ser_file+" --outputDirectory \"gzoltarOutput/"+this.buggyProject+"/target\" --family \"sfl\" --formula \"ochiai\" --metric \"entropy\" --formatter \"txt\""+ "\n"
				), buggyProject, "test".equals("test") ? 2 : 1);
	
			}
			catch(Exception e)
			{
				System.out.println("Failed to excecute GZoltar");
				System.exit(1);
			}
            
			suspiciousCodeList = readSuspiciousCodeFromFileGZoltar();

			if(Configuration.measurements.contains("gzoltar"))
			{
			long totalEndTimeGzoltar = System.nanoTime() - totalstartTimeGzoltar;
			System.out.println("Project: "+Configuration.bugId+" Total time GZoltar: "+((double) totalEndTimeGzoltar / 1_000_000_000)); 
			System.out.println("Stop execution, only measure GZoltar");
			System.exit(0);
			}
		}
		
		//READ FROM FILE, NO GZOLTAR
		else{
			suspiciousCodeList = readSuspiciousCodeFromFile(); 
		}
		}
		
		if (suspiciousCodeList == null) return;
		
		List<SuspCodeNode> triedSuspNode = new ArrayList<>();
		log.info("=======TBar: Start to fix suspicious code====== "+"List with:"+suspiciousCodeList.size());
		for (SuspiciousPosition suspiciousCode : suspiciousCodeList) {
			List<SuspCodeNode> scns = parseSuspiciousCode(suspiciousCode);

			if (scns == null) continue;
			System.out.println("Number of suspicious code nodes: "+scns.size());

			for (SuspCodeNode scn : scns) {
//				log.debug(scn.suspCodeStr);
				if (triedSuspNode.contains(scn)) continue;
				triedSuspNode.add(scn);

				System.out.println("suspiciousCode:");
				System.out.println(scn.suspCodeStr);
				
				// Parse context information of the suspicious code.
				List<Integer> contextInfoList = readAllNodeTypes(scn.suspCodeAstNode);
				List<Integer> distinctContextInfo = new ArrayList<>();
				for (Integer contInfo : contextInfoList) {
					//System.out.println("contextInfo:"+contInfo);
					if (!distinctContextInfo.contains(contInfo) && !Checker.isBlock(contInfo)) {
						distinctContextInfo.add(contInfo);
					}
				}
				//List<Integer> distinctContextInfo = contextInfoList.stream().distinct().collect(Collectors.toList());
				
		        // Match fix templates for this suspicious code with its context information.
				
				fixWithMatchedFixTemplates(scn, distinctContextInfo);
		        
				if (!isTestFixPatterns && minErrorTest == 0) break;
				if (this.patchId >= 10000) break;
			}

			//System.exit(0);
			
			if (!isTestFixPatterns && minErrorTest == 0) break;
			if (this.patchId >= 10000) break;
        }
		log.info("=======TBar: Finish off fixing======");
		
		FileHelper.deleteDirectory(Configuration.TEMP_FILES_PATH + this.dataType + "/" + this.buggyProject);
	}

	private List<SuspiciousPosition> readKnownBugPositionsFromFile() {
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();
		
		String[] posArray = FileHelper.readFile(Configuration.knownBugPositions).split("\n");
		Boolean isBuggyProject = null;
		for (String pos : posArray) {
			if (isBuggyProject == null || isBuggyProject) {
				if (pos.startsWith(this.buggyProject + "@")) {
					isBuggyProject = true;
					
					String[] elements = pos.split("@");
	            	String[] lineStrArr = elements[2].split(",");
	            	String classPath = elements[1];
	            	String shortSrcPath = dp.srcPath.substring(dp.srcPath.indexOf(this.buggyProject) + this.buggyProject.length() + 1);
	            	classPath = classPath.substring(shortSrcPath.length(), classPath.length() - 5);

	            	for (String lineStr : lineStrArr) {
	    				if (lineStr.contains("-")) {
	    					String[] subPos = lineStr.split("-");
	    					for (int line = Integer.valueOf(subPos[0]), endLine = Integer.valueOf(subPos[1]); line <= endLine; line ++) {
	    						SuspiciousPosition sp = new SuspiciousPosition();
	    		            	sp.classPath = classPath;
	    		            	sp.lineNumber = line;
	    		            	suspiciousCodeList.add(sp);
	    					}
	    				} else {
	    					SuspiciousPosition sp = new SuspiciousPosition();
	    	            	sp.classPath = classPath;
	    	            	sp.lineNumber = Integer.valueOf(lineStr);
	    	            	suspiciousCodeList.add(sp);
	    				}
	    			}
				} else if (isBuggyProject!= null && isBuggyProject) isBuggyProject = false;
			} else if (!isBuggyProject) break;
		}
		return suspiciousCodeList;
	} 

	private List<String> readKnownFileLevelBugPositions() {
		List<String> buggyFileList = new ArrayList<>();
		
		String[] posArray = FileHelper.readFile(Configuration.knownBugPositions).split("\n");
		Boolean isBuggyProject = null;
		for (String pos : posArray) {
			if (isBuggyProject == null || isBuggyProject) {
				if (pos.startsWith(this.buggyProject + "@")) {
					isBuggyProject = true;
					
					String[] elements = pos.split("@");
	            	String classPath = elements[1];
	            	String shortSrcPath = dp.srcPath.substring(dp.srcPath.indexOf(this.buggyProject) + this.buggyProject.length() + 1);
	            	classPath = classPath.substring(shortSrcPath.length(), classPath.length() - 5).replace("/", ".");

	            	if (!buggyFileList.contains(classPath)) {
	            		buggyFileList.add(classPath);
	            	}
				} else if (isBuggyProject!= null && isBuggyProject) isBuggyProject = false;
			} else if (!isBuggyProject) break;
		}
		return buggyFileList;
	}
	
	public List<SuspiciousPosition> readSuspiciousCodeFromFile(List<String> buggyFileList) {
		File suspiciousFile = null;
		String suspiciousFilePath = "";
		if (this.suspCodePosFile == null) {
			suspiciousFilePath = Configuration.suspPositionsFilePath;
		} else {
			suspiciousFilePath = this.suspCodePosFile.getPath();
		}
		
		suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/" + this.metric + ".txt");
		if (!suspiciousFile.exists()) {
			System.out.println("Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/" + this.metric.toLowerCase() + ".txt");
		}
		if (!suspiciousFile.exists()) {
			System.out.println("Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/All.txt");
		}
		if (!suspiciousFile.exists()) return null;
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();
		try {
			FileReader fileReader = new FileReader(suspiciousFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line = null;
            while ((line = reader.readLine()) != null) {
            	String[] elements = line.split("@");
            	if (!buggyFileList.contains(elements[0])) continue;
            	SuspiciousPosition sp = new SuspiciousPosition();
            	sp.classPath = elements[0];
            	sp.lineNumber = Integer.valueOf(elements[1]);
            	suspiciousCodeList.add(sp);
            }
            reader.close();
            fileReader.close();
        }catch (Exception e){
        	e.printStackTrace();
        	log.debug("Reloading Localization Result...");
            return null;
        }
		if (suspiciousCodeList.isEmpty()) return null;
		return suspiciousCodeList;
	}


	//normal
	public List<SuspiciousPosition> readSuspiciousCodeFromFile() {
		File suspiciousFile = null;
		String suspiciousFilePath = "";
		/*if (this.suspCodePosFile == null) {
			suspiciousFilePath = Configuration.suspPositionsFilePath;
		} else {
			suspiciousFilePath = this.suspCodePosFile.getPath();
		}
		suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/" + this.metric + ".txt");
		if (!suspiciousFile.exists()) {
			System.out.println("Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/" + this.metric.toLowerCase() + ".txt");
		}
		if (!suspiciousFile.exists()) {
			System.out.println("Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/All.txt");
		}*/

		suspiciousFile = this.suspCodePosFile;
		
		if (!suspiciousFile.exists()) return null;
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();
		try {
			FileReader fileReader = new FileReader(suspiciousFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line = null;
            while ((line = reader.readLine()) != null) {
            	String[] elements = line.split("@");
            	SuspiciousPosition sp = new SuspiciousPosition();
            	sp.classPath = elements[0];
            	sp.lineNumber = Integer.valueOf(elements[1]);
            	suspiciousCodeList.add(sp);
            }
            reader.close();
            fileReader.close();
        }catch (Exception e){
        	e.printStackTrace();
        	log.debug("Reloading Localization Result...");
            return null;
        }
		if (suspiciousCodeList.isEmpty()) return null;
		return suspiciousCodeList;
	}

	//read from Gzoltar output
	public List<SuspiciousPosition> readSuspiciousCodeFromFileGZoltar() {
		File suspiciousFile = null;
		//if (this.suspCodePosFile == null) {
			suspiciousFile = new File(Configuration.suspPositionsFilePathGZoltar);
		//} else {
		//	suspiciousFile = this.suspCodePosFile;
		//}
		
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();
		try {
			List<String> allTestCases = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new FileReader(suspiciousFile.getPath() + "/" + this.buggyProject + "/target/sfl/txt/tests.csv"));
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String testCase = line.substring(0, line.indexOf(","));
				allTestCases.add(testCase);
			}
			reader.close();
			//org.traccar.protocol$GoSafeProtocolDecoder#GoSafeProtocolDecoder(org.traccar.protocol.GoSafeProtocol):39;1.0
			reader = new BufferedReader(new FileReader(suspiciousFile.getPath() + "/" + this.buggyProject + "/target/sfl/txt/ochiai.ranking.csv"));
			line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				int dollarIndex = line.indexOf("$");
				int sharpIndex = line.indexOf("#");
				int colonIndex = line.indexOf(":");
				int semicolonIndex = line.indexOf(";");
				String className = line.substring(0, dollarIndex) + "." + line.substring(dollarIndex + 1, sharpIndex);
				String methodName = line.substring(sharpIndex + 1);
				methodName = methodName.substring(0, methodName.indexOf("("));
				Integer buggyLine = Integer.parseInt(line.substring(colonIndex + 1, semicolonIndex));
				Double suspiciousness = Double.parseDouble(line.substring(semicolonIndex + 1));
				if (Double.compare(suspiciousness, 0.0d) == 0) break;
				if (allTestCases.contains(className + "#" + methodName)) continue;// Test case.
				
				dollarIndex = className.indexOf("$");
				if (dollarIndex > 0) className = className.substring(0, dollarIndex);
				SuspiciousPosition sp = new SuspiciousPosition();
            	sp.classPath = className;
            	sp.lineNumber = buggyLine;
            	suspiciousCodeList.add(sp);
			}
			reader.close();
        }catch (Exception e){
        	e.printStackTrace();
        	log.debug("Reloading Localization Result...");
            return null;
        }
		
		if (suspiciousCodeList.isEmpty()) return null;
		return suspiciousCodeList;
	}

	
	public void fixWithMatchedFixTemplates(SuspCodeNode scn, List<Integer> distinctContextInfo) {
		// generate patches with fix templates of TBar.
		FixTemplate ft = null;
		
		if (!Checker.isMethodDeclaration(scn.suspCodeAstNode.getType())) {
			boolean nullChecked = false;
			boolean typeChanged = false;
			boolean methodChanged = false;
			boolean operator = false;
				
			for (Integer contextInfo : distinctContextInfo) {
				if (Checker.isCastExpression(contextInfo)) {
					ft = new ClassCastChecker();
					if (isTestFixPatterns) dataType = readDirectory() + "/ClassCastChecker";
	
					if (!typeChanged) {
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0) return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
						typeChanged = true;
						ft = new DataTypeReplacer();
						if (isTestFixPatterns) dataType = readDirectory() + "/DataTypeReplacer";
					}
				} else if (Checker.isClassInstanceCreation(contextInfo)) {
//					ft = new CNIdiomNoSuperCall();
//					if (isTestFixPatterns) dataType = readDirectory() + "/CNIdiomNoSuperCall";
					if (!methodChanged) {
//						generateAndValidatePatches(ft, scn);
//						if (!isTestFixPatterns && this.minErrorTest == 0) return;
						methodChanged = true;
						ft = new MethodInvocationMutator();
						if (isTestFixPatterns) dataType = readDirectory() + "/MethodInvocationMutator";
					}
				} else if (Checker.isIfStatement(contextInfo) || Checker.isDoStatement(contextInfo) || Checker.isWhileStatement(contextInfo)) {
					if (Checker.isInfixExpression(scn.suspCodeAstNode.getChild(0).getType()) && !operator) {
						operator = true;
						ft = new OperatorMutator(0);
						if (isTestFixPatterns) dataType = readDirectory() + "/OperatorMutator";
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0) return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
					}
					ft = new ConditionalExpressionMutator(2);
					if (isTestFixPatterns) dataType = readDirectory() + "/ConditionalExpressionMutator";
				} else if (Checker.isConditionalExpression(contextInfo)) {
					ft = new ConditionalExpressionMutator(0);
					if (isTestFixPatterns) dataType = readDirectory() + "/ConditionalExpressionMutator";
				} else if (Checker.isCatchClause(contextInfo) || Checker.isVariableDeclarationStatement(contextInfo)) {
					if (!typeChanged) {
						ft = new DataTypeReplacer();
						if (isTestFixPatterns) dataType = readDirectory() + "/DataTypeReplacer";
						typeChanged = true;
					}
				} else if (Checker.isInfixExpression(contextInfo)) {
					ft = new ICASTIdivCastToDouble();
					if (isTestFixPatterns) dataType = readDirectory() + "/ICASTIdivCastToDouble";
					generateAndValidatePatches(ft, scn);
					if (!isTestFixPatterns && this.minErrorTest == 0) return;
					if (this.fixedStatus == 2) {
						fixedStatus = 0;
						return;
					}
					
					if (!operator) {
						operator = true;
						ft = new OperatorMutator(0);
						if (isTestFixPatterns) dataType = readDirectory() + "/OperatorMutator";
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0) return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
					}
					
					ft = new ConditionalExpressionMutator(1);
					if (isTestFixPatterns) dataType = readDirectory() + "/ConditionalExpressionMutator";
					generateAndValidatePatches(ft, scn);
					if (!isTestFixPatterns && this.minErrorTest == 0) return;
					if (this.fixedStatus == 2) {
						fixedStatus = 0;
						return;
					}
					
					ft = new OperatorMutator(4);
					if (isTestFixPatterns) dataType = readDirectory() + "/OperatorMutator";
				} else if (Checker.isBooleanLiteral(contextInfo) || Checker.isNumberLiteral(contextInfo) || Checker.isCharacterLiteral(contextInfo)|| Checker.isStringLiteral(contextInfo)) {
					ft = new LiteralExpressionMutator();
					if (isTestFixPatterns) dataType = readDirectory() + "/LiteralExpressionMutator";
				} else if (Checker.isMethodInvocation(contextInfo) || Checker.isConstructorInvocation(contextInfo) || Checker.isSuperConstructorInvocation(contextInfo)) {
					if (!methodChanged) {
						ft = new MethodInvocationMutator();
						if (isTestFixPatterns) dataType = readDirectory() + "/MethodInvocationMutator";
						methodChanged = true;
					}
					
					if (Checker.isMethodInvocation(contextInfo)) {
						if (ft != null) {
							generateAndValidatePatches(ft, scn);
							if (!isTestFixPatterns && this.minErrorTest == 0) return;
							if (this.fixedStatus == 2) {
								fixedStatus = 0;
								return;
							}
						}
						ft = new NPEqualsShouldHandleNullArgument();
						if (isTestFixPatterns) dataType = readDirectory() + "/NPEqualsShouldHandleNullArgument";
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0) return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
						
						ft = new RangeChecker(false);
						if (isTestFixPatterns) dataType = readDirectory() + "/RangeChecker";
					}
				} else if (Checker.isAssignment(contextInfo)) {
					ft = new OperatorMutator(2);
					if (isTestFixPatterns) dataType = readDirectory() + "/OperatorMutator";
				} else if (Checker.isInstanceofExpression(contextInfo)) {
					ft = new OperatorMutator(5);
					if (isTestFixPatterns) dataType = readDirectory() + "/OperatorMutator";
				} else if (Checker.isArrayAccess(contextInfo)) {
					ft = new RangeChecker(true);
					if (isTestFixPatterns) dataType = readDirectory() + "/RangeChecker";
				} else if (Checker.isReturnStatement(contextInfo)) {
					String returnType = ContextReader.readMethodReturnType(scn.suspCodeAstNode);
					if ("boolean".equalsIgnoreCase(returnType)) {
						ft = new ConditionalExpressionMutator(2);
						if (isTestFixPatterns) dataType = readDirectory() + "/ConditionalExpressionMutator";
					} else {
						ft = new ReturnStatementMutator(returnType);
						if (isTestFixPatterns) dataType = readDirectory() + "/ReturnStatementMutator";
					}
				} else if (Checker.isSimpleName(contextInfo) || Checker.isQualifiedName(contextInfo)) {
					ft = new VariableReplacer();
					if (isTestFixPatterns) dataType = readDirectory() + "/VariableReplacer";
					
					if (!nullChecked) {
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0) return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
						nullChecked = true;
						ft = new NullPointerChecker();
						if (isTestFixPatterns) dataType = readDirectory() + "/NullPointerChecker";
					}
				} 
				if (ft != null) {
					generateAndValidatePatches(ft, scn);
					if (!isTestFixPatterns && this.minErrorTest == 0) return;
					if (this.fixedStatus == 2) {
						fixedStatus = 0;
						return;
					}
				}
				ft = null;
				if (this.patchId >= 10000) break;
			}
			
			if (!nullChecked) {
				nullChecked = true;
				ft = new NullPointerChecker();
				if (isTestFixPatterns) dataType = readDirectory() + "/NullPointerChecker";
				generateAndValidatePatches(ft, scn);
				if (!isTestFixPatterns && this.minErrorTest == 0) return;
				if (this.fixedStatus == 2) {
					fixedStatus = 0;
					return;
				}
			}

			ft = new StatementMover();
			if (isTestFixPatterns) dataType = readDirectory() + "/StatementMover";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0) return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}
			
			ft = new StatementRemover();
			if (isTestFixPatterns) dataType = readDirectory() + "/StatementRemover";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0) return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}
			
			ft = new StatementInserter();
			if (isTestFixPatterns) dataType = readDirectory() + "/StatementInserter";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0) return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}
		} else {
			ft = new StatementRemover();
			if (isTestFixPatterns) dataType = readDirectory() + "/StatementRemover";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0) return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}
		}
	}
	
	private String readDirectory() {
		int index = dataType.indexOf("/");
		if (index > -1) dataType = dataType.substring(0, index);
		return dataType;
	}
	
	protected void generateAndValidatePatches(FixTemplate ft, SuspCodeNode scn) {
		
		//System.out.println("FixTemplate"+ft.toString());
		String tpString = ft.toString();

		int indexSort=tpString.indexOf('@');
		String addtoMap=tpString.substring(0,indexSort);
		Configuration.lastFixPattern=addtoMap;
		System.out.println("FixTemplate: "+addtoMap);

		ft.setSuspiciousCodeStr(scn.suspCodeStr);
		ft.setSuspiciousCodeTree(scn.suspCodeAstNode);
		if (scn.javaBackup == null) ft.setSourceCodePath(dp.srcPath);
		else ft.setSourceCodePath(dp.srcPath, scn.javaBackup);
		ft.setDictionary(dic);


		long generatePatchstartTime = System.nanoTime(); //measure
		ft.generatePatches();
		long generatePatchEndTime = System.nanoTime() - generatePatchstartTime; //measure
		Configuration.generatePatchTime+=generatePatchEndTime;

		List<Patch> patchCandidates = ft.getPatches();
		
		//Measurements
		if(Configuration.measurements.contains("all"))
		{

		System.out.println("patchandidates"+patchCandidates.size());
		
		//Measure number of patches per pattern
		int count = Configuration.freq.containsKey(addtoMap) ? Configuration.freq.get(addtoMap) : 0;
		Configuration.freq.put(addtoMap, count + patchCandidates.size());

		}

		// Test generated patches.
		if (patchCandidates.isEmpty()) return;
		long validatePatchstartTime = System.nanoTime(); //measure
		testGeneratedPatches(patchCandidates, scn);		
		long validatePatchEndTime = System.nanoTime() - validatePatchstartTime; //measure
		Configuration.validatePatchTime+=validatePatchEndTime;

	
	}
	
	public List<Integer> readAllNodeTypes(ITree suspCodeAstNode) {
		List<Integer> nodeTypes = new ArrayList<>();
		nodeTypes.add(suspCodeAstNode.getType());
		List<ITree> children = suspCodeAstNode.getChildren();
		for (ITree child : children) {
			int childType = child.getType();
			if (Checker.isFieldDeclaration(childType) || 
					Checker.isMethodDeclaration(childType) ||
					Checker.isTypeDeclaration(childType) ||
					Checker.isStatement(childType)) break;
			nodeTypes.addAll(readAllNodeTypes(child));
		}
		return nodeTypes;
	}

}

package com.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
//import com.standalone.*;




public class DriverScript {

	public static Keywords keywords;
	public static Method method[];
	public Xls_Reader suiteXLS;
	public int currentSuiteID;
	public static Xls_Reader currentTestSuiteXLS, currentTestSuiteXLS_Ouput;
	public static String currentTestSuite;
	public static String currentProject;
	public static String currentProjSheet;
	public static int currentTestCaseID;
	public static int currentTestStepID;
	public static String objectPath;
	public static String objectData1;
	public static String objectData2;
	public static String currentKeyword;
	public static String currentTestCase;
	public static String keyword_execution_result;
	public static Properties props,DBStore;
	public static ArrayList<String> resultSet;
	public static int currentTestDataSetID = 2;
	public static String OutputData;
	public static String currentTestCaseName, currentTestCaseName_Output;
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		Date startdate = new Date();
		 System.out.println("Execution started at "+startdate);
		
		
		FileInputStream fs;
		keywords = new Keywords();
		method = keywords.getClass().getMethods();
				
		//Reading and clearing StoredValues properties file
		fs = new FileInputStream(System.getProperty("user.dir")+"//global_object_repo//StoredValues.properties");
		DBStore=new Properties();
		DBStore.load(fs);
		DBStore.clear();
		FileOutputStream fout = new FileOutputStream(System.getProperty("user.dir")+"//global_object_repo//StoredValues.properties");
		DBStore.store(fout, "");
		fout.close();
		
		
		//Reading Database Related Data
		fs = new FileInputStream(System.getProperty("user.dir")+"//global_object_repo//Database.properties");
		props = new Properties();
		props.load(fs);
		
		//Creating object for DriverScript class
		DriverScript test = new DriverScript();
		test.start();
	}

	private void start() throws IOException, InterruptedException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//Reading the suit file
		System.out.println(System.getProperty("user.dir")+"//Suite.xlsx");
		suiteXLS=new Xls_Reader(System.getProperty("user.dir")+"//Suite.xlsx");
		
		//Reading SuiteFile for current project and current input sheet
		for (currentSuiteID = 2; currentSuiteID <= suiteXLS.getRowCount(Constants.TEST_SUITE_SHEET); currentSuiteID++) {
			
			currentTestSuite = suiteXLS.getCellData(Constants.TEST_SUITE_SHEET,Constants.Test_Suite_ID, currentSuiteID);
			if (suiteXLS.getCellData(Constants.TEST_SUITE_SHEET,Constants.RUNMODE, currentSuiteID).equals(Constants.RUNMODE_YES)) {
				//reading current project name
				currentProject=suiteXLS.getCellData(Constants.TEST_SUITE_SHEET,Constants.PROJECT, currentSuiteID);
				//reading current input sheet name
				currentProjSheet=suiteXLS.getCellData(Constants.TEST_SUITE_SHEET,Constants.SHEETNAME, currentSuiteID);
				
				
				/*if(currentProject.equals("404")){
					
					RT_404_StandaloneScript.mainmethod();
					continue;
					
				}*/
				
				
				
				currentTestSuiteXLS = new Xls_Reader(System.getProperty("user.dir") +"//projects//"+ currentProject+ "//inputs//" + currentProjSheet+ ".xlsx");
				currentproject();				
				
				}
		}
		 Date enddate = new Date();
		 System.out.println("Execution ended at "+enddate);
	}

	private void currentproject() throws IOException, InterruptedException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		//Creating the Result file in Outputs folder and copying the input sheet
		File source = new File(System.getProperty("user.dir") +"//projects//"+ currentProject+ "//inputs//" + currentProjSheet+ ".xlsx");
		File desc = new File(System.getProperty("user.dir") +"//projects//"+ currentProject+ "//outputs//" + currentProjSheet+ "_result.xlsx");
		FileUtils.copyFile(source, desc);
		
		resultSet = new ArrayList<String>();
		//Reading number of rows in the input sheet and iterating 
		for (currentTestStepID = 2; currentTestStepID <= currentTestSuiteXLS.getRowCount(Constants.TEST_STEPS_SHEET); currentTestStepID++) {
			
			//Reading data from input sheet
			currentTestCase=currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.TESTCASE,currentTestStepID);
			objectPath=currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.OBJECTPATH,currentTestStepID);
			objectData1=currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.OBJECTDATA1,currentTestStepID);
			objectData2 = currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.OBJECTDATA2,currentTestStepID);
			currentKeyword=currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.KEYWORD,currentTestStepID);
			
			System.out.println("Executing "+currentTestStepID+" Row");
			
			executeKeywords();
								
	}
		
		createXLSReport();
		
	}

	private void executeKeywords() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		OutputData = "";
						
						//Calling the keyword
						for (int i = 0; i < method.length; i++) {

							if (method[i].getName().equals(currentKeyword)) {
								currentTestDataSetID = 2;
								
								//Reading data from test data sheet for ObjectPath column
								if (objectPath.startsWith("col")) {
								
								if (currentTestSuiteXLS.isSheetExist(objectPath.split(Constants.DATA_SPLIT)[1])) {
									objectPath="";
									for (; currentTestDataSetID <= currentTestSuiteXLS.getRowCount(currentTestCase); currentTestDataSetID++) {
										
										if (currentTestSuiteXLS.getCellData(currentTestCase, Constants.RUNMODE,currentTestDataSetID).equals(Constants.RUNMODE_YES)) {
											objectPath=objectPath+currentTestSuiteXLS.getCellData(currentTestCase, Constants.OBJBULKINPUT,currentTestDataSetID);
											objectPath=objectPath+"|";
										}
										
									}
									
								} 
								}
								
								//Reading data from test data sheet for DataValue1 column
								if (objectData1.startsWith("col")) {
									currentTestDataSetID = 2;
									if (currentTestSuiteXLS.isSheetExist(objectData1.split(Constants.DATA_SPLIT)[1])) {
										objectData1="";
										for (; currentTestDataSetID <= currentTestSuiteXLS.getRowCount(currentTestCase); currentTestDataSetID++) {
											
											if (currentTestSuiteXLS.getCellData(currentTestCase, Constants.RUNMODE,currentTestDataSetID).equals(Constants.RUNMODE_YES)) {
												objectData1=objectData1+currentTestSuiteXLS.getCellData(currentTestCase, Constants.DATA1BULK,currentTestDataSetID);
												objectData1=objectData1+"|";
											}
											
										}
										
									} 
									}
								
								//Reading data from test data sheet for DataValue2 column
								if (objectData2.startsWith("col")) {
									currentTestDataSetID = 2;
									if (currentTestSuiteXLS.isSheetExist(objectData2.split(Constants.DATA_SPLIT)[1])) {
										objectData2="";
										for (; currentTestDataSetID <= currentTestSuiteXLS.getRowCount(currentTestCase); currentTestDataSetID++) {
											
											if (currentTestSuiteXLS.getCellData(currentTestCase, Constants.RUNMODE,currentTestDataSetID).equals(Constants.RUNMODE_YES)) {
												objectData2=objectData2+currentTestSuiteXLS.getCellData(currentTestCase, Constants.DATA2BULK,currentTestDataSetID);
												objectData2=objectData2+"|";
											}
											
										}
										
									} 
									}
								
								keyword_execution_result = (String) method[i].invoke(keywords, objectData1, objectData2, objectPath);
								resultSet.add(keyword_execution_result+" "+OutputData);
								break;
							}
							
							if (i == method.length -1) {
								System.err.println("\n\n" + currentKeyword + " is not a valid keyword!");
								System.exit(0);
							}
						}
							
	}
	
	public void createXLSReport() {
		Date enddate = new Date();
		 System.out.println("Execution ended at "+enddate);
		
		currentTestSuiteXLS_Ouput = new Xls_Reader(System.getProperty("user.dir") +"//projects//"+ currentProject+ "//outputs//" + currentProjSheet+"_Result"+".xlsx");

		//Adding Result column to the ouyput file
		String colName = Constants.RESULT;
		currentTestSuiteXLS_Ouput.addColumn(Constants.TEST_STEPS_SHEET,colName);
		
		int index=0;
		for(int i = 2; i <= currentTestSuiteXLS_Ouput.getRowCount(Constants.TEST_STEPS_SHEET); i++){
		if (resultSet.size() == 0)
			currentTestSuiteXLS_Ouput.setCellData(Constants.TEST_STEPS_SHEET, colName, i,"SKIP");
		else
			currentTestSuiteXLS_Ouput.setCellData(Constants.TEST_STEPS_SHEET, colName, i,resultSet.get(index)); //add result to row					
			index++;
		}
		currentTestSuiteXLS_Ouput.writeToFile();
		
		
	}
	
	
	
	}
	


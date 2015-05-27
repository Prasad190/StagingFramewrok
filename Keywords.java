package com.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.commons.io.FileUtils;
//import org.apache.http.client.methods.HttpGet;
//import org.custommonkey.xmlunit.DetailedDiff;
//import org.custommonkey.xmlunit.Diff;
//import org.custommonkey.xmlunit.Difference;


public class Keywords {
	
	
	
	public String rt_404validation(String objectData1, String objectData2, String objectPath) throws IOException{
		HttpURLConnection conn1 = null;
		String result="Pass";
		//HttpGet httpget;
		try
		{
			URL url = new URL(objectData1);
			//httpget = new HttpGet(objectData1);
			conn1 = (HttpURLConnection)url.openConnection();
			conn1.getResponseCode();
		}
		catch(Exception e)
		{
			result="Error : Invalid Url";
			
		}
		if(conn1.getResponseCode() == 200)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(conn1.getInputStream(), "UTF-8"));
			StringBuilder a = new StringBuilder();
			String inputLine;
			while((inputLine = in.readLine()) != null) 
				a.append(inputLine);
			in.close();
			if(a.toString().contains("empty-content-well"))
				result="Error : Blank Page";
		} else
		{
			result="Error : 404 Error";
		}
		return result;
	}
	
	
	
	public String ats_DataComparision(String objectData1, String objectData2, String objectPath) throws InterruptedException{
		String result="Pass";
		try{
			
			  DriverScript.OutputData="Failed for:";
			String[] xpaths=objectPath.split("\\|");
			String[] data2 =objectData2.split("\\|");
			String id=null;
		     for(int j=0;j<xpaths.length;j++)
		     {
		       // System.out.println(xpaths[j]);
		        String xpath = xpaths[j];
		        String capturedtagvalue = ats_GetData(objectData1,objectData2,xpath);
		        if(data2[j].contains("sqldb-")){
		        	id=objectData1.split("=")[1];
		        	String dbString=data2[j].split("-")[1].replace("::",id);
		        	Connection conn=dbConnection(data2[j].split("-")[0]);
		        	data2[j]=dbResults(conn, dbString);
		        }if(data2[j].contains("storedValue-")){
		        	Enumeration enuKeys = DriverScript.DBStore.keys();
		        	while(enuKeys.hasMoreElements()){
		        		String key = (String) enuKeys.nextElement();
		        		String value = DriverScript.DBStore.getProperty(key);
		        		if(capturedtagvalue.equals(value))
		        		{
		        			System.out.println(capturedtagvalue+" equals "+value);
		        		}

		            }

		        }

		        if(!capturedtagvalue.equals(data2[j]))
		        {
		        	result= "Fail";
		        	DriverScript.OutputData=DriverScript.OutputData+xpath+" expected "+capturedtagvalue+";";
		        }
		     }
		 
			}catch(Exception e){
				e.printStackTrace();
				result="Fail";
			}
		return result;
		}
	
	
	
	public String ats_StoreData( String objectData1, String objectData2, String objectPath) throws InterruptedException{
		String result="Pass";
		try{
			String xpath = null;    
			String capturedtagvalue=null;
			Document doc1=ATSReading(objectData1);
			XPath xPath1 =XPathFactory.newInstance().newXPath();
			String[] xpaths=objectPath.split("\\|");
			for(int j=0;j<xpaths.length;j++){
				System.out.println(xpaths[j]);
				if(xpaths[j].contains("::")){
					xpath=xpaths[j].split("::")[0];
					NodeList nodeList2 = (NodeList) xPath1.compile(xpath).evaluate(doc1, XPathConstants.NODESET);
					if(nodeList2.getLength()>0){
                	for (int i =0;i< nodeList2.getLength();i++){
                		capturedtagvalue=((Element)nodeList2.item(i)).getAttribute(xpaths[j].split("::")[1]);
                        DriverScript.DBStore.setProperty(xpaths[j].split("::")[1]+i, capturedtagvalue);
                		DriverScript.DBStore.store(new FileOutputStream(System.getProperty("user.dir")+"//global_object_repo//StoredValues.properties"),null);
                		DriverScript.OutputData=DriverScript.OutputData+"--"+capturedtagvalue+"--"; 
                	} 
                }

         }else{
        	 xpath=xpaths[j];
             String tagX[] = xpath.split("/");
             int len = tagX.length-1;
             NodeList nodeList2 = (NodeList) xPath1.compile(xpath).evaluate(doc1, XPathConstants.NODESET);
             for (int i =0;i< nodeList2.getLength();i++){
            	 capturedtagvalue = nodeList2.item(i).getFirstChild().getNodeValue();
                        DriverScript.DBStore.setProperty(xpaths[j].split("/")[len], capturedtagvalue);
  						DriverScript.DBStore.store(new FileOutputStream(System.getProperty("user.dir")+"//global_object_repo//StoredValues.properties"),null);
  						DriverScript.OutputData=DriverScript.OutputData+"--"+capturedtagvalue+"--"; 
             	}
         	}
			}

     
        }catch(Exception e){
        	result="Fail";
        }
		return result;
	}



	
	public String dbColVerify( String DataValue1, String DataValue2, String objectPath) throws InterruptedException{
		String result="Pass";
		String dbresult=null;
		String[] expected = DataValue2.split("\\|");

		try {
			Connection connect =  dbConnection(objectPath);
			String query = DataValue1;
			PreparedStatement pstmt2 = connect.prepareStatement(query);
			ResultSet rs1 = pstmt2.executeQuery();

			for(int i=0;i<expected.length;i++){
				rs1.next();
				dbresult = rs1.getString(1);
				if(dbresult.contains(expected[i])){
					System.out.println(dbresult+"******Matched*********"+expected[i]);
				}	
				else{
					result= "Fail";
					System.out.println(dbresult+"******Not Matching Values*********"+expected[i]);
			       }
			}
	        connect.close();
			
		}catch(Exception e){
			
		}
		return result;
	}
	
	public String dbRowVerify( String DataValue1, String DataValue2, String objectPath) throws InterruptedException{
		String result="Pass";
		String dbresult=null;
		String[] expected = DataValue2.split("\\|");
		try {
			Connection connect =  dbConnection(objectPath);
			String query = DataValue1;
			PreparedStatement pstmt2 = connect.prepareStatement(query);
			ResultSet rs1 = pstmt2.executeQuery();
			while(rs1.next()){
				for(int i=1;i<=expected.length;i++){
					dbresult = rs1.getString(i);
					if(dbresult.equals(expected[i-1])){
						System.out.println(dbresult+"******Matched*********"+expected[i-1]);
					}	
					else{
						result= "Fail";
						System.out.println(dbresult+"******Not Matching Values*********"+expected[i-1]);
			        }
				}
			} 
			connect.close();
			
		}catch(Exception e){
			
		}
		return result;
	}
	//Connecting to the SQL/Oracle database
	private Connection dbConnection(String  objectpath) throws InterruptedException{
		 Connection conn = null; 
		
		  try {
			  if(objectpath.contains("sql")){
			  Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
			   conn = DriverManager.getConnection(DriverScript.props.getProperty("SQLString"),DriverScript.props.getProperty("SQLUserName"),DriverScript.props.getProperty("SQLPassword"));
			  System.out.println("connected");
			  }
			  else if (objectpath.contains("oracle")) {
				  Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
				  conn = DriverManager.getConnection(DriverScript.props.getProperty("OracleString"),DriverScript.props.getProperty("OracleUserName"),DriverScript.props.getProperty("OraclePassword"));
				  System.out.println("connected");
			  }
			  
			}catch(Exception e){
				System.out.println(" not connected" + e);
			}
				return conn;
		}
	//Reading result from database
	private String dbResults(Connection conn,String Query) throws SQLException{
		String dbresult=null;
		Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(Query);
            
        while(rs.next()){
               dbresult = rs.getString(1);
        }
		return dbresult;
	}	
	
	//Get the xml values for the given xpaths
	private String ats_GetData( String objectData1, String objectData2, String objectPath) throws InterruptedException{
		
		try{
			String xpath = null;	
			String capturedtagvalue=null;
			Document doc1=ATSReading(objectData1);
            XPath xPath1 =XPathFactory.newInstance().newXPath();
             
             String[] xpaths=objectPath.split("\\|");
             for(int j=0;j<xpaths.length;j++){
            	//System.out.println(xpaths[j]);
            	 if(xpaths[j].contains("::")){
            		xpath=xpaths[j].split("::")[0]; 
               		NodeList nodeList2 = (NodeList) xPath1.compile(xpath).evaluate(doc1, XPathConstants.NODESET);
               		if(nodeList2.getLength()>0){
               			for (int i =0;i< nodeList2.getLength();i++){
    						capturedtagvalue=((Element)nodeList2.item(i)).getAttribute(xpaths[j].split("::")[1]);
               			} 
                 }
            	 }else{
            		 xpath=xpaths[j];
            		 NodeList nodeList2 = (NodeList) xPath1.compile(xpath).evaluate(doc1, XPathConstants.NODESET);
            		 for (int i =0;i< nodeList2.getLength();i++){
            			  capturedtagvalue = nodeList2.item(i).getFirstChild().getNodeValue();
            		 }
                 
            	 }
             }
             return capturedtagvalue; 
		}catch(Exception e){
			return "Null";
		}
	}
	//xml file reading
	private Document ATSReading(String objectData1){
	
		URL url;
		Document doc1 = null;
		try {
			url = new URL(objectData1);
			URLConnection conn = url.openConnection();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc1 = builder.parse(conn.getInputStream());
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	return doc1;
	}
	

/*	public String ats_XMLComparision( String objectData1, String objectData2, String objectPath) throws InterruptedException{
		
		try{
			
			Xls_Reader currentTestSuiteXLS_Ouput = new Xls_Reader(System.getProperty("user.dir") +"//projects//"+ DriverScript.currentProject+ "//outputs//" + DriverScript.currentProjSheet+"_Result"+".xlsx");

			//Adding Result column to the ouyput file
			String colName = "XMLFileName";
			currentTestSuiteXLS_Ouput.addColumn(Constants.TEST_STEPS_SHEET,colName);
			
			System.out.println("In comparision");
			currentTestSuiteXLS_Ouput.writeToFile();
			File dir = new File(objectData1);
			String[] extensions = new String[] {"xml"};
			List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
			int i=2;
				for (File file : files) {
					
				currentTestSuiteXLS_Ouput.setCellData(Constants.TEST_STEPS_SHEET, colName, i,file.getName());
				i++;
				}
			
			currentTestSuiteXLS_Ouput.writeToFile();
			for (File file : files) {
				
				System.out.println(objectData2+"\\"+file.getName());
				File f = new File(objectData2+"\\"+file.getName());
				if(f.exists()){
					File fXmlFile1 = new File(objectData1+"\\"+file.getName());
					File fXmlFile2 = new File(objectData2+"\\"+file.getName());
					
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				    factory.setValidating( false ); // default is false
					factory.setIgnoringComments( true );
					factory.setIgnoringElementContentWhitespace( true );
					
					DocumentBuilder builder = factory.newDocumentBuilder();
					
					//System.out.println(objectData2+"\\"+file.getName());
					Document doc1=builder.parse(fXmlFile1);
					Document doc2= builder.parse(fXmlFile2);
					
					Diff diff = new Diff(doc1, doc2);
					DetailedDiff detailedDiff = new DetailedDiff(diff);
					List differenceList = detailedDiff.getAllDifferences();
					System.out.println("" + differenceList.size() + " differences:");
					Iterator differences = differenceList.iterator();
					while (differences.hasNext()) {
					Difference difference = (Difference) differences.next();
					System.out.println("+ [" + difference.getDescription() + "]");
					String node1 = difference.getControlNodeDetail().getNode().toString();
					String node2 = difference.getTestNodeDetail().getNode().toString();
					System.out.println("  [" + node1 + "] vs [" + node2 + "]");
					
					}
					 System.out.println("success");
				}
				else{
				    System.out.println("fail");
				}
				 //System.out.println(file.getName());
			}
						
			return "TRUE";
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("In catch block");
			return "FALSE";
		}
	}*/
	
	
}

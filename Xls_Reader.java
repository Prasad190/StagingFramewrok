package com.java;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;


public class Xls_Reader {

	private String path;
	private XSSFWorkbook workbook = null;
	private FormulaEvaluator evaluator;
	FileOutputStream fileOut = null;
	public Xls_Reader(String path) {

		this.path=path;
		
		try {
			FileInputStream fis = new FileInputStream(path);
			workbook = new XSSFWorkbook(fis);
			
			//evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}
	// returns the row count in a sheet
	public int getRowCount(String sheetName){
		int index = workbook.getSheetIndex(sheetName);
		if(index==-1)
			return 0;
		else{
			XSSFSheet sheet = workbook.getSheetAt(index);
			return sheet.getLastRowNum()+1;
		}

	}

	private int getColNum(String sheetName, String colName) {
		int index = workbook.getSheetIndex(sheetName);
		if(index==-1)
			return -1;

		XSSFSheet sheet = workbook.getSheetAt(index);
		XSSFRow row = sheet.getRow(0);
		int col_Num = -1;
		for(int i=row.getLastCellNum()-1;i>=0;i--){
			//System.out.println(row.getCell(i).getStringCellValue().trim());
			if(row.getCell(i).getStringCellValue().trim().equals(colName.trim())) {
				col_Num=i;
				break;
			}
		}
		return col_Num;
	}

	// returns the data from a cell
	public String getCellData(String sheetName,String colName,int rowNum){

		try{
			int col_Num = getColNum(sheetName, colName);
			return getCellData(sheetName, col_Num, rowNum);

		} catch(Exception e){
			e.printStackTrace();
			return "row "+rowNum+" or column "+colName +" does not exist in xls";
		}
	}

	// returns the data from a cell
	public String getCellData(String sheetName,int colNum,int rowNum){
		try{
			if ( rowNum <= 0 || colNum < 0)
				return "";

			int index = workbook.getSheetIndex(sheetName);

			if(index==-1)
				return "";


			XSSFSheet sheet = workbook.getSheetAt(index);
			XSSFRow row = sheet.getRow(rowNum-1);
			if(row==null)
				return "";
			XSSFCell cell = row.getCell(colNum);
			if(cell==null)
				return "";

			if(cell.getCellType()==Cell.CELL_TYPE_STRING)
				return cell.getStringCellValue();
			else if(cell.getCellType()==Cell.CELL_TYPE_NUMERIC) {
				double d = cell.getNumericCellValue();
				//return int value if possible
				if (d == Math.floor(d)) {
					return String.valueOf((int) d);
				} else {
					return String.valueOf(d); 
				}
			} else if (cell.getCellType()==Cell.CELL_TYPE_FORMULA ){

				String cellText = null;
				//****************HANDLES THE FORMULA IN THE SPREADSHEET****************************
				//	        System.out.println("Formula is " + cell.getCellFormula());
				switch(cell.getCachedFormulaResultType()) {
				case Cell.CELL_TYPE_NUMERIC:
					//	                System.out.println("Last evaluated as: " + cell.getNumericCellValue());
					double d = evaluator.evaluateInCell(cell).getNumericCellValue();
					if (d == Math.floor(d)) {
						cellText = String.valueOf((int) d);
					} else {
						cellText = String.valueOf(d); 
					}
					break;
				case Cell.CELL_TYPE_STRING:
					//	                System.out.println("Last evaluated as \"" + cell.getRichStringCellValue() + "\"");
					cellText = String.valueOf(evaluator.evaluateInCell(cell).getRichStringCellValue());
					break;
				}
				//************************************************************************************	

				//			  cellText  = String.valueOf(cell.getNumericCellValue()); 

				return cellText;
			}else if(cell.getCellType()==Cell.CELL_TYPE_BLANK)
				return ""; 
			else 
				return String.valueOf(cell.getBooleanCellValue());

		}
		catch(Exception e){

			e.printStackTrace();
			return "row "+rowNum+" or column "+colNum +" does not exist  in xls";
		}
	}

	// returns true if data is set successfully else false
	public boolean setCellData(String sheetName,String colName,int rowNum, String data){
		try{

			if(rowNum<=0)
				return false;

			int index = workbook.getSheetIndex(sheetName);
			int colNum=-1;
			if(index==-1)
				return false;


			XSSFSheet sheet = workbook.getSheetAt(index);

			XSSFRow row = sheet.getRow(0);
			for(int i=row.getLastCellNum()-1;i>=0;i--){
				//System.out.println(row.getCell(i).getStringCellValue().trim());
				if(row.getCell(i).getStringCellValue().trim().equals(colName)) {
					colNum=i;
					break;
				}
			}
			if(colNum==-1)
				return false;


			row = sheet.getRow(rowNum-1);
			if (row == null)
				row = sheet.createRow(rowNum-1);

			XSSFCell cell = row.getCell(colNum);	
			if (cell == null)
				cell = row.createCell(colNum);

			// cell style
			//CellStyle cs = workbook.createCellStyle();
			//cs.setWrapText(true);
			//cell.setCellStyle(cs);
			cell.setCellValue(data);

			// **********************************************************

			CellStyle style = workbook.createCellStyle();
			style = workbook.createCellStyle();

			if(data.contains("FAIL")){
				style.setFillForegroundColor(IndexedColors.RED.getIndex());
				style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			}

			cell.setCellStyle(style);

			//************************************************************	
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//Vijay modified this to accomadate collecting all the functions in a keyword
	public boolean setCellData(String sheetName,int colNum,int rowNum, String data){
		try{

			if(rowNum<=0)
				return false;

			int index = workbook.getSheetIndex(sheetName);
			colNum=0;
			if(index==-1)
				return false;

			XSSFSheet sheet = workbook.getSheetAt(index);

			XSSFRow row = sheet.getRow(0);

			if(colNum==-1)
				return false;


			row = sheet.getRow(rowNum-1);
			if (row == null)
				row = sheet.createRow(rowNum-1);

			XSSFCell cell = row.getCell(colNum);	
			if (cell == null)
				cell = row.createCell(colNum);

			// cell style
			//CellStyle cs = workbook.createCellStyle();
			//cs.setWrapText(true);
			//cell.setCellStyle(cs);
			cell.setCellValue(data);

			// ***********************

			System.out.println(data);

			CellStyle style = workbook.createCellStyle();
			style = workbook.createCellStyle();
			style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cell.setCellStyle(style);

			//****************************	    

		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// returns true if data is set successfully else false
	public boolean setCellData(String sheetName,String colName,int rowNum, String data,String url){
		//System.out.println("setCellData setCellData******************");
		try{

			if(rowNum<=0)
				return false;

			int index = workbook.getSheetIndex(sheetName);
			int colNum=-1;
			if(index==-1)
				return false;


			XSSFSheet sheet = workbook.getSheetAt(index);
			//System.out.println("A");
			XSSFRow row = sheet.getRow(0);
			for(int i=row.getLastCellNum()-1;i>=0;i--){
				//System.out.println(row.getCell(i).getStringCellValue().trim());
				if(row.getCell(i).getStringCellValue().trim().equalsIgnoreCase(colName)) {
					colNum=i;
					break;
				}
			}

			if(colNum==-1)
				return false;
			sheet.autoSizeColumn(colNum);
			row = sheet.getRow(rowNum-1);
			if (row == null)
				row = sheet.createRow(rowNum-1);

			XSSFCell cell = row.getCell(colNum);	
			if (cell == null)
				cell = row.createCell(colNum);

			cell.setCellValue(data);
			XSSFCreationHelper createHelper = workbook.getCreationHelper();

			//cell style for hyperlinks
			//by default hypelrinks are blue and underlined
			CellStyle hlink_style = workbook.createCellStyle();
			XSSFFont hlink_font = workbook.createFont();
			hlink_font.setUnderline(XSSFFont.U_SINGLE);
			hlink_font.setColor(IndexedColors.BLUE.getIndex());
			hlink_style.setFont(hlink_font);
			//hlink_style.setWrapText(true);

			XSSFHyperlink link = createHelper.createHyperlink(XSSFHyperlink.LINK_FILE);
			link.setAddress(url);
			cell.setHyperlink(link);
			cell.setCellStyle(hlink_style);

		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}




	// returns true if sheet is created successfully else false
	public boolean addSheet(String  sheetname){		

		try {
			workbook.createSheet(sheetname);	
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// returns true if sheet is removed successfully else false if sheet does not exist
	public boolean removeSheet(String sheetName){		
		int index = workbook.getSheetIndex(sheetName);
		if(index==-1)
			return false;

		try {
			workbook.removeSheetAt(index);
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
		return true;
	}
	// returns true if column is created successfully
	public boolean addColumn(String sheetName,String colName){
		//System.out.println("**************addColumn*********************");

		try{				
			int index = workbook.getSheetIndex(sheetName);
			if(index==-1)
				return false;

			XSSFCellStyle style = workbook.createCellStyle();
			style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

			XSSFSheet sheet = workbook.getSheetAt(index);

			XSSFRow row = sheet.getRow(0);
			if (row == null)
				row = sheet.createRow(0);

			XSSFCell cell;
			//cell = row.getCell();	
			//if (cell == null)
			//System.out.println(row.getLastCellNum());
			if(row.getLastCellNum() == -1)
				cell = row.createCell(0);
			else
				cell = row.createCell(row.getLastCellNum());

			cell.setCellValue(colName);
			cell.setCellStyle(style);

		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;


	}
	
	// find whether sheets exists	
	public boolean isSheetExist(String sheetName){
		int index = workbook.getSheetIndex(sheetName);
		if(index==-1){
			index=workbook.getSheetIndex(sheetName.toUpperCase());
			if(index==-1)
				return false;
			else
				return true;
		}
		else
			return true;
	}

	// returns number of columns in a sheet	
	public int getColumnCount(String sheetName){
		// check if sheet exists
		if(!isSheetExist(sheetName))
			return -1;

		XSSFSheet sheet = workbook.getSheet(sheetName);
		XSSFRow row = sheet.getRow(0);

		if(row==null)
			return -1;

		return row.getLastCellNum();



	}
	

	public int getCellRowNum(String sheetName,String colName,String cellValue){

		for(int i=2;i<=getRowCount(sheetName);i++){
			if(getCellData(sheetName,colName , i).equalsIgnoreCase(cellValue)){
				return i;
			}
		}
		return -1;
	}
	
	//This method is COSTLY (time-wise), call only when necessary
	public void writeToFile() {
		 try {
			FileOutputStream fileOut = new FileOutputStream(path);
			workbook.write(fileOut);
			fileOut.close();
			
			//have to re-read the file due to an poi bug where you cannot
			//write twice to the same file
			FileInputStream fis = new FileInputStream(path);
			workbook = new XSSFWorkbook(fis);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

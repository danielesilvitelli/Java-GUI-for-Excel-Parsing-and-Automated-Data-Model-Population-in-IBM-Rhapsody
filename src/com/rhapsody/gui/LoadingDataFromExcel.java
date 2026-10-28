package com.rhapsody.gui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPTag;

public class LoadingDataFromExcel {
	
	private static String family = null;
	private static DefaultMutableTreeNode messTypePkg = null;
	private static DefaultMutableTreeNode compositeTypes = null;
	private static DefaultMutableTreeNode elementaryTypes = null;
	private static DefaultMutableTreeNode pkg = null;
	private static XSSFCell versionCell = null;
	private static int introductiveSheets = 2; // N.B. this field is equals to 2 when there are "info" and "messages" sheets; instead, when there is also "Header" sheet, this value must be equals to 3
	private static int byteOffsetHeader = 0; // this variable tells me the byte offset of an attribute of Payload field; when there are Address and Control attributes, the ICD_Logical_Payload attribute will take this value
	protected static DefaultMutableTreeNode root = (DefaultMutableTreeNode) GUI.tree.getModel().getRoot();
	protected static HashMap<String, String> mappingCodeTypes = new HashMap<String, String>() 	{ 
																									{	put("UND", "unsigned int");
																										put("D", "bool");
																										put("C", "Coded");
																										put("2C", "Signed");
																										put("N", "int");
																										put("V", "bool");
																										put("uint8", "unsigned int");
																										put("Untyped", "Untyped");
																										put("int", "int");
																										put("double", "double");
																									}
																								};
	
	
	protected static void selectExcelFile() {
		
		try {
			JFileChooser jfc = new JFileChooser();
			int check = jfc.showOpenDialog(null);
			if (check == JFileChooser.APPROVE_OPTION) {
				String path = jfc.getSelectedFile().toString();
				String ext = 	String.valueOf(path.charAt(path.length()-5)) + 
								String.valueOf(path.charAt(path.length()-4)) +
								String.valueOf(path.charAt(path.length()-3)) +
								String.valueOf(path.charAt(path.length()-2)) +
								String.valueOf(path.charAt(path.length()-1));
				if (ext.equals(".xlsx"))
					loadingData(path);
				else {
					CustomPrint.printOnJTextPane("Invalid path selected: it must be an \".xlxs\" file.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				}
			}
		}catch(ArrayIndexOutOfBoundsException aioobe) {}
	}
	
	
	protected static void loadingData(String path) {
				
		CustomPrint.printOnJTextPane("Starting loading data from excel...");
		GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
		
		try {
			
			FileInputStream file = new FileInputStream(path);
			
			// Create Workbook instance holding reference to .xlsx file
			try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
				
				ArrayList<String> messages = new ArrayList<>();
				HashMap<String, String> mappingSheetToMessages = new HashMap<>();
				ArrayList<String> typeAnomalies = new ArrayList<>();
				ArrayList<Integer> totalBytesMessage = new ArrayList<>();
				
				String recap = null;
				
				int versionColumnIdx = Integer.MAX_VALUE;
				
				// O(n) --> n = sheets number
				for(int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
					
					ArrayList<String> wordNameList = new ArrayList<>();
					HashMap<String, Integer> mappingInitialByteOffset_WordName = new HashMap<>();
					
					int deviceNameColumnIdx = Integer.MAX_VALUE;
					int sheetNameColumnIdx = Integer.MAX_VALUE;
					int wordNameColumnIdx = Integer.MAX_VALUE;
					int nameColumnIdx = Integer.MAX_VALUE;
					int typeColumnIdx = Integer.MAX_VALUE;
					int lsbColumnIdx = Integer.MAX_VALUE;
					int endianismColumnIdx = Integer.MAX_VALUE;
					int sourceColumnIdx = Integer.MAX_VALUE;
					int lengthColumnIdx = Integer.MAX_VALUE;
					int descriptionColumnIdx = Integer.MAX_VALUE;
					int bitFieldsColumnIdx = Integer.MAX_VALUE;
					int byteColumnIdx = Integer.MAX_VALUE;
					int endByteColumnIdx = Integer.MAX_VALUE;
					int unitsColumnIdx = Integer.MAX_VALUE;
					int minSemanticColumnIdx = Integer.MAX_VALUE;
					int maxSemanticColumnIdx = Integer.MAX_VALUE;
					int extraInfoColumnIdx = Integer.MAX_VALUE;
					int codedValuesColumnIdx = Integer.MAX_VALUE;
					int biasColumnIdx = Integer.MAX_VALUE;
					int frequencyColumnIdx = Integer.MAX_VALUE;
					int totalBytesMessageColumnIdx = Integer.MAX_VALUE;
					int descriptionMessageColumnIdx = Integer.MAX_VALUE;
					int uniqueIDColumnIdx = Integer.MAX_VALUE;
					int usedInWdmMessageColumnIdx = Integer.MAX_VALUE;
					int usedInWdmColumnIdx = Integer.MAX_VALUE;
					int comparisonByteOffset = Integer.MAX_VALUE;
					int comparisonNumBytes = Integer.MAX_VALUE;
					int endByte = Integer.MAX_VALUE;
					int frequency = Integer.MAX_VALUE;
					int numOfCells = Integer.MAX_VALUE;
					int uniqueID = Integer.MAX_VALUE;
					
					DefaultMutableTreeNode comparisonNode = null;
					
					String comparisonAttributesOfTypeMessage = null;
					String descriptionMessage = null;
					String usedInWdmMessage = null;
					String mswName = null;
					
					// Get first/desired sheet from the workbook
					XSSFSheet sheet = workbook.getSheetAt(sheetNumber);					
					
					int numOfRows = sheet.getPhysicalNumberOfRows();
					
					recap = "Sheet: " + String.valueOf(sheetNumber+1) + "/" + String.valueOf(workbook.getNumberOfSheets()) +
						    "\t Rows: " + String.valueOf(numOfRows);
					
					CustomPrint.printOnJTextPane(recap);
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
					
					// Iterate over rows
					// O(n) --> n = rows number
					for(int rowIdx = 0; rowIdx < numOfRows; rowIdx++) {
						
						int lengthHeader = Integer.MAX_VALUE;
						int byteOffset = Integer.MAX_VALUE;
						int numBytes = Integer.MAX_VALUE;
						int startBit = Integer.MAX_VALUE;
						int numBits = Integer.MAX_VALUE;

						double bias = Double.NaN;
						
						String minSemantic = null;
						String maxSemantic = null;
						String lsbValue = null;
						String nameColumnContent = null;
						String typeMessage = null;
						String attributeType = null;
						String nameHeader = null;
						String description = null;
						String typeHeader = null;
						String endianism = null;
						String source = null;
						String units = null;
						String codedValues = null;
						String usedInWdm = null;
						
						Boolean wordNameIsEmpty = false;
						Boolean lswFoundExit = false;
												
						XSSFRow row = sheet.getRow(rowIdx);
						
						if(row != null) {
							
							if(rowIdx == 0)
								numOfCells = row.getPhysicalNumberOfCells();
							
							// Iterate over cells
							// O(n) --> n = cells number
							for(int cellIdx = 0; cellIdx < numOfCells; cellIdx++) {
								
								XSSFCell cell = row.getCell(cellIdx);
								
								if(cell != null && !cell.getCellType().toString().equals("BLANK")) {
									
									// The type is useful to understand if it is a String, a double or a Date field
									String cellType = cell.getCellType().toString();
																		
									String contentSTR = null;
									double contentNBR = Double.NaN;
									Date contentDT = null;
									Boolean isDateFormatted = false;
									
									// Eventually, reading date field
									try {
										isDateFormatted = DateUtil.isCellDateFormatted(cell);
										if(isDateFormatted) {
											Date date = cell.getDateCellValue();
											SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
											String formattedDate = sdf.format(date); // if it can be useful, the date is stored into "formattedDate" variable
										}
										
									}catch(IllegalStateException | DateTimeParseException dtpe) {}
									
									// Getting type of data from cell context
									if(cellType != null && !isDateFormatted) {
										try {
											if(cellType.equals("STRING"))
												contentSTR = cell.getStringCellValue();
											else if(cellType.equals("NUMERIC") || cellType.equals("FORMULA"))
												contentNBR = cell.getNumericCellValue();
										}catch(IllegalStateException ise) {
											continue;
										}
									}
									else
										continue;
									
									// HANDLING FIRST SHEET: "info"		
									if(sheet.getSheetName().equals("info") && rowIdx == 0) { // header
										if(contentSTR != null && contentSTR.equals("Device_Name"))
											// Store cell number
											deviceNameColumnIdx = cellIdx;
										else if(contentSTR != null && contentSTR.equals("Version"))
											versionColumnIdx = cellIdx;
									}
									if(sheet.getSheetName().equals("info") && rowIdx == 1 && deviceNameColumnIdx == cellIdx && contentSTR != null) {
										// Add package of level 1 to Rhapsody Plug-In
										family = contentSTR.substring("ICD_".length());
										
										// Getting the path to the package to delete, eventually
										for(int i = 0; i < ((DefaultMutableTreeNode) GUI.tree.getModel().getRoot()).getChildCount(); i++) {
											DefaultMutableTreeNode child = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) GUI.tree.getModel().getRoot()).getChildAt(i);
											if(((CustomIRPModelElement) child.getUserObject()).getElement().getName().equals(contentSTR)) {
												
												int result = JOptionPane.showConfirmDialog(null, "Already exists a Data Model with this name. Do you want to delete it "
																								+ "and import the one selected?", "Confirmation", JOptionPane.YES_NO_OPTION);
												
												if (result == JOptionPane.YES_OPTION) {
													GUI.tp = new TreePath(child.getPath());
													TreeDeletion.deleteNode();
												}
												else
													return;
											}
										}
									}
									else if(sheet.getSheetName().equals("info") && rowIdx == 1 && versionColumnIdx == cellIdx)
										versionCell = cell; // store the cell object
									
									// HANDLING SECOND SHEET: "messages"
									// In this 'if' I'll add the message type package to the DataModel
									if(contentSTR != null && sheet.getSheetName().equals("messages") && rowIdx > 0 && cellIdx == 0)								
										messages.add(contentSTR);
									
									// In this 'if' I'll get the column corresponding to the "Sheet" field
									if(contentSTR != null && sheet.getSheetName().equals("messages") && rowIdx == 0) { // header
										if(contentSTR.equals("Sheet"))
											sheetNameColumnIdx = cellIdx;
										
										if(contentSTR.equals("Endianism"))
											endianismColumnIdx = cellIdx;
										
										if(contentSTR.equals("Source"))
											sourceColumnIdx = cellIdx;
										
										if(contentSTR.equals("Frequency"))
											frequencyColumnIdx = cellIdx;
										
										if(contentSTR.equals("Bytes"))
											totalBytesMessageColumnIdx = cellIdx;
										
										if(contentSTR.equals("Description"))
											descriptionMessageColumnIdx = cellIdx;
										
										if(contentSTR.equals("Unique_id"))
											uniqueIDColumnIdx = cellIdx;
										
										if(contentSTR.equals("Used_In_WDM"))
											usedInWdmMessageColumnIdx = cellIdx;
									}
									
									// In this 'if' I'll create a mapping between Sheet and Message
									if(contentSTR != null && sheet.getSheetName().equals("messages") && rowIdx > 0 && sheetNameColumnIdx == cellIdx) {
										String mess = messages.get(messages.size()-1);
										mappingSheetToMessages.put(contentSTR, mess);
									}
									// In this 'if' I'll take the Endianism value
									if(contentSTR != null && sheet.getSheetName().equals("messages") && rowIdx > 0 && endianismColumnIdx == cellIdx)
										endianism = contentSTR;
									// In this 'if' I'll set the source of the messages
									if(contentSTR != null && sheet.getSheetName().equals("messages") && rowIdx > 0 && sourceColumnIdx == cellIdx)
										source = contentSTR;
									// In this 'if' I'll set the frequency of the messages
									if(sheet.getSheetName().equals("messages") && rowIdx > 0 && frequencyColumnIdx == cellIdx && !Double.isNaN(contentNBR))
										frequency = (int) contentNBR;
									// In this 'if' I'll set the total bytes length of the messages
									if(sheet.getSheetName().equals("messages") && rowIdx > 0 && totalBytesMessageColumnIdx == cellIdx && !Double.isNaN(contentNBR))
										totalBytesMessage.add((int) contentNBR);
									// In this 'if' I'll set the source of the messages
									if(contentSTR != null && sheet.getSheetName().equals("messages") && rowIdx > 0 && descriptionMessageColumnIdx == cellIdx)
										descriptionMessage = contentSTR;
									// In this 'if' I'll set the uniqueID of the messages
									if(sheet.getSheetName().equals("messages") && rowIdx > 0 && uniqueIDColumnIdx == cellIdx && !Double.isNaN(contentNBR))
										uniqueID = (int) contentNBR;
									// In this 'if' I'll set the usedInWdm flag of the messages
									if(contentSTR != null && sheet.getSheetName().equals("messages") && rowIdx > 0 && usedInWdmMessageColumnIdx == cellIdx)
										usedInWdmMessage = contentSTR;
									
									Boolean checkOnSheets = !(sheet.getSheetName().equals("info") || sheet.getSheetName().equals("messages"));
																	
									// HANDLING SHEETS: single messages
									if(checkOnSheets) {
										
										// I'm checking whether there's matching between the current Sheet analyzed and the Message
										if(mappingSheetToMessages.get(sheet.getSheetName()) != null) {
											
											// In this 'if' I'll get the column corresponding to the "Name" field
											if(contentSTR != null && rowIdx == 0) { // header
												if(contentSTR.equals("Word_Name"))
													wordNameColumnIdx = cellIdx;
												else if(contentSTR.equals("Name"))
													nameColumnIdx = cellIdx;
												else if(contentSTR.equals("Type"))
													typeColumnIdx = cellIdx;
												else if(contentSTR.equals("LSB"))
													lsbColumnIdx = cellIdx;
												else if(contentSTR.equals("Description"))
													descriptionColumnIdx = cellIdx;
												else if(contentSTR.equals("Byte"))
													byteColumnIdx = cellIdx;
												else if(contentSTR.equals("End_Byte"))
													endByteColumnIdx = cellIdx;
												else if(contentSTR.equals("Bit_Fields"))
													bitFieldsColumnIdx = cellIdx;
												else if(contentSTR.equals("Length"))
													lengthColumnIdx = cellIdx;
												else if(contentSTR.equals("Units"))
													unitsColumnIdx = cellIdx;
												else if(contentSTR.equals("MinSemantic"))
													minSemanticColumnIdx = cellIdx;
												else if(contentSTR.equals("MaxSemantic"))
													maxSemanticColumnIdx = cellIdx;
												else if(contentSTR.equals("ExtraInformation"))
													extraInfoColumnIdx = cellIdx;
												else if(contentSTR.equals("CodedValues"))
													codedValuesColumnIdx = cellIdx;
												else if(contentSTR.equals("Bias"))
													biasColumnIdx = cellIdx;
												else if(contentSTR.equals("Used_In_WDM"))
													usedInWdmColumnIdx = cellIdx;
											}
											
											// Case of Min Semantic
											if(rowIdx > 0 && minSemanticColumnIdx == cellIdx && !Double.isNaN(contentNBR)) {
												if((cellType.equals("NUMERIC") || cellType.equals("FORMULA"))) {
													minSemantic = BigDecimal.valueOf(contentNBR).toPlainString();
													// Checking if the minSemantic is an Integer value
													if(((double)Double.valueOf(minSemantic)) % 1 == 0)
														minSemantic = String.valueOf((int) ((double)Double.valueOf(minSemantic)));
												}
											}
											// Case of Max Semantic
											if(rowIdx > 0 && maxSemanticColumnIdx == cellIdx && !Double.isNaN(contentNBR)) {
												if((cellType.equals("NUMERIC") || cellType.equals("FORMULA"))) {
													maxSemantic = BigDecimal.valueOf(contentNBR).toPlainString();
													// Checking if the maxSemantic is an Integer value
													if(((double)Double.valueOf(maxSemantic)) % 1 == 0)
														maxSemantic = String.valueOf((int) ((double)Double.valueOf(maxSemantic)));
												}
											}
											// Case of Byte column
											if(rowIdx > 0 && byteColumnIdx == cellIdx && !Double.isNaN(contentNBR)) {
												byteOffset = (int) contentNBR;
											}
											// Case of End Byte column
											else if(rowIdx > 0 && endByteColumnIdx == cellIdx && !Double.isNaN(contentNBR)) {
												endByte = (int) contentNBR;
												numBytes = ((int) contentNBR) - byteOffset + 1;
											}
											// Case of Bit Fields column
											else if(rowIdx > 0 && bitFieldsColumnIdx == cellIdx && !Double.isNaN(contentNBR)) {
												if(String.valueOf(contentNBR) != null)
													startBit = (int) contentNBR;
											}
											// Case if Length column
											else if(rowIdx > 0 && lengthColumnIdx == cellIdx && !Double.isNaN(contentNBR))
												numBits = (int) contentNBR;
											// Case of Word_Name column
											else if(rowIdx > 0 && wordNameColumnIdx == cellIdx) {
												
												if(contentSTR == null)
													cell = null;
												else {
													if(contentSTR.contains("_MSW"))
													{
														contentSTR = contentSTR.substring(0, contentSTR.length() - "_MSW".length());
														mswName = contentSTR;
													}
													else if(contentSTR.contains("_LSW") && mswName != null && 
															mswName.equals(contentSTR.substring(0, contentSTR.length() - "_LSW".length())))
													{
														// Search for _MSW word in both Messages and TypeMessage
														DefaultMutableTreeNode tmMSW = null;
														String typeMessageName = "TypeMsg_" + family + "_" + mswName + "_" + mappingSheetToMessages.get(sheet.getSheetName());
														tmMSW = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, typeMessageName);
														// If the wordName is not a TypeMsg, error
														if(tmMSW == null)
														{
															JOptionPane.showMessageDialog(null, "1 ERROR CAUSE: " + "MSW-LSW word mismatch (row " + String.valueOf(rowIdx) + ").", null, JOptionPane.ERROR_MESSAGE);
															System.exit(1);
														}
														else {
															updateTypeMsgAndItsAttribute(tmMSW, mswName);
															DefaultMutableTreeNode node = checkExistenceOfDataMsg(mappingSheetToMessages.get(sheet.getSheetName()));
															if(node == null)
															{
																JOptionPane.showMessageDialog(null, "2 ERROR CAUSE: " + "MSW-LSW word mismatch (row " + String.valueOf(rowIdx) + ").", null, JOptionPane.ERROR_MESSAGE);
																System.exit(1);
															}
															updateDataMsgAttribute(node, mswName);
														}
														mswName = null;
														lswFoundExit = true;
														break;
													}
													else
														mswName = null;
												}
												
												// Saving TypeMsg info
												wordNameList.add(contentSTR);
												
											}
											// Case of Name column
											else if(contentSTR != null && rowIdx > 0 && nameColumnIdx == cellIdx) {
												
												if(contentSTR.contains("_MSW") || contentSTR.contains("_LSW"))
													contentSTR = contentSTR.substring(0, contentSTR.length() - "_MSW".length());
												
												contentSTR = contentSTR.replace("(", "_");
												contentSTR = contentSTR.replace(")", "");
												
												nameColumnContent = contentSTR;
											}
											// Case of Type column
											else if(contentSTR != null && rowIdx > 0 && typeColumnIdx == cellIdx) {
												// Saving the type info -> CREATE ALSO TYPE CORRESPONDENCE BETWEEN READ TYPES AND RHAPSODY TYPES
												attributeType = contentSTR;
												
												// Saving type anomalies into List
												if(mappingCodeTypes.get(attributeType) == null)
													typeAnomalies.add(	"Type: " + attributeType + ". Sheet: " + sheet.getSheetName() + 
																		". Word_Name: " + wordNameList.get(wordNameList.size()-1) + ". Name: " +
																		nameColumnContent);
												
											}
											// Case of LSB column
											else if(rowIdx > 0 && lsbColumnIdx == cellIdx) {
												if((cellType.equals("NUMERIC") || cellType.equals("FORMULA")) && !Double.isNaN(contentNBR) && contentNBR != 0 && contentNBR != 1) {
													lsbValue = BigDecimal.valueOf(contentNBR).toPlainString();
													// Checking if the lsbValue is an Integer value
													if(((double)Double.valueOf(lsbValue)) % 1 == 0)
														lsbValue = String.valueOf((int) ((double)Double.valueOf(lsbValue)));
												}
											}
											// Case of Description column
											else if(contentSTR != null && rowIdx > 0 && descriptionColumnIdx == cellIdx)
												description = contentSTR;
											// Case of Units column
											else if(contentSTR != null && rowIdx > 0 && unitsColumnIdx == cellIdx)
												units = contentSTR;											
											
											// Case of Extra Information
											if(rowIdx > 0 && extraInfoColumnIdx == cellIdx) {
												
												if(contentSTR != null && !contentSTR.equals("")) {
												
													String keyExtraInfo = contentSTR.substring(0, contentSTR.indexOf("="));
													String valueExtraInfo = contentSTR.substring(contentSTR.indexOf("=") + 1);
													
													if(keyExtraInfo.equals("IgnoreMinMaxSemantic") && valueExtraInfo.equals("true")) {
														minSemantic = null;
														maxSemantic = null;		
													}
												}
											}
											// Case of Coded Values
											if(contentSTR != null && rowIdx > 0 && codedValuesColumnIdx == cellIdx)
												codedValues = contentSTR;
											// Case of Bias
											if(rowIdx > 0 && biasColumnIdx == cellIdx && !Double.isNaN(contentNBR))
												bias = (double) contentNBR;
											// Case of Used In WDM
											if(contentSTR != null && rowIdx > 0 && usedInWdmColumnIdx == cellIdx)
												usedInWdm = contentSTR;
										}
										
										// HDLC_Header case
										else if(sheet.getSheetName().contains("_Header")) {
											
											introductiveSheets = 3;
											
											if(contentSTR != null && rowIdx == 0) { // header
												if(contentSTR.equals("Name"))
													nameColumnIdx = cellIdx;
												else if(contentSTR.equals("Type"))
													typeColumnIdx = cellIdx;
												else if(contentSTR.equals("Length"))
													lengthColumnIdx = cellIdx;
												else if(contentSTR.equals("Bit_Fields"))
													bitFieldsColumnIdx = cellIdx;
											}
											
											// Case of Name column
											if(contentSTR != null && rowIdx > 0 && nameColumnIdx == cellIdx)
												nameHeader = contentSTR;
											// Case of Length column
											else if(rowIdx > 0 && lengthColumnIdx == cellIdx && !Double.isNaN(contentNBR))
												lengthHeader = (int) contentNBR;
											// Case of Type column
											else if(contentSTR != null && rowIdx > 0 && typeColumnIdx == cellIdx)
												typeHeader = contentSTR;
											
										}
									}
								}
								else {
									if(rowIdx > 0 && wordNameColumnIdx == cellIdx)
										wordNameIsEmpty = true;
								}
							}
							
							if(rowIdx == 0 || lswFoundExit)
								continue;
							
							if(sheet.getSheetName().contains("_Header")) {
								// I have to add a Payload for each message
								for(String singleSheet : mappingSheetToMessages.keySet()) {
									addToPayload(nameHeader, lengthHeader, mappingCodeTypes.get(typeHeader), 
											mappingSheetToMessages.get(singleSheet));
								}
								byteOffsetHeader += 1;
								continue; // I'm not intested in executing the following instructions because the HDLC_Header doesn't have that properties
							}
							
							// Adding elements to JTree
							if(sheet.getSheetName().equals("info")) {
								try
								{
									addElementInRhapsodyPlugIn(versionCell.getStringCellValue());
								}catch(IllegalStateException ise)
								{
									addElementInRhapsodyPlugIn(String.valueOf(versionCell.getNumericCellValue()));
								}
								continue;
							}
							else if(sheet.getSheetName().equals("messages")) {
								addToMessageTypePkg(messages.get(messages.size()-1));
								setMessageTags(endianism, source, messages.get(messages.size()-1), frequency, 
										totalBytesMessage.get(totalBytesMessage.size()-1), descriptionMessage, uniqueID, usedInWdmMessage);
								setPayloadTags(messages.get(messages.size()-1), totalBytesMessage.get(totalBytesMessage.size()-1), usedInWdm);
								continue;
							}
							else {
								
								// IF WORD_NAME IS EMPTY
								if(wordNameIsEmpty) {
									
									DefaultMutableTreeNode node = checkExistenceOfDataMsg(mappingSheetToMessages.get(sheet.getSheetName()));
									
									if(node != null) {
										addAttributeToDataMsg(node, nameColumnContent);
										setTagsToDataMsgAttribute(node, nameColumnContent, byteOffset, numBytes, totalBytesMessage.get(sheetNumber-introductiveSheets), 
												mappingCodeTypes.get(attributeType), mappingSheetToMessages.get(sheet.getSheetName()), usedInWdm);
									}
									
								}
								else {
									DefaultMutableTreeNode node = checkExistenceOfDataMsg(mappingSheetToMessages.get(sheet.getSheetName()));
									
									if(node != null) {
										
										addAttributeToDataMsg(node, wordNameList.get(wordNameList.size()-1) + "_" + mappingSheetToMessages.get(sheet.getSheetName()));
										setTagsToDataMsgAttribute(node, wordNameList.get(wordNameList.size()-1) + "_" + mappingSheetToMessages.get(sheet.getSheetName()),
												byteOffset, numBytes, totalBytesMessage.get(sheetNumber-introductiveSheets), mappingCodeTypes.get(attributeType), 
													mappingSheetToMessages.get(sheet.getSheetName()), usedInWdm);
									}
								}
								
								if(mappingInitialByteOffset_WordName.get(wordNameList.get(wordNameList.size()-1)) == null)
									mappingInitialByteOffset_WordName.put(wordNameList.get(wordNameList.size()-1), byteOffset);
								
								// Inserting TypeMsg and attribute of DataMsg linked to Type Msg inserted
								addTypeMsgToCompositeTypes(wordNameList.get(wordNameList.size()-1) + "_" + mappingSheetToMessages.get(sheet.getSheetName()), 
										mappingSheetToMessages.get(sheet.getSheetName()), wordNameIsEmpty);
								
								// Case of Primitive Types
								attributeType = mappingCodeTypes.get(attributeType);
								
								// TypeMsg attribute insertion
								typeMessage = "TypeMsg_" + family + "_" + wordNameList.get(wordNameList.size()-1) + "_" + mappingSheetToMessages.get(sheet.getSheetName());
																
								// Case of Elementary Types
								if(minSemantic != null || maxSemantic != null || (lsbValue != null && Double.parseDouble(lsbValue) != 0 && Double.parseDouble(lsbValue) != 1 && !wordNameIsEmpty)) {
									
									//CCO
									addToElementaryTypes(nameColumnContent, mappingSheetToMessages.get(sheet.getSheetName()), attributeType, lsbValue, units, minSemantic, maxSemantic, numBits);
									
									attributeType = nameColumnContent;
									
									// Inserting attribute to TypeMsg
									addAttributeToTypeMessage(typeMessage, nameColumnContent, attributeType + "_" + mappingSheetToMessages.get(sheet.getSheetName()), wordNameIsEmpty, 
											wordNameList.get(wordNameList.size()-1));
									
								}
								else
									// Inserting attribute to TypeMsg
									addAttributeToTypeMessage(typeMessage, nameColumnContent, attributeType, wordNameIsEmpty, wordNameList.get(wordNameList.size()-1));
								
								// This is the case in which the attributeType of the DataMsg is a TypeMsg itself
								if(!wordNameIsEmpty && 
										TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, 
												"TypeMsg_" + family + "_" + wordNameList.get(wordNameList.size()-1)) + 
													"_" + mappingSheetToMessages.get(sheet.getSheetName()) != null)
									attributeType = wordNameList.get(wordNameList.size()-1) + "_" + mappingSheetToMessages.get(sheet.getSheetName());
								

								if(!wordNameIsEmpty) {
									
									// Inside the TypeMsg the byte offset is RELATIVE to its word
									if(mappingInitialByteOffset_WordName.get(wordNameList.get(wordNameList.size()-1)) != null) 
										setTagsToTypeMsgAttribute(typeMessage, nameColumnContent, description, codedValues, startBit, 
												numBits, byteOffset-mappingInitialByteOffset_WordName.get(wordNameList.get(wordNameList.size()-1)), bias, usedInWdm);
									
									DefaultMutableTreeNode node = checkExistenceOfDataMsg(mappingSheetToMessages.get(sheet.getSheetName()));
									if(node != null) {
										String lastWordName = wordNameList.get(wordNameList.size()-1) + "_" + mappingSheetToMessages.get(sheet.getSheetName());
										String lastLastWordName = null;
										
										try {
											lastLastWordName = wordNameList.get(wordNameList.size()-2) + "_" + mappingSheetToMessages.get(sheet.getSheetName());
										}catch(IndexOutOfBoundsException iobe) {}
										
										if(lastLastWordName == null) {													
											// Save ByteOffset and NumBytes
											comparisonByteOffset = byteOffset;
											comparisonNumBytes = endByte;
											comparisonNode = node;
											comparisonAttributesOfTypeMessage = lastWordName;
										}
										else if(lastWordName.equals(lastLastWordName)) {
											
											// Overwriting comparisonNumBytes
											comparisonNumBytes = endByte;
																								
											// Writing previous info
											addAttributeToDataMsg(comparisonNode, comparisonAttributesOfTypeMessage);
											setTagsToDataMsgAttribute(comparisonNode, comparisonAttributesOfTypeMessage, comparisonByteOffset, comparisonNumBytes - comparisonByteOffset + 1,
													totalBytesMessage.get(sheetNumber-introductiveSheets), attributeType, mappingSheetToMessages.get(sheet.getSheetName()), usedInWdm);
										}
										else {
											
											// Saving new info
											comparisonByteOffset = byteOffset; // Inside the DataMsg the byte offset is ABSOLUTE
											comparisonNumBytes = endByte;
											comparisonNode = node;
											comparisonAttributesOfTypeMessage = lastWordName;
																								
											// Writing previous info
											addAttributeToDataMsg(comparisonNode, comparisonAttributesOfTypeMessage);
											setTagsToDataMsgAttribute(comparisonNode, comparisonAttributesOfTypeMessage, comparisonByteOffset, comparisonNumBytes - comparisonByteOffset + 1,
													totalBytesMessage.get(sheetNumber-introductiveSheets), attributeType, mappingSheetToMessages.get(sheet.getSheetName()), usedInWdm);
											
										}
									}
								}					
							}						
						}
					}					
				}
				
				// Update DataModel version in Excel
				if(versionCell != null) {
					try {
						double oldVersion = versionCell.getNumericCellValue();
						versionCell.setCellValue(oldVersion + 1);
						
						// Write the changes back to the file
						try(FileOutputStream fileOut = new FileOutputStream(path)){
							workbook.write(fileOut);
						}catch(IOException e) {
							JOptionPane.showMessageDialog(null, "Could not update \"version\" field on Excel file because it was already opened when launching the GUI."
																+ "\n\n                                          ***** UPDATE MANUALLY EXCEL FILE VERSION *****", null, JOptionPane.WARNING_MESSAGE);
						}
					}catch(Exception vce) {
						JOptionPane.showMessageDialog(null, "Could not store DataModel version in Excel: the cell was corrupted."
								                                + "\n\n     ***** UPDATE MANUALLY EXCEL FILE VERSION *****", null, JOptionPane.WARNING_MESSAGE);
					}
				}
				
				workbook.close();
				
				CustomPrint.printOnJTextPane("...done");
				GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				
				if(typeAnomalies.size() > 0) {
					
					String anomaliesBuffer = "";
					
					for(int i = 0; i < typeAnomalies.size(); i++)
						anomaliesBuffer += typeAnomalies.get(i) + "\n";
					
					JOptionPane.showMessageDialog(null, "ANOMALIES DETECTED IN TYPES:\n" + anomaliesBuffer, null, JOptionPane.WARNING_MESSAGE);
					
				}				
			}
			
			file.close();
			
			CustomPrint.printOnJTextPane("Excel file loaded.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			
			resetStaticVariables();
			
		}catch(Exception ioe) {			
			JOptionPane.showMessageDialog(null, "ERROR CAUSE: " + ioe.toString() + ".\n\nProbably the error resides in wrong Type in Excel TYPE column.", null, JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	protected static void resetStaticVariables() {
		family = null;
		messTypePkg = null;
		compositeTypes = null;
		elementaryTypes = null;
		pkg = null;
		versionCell = null;
		introductiveSheets = 2;
		byteOffsetHeader = 0;
		root = (DefaultMutableTreeNode) GUI.tree.getModel().getRoot();
		mappingCodeTypes = new HashMap<String, String>() { {	put("UND", "unsigned int");
																put("D", "bool");
																put("C", "Coded");
																put("2C", "Signed");
																put("N", "int");
																put("V", "bool");
																put("uint8", "unsigned int");
																put("Untyped", "Untyped");
																put("int", "int");
																put("double", "double");
															} };
	}
	
	
	protected static void addElementInRhapsodyPlugIn(String version) {

		GUI.tp = new TreePath(root.getPath());
		GUI.nameTextField.setText(family);
		TreeInsertion.insertNode();
		
		// Find child of root
		// O(log(n))
		pkg = TreeSearch.binarySearchForNodesWithoutStereotypes(root, "ICD_" + family);
		
		IRPModelElement pkgIRP = ((CustomIRPModelElement) pkg.getUserObject()).getElement();
		
		IRPTag deviceNameTag = pkgIRP.getTag("Device_Name");
		pkgIRP.setTagValue(deviceNameTag, family);
		
		IRPTag versionTag = pkgIRP.getTag("Version");
		pkgIRP.setTagValue(versionTag, family + "_" + version);
		
	}
	
	
	protected static void addToMessageTypePkg(String name) {
		
		if(messTypePkg == null) {
			
			// O(n) where n = 3 in the worst case
			for(int i = 0; i < pkg.getChildCount(); i++) {
				messTypePkg = (DefaultMutableTreeNode) pkg.getChildAt(i);
				if(((CustomIRPModelElement) messTypePkg.getUserObject()).toStringWithoutStereotypes().contains("_Message_Type_Pkg"))
					break;
			}
		}
		
		// With this insertion also DataMsg and Payload will be inserted
		GUI.tp = new TreePath(messTypePkg.getPath());
		GUI.nameTextField.setText(name);
		TreeInsertion.insertNode();
	}
	
	
	protected static void addTypeMsgToCompositeTypes(String name, String message, Boolean wordNameIsEmpty) {
		
		if(!wordNameIsEmpty) {
		
			if(compositeTypes == null) {
				
				// O(n) where n = 3 in the worst case
				for(int i = 0; i < pkg.getChildCount(); i++) {
					compositeTypes = (DefaultMutableTreeNode) pkg.getChildAt(i);
					if(((CustomIRPModelElement) compositeTypes.getUserObject()).toStringWithoutStereotypes().contains("_Composite_Types"))
						break;
				}
			}
			
			GUI.tp = new TreePath(compositeTypes.getPath());
			GUI.nameTextField.setText(name);
			TreeInsertion.insertNode();
			
			// Set tags to TypeMsg
			// O(log(n))
			DefaultMutableTreeNode tm = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, "TypeMsg_" + family + "_" + name);
			
			IRPModelElement tmIRP = ((CustomIRPModelElement) tm.getUserObject()).getElement();
			IRPTag description = tmIRP.getTag("Description");
			tmIRP.setTagValue(description, "Composite per word " + name);
		
			// In this case it is necessary to insert the Type message also into the DataMsg
			DefaultMutableTreeNode dataMsg = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, "DataMsg_" + family + "_" + message);
			
			GUI.tp = new TreePath(dataMsg.getPath());
			GUI.nameTextField.setText(name);
			TreeInsertion.insertNode();
			
			DefaultMutableTreeNode dataMsgChild = null;
			Boolean found = false;
			
			// O(n)
			for(int i = 0; i < dataMsg.getChildCount(); i++) {
				dataMsgChild = (DefaultMutableTreeNode) dataMsg.getChildAt(i);
				if(((CustomIRPModelElement) dataMsgChild.getUserObject()).toStringWithoutStereotypes().equals(name)) {
					found = true;
					break;
				}
			}
			
			if(!found)
				dataMsgChild = null;
		
			IRPAttribute dataMsgChildIRP = (IRPAttribute) ((CustomIRPModelElement) dataMsgChild.getUserObject()).getElement();
			
			dataMsgChildIRP.setTypeDeclaration("TypeMsg_" + family + "_" + name);
		}
		
	}
	
	
	protected static void setMessageTags(String endianism, String src, String textMessage, int frequency, int totalBytes, String descriptionMessage, int uniqueID, String usedInWdmMessage) {
		
		// Setting endianism		
		if(endianism == null || endianism == "")
			endianism = "Little";
		else {
			// transforming "big" to "Big"
			endianism = endianism.toLowerCase();
			endianism = endianism.substring(0, 1).toUpperCase() + endianism.substring(1);
		}
		
		IRPModelElement messTypePkgIRP = ((CustomIRPModelElement) messTypePkg.getUserObject()).getElement();
		IRPTag tag = messTypePkgIRP.getTag("Indianism");
		messTypePkgIRP.setTagValue(tag, endianism);
		
		
		// Setting source
		if(src != null) {
			if(src.equals("WDM"))
				src = "fromWDM";
			else
				src = "toWDM";
		}
		
		DefaultMutableTreeNode message = null;
		DefaultMutableTreeNode messageAttribute = null;
		
		// O(n)
		for(int i = 0; i < messTypePkg.getChildCount(); i++) {
			message = (DefaultMutableTreeNode) messTypePkg.getChildAt(i);
			if(((CustomIRPModelElement) message.getUserObject()).toStringWithoutStereotypes().equals(textMessage))
				break;
		}
		
		IRPModelElement messageIRP = ((CustomIRPModelElement) message.getUserObject()).getElement();
		
		// Setting frequency tag
		if(frequency != Integer.MAX_VALUE) {
			IRPTag frequencyTag = messageIRP.getTag("Frequency");
			messageIRP.setTagValue(frequencyTag, String.valueOf(frequency) + " Hz");
		}
		
		// Setting Bytes tag
		if(totalBytes != Integer.MAX_VALUE) {
			IRPTag bytesTag = messageIRP.getTag("No_Bytes");
			messageIRP.setTagValue(bytesTag, String.valueOf(totalBytes));
		
			// Setting Bits tag
			IRPTag bitsTag = messageIRP.getTag("No_Bits");
			messageIRP.setTagValue(bitsTag, String.valueOf(totalBytes*8));
		}
		
		// Setting Description tag
		if(descriptionMessage != null) {
			IRPTag descriptionTag = messageIRP.getTag("Description");
			messageIRP.setTagValue(descriptionTag, descriptionMessage);
		}
		
		// Setting Direction tag
		if(src != null) {
			IRPTag directionTag = messageIRP.getTag("Direction");
			messageIRP.setTagValue(directionTag, src);
		}
		
		// Setting Unique_id tag
		if(uniqueID != Integer.MAX_VALUE) {
			IRPTag uniqueIDTag = messageIRP.getTag("Unique_id");
			messageIRP.setTagValue(uniqueIDTag, String.valueOf(uniqueID));
		}
		
		// Setting Used_In_WDM tag. By default, it is set to TRUE; so only if it is FALSE then I change its value
		if(usedInWdmMessage != null) {
			if((usedInWdmMessage.toLowerCase()).equals("false")) {
				IRPTag usedInWdmTag = messageIRP.getTag("Used_In_WDM");
				messageIRP.setTagValue(usedInWdmTag, "False");
			}
		}
		
		// Setting tags to Physical_Payload attribute
		
		// O(n) -> but it's O(1) because there's always 1 attribute
		for(int i = 0; i < message.getChildCount(); i++) {
			messageAttribute = (DefaultMutableTreeNode) message.getChildAt(i);
			if(((CustomIRPModelElement) messageAttribute.getUserObject()).toStringWithoutStereotypes().equals("Physical_Payload_" + textMessage))
				break;
		}
		
		IRPModelElement messageAttributeIRP = ((CustomIRPModelElement) messageAttribute.getUserObject()).getElement();

		if(totalBytes != Integer.MAX_VALUE) {
			IRPTag tagByteOffset = messageAttributeIRP.getTag("Byte_Offset");
			messageAttributeIRP.setTagValue(tagByteOffset, String.valueOf(0));
			IRPTag tagNumBytes = messageAttributeIRP.getTag("No_Bytes");
			messageAttributeIRP.setTagValue(tagNumBytes, String.valueOf(totalBytes));
		}
	}
	
	
	protected static void setPayloadTags(String message, int numBytesPayload, String usedInWdm) {
		
		if(compositeTypes == null) {
			
			// O(n) where n = 3 in the worst case
			for(int i = 0; i < pkg.getChildCount(); i++) {
				compositeTypes = (DefaultMutableTreeNode) pkg.getChildAt(i);
				if(((CustomIRPModelElement) compositeTypes.getUserObject()).toStringWithoutStereotypes().contains("_Composite_Types"))
					break;
			}
		}
		
		DefaultMutableTreeNode payload = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, "Payload_" + family + "_" + message);
		
		IRPModelElement payloadIRP = ((CustomIRPModelElement) payload.getUserObject()).getElement();
		
		IRPTag descriptionTag = payloadIRP.getTag("Description");
		payloadIRP.setTagValue(descriptionTag, "Composite per payload fisico msg " + message);
		
		IRPTag numBytesTag = payloadIRP.getTag("No_Bytes");
		payloadIRP.setTagValue(numBytesTag, String.valueOf(numBytesPayload));
		IRPTag numBitsTag = payloadIRP.getTag("No_Bits");
		payloadIRP.setTagValue(numBitsTag, String.valueOf(numBytesPayload*8));
		
		DefaultMutableTreeNode payloadChild = null;
		DefaultMutableTreeNode icdLogicalPayloadChild = null;
		
		// Setting tags to Payload attribute
		for(int i = 0; i < payload.getChildCount(); i++) {
			payloadChild = (DefaultMutableTreeNode) payload.getChildAt(i);
			IRPModelElement payloadChildIRP = ((CustomIRPModelElement) payloadChild.getUserObject()).getElement();
			// Setting Used_In_WDM tag. By default, it is set to TRUE; so only if it is FALSE then I change its value
			if(usedInWdm != null) {
				if((usedInWdm.toLowerCase()).equals("false")) {
					IRPTag usedInWdmTag = payloadChildIRP.getTag("Used_In_WDM");
					payloadChildIRP.setTagValue(usedInWdmTag, "False");
				}
			}
			if(((CustomIRPModelElement) payloadChild.getUserObject()).toString().contains("ICD_Logical_Payload"))
				icdLogicalPayloadChild = payloadChild;
		}
		
		IRPModelElement payloadChildIRP = ((CustomIRPModelElement) icdLogicalPayloadChild.getUserObject()).getElement();
		
		IRPTag byteOffsetTag = payloadChildIRP.getTag("Byte_Offset");
		payloadChildIRP.setTagValue(byteOffsetTag, String.valueOf(byteOffsetHeader));
		IRPTag noBytesTag = payloadChildIRP.getTag("No_Bytes");
		payloadChildIRP.setTagValue(noBytesTag, String.valueOf(numBytesPayload));
		IRPTag noBitsTag = payloadChildIRP.getTag("No_Bits");
		payloadChildIRP.setTagValue(noBitsTag, String.valueOf((numBytesPayload)*8));
		
	}
	
	
	protected static void addAttributeToTypeMessage(String typeMsg, String attribute, String attributeType, Boolean wordNameIsEmpty, String wordName) {
		
		DefaultMutableTreeNode tm = null;
		
		if(!wordNameIsEmpty) {
			
			// First, check whether the wordName is a TypeMsg itself
			tm = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, "TypeMsg_" + family + "_" + wordName);
			
			if(tm == null)
				// Find child of root
				// O(log(n))
				tm = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, typeMsg);
	
			GUI.tp = new TreePath(tm.getPath());
			GUI.nameTextField.setText(attribute);	
			TreeInsertion.insertNode();
			
			DefaultMutableTreeNode typeMsgChild = null;
			Boolean found = false;
	
			// O(n)
			for(int i = 0; i < tm.getChildCount(); i++) {
				typeMsgChild = (DefaultMutableTreeNode) tm.getChildAt(i);
				if(((CustomIRPModelElement) typeMsgChild.getUserObject()).toStringWithoutStereotypes().equals(attribute)) {
					found = true;
					break;
				}
			}
			
			if(!found)
				typeMsgChild = null;
		
			IRPAttribute typeMsgChildIRP = (IRPAttribute) ((CustomIRPModelElement) typeMsgChild.getUserObject()).getElement();
			
			if(attributeType.equals("Signed"))
				typeMsgChildIRP.setTypeDeclaration("int");
			else
				typeMsgChildIRP.setTypeDeclaration(attributeType);
		}
		
	}
	
	
	protected static void addToElementaryTypes(String name, String sheet, String signedOrUnsigned, String lsbValue, String units, String minSem, String maxSem, int numBits) {
		
		if(elementaryTypes == null) {
			
			// O(n) where n = 3 in the worst case
			for(int i = 0; i < pkg.getChildCount(); i++) {
				elementaryTypes = (DefaultMutableTreeNode) pkg.getChildAt(i);
				if(((CustomIRPModelElement) elementaryTypes.getUserObject()).toStringWithoutStereotypes().contains("_Elementary_Types"))
					break;
			}
		}
				
		DefaultMutableTreeNode eT = TreeSearch.binarySearchForNodesWithoutStereotypes(elementaryTypes, name + "_" + sheet);
		
		if(eT == null) {
			GUI.tp = new TreePath(elementaryTypes.getPath());
			GUI.nameTextField.setText(name + "_" + sheet);
			TreeInsertion.insertNode();
		}
		
		eT = TreeSearch.binarySearchForNodesWithoutStereotypes(elementaryTypes, name + "_" + sheet);
		IRPModelElement eTIRP = ((CustomIRPModelElement) eT.getUserObject()).getElement();
		
		if(lsbValue != null) {
			IRPTag tag = eTIRP.getTag("LSB");
			eTIRP.setTagValue(tag, lsbValue);
		}
		
		if(signedOrUnsigned != null) {
			
			if(signedOrUnsigned.equals("unsigned int"))
				signedOrUnsigned = "Unsigned";
			
			IRPTag tagNT = eTIRP.getTag("Numeric_Type");
			eTIRP.setTagValue(tagNT, signedOrUnsigned);
			
		}
		
		// Setting Units
		if(units != null) {
			IRPTag unitTag = eTIRP.getTag("Units");
//			if(units.equals("rads"))
//				units = "rad/s";
//			else if(units.equals("ms"))
//				units = "m/s";
//			else if(units.equals("ms2"))
//				units = "m/s^2";
//			else if(units.equals("ms3"))
//				units = "m/s^3";
//			else if(units.equals("kgm3"))
//				units = "kg/m^3";
//			else if(units.equals("Nm2"))
//				units = "N/m^2";
			eTIRP.setTagValue(unitTag, units);
		}
		
		// Setting min and max semantic
		if(minSem != null) {
			IRPTag minTag = eTIRP.getTag("Min_semantic");
			eTIRP.setTagValue(minTag, String.valueOf(minSem));
		}
		if(maxSem != null) {
			IRPTag maxTag = eTIRP.getTag("Max_semantic");
			eTIRP.setTagValue(maxTag, String.valueOf(maxSem));
		}
		
		// Setting numBits
		if(numBits != Integer.MAX_VALUE) {
			IRPTag numBitsTag = eTIRP.getTag("No_Bits");
			eTIRP.setTagValue(numBitsTag, String.valueOf(numBits));
		}
		
	}
	
	
	protected static void addToPayload(String attributeName, int length, String type, String sheet) {
		
		if(compositeTypes == null) {
			
			// O(n) where n = 3 in the worst case
			for(int i = 0; i < pkg.getChildCount(); i++) {
				compositeTypes = (DefaultMutableTreeNode) pkg.getChildAt(i);
				if(((CustomIRPModelElement) compositeTypes.getUserObject()).toStringWithoutStereotypes().contains("_Composite_Types"))
					break;
			}
		}
		
		DefaultMutableTreeNode payload = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, "Payload_" + family + "_" + sheet);
		
		GUI.tp = new TreePath(payload.getPath());
		GUI.nameTextField.setText(attributeName);
		TreeInsertion.insertNode();
		
		DefaultMutableTreeNode payloadChild = null;
		Boolean found = false;

		// O(n)
		for(int i = 0; i < payload.getChildCount(); i++) {
			payloadChild = (DefaultMutableTreeNode) payload.getChildAt(i);
			if(((CustomIRPModelElement) payloadChild.getUserObject()).toStringWithoutStereotypes().equals(attributeName)) {
				found = true;
				break;
			}
		}
		
		if(!found)
			payloadChild = null;
	
		IRPModelElement payloadChildIRP = ((CustomIRPModelElement) payloadChild.getUserObject()).getElement();
		
		((IRPAttribute) payloadChildIRP).setTypeDeclaration(type);
		
		IRPTag byteOffsetTag = payloadChildIRP.getTag("Byte_Offset");
		payloadChildIRP.setTagValue(byteOffsetTag, String.valueOf(byteOffsetHeader));
		
		IRPTag noBytesTag = payloadChildIRP.getTag("No_Bytes");
		payloadChildIRP.setTagValue(noBytesTag, String.valueOf((int) length/8));
		IRPTag noBitsTag = payloadChildIRP.getTag("No_Bits");
		payloadChildIRP.setTagValue(noBitsTag, String.valueOf(length));
		
		// Setting byteOffset of ICD_Signal_Payload
		
		DefaultMutableTreeNode ICD_Logical_Payload = null;
		
		// Setting tags to Payload attribute
		for(int i = 0; i < payload.getChildCount(); i++) {
			ICD_Logical_Payload = (DefaultMutableTreeNode) payload.getChildAt(i);
			if(((CustomIRPModelElement) ICD_Logical_Payload.getUserObject()).toString().contains("ICD_Logical_Payload"))
				break;
		}
		
		IRPModelElement ICD_Logical_PayloadIRP = ((CustomIRPModelElement) ICD_Logical_Payload.getUserObject()).getElement();
		
		byteOffsetTag = ICD_Logical_PayloadIRP.getTag("Byte_Offset");
		ICD_Logical_PayloadIRP.setTagValue(byteOffsetTag, String.valueOf(byteOffsetHeader+1));
		
	}
	
	
	protected static void setTagsToTypeMsgAttribute(String typeMessage, String attributesOfTypeMessage, String description, String codedValues, int startBit, 
			int numBits, int byteOffset, double bias, String usedInWdm) {
				
		// O(log(n))
		DefaultMutableTreeNode tm = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, typeMessage);
		
		DefaultMutableTreeNode node = null;
		
		// O(n)
		for(int i = 0; i < tm.getChildCount(); i++) {
			node = (DefaultMutableTreeNode) tm.getChildAt(i);
			if(((CustomIRPModelElement) node.getUserObject()).toStringWithoutStereotypes().equals(attributesOfTypeMessage))
				break;
		}
		
		IRPModelElement elem = ((CustomIRPModelElement) node.getUserObject()).getElement();
		elem.setDescription(codedValues); // TODO description is never used, only codedValues
		
		if(!Double.isNaN(bias) && bias != 0) {
			IRPTag voTag = elem.getTag("Value_Offset");
			elem.setTagValue(voTag, String.valueOf(bias));
		}
		
		// Setting Used_In_WDM tag. By default, it is set to TRUE; so only if it is FALSE then I change its value
		if(usedInWdm != null) {
			if((usedInWdm.toLowerCase()).equals("false")) {
				IRPTag usedInWdmTag = elem.getTag("Used_In_WDM");
				elem.setTagValue(usedInWdmTag, "False");
			}
		}
				
		try {
			if(startBit != Integer.MAX_VALUE && String.valueOf(startBit) != null) {
				IRPTag sb = elem.getTag("Start_Bit");
				elem.setTagValue(sb, String.valueOf(startBit));
			}
		}catch(NullPointerException npe) {}
		try {
			if(numBits != Integer.MAX_VALUE) {
				IRPTag nob = elem.getTag("No_Bits");
				elem.setTagValue(nob, String.valueOf(numBits));
				
//				IRPTag noby = elem.getTag("No_Bytes");
//				elem.setTagValue(noby, String.valueOf((int) numBits/8));
				
				// only for those fields which are Words it is necessary to store also the info on ByteOffset, otherwise Fra's plug-in doesn't work
				if(numBits == 16) {
					IRPTag nobo = elem.getTag("Byte_Offset");
					elem.setTagValue(nobo, String.valueOf(byteOffset));
				}
			}
		}catch(NullPointerException npe) {}
		
		
	}
	
	
	protected static DefaultMutableTreeNode checkExistenceOfDataMsg(String sheetName) {
		
		if(compositeTypes == null) {
			
			// O(n) where n = 3 in the worst case
			for(int i = 0; i < pkg.getChildCount(); i++) {
				compositeTypes = (DefaultMutableTreeNode) pkg.getChildAt(i);
				if(((CustomIRPModelElement) compositeTypes.getUserObject()).toStringWithoutStereotypes().contains("_Composite_Types"))
					break;
			}
		}
		
		// O(log(n))
		return TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, "DataMsg_" + family + "_" + sheetName);
	}
	
	
	protected static void addAttributeToDataMsg(DefaultMutableTreeNode node, String nameColumnContent) {
		
		GUI.tp = new TreePath(node.getPath());
		GUI.nameTextField.setText(nameColumnContent);
		TreeInsertion.insertNode();
		
	}
	
	
	protected static void setTagsToDataMsgAttribute(DefaultMutableTreeNode dm, String attributesOfTypeMessage, int byteOffset, int numBytes, int totalBytes, 
			String attributeType, String sheet, String usedInWdm) {
				
		// First, setting DataMsg tags
		IRPModelElement dmIRP = ((CustomIRPModelElement) dm.getUserObject()).getElement();
		IRPTag descriptionTag = dmIRP.getTag("Description");
		dmIRP.setTagValue(descriptionTag, "Composite per payload del msg " + dmIRP.getName().substring("DataMsg_".length() + family.length() + "_".length()));
		
		// Setting Bytes tag of DataMSG
		IRPTag bytesTag = dmIRP.getTag("No_Bytes");
		dmIRP.setTagValue(bytesTag, String.valueOf(totalBytes));
		
		// Setting Bits tag of DataMSG
		IRPTag bitsTag = dmIRP.getTag("No_Bits");
		dmIRP.setTagValue(bitsTag, String.valueOf((totalBytes)*8));
		
		DefaultMutableTreeNode node = null;
		
		// O(n)
		for(int i = 0; i < dm.getChildCount(); i++) {
			node = (DefaultMutableTreeNode) dm.getChildAt(i);
			if(((CustomIRPModelElement) node.getUserObject()).toStringWithoutStereotypes().equals(attributesOfTypeMessage))
				break;
		}
		
		// Setting attributes for DataMsg attribute
		
		IRPModelElement elem = ((CustomIRPModelElement) node.getUserObject()).getElement();
		
		DefaultMutableTreeNode tm = TreeSearch.binarySearchForNodesWithoutStereotypes(compositeTypes, "TypeMsg_" + family + "_" + attributeType);
		
		if(tm != null) {
			attributeType = "TypeMsg_" + family + "_" + attributeType;
			
			IRPModelElement tmIRP = ((CustomIRPModelElement) tm.getUserObject()).getElement();
			
			IRPTag no_bytes = tmIRP.getTag("No_Bytes");
			tmIRP.setTagValue(no_bytes, String.valueOf(numBytes));
			IRPTag no_bits = tmIRP.getTag("No_Bits");
			tmIRP.setTagValue(no_bits, String.valueOf(numBytes*8));
		}
		
		
		((IRPAttribute) elem).setTypeDeclaration(attributeType);
		
		// Setting Used_In_WDM tag. By default, it is set to TRUE; so only if it is FALSE then I change its value
		if(usedInWdm != null) {
			if((usedInWdm.toLowerCase()).equals("false")) {
				IRPTag usedInWdmTag = elem.getTag("Used_In_WDM");
				elem.setTagValue(usedInWdmTag, "False");
			}
		}
		
		try {
			if(byteOffset != Integer.MAX_VALUE) {
				IRPTag bo = elem.getTag("Byte_Offset");
				elem.setTagValue(bo, String.valueOf(byteOffset));
			}
		}catch(NullPointerException npe) {}
		try {
			if(numBytes != Integer.MAX_VALUE) {
				IRPTag nb = elem.getTag("No_Bytes");
				elem.setTagValue(nb, String.valueOf(numBytes));
				
				IRPTag nbits = elem.getTag("No_Bits");
				elem.setTagValue(nbits, String.valueOf(numBytes*8));
			}
		}catch(NullPointerException npe) {}
		
	}
	
	
	protected static void updateDataMsgAttribute(DefaultMutableTreeNode dm, String attributeName)
	{
		DefaultMutableTreeNode node = null;
		// O(n)
		for(int i = 0; i < dm.getChildCount(); i++) {
			node = (DefaultMutableTreeNode) dm.getChildAt(i);
			if(((CustomIRPModelElement) node.getUserObject()).toStringWithoutStereotypes().equals(attributeName))
				break;
		}
		
		// Setting attributes for DataMsg attribute
		IRPModelElement dmIRP = ((CustomIRPModelElement) node.getUserObject()).getElement();
		IRPTag bytesTag = dmIRP.getTag("No_Bytes");
		dmIRP.setTagValue(bytesTag, String.valueOf(4));
		IRPTag bitsTag = dmIRP.getTag("No_Bits");
		dmIRP.setTagValue(bitsTag, String.valueOf(4*8));
	}
	
	
	protected static void updateTypeMsgAndItsAttribute(DefaultMutableTreeNode tm, String attributeName)
	{
		// Updating TypeMsg info
		IRPModelElement tmElem = ((CustomIRPModelElement) tm.getUserObject()).getElement();
		IRPTag tmBytesTag = tmElem.getTag("No_Bytes");
		tmElem.setTagValue(tmBytesTag, String.valueOf(4));
		IRPTag tmBitsTag = tmElem.getTag("No_Bits");
		tmElem.setTagValue(tmBitsTag, String.valueOf(4*8));
		// Updating typeMsg's child info
		DefaultMutableTreeNode node = null;
		// O(n)
		for(int i = 0; i < tm.getChildCount(); i++) {
			node = (DefaultMutableTreeNode) tm.getChildAt(i);
			if(((CustomIRPModelElement) node.getUserObject()).toStringWithoutStereotypes().equals(attributeName))
				break;
		}
		IRPModelElement elem = ((CustomIRPModelElement) node.getUserObject()).getElement();
		IRPTag bytesTag = elem.getTag("No_Bytes");
		elem.setTagValue(bytesTag, String.valueOf(4));
		IRPTag bitsTag = elem.getTag("No_Bits");
		elem.setTagValue(bitsTag, String.valueOf(4*8));
	}
	

}

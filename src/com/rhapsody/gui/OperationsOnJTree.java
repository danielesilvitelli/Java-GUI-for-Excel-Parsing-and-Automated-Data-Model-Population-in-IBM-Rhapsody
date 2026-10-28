package com.rhapsody.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPTag;
import com.telelogic.rhapsody.core.RhapsodyRuntimeException;



/*
	This class contains each operation done on the JTree, except those which manipulate
	the tree (add, modify and remove a node)
*/
public class OperationsOnJTree {
	
	private static IRPModelElement searchedItem = null;
	private static HashMap<String, String[]> rhapsodySupportFolder = 
			new HashMap<String, String[]>();
	private static ArrayList<String> allTypes;
	public static HashMap<String, ArrayList<String>> typeCorrespondences = new HashMap<String, ArrayList<String>>();
	protected static ArrayList<String> listOfPaths = new ArrayList<>(); // useful to understand if idStereotypeRadioButton can be enabled or not
	
	/*
		Utility function which gets from Rhapsody all necessary info and populates a GUI tree
	*/
	protected static void populatingTheTree(DefaultTreeModel model, IRPCollection children, DefaultMutableTreeNode parent) {
		// Exit condition
		if (children.getCount() == 0)
			return;
		
		for (int i = 1; i <= children.getCount(); i++) {
			IRPModelElement currentElement = (IRPModelElement) children.getItem(i);
			
			/*
				I'm only interested in PACKAGES, TYPES and ATTRIBUTES
				If "currentElement" variable is not a package, neither a type, 
				neither a class, I will skip it
			*/
			if ((((currentElement.getIsOfMetaClass("Package") != 1) || 
					(currentElement.getIsOfMetaClass("Profile") == 1))
					&& (currentElement.getIsOfMetaClass("Type") != 1)
					&& (currentElement.getIsOfMetaClass("Attribute") != 1)))
				continue;
			else if (currentElement.getName().equals("Support")) {
				rhapsodySupportFolder = GUI.rs.supportTags((IRPPackage) currentElement);
				continue;
			}
			
			// Don't consider packages of level 1 that don't have "ICD_" prefix
			if (((CustomIRPModelElement) parent.getUserObject()).toStringWithoutStereotypes().equals(GUI.rs.getRoot().getName())
					&& !currentElement.getName().contains("ICD_"))
				continue;

			CustomIRPModelElement cie = new CustomIRPModelElement(currentElement);
			DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(currentNode, parent, parent.getChildCount());			
			
			// Storing Types into map of correspondences in which the keys are the ICD
			if(currentElement.getIsOfMetaClass("Type") == 1) {
				
				CustomIRPModelElement icd = (CustomIRPModelElement) ((DefaultMutableTreeNode) parent.getParent()).getUserObject();
				String icdName = icd.toStringWithoutStereotypes();
				// If the icd wasn't stored yet, store it now into the map
				if(typeCorrespondences.get(icdName) == null) {
					allTypes = new ArrayList<>( Arrays.asList(	"Untyped", "bool", "char", "char*", "double",
													            "float", "int", "long", "long double", "OMBoolean",
													            "OMString", "short", "unsigned char", "unsigned int",
													            "unsigned long", "unsigned short", "void", "void *",
													            "Coded", currentElement.getName()	));
					typeCorrespondences.put(icdName, allTypes);
				}
				// ...otherwise add the currentElement to the list and then store it again into the map 
				else {
					allTypes = typeCorrespondences.get(icdName);
					allTypes.add(currentElement.getName());
					typeCorrespondences.put(icdName, allTypes);
				}
			}
			
			// Storing Attribute Types into the map (useful for checking if the type has already an IDStereotype as attribute)
			if(currentElement.getIsOfMetaClass("Attribute") == 1) {
				if(((CustomIRPModelElement) currentNode.getUserObject()).toString().contains("ICD_Message_IDField")) {

					String type = ((CustomIRPModelElement) ((DefaultMutableTreeNode) currentNode.getParent()).getUserObject()).toStringWithoutStereotypes();
					String pack = ((CustomIRPModelElement) 
							((DefaultMutableTreeNode) ((DefaultMutableTreeNode) currentNode.getParent()).getParent()).getUserObject()).toStringWithoutStereotypes();
					String icd = ((CustomIRPModelElement) 
							((DefaultMutableTreeNode) ((DefaultMutableTreeNode) 
									((DefaultMutableTreeNode) currentNode.getParent()).getParent()).getParent()).getUserObject()).toStringWithoutStereotypes();
					
					listOfPaths.add(icd + "_" + pack + "_" + type);
				}
			}
	
			// recursion
			populatingTheTree(model, GUI.rs.getChildrenElements(currentElement), currentNode);
		}
	}
	
	
	/*
		Utility function which sorts the JTree nodes according to their alphabetical order
	*/
	protected static void sortNodesAlphabetically(DefaultMutableTreeNode nodeToSort, DefaultTreeModel model, Boolean recursion) {
		
		int nrOfChildren = nodeToSort.getChildCount();
		
		// exit condition
		if (nrOfChildren == 0)
			return;
		
		if (nrOfChildren > 1) {
			if(nodeToSort.getLevel() != 3) {
				// Sort items according to this nameNodeMap
				Map<String, DefaultMutableTreeNode> nameNodeMap = 
						new HashMap<String, DefaultMutableTreeNode>(nrOfChildren);
				// O(n) always
				for (int i = 0; i < nrOfChildren; i++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) nodeToSort.getChildAt(i);
					CustomIRPModelElement cie = (CustomIRPModelElement) child.getUserObject();
					nameNodeMap.put(cie.toStringWithoutStereotypes(), child);
				}
				List<String> names = new ArrayList<String>(nameNodeMap.keySet());
				Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
				
				// Removing nodes
				// O(n) always
				for (DefaultMutableTreeNode node : nameNodeMap.values())
					model.removeNodeFromParent(node);
				
				// Adding nodes according to their order
				// O(n) always
				for (int i = 0; i < names.size(); i++)
					model.insertNodeInto(nameNodeMap.get(names.get(i)), nodeToSort, i);
			}
			else {
				Map<Integer, DefaultMutableTreeNode> tagNodeMap = 
						new HashMap<Integer, DefaultMutableTreeNode>(nrOfChildren);
								
				// Determining what's the order to follow to sort attributes
				// O(n) worst case
				TagManager.determiningAttributesOrderByElementsAlreadyPresent(nodeToSort);
				
				Boolean ICD_Signal_Payload = false;
				DefaultMutableTreeNode childICD_Signal_Payload = null;
				
				// Ordering nodes according to their GUI.orderParameter value
				// O(n) always
				for (int i = 0; i < nrOfChildren; i++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) nodeToSort.getChildAt(i);
					CustomIRPModelElement cie = (CustomIRPModelElement) child.getUserObject();
					
					// Don't add ICD_Signal_Payload attribute to the map: we won't order it, always takes the first place
					// toString() inside for cycle -> O(n^2) in which you iterate over stereotypes that are max 2, so it goes as O(n)
					if(cie.toString().contains("ICD_Signal_Payload")) {
						ICD_Signal_Payload = true;
						childICD_Signal_Payload = child;
						continue;
					}

					// Creation of map tagNodeMap that allow us to store numbers as keys and childs as values so as to order 
					// childs according numbers.
					String valueTag = cie.getTag(GUI.orderParameter).getValue();
					try {
						int valueTagInt = Integer.parseInt(valueTag);
						tagNodeMap.put(valueTagInt, child);
					}catch(NumberFormatException | NullPointerException nfe) {}
				}
				
				List<Integer> byteOffsets = new ArrayList<Integer>(tagNodeMap.keySet());
				Collections.sort(byteOffsets);
				
				// Removing nodes
				// O(n) always
				for (DefaultMutableTreeNode node : tagNodeMap.values())
					model.removeNodeFromParent(node);
				
				int idx_signal_payload = 0;
				
				// If there is ICD_Signal_Payload as stereotype,
				// then this attribute must be inserted as first of the list
				if(ICD_Signal_Payload) {
					// Adding ICD_Signal_Payload
					model.insertNodeInto(childICD_Signal_Payload, nodeToSort, 0);
					idx_signal_payload = 1;
				}
				
				// Adding nodes according to their order
				// O(n) always
				for (int i = 0; i < byteOffsets.size(); i++)
					model.insertNodeInto(tagNodeMap.get(byteOffsets.get(i)), nodeToSort, i+idx_signal_payload);
			}
			model.reload();
		}
		
		if(recursion) {
			// Setting recursion also for all the children
			// O(n) always
			for (int i = 0; i < nrOfChildren; i++) {
				DefaultMutableTreeNode new_root = (DefaultMutableTreeNode) nodeToSort.getChildAt(i);
				sortNodesAlphabetically(new_root, model, recursion);
			}
		}
	}
	
	
	/*
		Utility function to order attribute tags according to their Byte_Offset and N_Bytes parameters
	*/
	protected static int rescalingTags(DefaultMutableTreeNode parent, IRPModelElement addedElement, int newStart, int newDuration) {
		
		int nrOfChildren = parent.getChildCount();
		
		if(nrOfChildren == 1)
			return 0;
		
		Boolean check = false;
		Boolean temp = true;
		int nextStart = -1;
		int currentStart = -1;
		int currentDuration = -1;
		int idxLastChild = nrOfChildren-1;
		DefaultMutableTreeNode child = null;
		IRPModelElement childInRhapsody = null;
		String orderDurationVariable = "";
		
		try {
			
			if(GUI.orderParameter.equals("Start_Bit"))
				orderDurationVariable = "No_Bits";
			else
				orderDurationVariable = "No_Bytes";
			
			// O(n)
			for (int i = 0; i < nrOfChildren; i++) {
				child = (DefaultMutableTreeNode) parent.getChildAt(i);
				
				childInRhapsody = ((CustomIRPModelElement) child.getUserObject()).getElement();
				
				// toString() inside for cycle -> O(n^2) in which you iterate over stereotypes that are max 2, so it goes as O(n)
				if(childInRhapsody.getName().equals(addedElement.getName()) ||
						((CustomIRPModelElement) child.getUserObject()).toString().contains("ICD_Signal_Payload"))
					continue;
				
				try {
					currentStart = Integer.parseInt(childInRhapsody.getTag(GUI.orderParameter).getValue());
					currentDuration = Integer.parseInt(childInRhapsody.getTag(orderDurationVariable).getValue());
				}catch(NullPointerException | NumberFormatException npe) {
					continue;
				}

				if(check) {
					// case in which the element has a start value bigger than the duration of the previous element
					if(currentStart > nextStart)
						return 1;
					childInRhapsody.setTagValue(childInRhapsody.getTag(GUI.orderParameter), String.valueOf(nextStart));
					nextStart += currentDuration;
					continue;
				}
				
				// rescaling
				if(currentStart == newStart) {
					// Creating space for the new added element
					childInRhapsody.setTagValue(childInRhapsody.getTag(GUI.orderParameter), String.valueOf(currentStart + newDuration));
					// Setting values of the new added element
					addedElement.setTagValue(addedElement.getTag(GUI.orderParameter), String.valueOf(newStart));
					addedElement.setTagValue(addedElement.getTag(orderDurationVariable), String.valueOf(newDuration));
					nextStart = currentStart + newDuration + currentDuration;
					check = true;
					continue;
				}
				
				// Checking if the element added has currentStart < than all
				if((newStart >= 0) && (newStart < currentStart)) {
					if(temp && ((newStart + newDuration) <= currentStart))
						return 0;
					temp = false;
				}
				
				// You cannot insert an element between the bytes of another element
				if(newStart > currentStart && (currentStart + currentDuration) > newStart)
					return -1;
			}
		}catch(RhapsodyRuntimeException rre) {}
		
		// In this case you can directly add because the start is greater than the last element of the collection ordered
		if(newStart >= (currentStart + currentDuration))
			return 0;
		
		return (check == true) ? 1 : -1;
	}
	
	
	protected static void treeEventsHandler(JLabel keyTagLabel1, JLabel keyTagLabel2, JLabel keyTagLabel3, JLabel keyTagLabel4, JLabel keyTagLabel5, 
			JLabel keyTagLabel6, JLabel keyTagLabel7, JLabel keyTagLabel8, JLabel keyTagLabel9, JLabel keyTagLabel10, 
			JLabel valueTagLabel1, JLabel valueTagLabel2, JLabel valueTagLabel3, JLabel valueTagLabel4, JLabel valueTagLabel5, 
			JLabel valueTagLabel6, JLabel valueTagLabel7, JLabel valueTagLabel8, JLabel valueTagLabel9, JLabel valueTagLabel10,
			JComboBox valueTagComboBox1, JComboBox valueTagComboBox2, JComboBox valueTagComboBox3, JComboBox valueTagComboBox4, 
			JComboBox valueTagComboBox5, JComboBox valueTagComboBox6, JComboBox valueTagComboBox7, JComboBox valueTagComboBox8,
			JComboBox valueTagComboBox9, JComboBox valueTagComboBox10, JButton updateTagsButton,
			JButton addButton, JButton modifyButton, JButton deleteButton, JButton updateTypeButton, 
			JLabel descriptionLabel, JButton descriptionButton) {
		
		// If more elements are selected, deselect all		
		if(GUI.tree.getSelectionCount() > 1) {
			TreePath sel = GUI.tree.getSelectionPath();
			GUI.tree.clearSelection();
			GUI.tree.setSelectionPath(sel);
			CustomPrint.printOnJTextPane("Select only one element at the time.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			return;
		}
		
		// Enabling objects only if one tree node has been clicked  
		GUI.elementSelectedTextField.setText((GUI.tp != null) ? GUI.tp.getLastPathComponent().toString() : "");
		GUI.elementSelectedTextField.setCaretPosition(0);
		GUI.elementSelectedTextField.setEnabled((GUI.tp != null) ? true : false);
		GUI.nameTextField.setText("");
		GUI.nameTextField.setEditable((GUI.tp != null) ? true : false);
		GUI.nameTextField.setEnabled((GUI.tp != null) ? true : false);
		GUI.typeAttributesComboBox.setModel(new DefaultComboBoxModel(new String[] {""}));
		
		// Reset radio button before setting them properly
		GUI.idStereotypeRadioButton.setSelected(false);
		GUI.idStereotypeRadioButton.setEnabled(false);
		GUI.metaclassTextField.setText("");
		
		// Reset all tag fields
		GUI.resetAllTagFields(keyTagLabel1, keyTagLabel2, keyTagLabel3, keyTagLabel4, keyTagLabel5, 
				keyTagLabel6, keyTagLabel7, keyTagLabel8, keyTagLabel9, keyTagLabel10, 
				valueTagLabel1, valueTagLabel2, valueTagLabel3, valueTagLabel4, valueTagLabel5, 
				valueTagLabel6, valueTagLabel7, valueTagLabel8, valueTagLabel9, valueTagLabel10,
				valueTagComboBox1, valueTagComboBox2, valueTagComboBox3, valueTagComboBox4, 
				valueTagComboBox5, valueTagComboBox6, valueTagComboBox7, valueTagComboBox8,
				valueTagComboBox9, valueTagComboBox10);
		updateTagsButton.setEnabled(false);
		descriptionLabel.setVisible(false);
		descriptionButton.setVisible(false);
		GUI.descriptionTextPane.setVisible(false);
		GUI.numOfTags = 0;
		
		try {
		
			if (GUI.tp != null) {
				
				searchedItem = ((CustomIRPModelElement)((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getUserObject()).getElement();
				
				if(searchedItem == null)
					return;
				
				// Getting name of element selected
				String elementClicked = searchedItem.getName();
				
				// Getting level of the tree element selected
				int lev = ((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getLevel();
				
				// Set label to show the type of element selected
				if(lev == 0 || lev == 1 || lev == 2) {
					GUI.metaclassTextField.setForeground(new Color(235, 196, 37));
					GUI.metaclassTextField.setText("Package");
				}
				else if(lev == 3) {
					GUI.metaclassTextField.setForeground(new Color(54, 146, 248));
					GUI.metaclassTextField.setText("Type");
				}
				else if(lev == 4) {
					GUI.metaclassTextField.setForeground(new Color(255, 0, 0));
					GUI.metaclassTextField.setText("Attribute");
					descriptionLabel.setVisible(true);
					descriptionButton.setVisible(true);
					GUI.descriptionTextPane.setVisible(true);
					GUI.descriptionTextPane.setText(searchedItem.getDescription());
				}
				
				// MANAGING TAGS: get tags from Rhapsody, filling and enabling text fields and combo boxes
				IRPCollection tagCollection = searchedItem.getAllTags();
				if(tagCollection.getCount() > 0) {
					GUI.numOfTags = tagCollection.getCount();
					updateTagsButton.setEnabled(true);
					
					// The following map contains those tags that must be reported in Combo Boxes
					// (e.g. Indianism can be Little, Medium or Big)					
					// Iterate over tags of selected item and storing them in JTree
					// O(n) always
					for (int q = 1; q <= tagCollection.getCount(); q++) {
						IRPModelElement currentTag = (IRPModelElement) tagCollection.getItem(q);
						
						String keyTag = currentTag.getName();
						String valueTag = searchedItem.getTag(keyTag).getValue();
						
						// O(1) always
						if (rhapsodySupportFolder.containsKey(keyTag)) {
							
							String[] vectOfString = rhapsodySupportFolder.get(keyTag);
							int idx = Arrays.asList(rhapsodySupportFolder.get(keyTag)).indexOf(valueTag);
							
							// Show comboBox
							switch (q) {
							case 1:
								GUI.setTextField(keyTagLabel1, GUI.keyTagTextField1, keyTag);
								GUI.setComboBox(valueTagLabel1, valueTagComboBox1, vectOfString, idx);
								break;
							case 2:
								GUI.setTextField(keyTagLabel2, GUI.keyTagTextField2, keyTag);
								GUI.setComboBox(valueTagLabel2, valueTagComboBox2, vectOfString, idx);
								break;
							case 3:
								GUI.setTextField(keyTagLabel3, GUI.keyTagTextField3, keyTag);
								GUI.setComboBox(valueTagLabel3, valueTagComboBox3, vectOfString, idx);
								break;
							case 4:
								GUI.setTextField(keyTagLabel4, GUI.keyTagTextField4, keyTag);
								GUI.setComboBox(valueTagLabel4, valueTagComboBox4, vectOfString, idx);
								break;
							case 5:
								GUI.setTextField(keyTagLabel5, GUI.keyTagTextField5, keyTag);
								GUI.setComboBox(valueTagLabel5, valueTagComboBox5, vectOfString, idx);
								break;
							case 6:
								GUI.setTextField(keyTagLabel6, GUI.keyTagTextField6, keyTag);
								GUI.setComboBox(valueTagLabel6, valueTagComboBox6, vectOfString, idx);
								break;
							case 7:
								GUI.setTextField(keyTagLabel7, GUI.keyTagTextField7, keyTag);
								GUI.setComboBox(valueTagLabel7, valueTagComboBox7, vectOfString, idx);
								break;
							case 8:
								GUI.setTextField(keyTagLabel8, GUI.keyTagTextField8, keyTag);
								GUI.setComboBox(valueTagLabel8, valueTagComboBox8, vectOfString, idx);
								break;
							case 9:
								GUI.setTextField(keyTagLabel9, GUI.keyTagTextField9, keyTag);
								GUI.setComboBox(valueTagLabel9, valueTagComboBox9, vectOfString, idx);
								break;
							case 10:
								GUI.setTextField(keyTagLabel10, GUI.keyTagTextField10, keyTag);
								GUI.setComboBox(valueTagLabel10, valueTagComboBox10, vectOfString, idx);
								break;
							default:
								break;
							}
						}
						else { // Show textfields
							switch (q) {
							case 1:
								GUI.setTextField(keyTagLabel1, GUI.keyTagTextField1, keyTag);
								GUI.setTextField(valueTagLabel1, GUI.valueTagTextField1, valueTag);
								break;
							case 2:
								GUI.setTextField(keyTagLabel2, GUI.keyTagTextField2, keyTag);
								GUI.setTextField(valueTagLabel2, GUI.valueTagTextField2, valueTag);
								break;
							case 3:
								GUI.setTextField(keyTagLabel3, GUI.keyTagTextField3, keyTag);
								GUI.setTextField(valueTagLabel3, GUI.valueTagTextField3, valueTag);
								break;
							case 4:
								GUI.setTextField(keyTagLabel4, GUI.keyTagTextField4, keyTag);
								GUI.setTextField(valueTagLabel4, GUI.valueTagTextField4, valueTag);
								break;
							case 5:
								GUI.setTextField(keyTagLabel5, GUI.keyTagTextField5, keyTag);
								GUI.setTextField(valueTagLabel5, GUI.valueTagTextField5, valueTag);
								break;
							case 6:
								GUI.setTextField(keyTagLabel6, GUI.keyTagTextField6, keyTag);
								GUI.setTextField(valueTagLabel6, GUI.valueTagTextField6, valueTag);
								break;
							case 7:
								GUI.setTextField(keyTagLabel7, GUI.keyTagTextField7, keyTag);
								GUI.setTextField(valueTagLabel7, GUI.valueTagTextField7, valueTag);
								break;
							case 8:
								GUI.setTextField(keyTagLabel8, GUI.keyTagTextField8, keyTag);
								GUI.setTextField(valueTagLabel8, GUI.valueTagTextField8, valueTag);
								break;
							case 9:
								GUI.setTextField(keyTagLabel9, GUI.keyTagTextField9, keyTag);
								GUI.setTextField(valueTagLabel9, GUI.valueTagTextField9, valueTag);
								break;
							case 10:
								GUI.setTextField(keyTagLabel10, GUI.keyTagTextField10, keyTag);
								GUI.setTextField(valueTagLabel10, GUI.valueTagTextField10, valueTag);
								break;
							default:
								break;
							}
						}
					}
				}
				
				// Get element selected
				DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) GUI.tp.getLastPathComponent();
				
				// Proper check on add button enabling
				Boolean var = (lev < 4 && lev != 1) ? true : false;
				addButton.setEnabled(var);
				Boolean checkElem = false;
				if (lev > 0)
					checkElem = ((CustomIRPModelElement)((DefaultMutableTreeNode) 
							GUI.tp.getParentPath().getLastPathComponent()).getUserObject()).toString().contains("Elementary_Type");
				addButton.setEnabled((lev == 3 && checkElem) ? false : var);
				
				// Proper check on modify button enabling
				var = (lev != 2) ? true : false;
				modifyButton.setEnabled(var);
				modifyButton.setEnabled((lev == 3 && (elementClicked.contains("Payload_") ||
						elementClicked.contains("DataMsg") ||
						((CustomIRPModelElement) nodeSelected.getUserObject()).toString().contains("ICD_Logical_Payload"))) ? false : var);
				
				// Proper check on delete button enabling
				var = (lev > 0 && lev != 2) ? true : false;
				deleteButton.setEnabled(var);
				deleteButton.setEnabled((lev == 3 && (elementClicked.contains("Payload_") ||
						elementClicked.contains("DataMsg") ||
						((CustomIRPModelElement) nodeSelected.getUserObject()).toString().contains("ICD_Logical_Payload"))) ? false : var);
				
				// Check on updateType button
				updateTypeButton.setEnabled((lev == 4) ? true : false);
				
				GUI.typeAttributesComboBox.setEnabled((lev == 4) ? true : false);

				// Getting Attribute type
				if (lev == 4) {
					try {
						TreePath bisnonnoPath = GUI.tp.getParentPath().getParentPath().getParentPath();
						IRPModelElement bisnonno = 
								((CustomIRPModelElement)((DefaultMutableTreeNode) 
										bisnonnoPath.getLastPathComponent()).getUserObject()).getElement();
						
						ArrayList<String> bisnonnoTypes = typeCorrespondences.get(bisnonno.getName());
						Object[] typesYouCanChoose = bisnonnoTypes.toArray();
						
						IRPAttribute a = (IRPAttribute) searchedItem;
						int idx = 0;
						
						try {
							idx = bisnonnoTypes.indexOf(a.getType().getName());
						}catch(NullPointerException npe) {}
						
						GUI.typeAttributesComboBox.setModel(new DefaultComboBoxModel(typesYouCanChoose));
						GUI.typeAttributesComboBox.setSelectedIndex(idx);
						GUI.typeAttributesComboBox.setEnabled(true);
					}catch(ArrayIndexOutOfBoundsException aioobe) {}
				}
				
				// Evaluating wheter activate or not the id stereotype button
				Boolean found = false;
				
				// If it is a Type node but not an �ICD_Elementary_Type�, check if among its children there is one
				// with stereotype "ICD_Message_IDField"; in this case, don't enable the idStereotypeRadioButton
				// O(1)
				if (nodeSelected.getLevel() == 3 &&
						!((CustomIRPModelElement) nodeSelected.getUserObject()).toString().contains("�ICD_Elementary_Type�")) {
					
					String type = ((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes();
					String pack = ((CustomIRPModelElement) ((DefaultMutableTreeNode) nodeSelected.getParent()).getUserObject()).toStringWithoutStereotypes();
					String icd = ((CustomIRPModelElement) 
							((DefaultMutableTreeNode) ((DefaultMutableTreeNode) nodeSelected.getParent()).getParent()).getUserObject()).toStringWithoutStereotypes();
					
					found = listOfPaths.contains(icd + "_" + pack + "_" + type);
					GUI.idStereotypeRadioButton.setEnabled(!found);
				}

				// Opening path selected
				GUI.tree.scrollPathToVisible(new TreePath(
								((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getPath()));
			}
			else {
				addButton.setEnabled(false);
				modifyButton.setEnabled(false);
				deleteButton.setEnabled(false);
				GUI.typeAttributesComboBox.setModel(new DefaultComboBoxModel(new String[] {""}));
				GUI.typeAttributesComboBox.setEnabled(false);
				updateTypeButton.setEnabled(false);
				GUI.tree.clearSelection();
			}
		}catch(Exception excpt) {
			System.out.println(excpt.getMessage());
			CustomPrint.printOnJTextPane("Something went wrong. Refresh.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			return;
		}
	}
}

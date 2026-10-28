package com.rhapsody.gui;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.RhapsodyRuntimeException;



public class TreeModification {
	
	protected static void modifyNode() {
		
		if(GUI.tp == null)
			return;
		
		// Get element clicked info
		DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) GUI.tp.getLastPathComponent();
		int nodeLevel = nodeSelected.getLevel();
		String elementClicked = ((CustomIRPModelElement)((DefaultMutableTreeNode) 
				GUI.tp.getLastPathComponent()).getUserObject()).toString();
		String elementClickedWithoutStereotypes = ((CustomIRPModelElement)((DefaultMutableTreeNode) 
				GUI.tp.getLastPathComponent()).getUserObject()).toStringWithoutStereotypes();
		
		// Get parent
		DefaultMutableTreeNode parent = null;
		if(nodeLevel != 0)
			parent = (DefaultMutableTreeNode) nodeSelected.getParent();

		// Get model
		DefaultTreeModel model = (DefaultTreeModel) GUI.tree.getModel();
		
		// Checking if the name of the element to modify has been inserted properly
		if (GUI.nameTextField.getText().isEmpty() || GUI.nameTextField.getText() == "") {
			CustomPrint.printOnJTextPane("You must insert a name in \"Enter a name\" before adding.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			return;
		}
		
		// Checking if there's already an item with the same name of the new that you want to add
		String nameChosenForElement = GUI.nameTextField.getText();
		// O(log(n))
		DefaultMutableTreeNode dftn = TreeSearch.binarySearchForNodesWithoutStereotypes(nodeSelected, nameChosenForElement);
		// If the name inserted in the textfield is equal to the name of another element already present in that package
		// (without prefixes), you can't add it; unless the element already present contains DataMsg_ or Payload_
		// ICD condition
		if(nodeSelected.equals(model.getRoot()))
			dftn = TreeSearch.binarySearchForNodesWithoutStereotypes(nodeSelected, "ICD_" + nameChosenForElement);
		// "_Composite_Types" condition
		if(elementClickedWithoutStereotypes.contains("_Composite_Types")) {
			String family = 
					elementClickedWithoutStereotypes.substring(0, 
							elementClickedWithoutStereotypes.length() - "_Composite_Types".length());
			dftn = TreeSearch.binarySearchForNodesWithoutStereotypes(nodeSelected, 
							"TypeMsg_" + family + "_" + nameChosenForElement);
		}
		if(dftn != null) {
			CustomPrint.printOnJTextPane("Choosen name not valid: already exists an element in this path with the same name.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			return;
		}
		
		// Changing JTree
		try {
			
			if (nodeLevel == 0)
				((CustomIRPModelElement) nodeSelected.getUserObject()).setElementName(nameChosenForElement);
			
			else if (nodeLevel == 1) {
				
				String family = ((CustomIRPModelElement) 
						nodeSelected.getUserObject()).toStringWithoutStereotypes().substring("ICD_".length());
	
				// Updating OperationsOnJTree.typeCorrespondences map
				ArrayList<String> allTypes = OperationsOnJTree.typeCorrespondences.get(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes());
				
				// Changing prefix to its children
				// O(n) but in the worst case n=3
				for (int k = 0; k < model.getChildCount(nodeSelected); k++) {
					DefaultMutableTreeNode son = (DefaultMutableTreeNode) nodeSelected.getChildAt(k);
					String sonName = ((CustomIRPModelElement) son.getUserObject()).toStringWithoutStereotypes();
					
					String end = "";
					
					if (sonName.contains("_Elementary_Types"))
						end = "_Elementary_Types";
					else if (sonName.contains("_Composite_Types")) {
						end = "_Composite_Types";
						
						// Changing Types family
						// O(n)
						for(int j = 0; j < son.getChildCount(); j++) {
							DefaultMutableTreeNode nephew = (DefaultMutableTreeNode) son.getChildAt(j);
							String nephewName = ((CustomIRPModelElement) nephew.getUserObject()).toStringWithoutStereotypes();
							String endSon = nephewName.substring("DataMsg_".length() + family.length());
							String newNephewName = nephewName.substring(0, "DataMsg_".length()) + nameChosenForElement + endSon;
							((CustomIRPModelElement) nephew.getUserObject()).setElementName(newNephewName);
							
							// Update typeCorrespondences map
							allTypes.remove(nephewName);
							allTypes.add(newNephewName);
						}
					}
					else if (sonName.contains("_Message_Type_Pkg"))
						end = "_Message_Type_Pkg";
					
					OperationsOnJTree.typeCorrespondences.remove(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes());
					OperationsOnJTree.typeCorrespondences.put("ICD_" + nameChosenForElement, allTypes);
					
					((CustomIRPModelElement) son.getUserObject()).setElementName(nameChosenForElement + end);
				}
				
				// Setting new name to the element in JTree
				((CustomIRPModelElement) nodeSelected.getUserObject()).setElementName("ICD_" + nameChosenForElement);				
			}
			else {
				if (nodeLevel == 3) { // In this case you can modify only Elementary_Type and Message_Type_Pkg; among composites, only TypeMsg can be modified
					
					String parentName = ((CustomIRPModelElement) parent.getUserObject()).toStringWithoutStereotypes();
					
					// The next two instructions will be useful to update OperationsOnJTree.typeCorrespondences map
					DefaultMutableTreeNode icd = (DefaultMutableTreeNode) parent.getParent();
					ArrayList<String> allTypes = OperationsOnJTree.typeCorrespondences.get(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes());
					
					if(((CustomIRPModelElement) parent.getUserObject()).toStringWithoutStereotypes().contains("_Message_Type_Pkg")) {
						
						String family = parentName.substring(0, parentName.length() - "_Message_Type_Pkg".length());
						DefaultMutableTreeNode granParent = (DefaultMutableTreeNode) parent.getParent();
						DefaultMutableTreeNode uncle = null;
						// O(n) but there are always three elements and the worst case is to travel all of them
						for (int t = 0; t < granParent.getChildCount(); t++) {
							uncle = (DefaultMutableTreeNode) granParent.getChildAt(t);
							String uncleName = ((CustomIRPModelElement) uncle.getUserObject()).toStringWithoutStereotypes();
							if(uncleName.contains("_Composite_Types"))
								break;
						}
						
						String dataMsgToFind = "DataMsg_" + family + "_" + elementClickedWithoutStereotypes;
						String payloadToFind = "Payload_" + family + "_" + elementClickedWithoutStereotypes;
						
						// Modifying physical_payload attribute
						// O(1)
						DefaultMutableTreeNode physical_payload = (DefaultMutableTreeNode) nodeSelected.getFirstChild();
						((CustomIRPModelElement) 
								physical_payload.getUserObject()).setElementName("Physical_Payload_" + nameChosenForElement);
						
						// Binary search of element to modify
						
						// TODO modifica ICD_Signal_Payload
						
						// O(log(n))
						DefaultMutableTreeNode payload = TreeSearch.binarySearchForNodesWithoutStereotypes(uncle, payloadToFind);
						// Changing payload name
						((CustomIRPModelElement) 
								payload.getUserObject()).setElementName("Payload_" + family + "_" + nameChosenForElement);
						
						DefaultMutableTreeNode icd_logical_payload = null;
						// O(n)
						for(int i = 0; i < payload.getChildCount(); i++) {
							icd_logical_payload = (DefaultMutableTreeNode) payload.getChildAt(i);
							if(((CustomIRPModelElement) icd_logical_payload.getUserObject()).toString().contains("ICD_Logical_Payload"))
								break;
						}
						((CustomIRPModelElement) icd_logical_payload.getUserObject()).setElementName(nameChosenForElement);
						
						// O(log(n))
						DefaultMutableTreeNode dataMsg = TreeSearch.binarySearchForNodesWithoutStereotypes(uncle, dataMsgToFind);
						// Changing dataMsg name
						((CustomIRPModelElement) 
								dataMsg.getUserObject()).setElementName("DataMsg_" + family + "_" + nameChosenForElement);
						
						// Update typeCorrespondences map
						allTypes.remove(allTypes.indexOf(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes()));
						allTypes.remove(allTypes.indexOf(dataMsgToFind));
						allTypes.remove(allTypes.indexOf(payloadToFind));
						allTypes.add(nameChosenForElement);
						allTypes.add("DataMsg_" + family + "_" + nameChosenForElement);
						allTypes.add("Payload_" + family + "_" + nameChosenForElement);
						OperationsOnJTree.typeCorrespondences.remove(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes());
						OperationsOnJTree.typeCorrespondences.put(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes(), allTypes);
						
						// Changing clicked element name
						((CustomIRPModelElement) nodeSelected.getUserObject()).setElementName(nameChosenForElement);	
						// O(n)
						OperationsOnJTree.sortNodesAlphabetically(uncle, model, false);
					}
					else if (elementClicked.contains("TypeMsg_")) {
						
						String family = parentName.substring(0, parentName.length() - "_Composite_Types".length());
						
						// Update typeCorrespondences map
						allTypes.remove(allTypes.indexOf(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes()));
						allTypes.add("TypeMsg_" + family + "_" + nameChosenForElement);
						OperationsOnJTree.typeCorrespondences.remove(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes());
						OperationsOnJTree.typeCorrespondences.put(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes(), allTypes);

						((CustomIRPModelElement) 
								nodeSelected.getUserObject()).setElementName("TypeMsg_" + family + "_" + nameChosenForElement);
					}
					else { // elementary type
						
						// Update typeCorrespondences map
						allTypes.remove(allTypes.indexOf(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes()));
						allTypes.add(nameChosenForElement);
						OperationsOnJTree.typeCorrespondences.remove(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes());
						OperationsOnJTree.typeCorrespondences.put(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes(), allTypes);
						
						((CustomIRPModelElement) nodeSelected.getUserObject()).setElementName(nameChosenForElement);
					}
				}
				else { // attributes
					String phy = "";
					if(elementClickedWithoutStereotypes.contains("Physical_Payload_"))
						phy = "Physical_Payload_";
					((CustomIRPModelElement) nodeSelected.getUserObject()).setElementName(phy + nameChosenForElement);
				}
			}
		}catch (RhapsodyRuntimeException rre){
			CustomPrint.printOnJTextPane("The chosen name was not accepted by the system. Try with another one.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			return;
		}
		
		model.nodeChanged(nodeSelected);
		model.reload();
		
		// Sort nodes of the model in alphabetical order
		// O(n)
		if (nodeLevel != 0)
			OperationsOnJTree.sortNodesAlphabetically(parent, model, false);
		
		// reset fields and opening the right path
		GUI.resetFrame(GUI.idStereotypeRadioButton, GUI.typeAttributesComboBox);
		GUI.tree.setSelectionPath(new TreePath(nodeSelected.getPath()));
	
		// Print info
		String var = (nodeLevel == 0) ? "Root" : "";
		var = (nodeLevel == 1) ? "Package" : var;
		var = (nodeLevel == 3) ? "Type" : var;
		var = (nodeLevel == 4) ? "Attribute" : var;
		CustomPrint.printOnJTextPane(var + " successfully modified.");
		GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
	}
}

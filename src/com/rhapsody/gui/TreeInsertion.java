package com.rhapsody.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPModelElement;



public class TreeInsertion {
	
	protected static void insertNode() {
		
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
		
		// Checking if the name of the element to add has been inserted properly
		if (GUI.nameTextField.getText().isEmpty() || GUI.nameTextField.getText() == "") {
			if(!GUI.disablePrints) {
				CustomPrint.printOnJTextPane("You must insert a name in \"Enter a name\" before adding.");
				GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			}
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
			if(!GUI.disablePrints) {
				CustomPrint.printOnJTextPane("Choosen name not valid: already exists an element in this path with the same name.");
				GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			}
			return;
		}
		
		// Create children and nephews
		DefaultMutableTreeNode child = null;
		DefaultMutableTreeNode child1 = null;
		DefaultMutableTreeNode child2 = null;
		DefaultMutableTreeNode nephew1 = null;
		DefaultMutableTreeNode nephew2 = null;
		DefaultMutableTreeNode compositeTypes = null;
		
		// ADDING ELEMENTS BOTH IN RHAPSODY AND JTREE
		
		IRPModelElement elementAdded = null;
		CustomIRPModelElement cie = null;
		
		// Getting compositeTypes node in case we add elements in _Message_Type_Pkg 
		if ((nodeLevel == 2) && (elementClicked.contains("_Message_Type_Pkg"))) {
			int i = 0;
			// O(n) but here there is an iteration over three items in the worst case
			while (!((((DefaultMutableTreeNode)parent.getChildAt(i)).getUserObject().toString()).contains("_Composite_Types")))
				i+=1;
			compositeTypes = (DefaultMutableTreeNode) parent.getChildAt(i);
		}
								
		// Add ICD
		if(nodeLevel == 0) {
			
			// Put this node into the typeCorrespondences map			
			ArrayList<String> allTypes = new ArrayList<>( Arrays.asList(	"Untyped", "bool", "char", "char*", "double",
																            "float", "int", "long", "long double", "OMBoolean",
																            "OMString", "short", "unsigned char", "unsigned int",
																            "unsigned long", "unsigned short", "void", "void *",
																            "Coded"	));
			OperationsOnJTree.typeCorrespondences.put("ICD_" + nameChosenForElement, allTypes);
			
			// Add to Rhapsody
			ArrayList<IRPModelElement> al = GUI.rs.addElement(nameChosenForElement, nodeLevel);
			elementAdded = al.get(0);
			if(elementAdded == null) {
				if(!GUI.disablePrints) {
					CustomPrint.printOnJTextPane("It wasn't possible to add.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				}
				return;
			}
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child, nodeSelected, nodeSelected.getChildCount());
			child = (DefaultMutableTreeNode) model.getChild(nodeSelected, nodeSelected.getChildCount()-1);
			// Adding _Message_Type_Pkg, Composite_Types, Elementary_Types
			elementAdded = al.get(1);
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child1 = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child1, child, child.getChildCount());
			elementAdded = al.get(2);
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child1 = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child1, child, child.getChildCount());
			elementAdded = al.get(3);
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child1 = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child1, child, child.getChildCount());
			child = child1; // I make this assignment to open path at last level folder
		}
		// Handling the add operation in "Composite_Types" package
		else if ((nodeLevel == 2) && (elementClicked.contains("_Composite_Types"))) {
			int usefulIdx = elementClickedWithoutStereotypes.length() - "_Composite_Types".length();
			String family = elementClickedWithoutStereotypes.substring(0, usefulIdx);
			elementAdded = GUI.rs.addElement(GUI.tp, "TypeMsg_" + family + "_" + nameChosenForElement, 
					nodeLevel, GUI.idStereotypeRadioButton.isSelected());
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child, nodeSelected, nodeSelected.getChildCount());
		}
		// Handling the add operation in "_Message_Type_Pkg" package
		else if ((nodeLevel == 2) && (elementClicked.contains("_Message_Type_Pkg"))) {
						
			// Add Message
			elementAdded = GUI.rs.addElement(GUI.tp, nameChosenForElement, 
					nodeLevel, GUI.idStereotypeRadioButton.isSelected());
			if(elementAdded == null) {
				if(!GUI.disablePrints) {
					CustomPrint.printOnJTextPane("It wasn't possible to add.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				}
				return;
			}
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child, nodeSelected, nodeSelected.getChildCount());
			child = (DefaultMutableTreeNode) model.getChild(nodeSelected, nodeSelected.getChildCount()-1);
			// Adding "Physical_Payload_" attribute to the node just added
			elementAdded = GUI.rs.addAttribute(new TreePath(child.getPath()), "Physical_Payload_" + nameChosenForElement, 
					child.getLevel(), GUI.idStereotypeRadioButton.isSelected());
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			nephew1 = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(nephew1, child, child.getChildCount());

			// Add DataMsg
			int usefulIdx = elementClickedWithoutStereotypes.length() - "_Message_Type_Pkg".length();
			String family = elementClickedWithoutStereotypes.substring(0, usefulIdx);
			elementAdded = GUI.rs.addElement(new TreePath(compositeTypes.getPath()), "DataMsg_" + family + "_" + nameChosenForElement, 
					compositeTypes.getLevel(), GUI.idStereotypeRadioButton.isSelected());
			if(elementAdded == null) {
				if(!GUI.disablePrints) {
					CustomPrint.printOnJTextPane("It wasn't possible to add.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				}
				return;
			}
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child1 = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child1, compositeTypes, compositeTypes.getChildCount());
			
			// Add Payload
			elementAdded = GUI.rs.addElement(new TreePath(compositeTypes.getPath()), "Payload_" + family + "_" + nameChosenForElement, 
					compositeTypes.getLevel(), GUI.idStereotypeRadioButton.isSelected());
			if(elementAdded == null) {
				if(!GUI.disablePrints) {
					CustomPrint.printOnJTextPane("It wasn't possible to add.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				}
				return;
			}
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child2 = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child2, compositeTypes, compositeTypes.getChildCount());
			child2 = (DefaultMutableTreeNode) model.getChild(compositeTypes, compositeTypes.getChildCount()-1);
			// Add attribute as child of Payload
			elementAdded = GUI.rs.addAttribute(new TreePath(child2.getPath()), nameChosenForElement, 
					child2.getLevel(), GUI.idStereotypeRadioButton.isSelected());
			cie = new CustomIRPModelElement(elementAdded);
			nephew2 = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(nephew2, child2, child2.getChildCount());
			
			// O(n)
			OperationsOnJTree.sortNodesAlphabetically(compositeTypes, model, false);
		}
		else { // Handling add operation in all other levels
			elementAdded = GUI.rs.addElement(GUI.tp, nameChosenForElement,
					nodeLevel, GUI.idStereotypeRadioButton.isSelected());
			if(elementAdded == null) {
				if(!GUI.disablePrints) {
					CustomPrint.printOnJTextPane("It wasn't possible to add.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				}
				return;
			}
			//rs.setTag(null, elementAdded, "allOfThemToNull", "");
			cie = new CustomIRPModelElement(elementAdded);
			child = new DefaultMutableTreeNode(cie);
			model.insertNodeInto(child, nodeSelected, nodeSelected.getChildCount());
		}
				
		// Every time that I add a Type, it must be added also to the typeCorrespondences map
		if(nodeLevel == 2) {
			CustomIRPModelElement icd = (CustomIRPModelElement) parent.getUserObject();
			String icdName = icd.toStringWithoutStereotypes();
			
			ArrayList<String> allTypes = OperationsOnJTree.typeCorrespondences.get(icdName);
			if(child != null)
				allTypes.add(((CustomIRPModelElement) child.getUserObject()).toStringWithoutStereotypes());
			if(child1 != null)
				allTypes.add(((CustomIRPModelElement) child1.getUserObject()).toStringWithoutStereotypes());
			if(child2 != null)
				allTypes.add(((CustomIRPModelElement) child2.getUserObject()).toStringWithoutStereotypes());
			
			OperationsOnJTree.typeCorrespondences.put(icdName, allTypes);
		}
				
		GUI.tree.scrollPathToVisible(new TreePath(child.getPath()));
		model.reload();
				
		// O(n)
		OperationsOnJTree.sortNodesAlphabetically(nodeSelected, model, false);
				
		// Adding the idStereotype to list
		if(GUI.idStereotypeRadioButton.isSelected()) {
			
			String type = ((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes();
			String pack = ((CustomIRPModelElement) ((DefaultMutableTreeNode) nodeSelected.getParent()).getUserObject()).toStringWithoutStereotypes();
			String icd = ((CustomIRPModelElement) 
					((DefaultMutableTreeNode) ((DefaultMutableTreeNode) nodeSelected.getParent()).getParent()).getUserObject()).toStringWithoutStereotypes();
			
			OperationsOnJTree.listOfPaths.add(icd + "_" + pack + "_" + type);
		}
				
		// reset fields and opening the right path
		GUI.resetFrame(GUI.idStereotypeRadioButton, GUI.typeAttributesComboBox);
		GUI.tree.setSelectionPath(new TreePath(child.getPath()));
				
		// Printing info on console log
		if(!GUI.disablePrints) {
			String var = (nodeLevel == 0) ? "Package" : "";
			var = (nodeLevel == 2) ? "Type" : var;
			var = (nodeLevel == 3) ? "Attribute" : var;
			CustomPrint.printOnJTextPane(var + " successfully added.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
		}
		
	}
	
}

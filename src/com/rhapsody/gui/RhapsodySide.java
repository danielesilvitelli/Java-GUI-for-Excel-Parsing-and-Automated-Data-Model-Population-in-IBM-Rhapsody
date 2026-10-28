package com.rhapsody.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPTag;
import com.telelogic.rhapsody.core.IRPType;
import com.telelogic.rhapsody.core.RhapsodyAppServer;
import com.telelogic.rhapsody.core.RhapsodyRuntimeException;



public class RhapsodySide {
	
	private IRPApplication app;
	private IRPProject prj;
	public Boolean flag = false;
	private IRPAttribute physical_payload = null;
	
	
	public RhapsodySide() {
		try {
			// The following object will represent Rhapsody
			app = RhapsodyAppServer.getActiveRhapsodyApplication();
		}catch(RhapsodyRuntimeException rre) {
			return;
		}
		flag = true;
	}
	
	
	protected Boolean checkIfRhapsodyIsOn() {		
		return RhapsodyAppServer.getActiveRhapsodyApplicationIDList().size() != 0;
	}
	
	
	protected void projectSetUp(String todo, String path) {
		/*
		 	Creating a new project (path, name):
		  		app.createNewProject("l:\\temp\\_hello_world", "Hello_World");
		  	Opening an existing project (path):
				app.openProject("l:\\temp\\hello_world\\Hello_World.rpy");
			Obtaining the current active project in Rhapsody:
				app.activeProject();
		*/
		switch (todo) {
			case "Create a new project": // not handled
//				app.createNewProject("l:\\temp\\_hello_world", "Hello_World");
//				prj = app.activeProject();
				break;
			case "Open an existing project":
//				prj = app.openProject("l:\\temp\\hello_world\\Hello_World.rpy");
				prj = app.openProject(path);
				break;
			case "Obtain the current active project":
				prj = app.activeProject();
				break;
			default:
				return;
		}
	}
	
	
	protected IRPModelElement getRoot() {
		return (IRPModelElement) prj;
	}
	
	
	protected Boolean rootHasChildren() {
		return prj.hasNestedElements() == 1;
	}
	
	
	protected IRPCollection getChildrenRoot() {
		return prj.getPackages();
	}
	
	
	protected IRPCollection getChildrenElements(IRPModelElement elem) {
		return elem.getNestedElements();
	}
	
	
	protected void setDescription(String description) {
		IRPModelElement element = ((CustomIRPModelElement) ((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getUserObject()).getElement();
		element.setDescription(description);
	}
	
	
	protected void setNewTypeMsg(IRPAttribute attr, String newtm) {
		attr.setTypeDeclaration(newtm);
	}
	
	
	/*
	 	Utility method that returns the possible stereotype that a Rhapsody object could have
	*/
	protected HashMap<String, String[]> supportTags(IRPPackage support) {
		
		IRPCollection coll = support.getTypes();
		
		HashMap<String, String[]> utilityMap = new HashMap<String, String[]>();
		
		// O(n^2)
		for (int i = 1; i <= coll.getCount(); i++) {
			IRPType t = (IRPType) coll.getItem(i);
			// Literals is the list of possible choices (e.g. for Indianism the literals are Little, Medium, Big)
			IRPCollection literals = t.getEnumerationLiterals();
			String typeName = ((IRPModelElement) coll.getItem(i)).getName();
			String[] vectOfString = new String[literals.getCount()];
			for (int y = 1; y <= literals.getCount(); y++)
				vectOfString[y-1] = ((IRPModelElement) literals.getItem(y)).getName();
			utilityMap.put(typeName, vectOfString);
		}
		
		return utilityMap;
	}
	
	
	protected void setTag(DefaultMutableTreeNode nodeSelected, String key, String value) {
		
		IRPModelElement elementForTags = ((CustomIRPModelElement) nodeSelected.getUserObject()).getElement();
		IRPCollection tagCollection = elementForTags.getAllTags();
		
		DefaultMutableTreeNode parent = null;
		if(nodeSelected.getLevel() > 0)
			parent = (DefaultMutableTreeNode) nodeSelected.getParent();
		
		// O(n) where n is max = 10
		// Totally, the computational complexity of this block of code is O(n*(2log(n)))
		for (int q = 1; q <= tagCollection.getCount(); q++) {
			
			Boolean temp = false;
			
			IRPTag currentTag = (IRPTag) tagCollection.getItem(q);
			
			String keyTag = currentTag.getName();
			String valueTag = elementForTags.getTag(keyTag).getValue();
			
			if(!key.equals("allOfThemToNull")) {
				if (!keyTag.equals(key))
					continue;
				if (valueTag.equals(value))
					return;
			}
			
			// When setting No_Bytes for DataMsg, set the same No_Bytes for ICD_Logical_Payload
			if(parent != null) {
				if(elementForTags.getName().contains("DataMsg_") && keyTag.equals("No_Bytes")) {
					
					String suffix = elementForTags.getName().substring("DataMsg_".length());
					String payloadToFind = "Payload_" + suffix;
										
					// O(log(n))
					DefaultMutableTreeNode payload = TreeSearch.binarySearchForNodesWithoutStereotypes(parent, payloadToFind);
										
					String parentName = ((CustomIRPModelElement) parent.getUserObject()).toStringWithoutStereotypes();
					String family = parentName.substring(0, parentName.length() - "_Composite_Types".length());
					String payload_name = ((CustomIRPModelElement) payload.getUserObject()).toStringWithoutStereotypes();
					String icd_logical_payload_name = payload_name.substring("Payload_".length() + family.length() + 1);
					
					// O(log(n))
					DefaultMutableTreeNode icd_logical_payload = TreeSearch.binarySearchForNodesWithoutStereotypes(payload, icd_logical_payload_name);
					
					// Setting tag "No_Bytes" ordering data
					try {
						int byte_offset = Integer.parseInt(((CustomIRPModelElement) icd_logical_payload.getUserObject()).getElement().getTag("Byte_Offset").getValue());
						int v = Integer.parseInt(value);
						// Since we have modified the "No_Bytes", it is necessary to rescale tags
						int ret = OperationsOnJTree.rescalingTags(payload, ((CustomIRPModelElement) icd_logical_payload.getUserObject()).getElement(), byte_offset, v);
						
						if(ret == 0)
							throw new NumberFormatException(); // I throw this exception because I want to execute the code in the catch statement
						else if(ret == -1)
							return; // don't update, something went wrong
					}catch(NumberFormatException nfe) {
						// Setting tag "No_Bytes" without ordering data
						IRPTag tag_payload = ((CustomIRPModelElement) icd_logical_payload.getUserObject()).getElement().getTag("No_Bytes");
						((CustomIRPModelElement) icd_logical_payload.getUserObject()).getElement().setTagValue(tag_payload, value);
					}
				}
				else {
					// Don't set No_Bytes for ICD_Logical_Payload
					if(((CustomIRPModelElement) nodeSelected.getUserObject()).toString().contains("ICD_Logical_Payload")) {
						if(keyTag.equals("No_Bytes")) {
							temp = true;
							break;
						}
					}
				}
			}
			
			if(!temp)
				elementForTags.setTagValue(currentTag, value);
		}
	}
	
	
	protected ArrayList<IRPModelElement> addElement(String nameSon, int val) {
		
		if (val == 0) {
			
			IRPPackage irppa = null;
			
			// Adding child to root
			IRPProject root = (IRPProject) ((CustomIRPModelElement) ((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getUserObject()).getElement();
			irppa = (IRPPackage) root.addPackage("ICD_" + nameSon);

			// Adding proper stereotype to added package
			irppa.addStereotype("ICD", "Package");
			
			// Creating nested packages predefined
			IRPPackage messageTypePkg = 
					(IRPPackage) irppa.addNestedPackage(nameSon + "_Message_Type_Pkg");
			IRPModelElement composite = irppa.addNestedPackage(nameSon + "_Composite_Types");
			IRPModelElement elementary = irppa.addNestedPackage(nameSon + "_Elementary_Types");
					
			// Adding stereotype to one of the nested packages created
			messageTypePkg.addStereotype("ICD_Message_Type", "Package");
			
			ArrayList<IRPModelElement> al = new ArrayList<>();
			al.add(irppa);
			al.add((IRPModelElement) messageTypePkg);
			al.add(composite);
			al.add(elementary);
			
			return al;
		}
		else
			return null;		
	}
	
	
	protected IRPModelElement addElement(TreePath tp, String nameSon, int val, Boolean id) { //, String typeDeclaration) {
		
		IRPType irpt = null;
		IRPModelElement returnValue = null;
		IRPPackage me = null;
		
		try {
			// Every level (val) has its own requirements and policies
			if (val == 2) {
				// it is a type
				me = (IRPPackage) ((CustomIRPModelElement)((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject()).getElement();
				irpt = (IRPType) me.addType(nameSon);
				irpt.setKind("Structure");
				
				String nameParent = me.getName();
				
				if (nameParent.contains("_Message_Type_Pkg"))
					irpt.addStereotype("ICD_Message", "Type");
				else if (nameParent.contains("_Composite_Types"))
					irpt.addStereotype("ICD_Composite_Type", "Type");
				else if (nameParent.contains("_Elementary_Types"))
					irpt.addStereotype("ICD_Elementary_Type", "Type");
				
				return (IRPModelElement) irpt;
			}
			else if (val == 3) {
				// it is an attribute
				IRPType irpme = (IRPType) ((CustomIRPModelElement) ((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getUserObject()).getElement();
				returnValue = (IRPModelElement) irpme.addAttribute(nameSon);
				returnValue.setDescription("");
				returnValue.addStereotype("ICD_Signal", "Attribute");
				//if(typeDeclaration.equals(""))									// COME LEGGERE I TYPE DA EXCEL???
				((IRPAttribute)returnValue).setTypeDeclaration("Untyped");
				//else if(typeDeclaration.equals(""))
				//	((IRPAttribute)returnValue).setTypeDeclaration("Untyped");
				if (id)
					returnValue.addStereotype("ICD_Message_IDField", "Attribute");
				return returnValue;
			}
		}catch(RhapsodyRuntimeException | NullPointerException rre) {
			return null;
		}
		return null;
	}
	
	
	protected IRPModelElement addAttribute(TreePath tp, String nameSon, int val, Boolean id) {
		
		IRPAttribute irpa = null;
		
		if (val == 3) {
			IRPType clicked = (IRPType) ((CustomIRPModelElement)((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject()).getElement();
			IRPModelElement parent = ((CustomIRPModelElement)((DefaultMutableTreeNode) tp.getParentPath().getLastPathComponent()).getUserObject()).getElement();
			
			if (parent.getName().contains("_Message_Type_Pkg")) {
				irpa = clicked.addAttribute(nameSon);
				irpa.addStereotype("ICD_Signal_Payload", "Attribute");
				irpa.setDescription("");
				physical_payload = irpa; // I made this assignment because, first, the Physical_Payload attribute will be created, 
										 //	then, after the creation of the Payload type, I setTypeDeclaration the Physical_Payload 
										 // with the Payload
			}
			else if (parent.getName().contains("_Composite_Types")) {
				if (clicked.getName().contains("Payload_")) {
					String family = clicked.getName().substring("Payload_".length(), clicked.getName().length() - nameSon.length());
					// Adding attribute with parent name and "ICD_Logical_Payload" as attribute
					irpa = clicked.addAttribute(nameSon);
					irpa.setDescription("");
					irpa.addStereotype("ICD_Logical_Payload", "Attribute");
					irpa.setTypeDeclaration("DataMsg_" + family + nameSon);
										
					// Finding ICD_Signal_Payload type
					physical_payload.setTypeDeclaration(clicked.getName());
					physical_payload = null;
				}
			}

			// Every attribute can have an id stereotype
			if (id)
				irpa.addStereotype("ICD_Message_IDField", "Attribute");
			
			return irpa;
		}
		else
			return null;
	}
	
}


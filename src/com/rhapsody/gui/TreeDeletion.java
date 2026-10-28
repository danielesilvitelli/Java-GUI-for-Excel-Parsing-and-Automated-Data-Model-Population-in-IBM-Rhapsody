package com.rhapsody.gui;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.telelogic.rhapsody.core.RhapsodyRuntimeException;



public class TreeDeletion {

	protected static void deleteNode() {
		
		if(GUI.tp == null)
			return;
		
		int result = JOptionPane.showConfirmDialog(null, "Are you sure to cancel the selected"
				+ " element? By deleting it, you will lose all of its subfolders.", "Confirmation",
				JOptionPane.YES_NO_OPTION);
		
		if (result == JOptionPane.YES_OPTION) {
			
			String elementClicked = String.valueOf(GUI.tp.getLastPathComponent());
			
			// Get model
			DefaultTreeModel model = (DefaultTreeModel) GUI.tree.getModel();
			// Get node selected
			DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) GUI.tp.getLastPathComponent();
			String elementClickedWithoutStereotypes = ((CustomIRPModelElement)((DefaultMutableTreeNode) 
					GUI.tp.getLastPathComponent()).getUserObject()).toStringWithoutStereotypes();
			
			int nodeLevel = nodeSelected.getLevel();
			
			// Deleting from listOfIDStereotypes
			if (nodeLevel == 4 && ((CustomIRPModelElement) nodeSelected.getUserObject()).toString().contains("ICD_Message_IDField")) {
				String type = ((CustomIRPModelElement) 
								((DefaultMutableTreeNode) nodeSelected.getParent()).getUserObject()).toStringWithoutStereotypes();
				String pack = ((CustomIRPModelElement) 
								((DefaultMutableTreeNode) 
									((DefaultMutableTreeNode) 
										nodeSelected.getParent()).getParent()).getUserObject()).toStringWithoutStereotypes();
				String icd = ((CustomIRPModelElement) 
								((DefaultMutableTreeNode) 
									((DefaultMutableTreeNode) 
										((DefaultMutableTreeNode)
											nodeSelected.getParent()).getParent()).getParent()).getUserObject()).toStringWithoutStereotypes();
				OperationsOnJTree.listOfPaths.remove(icd + "_" + pack + "_" + type);
			}
			
			// Deleting from JTree

			// Get relatives
			DefaultMutableTreeNode parent = 
					(DefaultMutableTreeNode) GUI.tp.getParentPath().getLastPathComponent();
			DefaultMutableTreeNode uncle = null;
			
			try {
				
				// Finding the Payload package to remove also with DataMsg one
				if (nodeLevel == 3) {
					
					// The next two instructions will be useful to update OperationsOnJTree.typeCorrespondences map
					DefaultMutableTreeNode icd = (DefaultMutableTreeNode) parent.getParent();
					ArrayList<String> allTypes = OperationsOnJTree.typeCorrespondences.get(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes());
					
					if(((CustomIRPModelElement) parent.getUserObject()).toStringWithoutStereotypes().contains("_Message_Type_Pkg")) {
					
						String parentName = ((CustomIRPModelElement) parent.getUserObject()).toStringWithoutStereotypes();
						DefaultMutableTreeNode granParent = (DefaultMutableTreeNode) parent.getParent();
						String family = parentName.substring(0, parentName.length() - "_Message_Type_Pkg".length());
						
						// O(n) but there are always three elements and the worst case is to travel all of them
						for (int t = 0; t < granParent.getChildCount(); t++) {
							uncle = (DefaultMutableTreeNode) granParent.getChildAt(t);
							String uncleName = ((CustomIRPModelElement) uncle.getUserObject()).toStringWithoutStereotypes();
							if(uncleName.contains("_Composite_Types"))
								break;
						}
						
						String dataMsgToFind = "DataMsg_" + family + "_" + elementClickedWithoutStereotypes;
						String payloadToFind = "Payload_" + family + "_" + elementClickedWithoutStereotypes;
						
						// Update typeCorrespondences map
						allTypes.remove(allTypes.indexOf(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes()));
						allTypes.remove(allTypes.indexOf(dataMsgToFind));
						allTypes.remove(allTypes.indexOf(payloadToFind));
						OperationsOnJTree.typeCorrespondences.remove(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes());
						OperationsOnJTree.typeCorrespondences.put(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes(), allTypes);
						
						// First, I must delete the Physical_Payload attribute which depends on Payload Type (otherwise, if I directly delete the Payload Type I would have an error)
						// O(1)
						DefaultMutableTreeNode physical_payload = (DefaultMutableTreeNode) nodeSelected.getFirstChild();
						((CustomIRPModelElement) physical_payload.getUserObject()).rmvElement();
						model.removeNodeFromParent(physical_payload);
						
						// Binary search of element to delete
						// O(log(n))
						DefaultMutableTreeNode payload = TreeSearch.binarySearchForNodesWithoutStereotypes(uncle, payloadToFind);
						// Deleting payload
						((CustomIRPModelElement) payload.getUserObject()).rmvElement();
						model.removeNodeFromParent(payload);
						// O(log(n))
						DefaultMutableTreeNode dataMsg = TreeSearch.binarySearchForNodesWithoutStereotypes(uncle, dataMsgToFind);
						// Deleting dataMsg
						((CustomIRPModelElement) dataMsg.getUserObject()).rmvElement();
						model.removeNodeFromParent(dataMsg);
					}
					else {
						// Update typeCorrespondences map
						allTypes.remove(allTypes.indexOf(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes()));
						OperationsOnJTree.typeCorrespondences.remove(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes());
						OperationsOnJTree.typeCorrespondences.put(((CustomIRPModelElement) icd.getUserObject()).toStringWithoutStereotypes(), allTypes);
					}
				}
			}catch(RhapsodyRuntimeException rre) {
				CustomPrint.printOnJTextPane(rre.getMessage());
				GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				return;
			}
						
			String var = (nodeSelected.getLevel() == 1) ? "Package" : "";
			var = (nodeSelected.getLevel() == 3) ? "Type" : var;
			var = (nodeSelected.getLevel() == 4) ? "Attribute" : var;
			CustomPrint.printOnJTextPane(var + " successfully deleted.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			
			// Deleting node selected from OperationsOnJTree.typeCorrespondences map
			if(nodeLevel == 1)
				OperationsOnJTree.typeCorrespondences.remove(((CustomIRPModelElement) nodeSelected.getUserObject()).toStringWithoutStereotypes());
			// Deleting nodeSelected
			((CustomIRPModelElement) nodeSelected.getUserObject()).rmvElement();
			model.removeNodeFromParent(nodeSelected);
			GUI.tree.scrollPathToVisible(new TreePath(parent.getPath()));
			model.reload();
			
			// reset fields and opening the right path
			GUI.resetFrame(GUI.idStereotypeRadioButton, GUI.typeAttributesComboBox);
			if (parent.getChildCount() == 0)
				GUI.tree.setSelectionPath(new TreePath(parent.getPath()));
			else {
				DefaultMutableTreeNode brother = (DefaultMutableTreeNode) parent.getFirstChild();
				GUI.tree.setSelectionPath(new TreePath(brother.getPath()));
			}
		}
	}
	
}

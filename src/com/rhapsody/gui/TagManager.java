package com.rhapsody.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPModelElement;

public class TagManager {
	
	protected static Boolean allAttributesHaveNoOrder = false;
	
	
	protected static void determiningAttributesOrderByElementsAlreadyPresent(DefaultMutableTreeNode node) {
		
		int nrOfChildren = node.getChildCount();
		
		// Determining what's the order to follow to sort attributes
		// O(n) worst case
		for (int i = 0; i < nrOfChildren; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			CustomIRPModelElement cie = (CustomIRPModelElement) child.getUserObject();
			
			// toString() inside for cycle -> O(n^2) in which you iterate over stereotypes that are max 2, so it goes as O(n)
			if(cie.toString().contains("ICD_Signal_Payload"))
				continue;
			
			try {
				if(cie.getTag("Byte_Offset").getValue() != "" && 
						!cie.getTag("Byte_Offset").getValue().isEmpty()) {
					GUI.orderParameter = "Byte_Offset";
					allAttributesHaveNoOrder = false;
					break;
				}
				else if(cie.getTag("Start_Bit").getValue() != "" && 
						!cie.getTag("Start_Bit").getValue().isEmpty()) {
					GUI.orderParameter = "Start_Bit";
					allAttributesHaveNoOrder = false;
					break;
				}
				else   // both Byte_Offset and Start_Bit are null. The new order will be given by the element added
					allAttributesHaveNoOrder = true;
			}catch(NullPointerException npe) {
				continue;
			}
			
		}
	}
	

	protected static void update(JComboBox valueTagComboBox1, JComboBox valueTagComboBox2, 
			JComboBox valueTagComboBox3, JComboBox valueTagComboBox4, JComboBox valueTagComboBox5, 
			JComboBox valueTagComboBox6, JComboBox valueTagComboBox7, JComboBox valueTagComboBox8,
			JComboBox valueTagComboBox9, JComboBox valueTagComboBox10) {
		
		if(GUI.tp == null)
			return;
		
		// Set up maps
		Map<JTextField, JTextField> m1 = new HashMap<>();
		Map<JTextField, JComboBox> m2 = new HashMap<>();
		
		if(GUI.keyTagTextField1.getText() != "" && !GUI.keyTagTextField1.getText().isEmpty() && GUI.keyTagTextField1.getText() != null) {
			m1.put(GUI.keyTagTextField1, GUI.valueTagTextField1);
			m2.put(GUI.keyTagTextField1, valueTagComboBox1);
		}
		if(GUI.keyTagTextField2.getText() != "" && !GUI.keyTagTextField2.getText().isEmpty() && GUI.keyTagTextField2.getText() != null) {
			m1.put(GUI.keyTagTextField2, GUI.valueTagTextField2);
			m2.put(GUI.keyTagTextField2, valueTagComboBox2);
		}
		if(GUI.keyTagTextField3.getText() != "" && !GUI.keyTagTextField3.getText().isEmpty() && GUI.keyTagTextField3.getText() != null) {
			m1.put(GUI.keyTagTextField3, GUI.valueTagTextField3);
			m2.put(GUI.keyTagTextField3, valueTagComboBox3);
		}
		if(GUI.keyTagTextField4.getText() != "" && !GUI.keyTagTextField4.getText().isEmpty() && GUI.keyTagTextField4.getText() != null) {
			m1.put(GUI.keyTagTextField4, GUI.valueTagTextField4);
			m2.put(GUI.keyTagTextField4, valueTagComboBox4);
		}
		if(GUI.keyTagTextField5.getText() != "" && !GUI.keyTagTextField5.getText().isEmpty() && GUI.keyTagTextField5.getText() != null) {
			m1.put(GUI.keyTagTextField5, GUI.valueTagTextField5);
			m2.put(GUI.keyTagTextField5, valueTagComboBox5);
		}
		if(GUI.keyTagTextField6.getText() != "" && !GUI.keyTagTextField6.getText().isEmpty() && GUI.keyTagTextField6.getText() != null) {
			m1.put(GUI.keyTagTextField6, GUI.valueTagTextField6);
			m2.put(GUI.keyTagTextField6, valueTagComboBox6);
		}
		if(GUI.keyTagTextField7.getText() != "" && !GUI.keyTagTextField7.getText().isEmpty() && GUI.keyTagTextField7.getText() != null) {
			m1.put(GUI.keyTagTextField7, GUI.valueTagTextField7);
			m2.put(GUI.keyTagTextField7, valueTagComboBox7);
		}
		if(GUI.keyTagTextField8.getText() != "" && !GUI.keyTagTextField8.getText().isEmpty() && GUI.keyTagTextField8.getText() != null) {
			m1.put(GUI.keyTagTextField8, GUI.valueTagTextField8);
			m2.put(GUI.keyTagTextField8, valueTagComboBox8);
		}
		if(GUI.keyTagTextField9.getText() != "" && !GUI.keyTagTextField9.getText().isEmpty() && GUI.keyTagTextField9.getText() != null) {
			m1.put(GUI.keyTagTextField9, GUI.valueTagTextField9);
			m2.put(GUI.keyTagTextField9, valueTagComboBox9);
		}
		if(GUI.keyTagTextField10.getText() != "" && !GUI.keyTagTextField10.getText().isEmpty() && GUI.keyTagTextField10.getText() != null) {
			m1.put(GUI.keyTagTextField10, GUI.valueTagTextField10);
			m2.put(GUI.keyTagTextField10, valueTagComboBox10);
		}

		// Getting name of element selected
		String elementClicked = String.valueOf(GUI.tp.getLastPathComponent());
		DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) GUI.tp.getLastPathComponent();
		
		// Getting model
		DefaultTreeModel model = (DefaultTreeModel) GUI.tree.getModel();
		
		// Getting level of the tree element selected
		int nodeLevel = ((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getLevel();
		
		// STEP 1: determining the order to give to attributes --> it could be ("byte_offset","no_bytes") or ("start_bit","no_bits")

		// Declaration and initialization of variables that will catch the values inserted by the user after he has clicked the Update Tags button
		int byte_offset = -1;
		int no_bytes = -1;
		int start_bit = -1;
		int no_bits = -1;
		int ret = -1;
									
		// Get the IRPModelElement on which you want to set tags
		IRPModelElement elementForTags = ((CustomIRPModelElement) ((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getUserObject()).getElement();
		
		// Sorting attributes after having modified Byte_Offset tag field
		DefaultMutableTreeNode parent = null;
		IRPModelElement rParent = null;
		
		if(nodeLevel > 0) {
			parent = (DefaultMutableTreeNode) GUI.tp.getParentPath().getLastPathComponent();
			rParent = ((CustomIRPModelElement) parent.getUserObject()).getElement();
		}
		
		// Rescaling tags
		if(nodeLevel == 4) {
			
			// Special case: ICD_Signal_Payload
			// This node will not be ordered because it doesn't have the "Start_bit" field. It will always occupy the first position
			if(((CustomIRPModelElement) nodeSelected.getUserObject()).toString().contains("ICD_Signal_Payload")) {
				
				// Update all tags for this node
				for(JTextField jtf : m1.keySet()) {
					if(jtf.isEnabled())
						GUI.updateTagsFromGUI(jtf, m1.get(jtf),	m2.get(jtf), nodeSelected);
				}
				
				// Set this node as first in the scheduling
				// O(n)
				OperationsOnJTree.sortNodesAlphabetically(parent, model, false);
						
				// Opening path selected
				GUI.tree.scrollPathToVisible(new TreePath(
								((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getPath()));
				
				CustomPrint.printOnJTextPane("Tags updated.");
				GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				
				return;
			}
			
			// I'm iterating over all tags of the selected element in order to read "byte_offset", 
			// "no_bytes", "start_bit" and "no_bits" values inserted from GUI
			// O(n) but n can be max = 10
			for(JTextField jtf : m1.keySet()) {
				try {
					String str = m1.get(jtf).getText();
					
					if(jtf.getText().equals("Byte_Offset"))
						byte_offset = Integer.parseInt(str);
					else if(jtf.getText().equals("No_Bytes"))
						no_bytes = Integer.parseInt(str);
					else if(jtf.getText().equals("Start_Bit"))
						start_bit = Integer.parseInt(str);
					else if(jtf.getText().equals("No_Bits"))
						no_bits = Integer.parseInt(str);
					
				}catch(NumberFormatException | NullPointerException nfe) { continue; }
			}
			
			// Determining the order
			// O(n) worst case
			determiningAttributesOrderByElementsAlreadyPresent(parent);
			
			// If the instruction above didn't lead to an ordering rule, the order will be decided by the new element added
			if(allAttributesHaveNoOrder) {
				if(byte_offset != -1 && no_bytes != -1)
					GUI.orderParameter = "Byte_Offset";
				else if(start_bit != -1 && no_bits != -1)
					GUI.orderParameter = "Start_Bit";
			}
			
			// Order according to Byte_Offset and No_Bytes
			if(byte_offset != -1 && no_bytes != -1)
				ret = OperationsOnJTree.rescalingTags(parent, elementForTags, byte_offset, no_bytes);
			else if(start_bit != -1 && no_bits != -1)
				ret = OperationsOnJTree.rescalingTags(parent, elementForTags, start_bit, no_bits);
			
			// Hybrid filling: Byte_Offset filled and No_Bytes not filled or vice-versa
			if(((byte_offset != -1 && no_bytes == -1) || (byte_offset == -1 && no_bytes != -1))
					&& ((start_bit != -1 && no_bits == -1) || (start_bit == -1 && no_bits != -1))) {
				CustomPrint.printOnJTextPane("ERROR: when updating Byte_Offset, also update No_Bytes (and vice-versa). "
						+ "Same thing with Start_Bit and No_Bits.");
				GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				
				// Opening path selected
				GUI.tree.scrollPathToVisible(new TreePath(
								((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getPath()));
				
				return;
			}
			
			// Checking if the tags that you are modifying are not Byte_Offset and No_Bytes
			try {
				int currentByteOffset = Integer.parseInt(elementForTags.getTag("Byte_Offset").getValue());
				int currentNBytes = Integer.parseInt(elementForTags.getTag("No_Bytes").getValue());
				if(currentByteOffset == byte_offset && currentNBytes == no_bytes)
					ret = 1;
			}catch(NumberFormatException | NullPointerException npe) {
				// Checking if the tags that you are modifying are not Start_Bit and No_Bits
				try {
					int currentByteOffset = Integer.parseInt(elementForTags.getTag("Start_Bit").getValue());
					int currentNBytes = Integer.parseInt(elementForTags.getTag("No_Bits").getValue());
					if(currentByteOffset == byte_offset && currentNBytes == no_bytes)
						ret = 2;
				}catch(NumberFormatException | NullPointerException nfe) {}
			}
			
			// Update other fields than Byte_Offset, No_Bytes, Start_Bit, No_Bits
			if(byte_offset == -1 && no_bytes == -1 && start_bit == -1 && no_bits == -1)
				ret = 0;
			
			if(ret == -1) { // something went wrong
				CustomPrint.printOnJTextPane("Error while updating tag fields.");
				GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				
				// Opening path selected
				GUI.tree.scrollPathToVisible(new TreePath(
								((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getPath()));
				
				return;
			}
			else if(ret == 0) { // all goes well, it is necessary to update each tag value
				// O(n)
				for(JTextField jtf : m1.keySet()) {
					if(jtf.isEnabled())
						GUI.updateTagsFromGUI(jtf, m1.get(jtf),	m2.get(jtf), nodeSelected);
				}
			}
			else if(ret == 1) { // all goes well, it is necessary to update other tags than Byte_Offset and No_Bytes because they were previously updated
				// O(n)
				for(JTextField jtf : m1.keySet()) {
					if(jtf.isEnabled()) {
						if(!(jtf.getText().equals("Byte_Offset") || jtf.getText().equals("No_Bytes")))
							GUI.updateTagsFromGUI(jtf, m1.get(jtf),	m2.get(jtf), nodeSelected);
					}
				}
			}
			else if(ret == 2) { // all goes well, it is necessary to update other tags than Start_Bit and No_Bits because they were previously updated
				// O(n)
				for(JTextField jtf : m1.keySet()) {
					if(jtf.isEnabled()) {
						if(!(jtf.getText().equals("Start_Bit") || jtf.getText().equals("No_Bits")))
							GUI.updateTagsFromGUI(jtf, m1.get(jtf),	m2.get(jtf), nodeSelected);
					}
				}
			}
		}
		else {
			// O(n)
			for(JTextField jtf : m1.keySet()) {
				if(jtf.isEnabled())
					GUI.updateTagsFromGUI(jtf, m1.get(jtf),	m2.get(jtf), nodeSelected);
			}
		}
		
		// O(n)
		if(nodeLevel > 0)
			OperationsOnJTree.sortNodesAlphabetically(parent, model, false);
				
		// Opening path selected
		GUI.tree.scrollPathToVisible(new TreePath(
						((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getPath()));
		
		CustomPrint.printOnJTextPane("Tags updated.");
		GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
		
	}
	
}

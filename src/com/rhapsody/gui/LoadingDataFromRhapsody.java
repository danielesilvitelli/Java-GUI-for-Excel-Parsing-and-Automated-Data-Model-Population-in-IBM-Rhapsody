package com.rhapsody.gui;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.telelogic.rhapsody.core.IRPCollection;



public class LoadingDataFromRhapsody {
	
	protected static void load(JButton setUpButton) {
		
		GUI.tree.setRootVisible(true);
		
		// Get model
		DefaultTreeModel model = (DefaultTreeModel) GUI.tree.getModel();
		// Set root node
		CustomIRPModelElement cie = new CustomIRPModelElement(GUI.rs.getRoot());
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(cie);
		model.setRoot(root);
		
		if (GUI.rs.rootHasChildren()) {
			IRPCollection childrenRoot = GUI.rs.getChildrenRoot();
			OperationsOnJTree.populatingTheTree(model, childrenRoot, root);
		}
		model.reload();
		
		/*
			Sort nodes of the model in alphabetical order (the attributes have a different order,
			better explained in the proper class)
		*/
		OperationsOnJTree.sortNodesAlphabetically(root, model, true);
		
		CustomPrint.printOnJTextPane("Set up done.");
		GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
		
		setUpButton.setEnabled(false);
	}
	
}

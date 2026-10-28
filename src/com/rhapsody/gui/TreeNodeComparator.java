package com.rhapsody.gui;

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;


public class TreeNodeComparator implements Comparator<DefaultMutableTreeNode>  {

	@Override
	public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
		return o1.getUserObject().toString().compareTo(o2.getUserObject().toString());
	}

}

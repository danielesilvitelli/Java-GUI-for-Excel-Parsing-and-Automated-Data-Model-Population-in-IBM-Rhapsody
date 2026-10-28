package com.rhapsody.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import com.telelogic.rhapsody.core.IRPModelElement;

public class TreeSearch {
	
	
	public static String binarySearchForStrings(String[] list, String elementToFind) {
		int start = 0;
		int end = list.length - 1;
		
		while (start <= end) {
			int mid = start + ((end - start) / 2);
			
			if (list[mid].compareToIgnoreCase(elementToFind) > 0) //list[mid] > elementToFind
				end = mid - 1;
			else if (list[mid].compareToIgnoreCase(elementToFind) < 0)  //list[mid] < elementToFind
				start = mid + 1;
			else
				return list[mid];
		}
		return null;
	}
	
	
	public static DefaultMutableTreeNode binarySearchForNodes(DefaultMutableTreeNode node, String elementToFind) {
		int start = 0;
		int end = node.getChildCount() - 1;
		
		while (start <= end) {
			int mid = start + ((end - start) / 2);
			
			DefaultMutableTreeNode middleNode = (DefaultMutableTreeNode) node.getChildAt(mid);
			CustomIRPModelElement middleElement = (CustomIRPModelElement) middleNode.getUserObject();
			
			if(middleElement.toString().compareToIgnoreCase(elementToFind) > 0) //list[mid] > elementToFind
				end = mid - 1;
			else if (middleElement.toString().compareToIgnoreCase(elementToFind) < 0)  //list[mid] < elementToFind
				start = mid + 1;
			else
				return middleNode;
		}
		return null;
	}
	
	
	public static DefaultMutableTreeNode binarySearchForNodesWithoutStereotypes(DefaultMutableTreeNode node, String elementToFind) {
		int start = 0;
		int end = node.getChildCount() - 1;
		
		while (start <= end) {
			int mid = start + ((end - start) / 2);
			
			String middleElementName = 
					((CustomIRPModelElement) ((DefaultMutableTreeNode) node.getChildAt(mid)).getUserObject()).toStringWithoutStereotypes();
			
			if(middleElementName.compareToIgnoreCase(elementToFind) > 0) //list[mid] > elementToFind
				end = mid - 1;
			else if (middleElementName.compareToIgnoreCase(elementToFind) < 0)  //list[mid] < elementToFind
				start = mid + 1;
			else
				return (DefaultMutableTreeNode) node.getChildAt(mid);
		}
		return null;
	}
	
}

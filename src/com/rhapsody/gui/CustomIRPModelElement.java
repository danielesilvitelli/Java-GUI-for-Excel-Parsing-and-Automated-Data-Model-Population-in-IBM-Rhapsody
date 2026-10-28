package com.rhapsody.gui;

import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPTag;
import com.telelogic.rhapsody.core.RhapsodyRuntimeException;



public class CustomIRPModelElement {
	
	private IRPModelElement element;
	
	
	public CustomIRPModelElement(IRPModelElement element) {
		this.element = element;
	}
	
	
	public IRPModelElement getElement() {
		return element;
	}
	
	
	public void setElementName(String newName) {
		element.setName(newName);
	}
	
	
	public void rmvElement() {
		element.deleteFromProject();
	}
	
	
	private String buildName() {
		
		try {
			String name = element.getName();
			IRPCollection listOfStereotypes = element.getStereotypes();
			StringBuilder st = new StringBuilder("�");
			
			if (listOfStereotypes.getCount() > 0) {
				// O(n)
				for (int j = 1; j <= listOfStereotypes.getCount(); j++) {
					if (j == listOfStereotypes.getCount())
						st.append(((IRPModelElement)listOfStereotypes.getItem(j)).getName()).append("� ");
					else
						st.append(((IRPModelElement)listOfStereotypes.getItem(j)).getName()).append(", ");
				}
			}
			String stereotypes = st.toString();
			
			if (stereotypes.equals("�"))
				stereotypes = "";
			
			return stereotypes + name;
		}catch(RhapsodyRuntimeException rre) {}
		
		return null;
		
	}
	
	
	@Override
	public String toString() {
		return buildName();
	}
	
	
	public String toStringWithoutStereotypes() {
		return element.getName();
	}
	
	
	public IRPTag getTag(String tagName) {
		return element.getTag(tagName);
	}
	
}

package com.rhapsody.gui;

import java.util.Comparator;

import com.telelogic.rhapsody.core.IRPModelElement;

public class IRPModelElementComparator implements Comparator<IRPModelElement> {

	@Override
	public int compare(IRPModelElement o1, IRPModelElement o2) {
		return o1.getName().compareTo(o2.getName());
	}

}

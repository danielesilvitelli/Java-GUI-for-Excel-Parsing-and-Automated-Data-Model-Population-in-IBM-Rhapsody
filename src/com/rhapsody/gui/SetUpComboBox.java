package com.rhapsody.gui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;

public class SetUpComboBox {

	protected static void settingUpCB(JComboBox startingComboBox, JButton setUpButton) {
		
		String selectedItem = startingComboBox.getSelectedItem().toString();
		if(selectedItem.equals("Select ...")) {
			setUpButton.setEnabled(false);
		}
		else if (selectedItem.equals("Open an existing project")) {
			setUpButton.setEnabled(true);
			JFileChooser jfc = new JFileChooser();
			int check = jfc.showOpenDialog(null);
			if (check == JFileChooser.APPROVE_OPTION) {
				String path = jfc.getSelectedFile().toString();
				String ext = 	String.valueOf(path.charAt(path.length()-4)) + 
								String.valueOf(path.charAt(path.length()-3)) +
								String.valueOf(path.charAt(path.length()-2)) +
								String.valueOf(path.charAt(path.length()-1));
				if (ext.equals(".rpy")) {
					GUI.rs.projectSetUp(selectedItem, path);
					CustomPrint.printOnJTextPane("Project is up. Now you can add components to it.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
					startingComboBox.setEnabled(false);
				}
				else {
					CustomPrint.printOnJTextPane("Invalid path selected. It must be an \".rpy\" file.");
					GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
				}
			}
		}
		else if (selectedItem.equals("Obtain the current active project")) {
			setUpButton.setEnabled(true);
			GUI.rs.projectSetUp(selectedItem, "");
			CustomPrint.printOnJTextPane("Project is up. Now you can add components to it.");
			GUI.consoleLogTextPane.setText(CustomPrint.getBufferContent());
			startingComboBox.setEnabled(false);
		}
	}
	
}

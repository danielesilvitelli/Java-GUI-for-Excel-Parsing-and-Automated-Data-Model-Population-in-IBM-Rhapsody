package com.rhapsody.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JTree;

import java.awt.Color;
import javax.swing.JTextPane;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.telelogic.rhapsody.core.IRPAttribute;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.RhapsodyRuntimeException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JRadioButton;
import java.awt.Dimension;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.Toolkit;



/*
	JFrame implementation
*/
public class GUI extends JFrame {

	private String version = " �Version 3.4�";
	private JPanel contentPane;
	private static GUI frame;
	private static Boolean activateSelectionListener = false;
	private static Boolean connectionCreated = false;
	protected static Boolean disablePrints = false;
	protected static int numOfTags;
	protected static JTree tree;
	protected static TreePath tp;
	protected static RhapsodySide rs;
	protected static String orderParameter = "Byte_Offset";
	protected static JTextField elementSelectedTextField;
	protected static JTextField nameTextField;
	protected static JTextField keyTagTextField1;
	protected static JTextField keyTagTextField3;
	protected static JTextField keyTagTextField5;
	protected static JTextField keyTagTextField7;
	protected static JTextField keyTagTextField9;
	protected static JTextField keyTagTextField2;
	protected static JTextField keyTagTextField4;
	protected static JTextField keyTagTextField6;
	protected static JTextField keyTagTextField8;
	protected static JTextField keyTagTextField10;
	protected static JTextField valueTagTextField1;
	protected static JTextField valueTagTextField3;
	protected static JTextField valueTagTextField5;
	protected static JTextField valueTagTextField7;
	protected static JTextField valueTagTextField9;
	protected static JTextField valueTagTextField2;
	protected static JTextField valueTagTextField4;
	protected static JTextField valueTagTextField6;
	protected static JTextField valueTagTextField8;
	protected static JTextField valueTagTextField10;
	protected static JTextField metaclassTextField;
	protected static JTextPane descriptionTextPane;
	protected static JTextPane consoleLogTextPane;
	protected static JRadioButton idStereotypeRadioButton;
	protected static JComboBox typeAttributesComboBox;

	/*
	 	Launch the application.
	*/
	public static void main(String[] args) {
		
		rs = new RhapsodySide();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Set appearance (look and feel) to Windows
					//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					frame = new GUI(rs);
					frame.setVisible(true);
					
					if(!connectionCreated) {
						isRhapsodyOpened();						
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame, e.getMessage(), null, JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
	}
	
	
	/*
		Function to check if Rhapsody is opened
	*/
	protected static void isRhapsodyOpened() {
		JOptionPane.showMessageDialog(frame, "You have to open Rhapsody.", null, JOptionPane.WARNING_MESSAGE);
		
		frame.dispose();
		
		while(!rs.checkIfRhapsodyIsOn());
		
		rs = new RhapsodySide();
		frame = new GUI(rs);
		frame.setVisible(true);
	}
	
	
	/*
		Utility function which resets the buttons and text fields
	*/
	protected static void resetFrame(JRadioButton idStereotypeButton, JComboBox attributesComboBox) {
		// Reset fields
		idStereotypeButton.setSelected(false);
		idStereotypeButton.setEnabled(false);
		nameTextField.setText("");
		nameTextField.setEnabled(false);
		nameTextField.setEditable(false);
		elementSelectedTextField.setText("");
		elementSelectedTextField.setEnabled(false);
		attributesComboBox.setModel(new DefaultComboBoxModel(new String[] {""}));
		attributesComboBox.setEnabled(false);
	}
	
	
	/*
	 	Utility function to set text field
	*/
	protected static void setTextField(JLabel lb, JTextField tf, String kt) {
		lb.setVisible(true);
		tf.setVisible(true);
		tf.setText(kt);
		tf.setCaretPosition(0);
	}
	
	
	/*
	 	Utility function to set combo boxes
	*/
	protected static void setComboBox(JLabel lb, JComboBox cb, String[] vectOfString, int idx) {
		lb.setVisible(true);
		cb.setVisible(true);
		cb.setModel(new DefaultComboBoxModel(vectOfString));
		cb.setSelectedIndex(idx);
	}
	
	
	/*
	 	Utility function to reset all (making everything not visible)
	*/
	protected static void resetAllTagFields(JLabel lb1, JLabel lb2, JLabel lb3, JLabel lb4, JLabel lb5, 
			JLabel lb6, JLabel lb7, JLabel lb8, JLabel lb9, JLabel lb10,
			JLabel lbv1, JLabel lbv2, JLabel lbv3, JLabel lbv4, JLabel lbv5, 
			JLabel lbv6, JLabel lbv7, JLabel lbv8, JLabel lbv9, JLabel lbv10,
			JComboBox cb1, JComboBox cb2, JComboBox cb3, JComboBox cb4, JComboBox cb5, 
			JComboBox cb6, JComboBox cb7, JComboBox cb8, JComboBox cb9, JComboBox cb10) {
		keyTagTextField1.setVisible(false);
		keyTagTextField2.setVisible(false);
		keyTagTextField3.setVisible(false);
		keyTagTextField4.setVisible(false);
		keyTagTextField5.setVisible(false);
		keyTagTextField6.setVisible(false);
		keyTagTextField7.setVisible(false);
		keyTagTextField8.setVisible(false);
		keyTagTextField9.setVisible(false);
		keyTagTextField10.setVisible(false);
		valueTagTextField1.setVisible(false);
		valueTagTextField2.setVisible(false);
		valueTagTextField3.setVisible(false);
		valueTagTextField4.setVisible(false);
		valueTagTextField5.setVisible(false);
		valueTagTextField6.setVisible(false);
		valueTagTextField7.setVisible(false);
		valueTagTextField8.setVisible(false);
		valueTagTextField9.setVisible(false);
		valueTagTextField10.setVisible(false);
		cb1.setVisible(false);
		cb2.setVisible(false);
		cb3.setVisible(false);
		cb4.setVisible(false);
		cb5.setVisible(false);
		cb6.setVisible(false);
		cb7.setVisible(false);
		cb8.setVisible(false);
		cb9.setVisible(false);
		cb10.setVisible(false);
		lb1.setVisible(false);
		lb2.setVisible(false);
		lb3.setVisible(false);
		lb4.setVisible(false);
		lb5.setVisible(false);
		lb6.setVisible(false);
		lb7.setVisible(false);
		lb8.setVisible(false);
		lb9.setVisible(false);
		lb10.setVisible(false);
		lbv1.setVisible(false);
		lbv2.setVisible(false);
		lbv3.setVisible(false);
		lbv4.setVisible(false);
		lbv5.setVisible(false);
		lbv6.setVisible(false);
		lbv7.setVisible(false);
		lbv8.setVisible(false);
		lbv9.setVisible(false);
		lbv10.setVisible(false);
	}


	protected static void updateTagsFromGUI(JTextField tf, JTextField tv, JComboBox cb, DefaultMutableTreeNode nodeSelected) {
		if(tf.isVisible() && cb.isVisible()) {
			String k = tf.getText();
			String v = cb.getSelectedItem().toString();
			rs.setTag(nodeSelected, k, v);
		}
		else if(tf.isVisible() && tv.isVisible()) {
			String k = tf.getText();
			String v = tv.getText();
			rs.setTag(nodeSelected, k, v);
		}
	}
	
	
	protected static String getElementStereotypes(IRPModelElement elementAdded) {
		// Getting stereotypes
		IRPCollection listOfStereotypes = elementAdded.getStereotypes();
		StringBuilder st = new StringBuilder("�");
		
		if (listOfStereotypes.getCount() > 0) {
			for (int j = 1; j <= listOfStereotypes.getCount(); j++) {
				if (j == listOfStereotypes.getCount())
					st.append(((IRPModelElement)listOfStereotypes.getItem(j)).getName()).append("� ");
				else
					st.append(((IRPModelElement)listOfStereotypes.getItem(j)).getName()).append(", ");
			}
		}
		return st.toString();
	}
	
	
	protected static String removeElementPrefix(String element, int level) {
		if(level == 1)
			return element.substring("ICD_".length(), element.length());
		else if(level == 3 && (element.contains("DataMsg_") || element.contains("Payload_") || element.contains("TypeMsg_")))
			return element.substring("DataMsg_".length(), element.length()); // len("DataMsg_") == len("Payload_") == len("TypeMsg_")
		else if(level == 4 && element.contains("Physical_Payload_"))
			return element.substring("Physical_Payload_".length(), element.length());
		return element;			
	}

	
	/*
		Create the frame.
	*/
	private GUI(RhapsodySide rs) {
		
		try {
		
			// Setting icon
			setIconImage(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/com/rhapsody/gui/IFplugin_icon.png")));
			
			// Setting frame base structure.
			setTitle("Rhapsody GUI");
			setBackground(new Color(128, 128, 128));
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(0, 0, 1650, 1000);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
			
			JPanel leftPanel = new JPanel();
			leftPanel.setBorder(null);
			leftPanel.setBackground(new Color(255, 255, 255));
			contentPane.add(leftPanel);
			leftPanel.setLayout(null);
			
			JScrollPane scrollPaneLeftPanel = new JScrollPane();
			scrollPaneLeftPanel.setBorder(null);
			scrollPaneLeftPanel.setBackground(new Color(63, 57, 56));
			scrollPaneLeftPanel.setBounds(10, 11, 792, 929);
			leftPanel.add(scrollPaneLeftPanel);
			
			tree = new JTree();
			tree.setBorder(null);
			tree.setRootVisible(false);
			scrollPaneLeftPanel.setViewportView(tree);
			tree.setFont(new Font("Tahoma", Font.PLAIN, 14));
			tree.setForeground(new Color(63, 57, 56));
			tree.setModel(new DefaultTreeModel(
				new DefaultMutableTreeNode("JTree") {
					{
					}
				}
			));
			tree.setBackground(new Color(255, 255, 255));
			
			JPanel rightPanel = new JPanel();
			rightPanel.setBorder(null);
			rightPanel.setFont(new Font("Tahoma", Font.PLAIN, 14));
			rightPanel.setBackground(new Color(255, 255, 255));
			contentPane.add(rightPanel);
			rightPanel.setLayout(null);
			
			JScrollPane scrollPaneRightPanel = new JScrollPane();
			scrollPaneRightPanel.setBorder(null);
			scrollPaneRightPanel.setBounds(10, 835, 792, 105);
			scrollPaneRightPanel.setForeground(new Color(63, 57, 56));
			scrollPaneRightPanel.setBackground(new Color(63, 57, 56));
			rightPanel.add(scrollPaneRightPanel);
			
			consoleLogTextPane = new JTextPane();
			consoleLogTextPane.setBorder(null);
			consoleLogTextPane.setEditable(false);
			consoleLogTextPane.setForeground(new Color(0, 0, 0));
			consoleLogTextPane.setBackground(new Color(240, 240, 240));
			scrollPaneRightPanel.setViewportView(consoleLogTextPane);
			
			JLabel rhapsodyImageLabel = new JLabel("");
			rhapsodyImageLabel.setBounds(670, 15, 134, 39);
			rhapsodyImageLabel.setIcon(new ImageIcon(GUI.class.getResource("/com/rhapsody/gui/rhapsody_Logo.PNG")));
			rightPanel.add(rhapsodyImageLabel);
			
			// Checking Rhapsody start
			if (!rs.flag) {
				CustomPrint.printOnJTextPane(version + " Rhapsody app is closed. You need to open it before using the GUI.");
				consoleLogTextPane.setText(CustomPrint.getBufferContent());

				return;
			}
			
			connectionCreated = true;
			
			CustomPrint.printOnJTextPane(version + " Connection with Rhapsody successfully created.");
			consoleLogTextPane.setText(CustomPrint.getBufferContent());
	
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setBorder(null);
			buttonsPanel.setBounds(673, 138, 129, 182);
			buttonsPanel.setBackground(new Color(240, 240, 240));
			buttonsPanel.setForeground(new Color(245, 245, 245));
			rightPanel.add(buttonsPanel);
			buttonsPanel.setLayout(null);
	
			JButton setUpButton = new JButton("Set up");
			setUpButton.setBorder(null);
			setUpButton.setEnabled(false);
			setUpButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			setUpButton.setForeground(new Color(240, 240, 240));
			setUpButton.setBackground(new Color(54, 146, 248));
			setUpButton.setBounds(10, 11, 111, 23);
			buttonsPanel.add(setUpButton);
			
			JButton clearConsoleLogButton = new JButton("Clear Console");
			clearConsoleLogButton.setBorder(null);
			clearConsoleLogButton.setBounds(10, 151, 111, 23);
			buttonsPanel.add(clearConsoleLogButton);
			clearConsoleLogButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CustomPrint.emptyBuffer();
					consoleLogTextPane.setText("");
				}
			});
			clearConsoleLogButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			clearConsoleLogButton.setForeground(new Color(245, 245, 245));
			clearConsoleLogButton.setBackground(new Color(54, 146, 248));
			
			JButton modifyButton = new JButton("Modify");
			modifyButton.setBorder(null);
			modifyButton.setForeground(new Color(245, 245, 245));
			modifyButton.setEnabled(false);
			modifyButton.setBackground(new Color(54, 146, 248));
			modifyButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			modifyButton.setBounds(10, 67, 111, 23);
			buttonsPanel.add(modifyButton);
			
			JButton addButton = new JButton("Add");
			addButton.setBorder(null);
			addButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			addButton.setForeground(new Color(245, 245, 245));
			addButton.setEnabled(false);
			addButton.setBackground(new Color(54, 146, 248));
			addButton.setBounds(10, 39, 111, 23);
			buttonsPanel.add(addButton);
			
			JButton deleteButton = new JButton("Delete");
			deleteButton.setBorder(null);
			deleteButton.setBackground(new Color(54, 146, 248));
			deleteButton.setEnabled(false);
			deleteButton.setForeground(new Color(245, 245, 245));
			deleteButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			deleteButton.setBounds(10, 95, 111, 23);
			buttonsPanel.add(deleteButton);
			
			JButton refreshButton = new JButton("Refresh");
			refreshButton.setBorder(null);
			refreshButton.setForeground(new Color(245, 245, 245));
			refreshButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			refreshButton.setBackground(new Color(54, 146, 248));
			refreshButton.setBounds(10, 123, 111, 23);
			buttonsPanel.add(refreshButton);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setBorder(null);
			mainPanel.setBounds(10, 11, 658, 309);
			mainPanel.setBackground(new Color(240, 240, 240));
			rightPanel.add(mainPanel);
			mainPanel.setLayout(null);
			
			JLabel elementSelectedLabel = new JLabel("Element selected");
			elementSelectedLabel.setBackground(new Color(255, 255, 255));
			elementSelectedLabel.setForeground(new Color(0, 0, 0));
			elementSelectedLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
			elementSelectedLabel.setBounds(10, 100, 114, 27);
			mainPanel.add(elementSelectedLabel);
			
			elementSelectedTextField = new JTextField();
			elementSelectedTextField.setBackground(new Color(255, 255, 255));
			elementSelectedTextField.setForeground(new Color(40, 40, 40));
			elementSelectedTextField.setEditable(false);
			elementSelectedTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
			elementSelectedTextField.setBounds(134, 100, 514, 25);
			mainPanel.add(elementSelectedTextField);
			elementSelectedTextField.setColumns(10);
			
			nameTextField = new JTextField();
			nameTextField.setBackground(new Color(255, 255, 255));
			nameTextField.setEditable(false);
			nameTextField.setEnabled(false);
			nameTextField.setBounds(134, 160, 514, 25);
			mainPanel.add(nameTextField);
			nameTextField.setColumns(10);
			
			JLabel enterTheNameLabel = new JLabel("Enter the name");
			enterTheNameLabel.setBackground(new Color(255, 255, 255));
			enterTheNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
			enterTheNameLabel.setForeground(new Color(0, 0, 0));
			enterTheNameLabel.setBounds(10, 160, 112, 27);
			mainPanel.add(enterTheNameLabel);
			
			// Creation of a JComboBox: it is a menu with items
			JComboBox startingComboBox = new JComboBox();
			startingComboBox.setBounds(10, 11, 245, 23);
			mainPanel.add(startingComboBox);
			startingComboBox.setFont(new Font("Tahoma", Font.PLAIN, 14));
			startingComboBox.setModel(new DefaultComboBoxModel(new String[] {"Select ...", "Open an existing project",
					"Obtain the current active project"}));
			startingComboBox.setToolTipText("");
			startingComboBox.setBackground(new Color(255, 255, 255));
			startingComboBox.setForeground(new Color(0, 0, 0));
			
			idStereotypeRadioButton = new JRadioButton("Add id stereotype");
			idStereotypeRadioButton.setForeground(new Color(0, 0, 0));
			idStereotypeRadioButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			idStereotypeRadioButton.setEnabled(false);
			idStereotypeRadioButton.setBackground(new Color(240, 240, 240));
			idStereotypeRadioButton.setBounds(515, 192, 133, 23);
			mainPanel.add(idStereotypeRadioButton);
			
			JButton updateTypeButton = new JButton("Update type");
			updateTypeButton.setBorder(null);
			updateTypeButton.setForeground(new Color(240, 240, 240));
			updateTypeButton.setBackground(new Color(54, 146, 248));
			updateTypeButton.setEnabled(false);
			updateTypeButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			updateTypeButton.setBounds(537, 222, 111, 25);
			mainPanel.add(updateTypeButton);
			
			typeAttributesComboBox = new JComboBox();
			typeAttributesComboBox.setEnabled(false);
			typeAttributesComboBox.setFont(new Font("Tahoma", Font.PLAIN, 14));
			typeAttributesComboBox.setBounds(10, 222, 517, 25);
			mainPanel.add(typeAttributesComboBox);
			
			JLabel metaclassLabel = new JLabel("Metaclass");
			metaclassLabel.setMinimumSize(new Dimension(74, 14));
			metaclassLabel.setMaximumSize(new Dimension(74, 14));
			metaclassLabel.setPreferredSize(new Dimension(74, 14));
			metaclassLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
			metaclassLabel.setBounds(10, 130, 114, 27);
			mainPanel.add(metaclassLabel);
			
			metaclassTextField = new JTextField();
			metaclassTextField.setOpaque(false);
			metaclassTextField.setForeground(new Color(40, 40, 40));
			metaclassTextField.setEditable(false);
			metaclassTextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
			metaclassTextField.setBounds(134, 130, 514, 25);
			mainPanel.add(metaclassTextField);
			metaclassTextField.setColumns(10);
			
			JButton excelButton = new JButton("Read data from Excel");
			excelButton.setEnabled(false);
			excelButton.setForeground(new Color(240, 240, 240));
			excelButton.setBorder(null);
			excelButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			excelButton.setBackground(new Color(54, 146, 248));
			excelButton.setBounds(460, 10, 188, 25);
			mainPanel.add(excelButton);
			
			startingComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					try {
						SetUpComboBox.settingUpCB(startingComboBox, setUpButton);
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			JLabel ifPluginImageLabel = new JLabel("");
			ifPluginImageLabel.setBounds(689, 63, 93, 23);
			ifPluginImageLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
			BufferedImage bannerText = null;
			try {
				bannerText = ImageIO.read(GUI.class.getResource("/com/rhapsody/gui/IFplugin_bannertext.png"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Image bannerTextIm = bannerText.getScaledInstance(93, 23, Image.SCALE_DEFAULT);
			ImageIcon u = new ImageIcon(bannerTextIm);
			ifPluginImageLabel.setIcon(u);
			rightPanel.add(ifPluginImageLabel);
			
			JLabel arrowsImageLabel = new JLabel("New label");
			arrowsImageLabel.setBounds(716, 96, 38, 39);
			BufferedImage bt = null;
			try {
				bt = ImageIO.read(GUI.class.getResource("/com/rhapsody/gui/IFplugin_icon.png"));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			Image bti = bt.getScaledInstance(38, 39, Image.SCALE_DEFAULT);
			ImageIcon ii = new ImageIcon(bti);
			arrowsImageLabel.setIcon(ii);
			
			rightPanel.add(arrowsImageLabel);
			
			JPanel tagPanel = new JPanel();
			tagPanel.setLayout(null);
			tagPanel.setBorder(null);
			tagPanel.setBackground(new Color(240, 240, 240));
			tagPanel.setBounds(10, 324, 792, 506);
			rightPanel.add(tagPanel);
			
			JLabel tagLabel = new JLabel("    Tags");
			tagLabel.setBackground(new Color(255, 255, 255));
			tagLabel.setForeground(new Color(0, 0, 0));
			tagLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
			tagLabel.setBounds(0, 8, 97, 20);
			tagPanel.add(tagLabel);
			
			keyTagTextField1 = new JTextField();
			keyTagTextField1.setBackground(new Color(255, 255, 255));
			keyTagTextField1.setForeground(new Color(0, 0, 0));
			keyTagTextField1.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField1.setEditable(false);
			keyTagTextField1.setVisible(false);
			keyTagTextField1.setColumns(10);
			keyTagTextField1.setBounds(96, 36, 252, 20);
			tagPanel.add(keyTagTextField1);
			
			keyTagTextField3 = new JTextField();
			keyTagTextField3.setBackground(new Color(255, 255, 255));
			keyTagTextField3.setForeground(new Color(0, 0, 0));
			keyTagTextField3.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField3.setEditable(false);
			keyTagTextField3.setVisible(false);
			keyTagTextField3.setColumns(10);
			keyTagTextField3.setBounds(96, 86, 252, 20);
			tagPanel.add(keyTagTextField3);
			
			keyTagTextField5 = new JTextField();
			keyTagTextField5.setBackground(new Color(255, 255, 255));
			keyTagTextField5.setForeground(new Color(0, 0, 0));
			keyTagTextField5.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField5.setEditable(false);
			keyTagTextField5.setVisible(false);
			keyTagTextField5.setColumns(10);
			keyTagTextField5.setBounds(96, 136, 252, 20);
			tagPanel.add(keyTagTextField5);
			
			keyTagTextField7 = new JTextField();
			keyTagTextField7.setBackground(new Color(255, 255, 255));
			keyTagTextField7.setForeground(new Color(0, 0, 0));
			keyTagTextField7.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField7.setEditable(false);
			keyTagTextField7.setVisible(false);
			keyTagTextField7.setColumns(10);
			keyTagTextField7.setBounds(96, 186, 252, 20);
			tagPanel.add(keyTagTextField7);
			
			keyTagTextField9 = new JTextField();
			keyTagTextField9.setBackground(new Color(255, 255, 255));
			keyTagTextField9.setForeground(new Color(0, 0, 0));
			keyTagTextField9.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField9.setEditable(false);
			keyTagTextField9.setVisible(false);
			keyTagTextField9.setColumns(10);
			keyTagTextField9.setBounds(96, 236, 252, 20);
			tagPanel.add(keyTagTextField9);
			
			JLabel keyTagLabel1 = new JLabel("Name:");
			keyTagLabel1.setVisible(false);
			keyTagLabel1.setBackground(new Color(240, 240, 240));
			keyTagLabel1.setForeground(new Color(0, 0, 0));
			keyTagLabel1.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel1.setBounds(40, 39, 39, 14);
			tagPanel.add(keyTagLabel1);
			
			JLabel valueTagLabel1 = new JLabel("Value:");
			valueTagLabel1.setVisible(false);
			valueTagLabel1.setBackground(new Color(240, 240, 240));
			valueTagLabel1.setForeground(new Color(0, 0, 0));
			valueTagLabel1.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel1.setBounds(40, 60, 39, 14);
			tagPanel.add(valueTagLabel1);
			
			JLabel keyTagLabel3 = new JLabel("Name:");
			keyTagLabel3.setVisible(false);
			keyTagLabel3.setBackground(new Color(240, 240, 240));
			keyTagLabel3.setForeground(new Color(0, 0, 0));
			keyTagLabel3.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel3.setBounds(40, 89, 39, 14);
			tagPanel.add(keyTagLabel3);
			
			JLabel valueTagLabel3 = new JLabel("Value:");
			valueTagLabel3.setVisible(false);
			valueTagLabel3.setBackground(new Color(240, 240, 240));
			valueTagLabel3.setForeground(new Color(0, 0, 0));
			valueTagLabel3.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel3.setBounds(40, 109, 39, 14);
			tagPanel.add(valueTagLabel3);
			
			JLabel keyTagLabel9 = new JLabel("Name:");
			keyTagLabel9.setVisible(false);
			keyTagLabel9.setBackground(new Color(240, 240, 240));
			keyTagLabel9.setForeground(new Color(0, 0, 0));
			keyTagLabel9.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel9.setBounds(40, 239, 39, 14);
			tagPanel.add(keyTagLabel9);
			
			JLabel keyTagLabel5 = new JLabel("Name:");
			keyTagLabel5.setVisible(false);
			keyTagLabel5.setBackground(new Color(240, 240, 240));
			keyTagLabel5.setForeground(new Color(0, 0, 0));
			keyTagLabel5.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel5.setBounds(40, 139, 39, 14);
			tagPanel.add(keyTagLabel5);
			
			JLabel valueTagLabel5 = new JLabel("Value:");
			valueTagLabel5.setVisible(false);
			valueTagLabel5.setBackground(new Color(240, 240, 240));
			valueTagLabel5.setForeground(new Color(0, 0, 0));
			valueTagLabel5.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel5.setBounds(40, 160, 39, 14);
			tagPanel.add(valueTagLabel5);
			
			JLabel keyTagLabel7 = new JLabel("Name:");
			keyTagLabel7.setVisible(false);
			keyTagLabel7.setBackground(new Color(240, 240, 240));
			keyTagLabel7.setForeground(new Color(0, 0, 0));
			keyTagLabel7.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel7.setBounds(40, 189, 39, 14);
			tagPanel.add(keyTagLabel7);
			
			JLabel valueTagLabel7 = new JLabel("Value:");
			valueTagLabel7.setVisible(false);
			valueTagLabel7.setBackground(new Color(240, 240, 240));
			valueTagLabel7.setForeground(new Color(0, 0, 0));
			valueTagLabel7.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel7.setBounds(40, 210, 39, 14);
			tagPanel.add(valueTagLabel7);
			
			JComboBox valueTagComboBox1 = new JComboBox();
			valueTagComboBox1.setVisible(false);
			valueTagComboBox1.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox1.setForeground(new Color(0, 0, 0));
			valueTagComboBox1.setBackground(new Color(255, 255, 255));
			valueTagComboBox1.setBounds(96, 56, 271, 22);
			tagPanel.add(valueTagComboBox1);
			
			JComboBox valueTagComboBox3 = new JComboBox();
			valueTagComboBox3.setVisible(false);
			valueTagComboBox3.setBackground(new Color(255, 255, 255));
			valueTagComboBox3.setForeground(new Color(0, 0, 0));
			valueTagComboBox3.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox3.setBounds(96, 106, 271, 22);
			tagPanel.add(valueTagComboBox3);
			
			JComboBox valueTagComboBox5 = new JComboBox();
			valueTagComboBox5.setVisible(false);
			valueTagComboBox5.setForeground(new Color(0, 0, 0));
			valueTagComboBox5.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox5.setBackground(new Color(255, 255, 255));
			valueTagComboBox5.setBounds(96, 156, 271, 22);
			tagPanel.add(valueTagComboBox5);
			
			JComboBox valueTagComboBox7 = new JComboBox();
			valueTagComboBox7.setVisible(false);
			valueTagComboBox7.setForeground(new Color(0, 0, 0));
			valueTagComboBox7.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox7.setBackground(new Color(255, 255, 255));
			valueTagComboBox7.setBounds(96, 206, 271, 22);
			tagPanel.add(valueTagComboBox7);
			
			JComboBox valueTagComboBox9 = new JComboBox();
			valueTagComboBox9.setVisible(false);
			valueTagComboBox9.setForeground(new Color(0, 0, 0));
			valueTagComboBox9.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox9.setBackground(new Color(255, 255, 255));
			valueTagComboBox9.setBounds(96, 256, 271, 22);
			tagPanel.add(valueTagComboBox9);
			
			JLabel valueTagLabel9 = new JLabel("Value:");
			valueTagLabel9.setVisible(false);
			valueTagLabel9.setBackground(new Color(240, 240, 240));
			valueTagLabel9.setForeground(new Color(0, 0, 0));
			valueTagLabel9.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel9.setBounds(40, 260, 39, 14);
			tagPanel.add(valueTagLabel9);
			
			JLabel keyTagLabel2 = new JLabel("Name:");
			keyTagLabel2.setVisible(false);
			keyTagLabel2.setBackground(new Color(240, 240, 240));
			keyTagLabel2.setForeground(new Color(0, 0, 0));
			keyTagLabel2.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel2.setBounds(455, 39, 39, 14);
			tagPanel.add(keyTagLabel2);
			
			keyTagTextField2 = new JTextField();
			keyTagTextField2.setBackground(new Color(255, 255, 255));
			keyTagTextField2.setForeground(new Color(0, 0, 0));
			keyTagTextField2.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField2.setEditable(false);
			keyTagTextField2.setVisible(false);
			keyTagTextField2.setColumns(10);
			keyTagTextField2.setBounds(511, 36, 252, 20);
			tagPanel.add(keyTagTextField2);
			
			JLabel valueTagLabel2 = new JLabel("Value:");
			valueTagLabel2.setVisible(false);
			valueTagLabel2.setBackground(new Color(240, 240, 240));
			valueTagLabel2.setForeground(new Color(0, 0, 0));
			valueTagLabel2.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel2.setBounds(455, 60, 39, 14);
			tagPanel.add(valueTagLabel2);
			
			JComboBox valueTagComboBox2 = new JComboBox();
			valueTagComboBox2.setVisible(false);
			valueTagComboBox2.setForeground(new Color(0, 0, 0));
			valueTagComboBox2.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox2.setBackground(new Color(255, 255, 255));
			valueTagComboBox2.setBounds(511, 56, 271, 22);
			tagPanel.add(valueTagComboBox2);
			
			JLabel keyTagLabel4 = new JLabel("Name:");
			keyTagLabel4.setVisible(false);
			keyTagLabel4.setBackground(new Color(240, 240, 240));
			keyTagLabel4.setForeground(new Color(0, 0, 0));
			keyTagLabel4.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel4.setBounds(455, 89, 39, 14);
			tagPanel.add(keyTagLabel4);
			
			keyTagTextField4 = new JTextField();
			keyTagTextField4.setBackground(new Color(255, 255, 255));
			keyTagTextField4.setForeground(new Color(0, 0, 0));
			keyTagTextField4.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField4.setEditable(false);
			keyTagTextField4.setVisible(false);
			keyTagTextField4.setColumns(10);
			keyTagTextField4.setBounds(511, 86, 252, 20);
			tagPanel.add(keyTagTextField4);
			
			JLabel valueTagLabel4 = new JLabel("Value:");
			valueTagLabel4.setVisible(false);
			valueTagLabel4.setBackground(new Color(240, 240, 240));
			valueTagLabel4.setForeground(new Color(0, 0, 0));
			valueTagLabel4.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel4.setBounds(455, 110, 39, 14);
			tagPanel.add(valueTagLabel4);
			
			JComboBox valueTagComboBox4 = new JComboBox();
			valueTagComboBox4.setVisible(false);
			valueTagComboBox4.setForeground(new Color(0, 0, 0));
			valueTagComboBox4.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox4.setBackground(new Color(255, 255, 255));
			valueTagComboBox4.setBounds(511, 106, 271, 22);
			tagPanel.add(valueTagComboBox4);
			
			JLabel keyTagLabel6 = new JLabel("Name:");
			keyTagLabel6.setVisible(false);
			keyTagLabel6.setBackground(new Color(240, 240, 240));
			keyTagLabel6.setForeground(new Color(0, 0, 0));
			keyTagLabel6.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel6.setBounds(455, 139, 39, 14);
			tagPanel.add(keyTagLabel6);
			
			keyTagTextField6 = new JTextField();
			keyTagTextField6.setBackground(new Color(255, 255, 255));
			keyTagTextField6.setForeground(new Color(0, 0, 0));
			keyTagTextField6.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField6.setEditable(false);
			keyTagTextField6.setVisible(false);
			keyTagTextField6.setColumns(10);
			keyTagTextField6.setBounds(511, 136, 252, 20);
			tagPanel.add(keyTagTextField6);
			
			JLabel valueTagLabel6 = new JLabel("Value:");
			valueTagLabel6.setVisible(false);
			valueTagLabel6.setBackground(new Color(240, 240, 240));
			valueTagLabel6.setForeground(new Color(0, 0, 0));
			valueTagLabel6.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel6.setBounds(455, 160, 39, 14);
			tagPanel.add(valueTagLabel6);
			
			JComboBox valueTagComboBox6 = new JComboBox();
			valueTagComboBox6.setVisible(false);
			valueTagComboBox6.setForeground(new Color(0, 0, 0));
			valueTagComboBox6.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox6.setBackground(new Color(255, 255, 255));
			valueTagComboBox6.setBounds(511, 156, 271, 22);
			tagPanel.add(valueTagComboBox6);
			
			JLabel keyTagLabel8 = new JLabel("Name:");
			keyTagLabel8.setVisible(false);
			keyTagLabel8.setBackground(new Color(240, 240, 240));
			keyTagLabel8.setForeground(new Color(0, 0, 0));
			keyTagLabel8.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel8.setBounds(455, 189, 39, 14);
			tagPanel.add(keyTagLabel8);
			
			keyTagTextField8 = new JTextField();
			keyTagTextField8.setBackground(new Color(255, 255, 255));
			keyTagTextField8.setForeground(new Color(0, 0, 0));
			keyTagTextField8.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField8.setEditable(false);
			keyTagTextField8.setVisible(false);
			keyTagTextField8.setColumns(10);
			keyTagTextField8.setBounds(511, 186, 252, 20);
			tagPanel.add(keyTagTextField8);
			
			JLabel valueTagLabel8 = new JLabel("Value:");
			valueTagLabel8.setVisible(false);
			valueTagLabel8.setBackground(new Color(240, 240, 240));
			valueTagLabel8.setForeground(new Color(0, 0, 0));
			valueTagLabel8.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel8.setBounds(455, 210, 39, 14);
			tagPanel.add(valueTagLabel8);
			
			JComboBox valueTagComboBox8 = new JComboBox();
			valueTagComboBox8.setVisible(false);
			valueTagComboBox8.setForeground(new Color(0, 0, 0));
			valueTagComboBox8.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox8.setBackground(new Color(255, 255, 255));
			valueTagComboBox8.setBounds(511, 206, 271, 22);
			tagPanel.add(valueTagComboBox8);
			
			JLabel keyTagLabel10 = new JLabel("Name:");
			keyTagLabel10.setVisible(false);
			keyTagLabel10.setBackground(new Color(240, 240, 240));
			keyTagLabel10.setForeground(new Color(0, 0, 0));
			keyTagLabel10.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagLabel10.setBounds(455, 239, 39, 14);
			tagPanel.add(keyTagLabel10);
			
			keyTagTextField10 = new JTextField();
			keyTagTextField10.setBackground(new Color(255, 255, 255));
			keyTagTextField10.setForeground(new Color(0, 0, 0));
			keyTagTextField10.setFont(new Font("Tahoma", Font.PLAIN, 12));
			keyTagTextField10.setEditable(false);
			keyTagTextField10.setVisible(false);
			keyTagTextField10.setColumns(10);
			keyTagTextField10.setBounds(511, 236, 252, 20);
			tagPanel.add(keyTagTextField10);
			
			JLabel valueTagLabel10 = new JLabel("Value:");
			valueTagLabel10.setVisible(false);
			valueTagLabel10.setBackground(new Color(240, 240, 240));
			valueTagLabel10.setForeground(new Color(0, 0, 0));
			valueTagLabel10.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagLabel10.setBounds(455, 260, 39, 14);
			tagPanel.add(valueTagLabel10);
			
			JComboBox valueTagComboBox10 = new JComboBox();
			valueTagComboBox10.setVisible(false);
			valueTagComboBox10.setForeground(new Color(0, 0, 0));
			valueTagComboBox10.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagComboBox10.setBackground(new Color(255, 255, 255));
			valueTagComboBox10.setBounds(511, 256, 271, 22);
			tagPanel.add(valueTagComboBox10);
			
			valueTagTextField1 = new JTextField();
			valueTagTextField1.setBackground(new Color(255, 255, 255));
			valueTagTextField1.setForeground(new Color(0, 0, 0));
			valueTagTextField1.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField1.setVisible(false);
			valueTagTextField1.setBounds(96, 58, 252, 20);
			tagPanel.add(valueTagTextField1);
			valueTagTextField1.setColumns(10);
			
			valueTagTextField3 = new JTextField();
			valueTagTextField3.setBackground(new Color(255, 255, 255));
			valueTagTextField3.setForeground(new Color(0, 0, 0));
			valueTagTextField3.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField3.setVisible(false);
			valueTagTextField3.setBounds(96, 107, 252, 20);
			tagPanel.add(valueTagTextField3);
			valueTagTextField3.setColumns(10);
			
			valueTagTextField5 = new JTextField();
			valueTagTextField5.setBackground(new Color(255, 255, 255));
			valueTagTextField5.setForeground(new Color(0, 0, 0));
			valueTagTextField5.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField5.setVisible(false);
			valueTagTextField5.setBounds(96, 158, 252, 20);
			tagPanel.add(valueTagTextField5);
			valueTagTextField5.setColumns(10);
			
			valueTagTextField7 = new JTextField();
			valueTagTextField7.setBackground(new Color(255, 255, 255));
			valueTagTextField7.setForeground(new Color(0, 0, 0));
			valueTagTextField7.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField7.setVisible(false);
			valueTagTextField7.setBounds(96, 208, 252, 20);
			tagPanel.add(valueTagTextField7);
			valueTagTextField7.setColumns(10);
			
			valueTagTextField9 = new JTextField();
			valueTagTextField9.setBackground(new Color(255, 255, 255));
			valueTagTextField9.setForeground(new Color(0, 0, 0));
			valueTagTextField9.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField9.setVisible(false);
			valueTagTextField9.setBounds(96, 259, 252, 20);
			tagPanel.add(valueTagTextField9);
			valueTagTextField9.setColumns(10);
			
			valueTagTextField2 = new JTextField();
			valueTagTextField2.setBackground(new Color(255, 255, 255));
			valueTagTextField2.setForeground(new Color(0, 0, 0));
			valueTagTextField2.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField2.setVisible(false);
			valueTagTextField2.setBounds(511, 58, 252, 20);
			tagPanel.add(valueTagTextField2);
			valueTagTextField2.setColumns(10);
			
			valueTagTextField4 = new JTextField();
			valueTagTextField4.setBackground(new Color(255, 255, 255));
			valueTagTextField4.setForeground(new Color(0, 0, 0));
			valueTagTextField4.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField4.setVisible(false);
			valueTagTextField4.setBounds(511, 107, 252, 20);
			tagPanel.add(valueTagTextField4);
			valueTagTextField4.setColumns(10);
			
			valueTagTextField6 = new JTextField();
			valueTagTextField6.setBackground(new Color(255, 255, 255));
			valueTagTextField6.setForeground(new Color(0, 0, 0));
			valueTagTextField6.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField6.setVisible(false);
			valueTagTextField6.setBounds(511, 158, 252, 20);
			tagPanel.add(valueTagTextField6);
			valueTagTextField6.setColumns(10);
			
			valueTagTextField8 = new JTextField();
			valueTagTextField8.setBackground(new Color(255, 255, 255));
			valueTagTextField8.setForeground(new Color(0, 0, 0));
			valueTagTextField8.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField8.setVisible(false);
			valueTagTextField8.setBounds(511, 208, 252, 20);
			tagPanel.add(valueTagTextField8);
			valueTagTextField8.setColumns(10);
			
			valueTagTextField10 = new JTextField();
			valueTagTextField10.setBackground(new Color(255, 255, 255));
			valueTagTextField10.setForeground(new Color(0, 0, 0));
			valueTagTextField10.setFont(new Font("Tahoma", Font.PLAIN, 12));
			valueTagTextField10.setVisible(false);
			valueTagTextField10.setBounds(511, 259, 252, 20);
			tagPanel.add(valueTagTextField10);
			valueTagTextField10.setColumns(10);
			
			JButton updateTagsButton = new JButton("Update Tags");
			updateTagsButton.setEnabled(false);
			updateTagsButton.setBorder(null);
			updateTagsButton.setForeground(new Color(240, 240, 240));
			updateTagsButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			updateTagsButton.setBackground(new Color(54, 146, 248));
			updateTagsButton.setBounds(671, 289, 111, 23);
			tagPanel.add(updateTagsButton);
			
			JLabel descriptionLabel = new JLabel("Description");
			descriptionLabel.setVisible(false);
			descriptionLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
			descriptionLabel.setBounds(10, 323, 83, 20);
			tagPanel.add(descriptionLabel);
			
			JButton descriptionButton = new JButton("Update description");
			descriptionButton.setVisible(false);
			descriptionButton.setBorder(null);
			descriptionButton.setBackground(new Color(54, 146, 248));
			descriptionButton.setForeground(new Color(245, 245, 245));
			descriptionButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
			descriptionButton.setBounds(629, 476, 153, 23);
			tagPanel.add(descriptionButton);
			
			JScrollPane descriptionScrollPane = new JScrollPane();
			descriptionScrollPane.setBorder(null);
			descriptionScrollPane.setBounds(10, 348, 772, 124);
			tagPanel.add(descriptionScrollPane);
			descriptionTextPane = new JTextPane();
			descriptionTextPane.setVisible(false);
			descriptionScrollPane.setViewportView(descriptionTextPane);
			
			// Action associated to the JButton click
			setUpButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					try {
						LoadingDataFromRhapsody.load(setUpButton);
						excelButton.setEnabled(true);
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			tree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					try {
						// Left click
						if (e.getButton() == 1) {
							// Get element clicked; if no element is clicked, tp is null
							tp = tree.getPathForLocation(e.getX(), e.getY());
							
							OperationsOnJTree.treeEventsHandler(keyTagLabel1, keyTagLabel2, keyTagLabel3, keyTagLabel4, keyTagLabel5, 
									 keyTagLabel6, keyTagLabel7, keyTagLabel8, keyTagLabel9, keyTagLabel10, 
									 valueTagLabel1, valueTagLabel2, valueTagLabel3, valueTagLabel4, valueTagLabel5, 
									 valueTagLabel6, valueTagLabel7, valueTagLabel8, valueTagLabel9, valueTagLabel10,
									 valueTagComboBox1, valueTagComboBox2, valueTagComboBox3, valueTagComboBox4, 
									 valueTagComboBox5, valueTagComboBox6, valueTagComboBox7, valueTagComboBox8,
									 valueTagComboBox9, valueTagComboBox10, updateTagsButton,
									 addButton, modifyButton, deleteButton, updateTypeButton,
									 descriptionLabel, descriptionButton);
						}
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			
			// Handling the movement with keyboard arrows
			// Unfortunatelly, after it moves on tree, it will return the previous element.
			// So it is necessary to manage this behaviour: when the tree detects a KeyEvent,
			// it toggles a variable to true; this will generate a valueChanged event that will
			// be captured from the TreeSelectionListener that will perform the action only after
			// it has moved to the next element determined by arrows movement
			tree.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					
					try {
						if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
							
							activateSelectionListener = true;
							
						}
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					// TODO Auto-generated method stub
					
					try {
					
						if(activateSelectionListener) {
							tp = e.getPath();
							
							OperationsOnJTree.treeEventsHandler(keyTagLabel1, keyTagLabel2, keyTagLabel3, keyTagLabel4, keyTagLabel5, 
									 keyTagLabel6, keyTagLabel7, keyTagLabel8, keyTagLabel9, keyTagLabel10, 
									 valueTagLabel1, valueTagLabel2, valueTagLabel3, valueTagLabel4, valueTagLabel5, 
									 valueTagLabel6, valueTagLabel7, valueTagLabel8, valueTagLabel9, valueTagLabel10,
									 valueTagComboBox1, valueTagComboBox2, valueTagComboBox3, valueTagComboBox4, 
									 valueTagComboBox5, valueTagComboBox6, valueTagComboBox7, valueTagComboBox8,
									 valueTagComboBox9, valueTagComboBox10, updateTagsButton,
									 addButton,  modifyButton, deleteButton, updateTypeButton,
									 descriptionLabel, descriptionButton);
						}
						
						activateSelectionListener = false; 	// it is necessary since I don't want to execute this when clicking
															// with mouse (that also activates the treeSelectionListener)
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						TreeInsertion.insertNode();
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
	
			modifyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						TreeModification.modifyNode();
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
	
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						TreeDeletion.deleteNode();
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
	
			updateTagsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						TagManager.update(valueTagComboBox1, valueTagComboBox2, valueTagComboBox3,
								valueTagComboBox4, valueTagComboBox5, valueTagComboBox6, valueTagComboBox7,
								valueTagComboBox8, valueTagComboBox9, valueTagComboBox10);
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			excelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
						try {
							new Thread(()->{
								disablePrints = true;
								LoadingDataFromExcel.selectExcelFile();
								disablePrints = false;
							}).start();
						}catch(ArrayIndexOutOfBoundsException aioobe) {
							
						}catch(Exception excpt) {
							JOptionPane.showMessageDialog(frame, excpt.getMessage(), null, JOptionPane.ERROR_MESSAGE);
						}
					
				}
			});
			
	
			/*
			 	Updating type of an attribute
			*/
			updateTypeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						String selectedItemInComboBox = String.valueOf(typeAttributesComboBox.getSelectedItem());
						IRPAttribute attr = (IRPAttribute) ((CustomIRPModelElement) ((DefaultMutableTreeNode) GUI.tp.getLastPathComponent()).getUserObject()).getElement();
						
						rs.setNewTypeMsg(attr, selectedItemInComboBox);
						
						CustomPrint.printOnJTextPane("Attribute type updated.");
						consoleLogTextPane.setText(CustomPrint.getBufferContent());
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			
			/*
			  	Reloading information from Rhapsody
			*/
			refreshButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						CustomPrint.printOnJTextPane("I'm refreshing ...");
						consoleLogTextPane.setText(CustomPrint.getBufferContent());
						setUpButton.setEnabled(true);
						setUpButton.doClick();
						setUpButton.setEnabled(false);
						CustomPrint.printOnJTextPane("Successfully refreshed.");
						consoleLogTextPane.setText(CustomPrint.getBufferContent());
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			
			/*
				Setting description for attributes
			*/
			descriptionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						rs.setDescription(descriptionTextPane.getText());
						CustomPrint.printOnJTextPane("Description successfully updated.");
						consoleLogTextPane.setText(CustomPrint.getBufferContent());
					}catch(RhapsodyRuntimeException rre) {
						isRhapsodyOpened();
					}
				}
			});
			
			
		}catch(Exception e) {
			isRhapsodyOpened();
		}
	}
}

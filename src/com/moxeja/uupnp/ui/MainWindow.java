/*
 * Copyright (C) 2019  Moxeja
 * MainWindow.java is part of UniversalUPnP
 *
 * UniversalUPnP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.moxeja.uupnp.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.moxeja.uupnp.FileLocations;
import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.Main;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;
import com.moxeja.uupnp.network.NetworkUtils;

public class MainWindow {

	private JFrame frmUniversalupnp;
	private JTable table;
	private DefaultTableModel tablemodel;

	/**
	 * Create the application.
	 */
	public MainWindow() {
		Main.LOGGER.log(LogSeverity.INFO, "Initialising window.");
		initialize();
		frmUniversalupnp.setVisible(true);
		
		// Start updater thread
		Thread updater = new Thread(() -> {
			Main.LOGGER.log(LogSeverity.INFO, "Checking for updates.");
			boolean update = NetworkUtils.needsUpdate(Main.VERSION);
			Main.LOGGER.log(LogSeverity.INFO, "Update available: "+update);
			
			if (update) {
				String url = "https://github.com/Moxeja/UniversalUPnP/releases";
				
				// Allow copying link to clipboard
				JButton copyToClipboard = new JButton("Copy URL to Clipboard");
				copyToClipboard.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						StringSelection selection = new StringSelection(url);
						clipboard.setContents(selection, null);
					}
				});
				
				Object[] message = {
						"An update is available at: "+url,
						copyToClipboard
				};
				
				JOptionPane.showMessageDialog(frmUniversalupnp, message, "Update Found!", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		updater.start();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {
		frmUniversalupnp = new JFrame();
		frmUniversalupnp.setVisible(true);
		frmUniversalupnp.setSize(new Dimension(680, 400));
		frmUniversalupnp.setResizable(false);
		frmUniversalupnp.setTitle("UniversalUPnP V"+Main.VERSION);
		frmUniversalupnp.setBounds(100, 100, 680, 400);
		frmUniversalupnp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmUniversalupnp.getContentPane().setLayout(null);
		
		// Centre window
		Dimension screen = frmUniversalupnp.getToolkit().getScreenSize();
		frmUniversalupnp.setLocation(screen.width/2-frmUniversalupnp.getWidth()/2, 
				screen.height/2-frmUniversalupnp.getHeight()/2);
		
		JButton btnDeleteMapping = new JButton("Delete Mapping");
		btnDeleteMapping.setToolTipText("Delete the selected UPnP mapping.");
		btnDeleteMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnDeleteMappingClicked();
			}
		});
		btnDeleteMapping.setBounds(515, 109, 149, 40);
		frmUniversalupnp.getContentPane().add(btnDeleteMapping);
		
		JButton btnNewMapping = new JButton("New Mapping");
		btnNewMapping.setToolTipText("Create a new UPnP mapping.");
		btnNewMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddMappingClicked();
			}
		});
		btnNewMapping.setBounds(515, 7, 149, 40);
		frmUniversalupnp.getContentPane().add(btnNewMapping);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 7, 495, 353);
		frmUniversalupnp.getContentPane().add(scrollPane);
		
		String col[] = {"Name", "Ports", "Protocol", "Running"};
		tablemodel = new NoCellEditableModel(col, 0);
		
		table = new JTable(tablemodel);
		table.setFont(new Font("Tahoma", Font.PLAIN, 14));
		table.setBackground(UIManager.getColor("Table.background"));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getColumnModel().getColumn(1).setCellRenderer(new MultiLineCellRenderer());
		table.getColumnModel().getColumn(2).setCellRenderer(new MultiLineCellRenderer());
		table.getTableHeader().setBackground(Color.lightGray);
		table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 16));
		scrollPane.setViewportView(table);
		
		JButton btnStopMapping = new JButton("Stop Mapping");
		btnStopMapping.setToolTipText("Stops the selected UPnP service (closes the port).");
		btnStopMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStopMappingClicked();
			}
		});
		btnStopMapping.setBounds(515, 224, 149, 40);
		frmUniversalupnp.getContentPane().add(btnStopMapping);
		
		JButton btnStartMapping = new JButton("Start Mapping");
		btnStartMapping.setToolTipText("Start the selected UPnP service (will open the port).");
		btnStartMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnStartMappingClicked();
			}
		});
		btnStartMapping.setBounds(515, 173, 149, 40);
		frmUniversalupnp.getContentPane().add(btnStartMapping);
		
		JButton btnShowIP = new JButton("Show External IP");
		btnShowIP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnShowIPClicked();
			}
		});
		btnShowIP.setToolTipText("Shows your external IP address.");
		btnShowIP.setBounds(515, 320, 149, 40);
		frmUniversalupnp.getContentPane().add(btnShowIP);
		
		JButton btnEditMapping = new JButton("Edit Mapping");
		btnEditMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnEditMappingClicked();
			}
		});
		btnEditMapping.setToolTipText("Edit the selected entry.");
		btnEditMapping.setBounds(515, 58, 149, 40);
		frmUniversalupnp.getContentPane().add(btnEditMapping);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(515, 160, 149, 2);
		frmUniversalupnp.getContentPane().add(separator_2);
		
		// Right-click context menu
		JPopupMenu popupMenu = new JPopupMenu();
		scrollPane.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
				
				int row = table.rowAtPoint(e.getPoint());
				if (row >= 0 && row < table.getRowCount()) {
					table.setRowSelectionInterval(row, row);
				} else {
					table.clearSelection();
				}
			}
			private void showMenu(MouseEvent e) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		JMenuItem mntmAddMapping = new JMenuItem("Add Mapping");
		mntmAddMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnAddMappingClicked();
			}
		});
		popupMenu.add(mntmAddMapping);
		
		JMenuItem mntmEditMapping = new JMenuItem("Edit Mapping");
		mntmEditMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnEditMappingClicked();
			}
		});
		popupMenu.add(mntmEditMapping);
		
		JMenuItem mntmDeleteMapping = new JMenuItem("Delete Mapping");
		mntmDeleteMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnDeleteMappingClicked();
			}
		});
		popupMenu.add(mntmDeleteMapping);
		
		JSeparator separator = new JSeparator();
		popupMenu.add(separator);
		
		JMenuItem mntmStartMapping = new JMenuItem("Start Mapping");
		mntmStartMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnStartMappingClicked();
			}
		});
		popupMenu.add(mntmStartMapping);
		
		JMenuItem mntmStopMapping = new JMenuItem("Stop Mapping");
		mntmStopMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnStopMappingClicked();
			}
		});
		popupMenu.add(mntmStopMapping);
		
		showStartupWarning();
		
		// Show data in table
		refreshTable();
		Main.LOGGER.log(LogSeverity.INFO, "Window initialisation complete.");
	}
	
	private void showStartupWarning() {
		Main.LOGGER.log(LogSeverity.INFO, "Checking for warning disable file.");
		if (FileLocations.warningDisabled()) {
			Main.LOGGER.log(LogSeverity.INFO, "Startup warning disabled.");
			return;
		}
		
		// Use JLabel html capabilities
		String license = "<html><nobr>UniversalUPnP  Copyright (C) 2019  Moxeja"
				+ "<br>UniversalUPnP comes with <u>ABSOLUTELY NO WARRANTY</u>."
				+ "<br>UniversalUPnP is free software, and you are welcome to redistribute it"
				+ "<br>under certain conditions. For more information, view the LICENSE file"
				+ "<br>that came with the binary file.</nobr></html>";
		JLabel lblLicense = new JLabel(license);
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		
		String warning = "<html><nobr><u>WARNING</u>: You <u>must</u> make sure to stop the UPnP mapping once you are finished with it!"
				+ "<br>Leaving ports open can be a big <u>security risk</u>!"
				+ "<br>Closing the program will also close all ports opened by it this runtime.</nobr></html>";
		JLabel lblWarning = new JLabel(warning);
		JCheckBox chkDisable = new JCheckBox("Disable Warning");
		
		Object[] message = {
				lblLicense, separator, lblWarning, chkDisable
		};
		
		// Show warning to user
		Main.LOGGER.log(LogSeverity.INFO, "Showing warning to user.");
		JOptionPane.showMessageDialog(frmUniversalupnp, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
		
		if (chkDisable.isSelected()) {
			FileLocations.createWarningFile();
		}
	}
	
	private void refreshTable() {
		while (tablemodel.getRowCount() > 0)
			tablemodel.removeRow(0);
		
		// Re-add entries to table
		for (MappingEntry entry : Main.DATA.getEntryList()) {			
			if (entry.getPorts() != null) {
				StringBuilder sbPorts = new StringBuilder();
				StringBuilder sbProtocols = new StringBuilder();
				
				for (PortInfo port : entry.getPorts()) {
					if (port.portRange.x == port.portRange.y)
						sbPorts.append(Integer.toString(port.portRange.x)+'\n');
					else
						sbPorts.append(port.portRange.x+"->"+port.portRange.y+'\n');
					
					sbProtocols.append(port.protocol.toString()+'\n');
				}
				
				Object[] temp = {entry.getName(), sbPorts.toString(), sbProtocols.toString(), entry.isRunning()};
				tablemodel.addRow(temp);
			}
		}
		Main.LOGGER.log(LogSeverity.INFO, "Refreshed table.");
	}
	
	private void btnStartMappingClicked() {
		// Check if any row is selected
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		try {
			Main.DATA.startEntry(selectedIndex, frmUniversalupnp);
			refreshTable();
		} catch (ArrayIndexOutOfBoundsException e) {
			Main.LOGGER.log(LogSeverity.ERROR, "Cannot start entry! Invalid index specified: " + selectedIndex);
		}
	}
	
	private void btnStopMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		try {
			Main.DATA.stopEntry(selectedIndex);
			refreshTable();
		} catch (ArrayIndexOutOfBoundsException e) {}
	}
	
	private void btnAddMappingClicked() {
		MappingInputForm inputForm = new MappingInputForm(frmUniversalupnp);
		inputForm.setVisible(true);
		
		if (inputForm.okClose) {
			Main.DATA.addEntry(new MappingEntry(inputForm.name, inputForm.ports));
			refreshTable();
		}
		inputForm.dispose();
	}
	
	private void btnEditMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		try {
			MappingEntry entry = Main.DATA.getEntry(selectedIndex);
			MappingInputForm inputForm = new MappingInputForm(frmUniversalupnp, entry);
			inputForm.setVisible(true);
			
			if (inputForm.okClose) {
				Main.DATA.deleteEntry(selectedIndex);
				Main.DATA.addEntry(new MappingEntry(inputForm.name, inputForm.ports));
				Main.LOGGER.log(LogSeverity.INFO, "Edit finished.");
				refreshTable();
			}
			inputForm.dispose();
		} catch (ArrayIndexOutOfBoundsException e) {
			Main.LOGGER.log(LogSeverity.ERROR, "Failed to edit entry.");
		}
	}
	
	private void btnDeleteMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		// User confirmation
		int option = JOptionPane.showConfirmDialog(frmUniversalupnp, "Are you sure you want to delete an entry?", 
				"Delete Entry", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			try {
				Main.DATA.deleteEntry(selectedIndex);
				refreshTable();
			} catch (ArrayIndexOutOfBoundsException e) {
				Main.LOGGER.log(LogSeverity.ERROR, "Failed to delete entry.");
			}
		}
	}
	
	private void btnShowIPClicked() {
		String externalIP = NetworkUtils.getExternalIPAddress();
		
		// Allow copying IP address to clipboard
		JButton copyToClipboard = new JButton("Copy IP to Clipboard");
		copyToClipboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection selection = new StringSelection(externalIP);
				clipboard.setContents(selection, null);
				Main.LOGGER.log(LogSeverity.INFO, "Copied external IP address to clipboard.");
			}
		});
		
		Object[] message = {
				"External IP:    "+externalIP,
				copyToClipboard
		};
		
		Main.LOGGER.log(LogSeverity.INFO, "Showing external IP address.");
		JOptionPane.showMessageDialog(frmUniversalupnp, message, "External IP", JOptionPane.INFORMATION_MESSAGE);
	}
}

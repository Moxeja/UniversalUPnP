package com.moxeja.uupnp.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.Main;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;

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
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {
		frmUniversalupnp = new JFrame();
		frmUniversalupnp.setVisible(true);
		frmUniversalupnp.setSize(new Dimension(680, 400));
		frmUniversalupnp.setResizable(false);
		frmUniversalupnp.setTitle("UniversalUPnP "+Main.VERSION);
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
		btnDeleteMapping.setBounds(515, 58, 149, 40);
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
		tablemodel = new DefaultTableModel(col, 0);
		
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
		btnStopMapping.setBounds(515, 320, 149, 40);
		frmUniversalupnp.getContentPane().add(btnStopMapping);
		
		JButton btnStartMapping = new JButton("Start Mapping");
		btnStartMapping.setToolTipText("Start the selected UPnP service (will open the port).");
		btnStartMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnStartMappingClicked();
			}
		});
		btnStartMapping.setBounds(515, 269, 149, 40);
		frmUniversalupnp.getContentPane().add(btnStartMapping);
		
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
		
		// Show data in table
		refreshTable();
		Main.LOGGER.log(LogSeverity.INFO, "Window initialisation complete.");
	}
	
	private void refreshTable() {
		while (tablemodel.getRowCount() > 0)
			tablemodel.removeRow(0);
		
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
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		Main.DATA.startEntry(selectedIndex);
		refreshTable();
	}
	
	private void btnStopMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		Main.DATA.stopEntry(selectedIndex);
		refreshTable();
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
	
	private void btnDeleteMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		// User confirmation
		int option = JOptionPane.showConfirmDialog(frmUniversalupnp, "Are you sure you want to delete an entry?", 
				"Delete Entry", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			Main.DATA.deleteEntry(selectedIndex);
			refreshTable();
		}
	}
}
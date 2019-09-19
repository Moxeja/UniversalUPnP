package com.moxeja.uupnp.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.moxeja.uupnp.FileLocations;
import com.moxeja.uupnp.Logger;
import com.moxeja.uupnp.MappingList;
import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;

public class MainWindow {

	private JFrame frmUniversalupnp;
	private JTable table;
	private DefaultTableModel tablemodel;
	
	public static Logger LOGGER;
	public static final String VERSION = "V1.3";
	private static MappingList DATA;

	private static int getJavaVersion() {
		String version = System.getProperty("java.version");
		if (version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf(".");
			if (dot != -1) { version = version.substring(0, dot); }
		}
		return Integer.parseInt(version);
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		// Setup logging
		LOGGER = new Logger(FileLocations.getLogFilename("uupnp"));
		LOGGER.log(LogSeverity.INFO, "Uses GSON, Cling, Seamless and Jetty libraries from "
				+ "https://github.com/google/gson, https://github.com/4thline/cling,"
				+ "https://github.com/4thline/seamless and https://github.com/eclipse/jetty.project respectively.");
		LOGGER.log(LogSeverity.INFO, "Running version: "+VERSION);
		
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(FileLocations.getLogFilename("uupnp-err-output")))));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			LOGGER.log(LogSeverity.WARN, "Could not redirect error stream to file.");
		}
		
		// Program cannot be run in headless mode since it is GUI based
		if (GraphicsEnvironment.isHeadless()) {
			LOGGER.log(LogSeverity.FATAL, "Application cannot be run in headless mode.");
			LOGGER.close();
			System.exit(1);
		}
		
		// Java 8 is the minimum required
		if (getJavaVersion() < 8) {
			LOGGER.log(LogSeverity.FATAL, "At least Java 8 is required to run this software.");
			LOGGER.close();
			System.exit(1);
		}
		
		// Add shutdown hook to ensure data gets written and finalised
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				DATA.stopAll();
				
				try {
					PrintWriter outwriter = new PrintWriter(new BufferedWriter(new FileWriter(FileLocations.getEntriesFilename())));
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					gson.toJson(DATA, outwriter);
					outwriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				LOGGER.log(LogSeverity.INFO, "Closing program.");
				LOGGER.close();
			}
		});
		LOGGER.log(LogSeverity.INFO, "Added shutdown hook.");
		
		LOGGER.log(LogSeverity.INFO, "Initialising window.");
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
		frmUniversalupnp.setName("frame");
		frmUniversalupnp.setTitle("UniversalUPnP "+VERSION);
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
		
		// Try to load entries from Json file
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(FileLocations.getEntriesFilename()));
			Gson gs = new Gson();
			DATA = gs.fromJson(br, MappingList.class);
		} catch (JsonSyntaxException jse) {
			LOGGER.log(LogSeverity.WARN, "Entries file was found, but was not formed correctly. Could not load data.");
			DATA = new MappingList();
		} catch (Exception e) {
			LOGGER.log(LogSeverity.WARN, "Entries file could not be found. One will be created on application exit.");
			DATA = new MappingList();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e1) {}
			}
		}
		
		// Show data in table
		refreshTable();
		LOGGER.log(LogSeverity.INFO, "Window initialisation complete.");
	}
	
	private void refreshTable() {
		while (tablemodel.getRowCount() > 0)
			tablemodel.removeRow(0);
		
		for (MappingEntry entry : DATA.getEntryList()) {			
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
		LOGGER.log(LogSeverity.INFO, "Refreshed table.");
	}
	
	private void btnStartMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		DATA.startEntry(selectedIndex);
		refreshTable();
	}
	
	private void btnStopMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		DATA.stopEntry(selectedIndex);
		refreshTable();
	}
	
	private void btnAddMappingClicked() {
		MappingInputForm inputForm = new MappingInputForm(this.frmUniversalupnp);
		inputForm.setVisible(true);
		
		if (inputForm.okClose) {
			DATA.addEntry(new MappingEntry(inputForm.name, inputForm.ports));
			refreshTable();
		}
		inputForm.dispose();
	}
	
	private void btnDeleteMappingClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		// User confirmation
		int option = JOptionPane.showConfirmDialog(this.frmUniversalupnp, "Are you sure you want to delete an entry?", 
				"Delete Entry", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			DATA.deleteEntry(selectedIndex);
			refreshTable();
		}
	}
}

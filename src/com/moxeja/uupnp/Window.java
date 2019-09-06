package com.moxeja.uupnp;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.moxeja.uupnp.Logger.LogSeverity;

public class Window {

	private JFrame frmUniversalupnp;
	private JTable table;
	private DefaultTableModel tablemodel;
	
	public static Logger LOGGER;
	public static final String VERSION = "V1.1";
	private static MappingList DATA;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		
		// Start GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					window.frmUniversalupnp.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window() {
		LOGGER.log(LogSeverity.INFO, "Initialising window.");
		initialize();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {
		frmUniversalupnp = new JFrame();
		frmUniversalupnp.setVisible(true);
		frmUniversalupnp.setSize(new Dimension(600, 400));
		frmUniversalupnp.setResizable(false);
		frmUniversalupnp.setName("frame");
		frmUniversalupnp.setTitle("UniversalUPnP "+VERSION);
		frmUniversalupnp.setBounds(100, 100, 600, 400);
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
		btnDeleteMapping.setBounds(435, 58, 149, 40);
		frmUniversalupnp.getContentPane().add(btnDeleteMapping);
		
		JButton btnNewMapping = new JButton("New Mapping");
		btnNewMapping.setToolTipText("Create a new UPnP mapping.");
		btnNewMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddMappingClicked();
			}
		});
		btnNewMapping.setBounds(435, 7, 149, 40);
		frmUniversalupnp.getContentPane().add(btnNewMapping);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 7, 415, 353);
		frmUniversalupnp.getContentPane().add(scrollPane);
		
		String col[] = {"Name", "Ports", "Protocol", "Running"};
		tablemodel = new DefaultTableModel(col, 0);
		
		table = new JTable(tablemodel);
		table.setBackground(UIManager.getColor("Table.background"));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(table);
		
		JButton btnStopMapping = new JButton("Stop Mapping");
		btnStopMapping.setToolTipText("Stops the selected UPnP service (closes the port).");
		btnStopMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStopMappingClicked();
			}
		});
		btnStopMapping.setBounds(435, 320, 149, 40);
		frmUniversalupnp.getContentPane().add(btnStopMapping);
		
		JButton btnStartMapping = new JButton("Start Mapping");
		btnStartMapping.setToolTipText("Start the selected UPnP service (will open the port).");
		btnStartMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnStartMappingClicked();
			}
		});
		btnStartMapping.setBounds(435, 269, 149, 40);
		frmUniversalupnp.getContentPane().add(btnStartMapping);
		
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
			Object[] temp = {entry.getName(), entry.getPort(), entry.getProtocol(), entry.getRunning()};
			tablemodel.addRow(temp);
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
		// Setup UI elements
		JTextField name = new JTextField();
		name.setText("Default-Name");
		
		JSpinner port = new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1));
		NumberEditor editor = new NumberEditor(port, "#");
		port.setEditor(editor);
		
		JList<Protocols> protocol = new JList<Protocols>(Protocols.values());
		protocol.setSelectedIndex(0);
		
		Object[] message = {
				"Name:", name,
				"Port:", port,
				"Protocol:", protocol
		};
		
		// Prompt user for settings
		int option = JOptionPane.showConfirmDialog(this.frmUniversalupnp, message, "Add Entry", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			if (name.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this.frmUniversalupnp, "Cannot leave the name field empty. Please try again.", 
						"Empty Fields", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			DATA.addEntry(new MappingEntry(name.getText(), protocol.getSelectedValue(), (Integer)port.getValue()));
			refreshTable();
		}
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

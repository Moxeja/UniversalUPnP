package com.moxeja.uupnp;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.ui.MainWindow;

public class Main {
	
	public static Logger LOGGER;
	public static final String VERSION = "1.7.3";
	public static MappingList DATA;

	public static void main(String[] args) {
		// Setup logging
		LOGGER = new Logger(FileLocations.getLogFilename("uupnp"));
		LOGGER.log(LogSeverity.INFO, "Uses GSON, Cling and Seamless libraries from "
				+ "https://github.com/google/gson, https://github.com/4thline/cling and "
				+ "https://github.com/4thline/seamless respectively.");
		LOGGER.log(LogSeverity.INFO, "Running version: "+VERSION);
		
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(FileLocations.getLogFilename("uupnp-err-output")))));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			LOGGER.log(LogSeverity.WARN, "Could not redirect error stream to file.");
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
		
		// Check if program is being run in CL mode
		if (args.length > 0) {
			LOGGER.log(LogSeverity.INFO, "Starting UUPnP in commandline mode.");
			new NoGUI(args);
			System.exit(0);
		} else {
			// Program cannot be run in headless mode since it is GUI based
			if (GraphicsEnvironment.isHeadless()) {
				LOGGER.log(LogSeverity.FATAL, "GUI mode cannot be started in a headless environment.");
				System.exit(1);
			}
			
			// Start GUI
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						new MainWindow();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}

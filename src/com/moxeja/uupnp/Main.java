/*
 * Copyright (C) 2019  Moxeja
 * This file is part of UniversalUPnP (https://github.com/Moxeja/UniversalUPnP)
 *
 * This program is free software: you can redistribute it and/or modify
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
import com.moxeja.uupnp.data.MappingList;
import com.moxeja.uupnp.io.FileLocations;
import com.moxeja.uupnp.ui.MainWindow;

public class Main {
	
	public static Logger LOGGER;
	public static final String VERSION = "1.8.2";
	public static MappingList DATA;

	public static void main(String[] args) {
		// Setup logging
		LOGGER = new Logger(FileLocations.getLogFilename("uupnp"));
		LOGGER.log(LogSeverity.INFO, "UniversalUPnP  Copyright (C) 2019  Moxeja\n"
				+ "\tUniversalUPnP comes with ABSOLUTELY NO WARRANTY.\n"
				+ "\tUniversalUPnP is free software, and you are welcome to redistribute it\n"
				+ "\tunder certain conditions. For more information, view the LICENSE file\n"
				+ "\tthat came with the binary file.");
		
		LOGGER.log(LogSeverity.INFO, "Uses GSON, apache-commons-io, Cling and Seamless libraries from:\n"
				+ "\thttps://github.com/google/gson, https://github.com/apache/commons-io,\n"
				+ "\thttps://github.com/4thline/cling and https://github.com/4thline/seamless respectively.");
		LOGGER.log(LogSeverity.INFO, "Running version: "+VERSION);
		
		// Cling only prints to System.err, so redirect to file
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
			jse.printStackTrace();
			LOGGER.log(LogSeverity.WARN, "Entries file was found, but was not formed correctly. Could not load data.");
			DATA = new MappingList();
		} catch (Exception e) {
			e.printStackTrace();
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

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.data.MappingEntry;
import com.moxeja.uupnp.data.Port;
import com.moxeja.uupnp.data.PortInfo;
import com.moxeja.uupnp.io.FileLocations;
import com.moxeja.uupnp.io.InputReader;
import com.moxeja.uupnp.network.NetworkUtils;
import com.moxeja.uupnp.network.Protocols;

public class NoGUI {

	private enum Commands {
		Create,
		List,
		Start,
		StartAll,
		Stop,
		IpAddress,
		Clear,
		Unknown
	}
	
	private Commands parseArg(String arg) {
		// Supported commands
		switch (arg) {
		case "-create":
		case "--create":
			return Commands.Create;
		case "-start":
		case "--start":
			return Commands.Start;
		case "-startall":
		case "--startall":
			return Commands.StartAll;
		case "stop":
		case "quit":
		case "exit":
		case "q":
			return Commands.Stop;
		case "-list":
		case "--list":
			return Commands.List;
		case "ip":
		case "-ip":
		case "--ip":
			return Commands.IpAddress;
		case "clear":
		case "-clear":
		case "--clear":
			return Commands.Clear;
		default:
			return Commands.Unknown;
		}
	}
	
	public NoGUI(String[] args) {
		Main.LOGGER.log(LogSeverity.INFO, "Checking for updates...");
		try {
			Main.LOGGER.log(LogSeverity.INFO, "Update available: "+NetworkUtils.needsUpdate(Main.VERSION));
		} catch (Exception e) {
			Main.LOGGER.log(LogSeverity.WARN, "Checking for update failed!");
		}
		
		Commands command = parseArg(args[0]);
		if (command == Commands.Unknown || command == Commands.Stop) {
			Main.LOGGER.log(LogSeverity.INFO, "Available arguments: -list, -create, -startall, -start <entry-index>, -clear");
			return;
		}
		
		// Allow the user to clear the entries file from command line argument
		if (command == Commands.Clear) {
			try {
				Files.delete(Paths.get(FileLocations.getEntriesFilename()));
				
				// Check file was actually deleted
				if (Files.notExists(Paths.get(FileLocations.getEntriesFilename()))) {
					Main.LOGGER.log(LogSeverity.INFO, "Deleted entries file successfully.");
				} else {
					throw new IOException();
				}
			} catch (IOException e) {
				Main.LOGGER.log(LogSeverity.ERROR, "Failed to delete entries file.");
			}
			
			return;
		}
		
		// Check for entries list corruption
		if (Main.DATA.isListCorrupted()) {
			Main.LOGGER.log(LogSeverity.WARN, "Entries list is most likely corrupted. "
					+ "Please use the -clear command to delete the file.");
			
			return;
		}
		
		if (command == Commands.Create) {
			// Create a template file for editing
			ArrayList<PortInfo> templatePorts = new ArrayList<PortInfo>();
			templatePorts.add(new PortInfo(new Port(8080), Protocols.UDP));
			templatePorts.add(new PortInfo(new Port(8081), new Port(8085), Protocols.TCP));
			templatePorts.add(new PortInfo(new Port(8090, 8091), Protocols.UDP_TCP));
			
			Main.LOGGER.log(LogSeverity.INFO, "A template file will be created at: " + FileLocations.getWorkingDir());
			Main.DATA.addEntry(new MappingEntry("Template Entry", templatePorts));
		} else if (command == Commands.StartAll) {
			// Start all mappings from entries file
			if (Main.DATA.isEmpty()) {
				Main.LOGGER.log(LogSeverity.FATAL, "No valid UPnP services in entries file. "
						+ "Please use -create and edit entries file.");
				return;
			}
			
			Main.DATA.startAll();
			blockUntilClose();
		} else if (command == Commands.Start) {
			// Check for valid arguments
			if (Main.DATA.isEmpty()) {
				Main.LOGGER.log(LogSeverity.FATAL, "No valid UPnP services in entries file. "
						+ "Please use -create and edit entries file.");
				return;
			} else if (args.length != 2) {
				Main.LOGGER.log(LogSeverity.FATAL, "Incorrect -start usage! Should be: -start <entry-index>");
				return;
			}
			
			// Start entry specified by user
			try {
				int index = Integer.parseInt(args[1]);
				Main.DATA.startEntry(index, null);
				blockUntilClose();
			} catch (NumberFormatException e) {
				Main.LOGGER.log(LogSeverity.FATAL, "Could not convert argument to integer! Argument: " + args[1]);
				return;
			} catch (ArrayIndexOutOfBoundsException e2) {
				Main.LOGGER.log(LogSeverity.FATAL, "Invalid entry-index specified: " + args[1]);
				return;
			} catch (Exception e) {
				Main.LOGGER.log(LogSeverity.FATAL, "Could not start entry!");
				return;
			}
		} else if (command == Commands.List) {
			// Print all entries loaded at startup from file
			Main.LOGGER.log(LogSeverity.INFO, "Current entries:");
			for (int i = 0; i < Main.DATA.getSize(); i++) {
				try {
					MappingEntry entry = Main.DATA.getEntry(i);
					Main.LOGGER.log(LogSeverity.INFO, String.format("\tIndex: %d, Name: %s", i, entry.getName()));
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		} else if (command == Commands.IpAddress) {
			showExternalIP();
		}
	}
	
	private void blockUntilClose() {
		// Wait for user to input any of the stop values
		boolean stop = false;
		showCLInfoMsg();
		while (!stop) {
			String input = InputReader.getInstance().nextLine().toLowerCase(Locale.ENGLISH);
			Commands command = parseArg(input);
			if (command == Commands.Stop) {
				stop = true;
			} else if (command == Commands.IpAddress) {
				showExternalIP();
			} else {
				showCLInfoMsg();
			}
		}
	}
	
	private void showCLInfoMsg() {
		// Warn user about closing issue
		Main.LOGGER.log(LogSeverity.INFO, "IMPORTANT: Make sure to NOT close the console before "
				+ "stopping the mappings or ports WON'T be closed!");
		
		// Print valid commands
		Main.LOGGER.log(LogSeverity.INFO, "Valid commands:");
		Main.LOGGER.log(LogSeverity.FOLLOW, "stop, quit, exit, q = Stop mappings");
		Main.LOGGER.log(LogSeverity.FOLLOW, "ip = Show external IP address");
	}
	
	private void showExternalIP() {
		try {
			String ipAddress = NetworkUtils.getExternalIPAddress();
			Main.LOGGER.log(LogSeverity.INFO, String.format("External IP Address: %s", ipAddress));
		} catch (Exception e) {
			Main.LOGGER.log(LogSeverity.ERROR, "Could not fetch external ip address!");
			e.printStackTrace();
		}
	}
}

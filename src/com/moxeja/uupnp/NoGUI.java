package com.moxeja.uupnp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;
import com.moxeja.uupnp.datatypes.Protocols;
import com.moxeja.uupnp.network.NetworkUtils;

public class NoGUI {

	private enum Commands {
		Create,
		List,
		Start,
		StartAll,
		Stop,
		Unknown
	}
	
	private Commands parseArg(String arg) {
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
		default:
			return Commands.Unknown;
		}
	}
	
	public NoGUI(String[] args) {
		Main.LOGGER.log(LogSeverity.INFO, "Checking for updates...");
		boolean update = NetworkUtils.needsUpdate(Main.VERSION);
		Main.LOGGER.log(LogSeverity.INFO, "Update available: "+update);
		
		Commands command = parseArg(args[0]);
		if (command == Commands.Unknown || command == Commands.Stop) {
			Main.LOGGER.log(LogSeverity.INFO, "Available arguments: -list, -create, -startall, -start <entry-index>");
			return;
		}
		
		if (command == Commands.Create) {	// Create a template file for editing
			ArrayList<PortInfo> templatePorts = new ArrayList<PortInfo>();
			templatePorts.add(new PortInfo(new Point(8080, 8080), Protocols.UDP));
			templatePorts.add(new PortInfo(new Point(8081, 8085), Protocols.TCP));
			templatePorts.add(new PortInfo(new Point(8090, 8090), Protocols.UDP_TCP));
			
			Main.LOGGER.log(LogSeverity.INFO, "A template file will be created at: " + FileLocations.getWorkingDir());
			Main.DATA.addEntry(new MappingEntry("Template Entry", templatePorts));
		} else if (command == Commands.StartAll) {	// Start all mappings from entries file
			if (Main.DATA.isEmpty()) {
				Main.LOGGER.log(LogSeverity.FATAL, "No valid UPnP services in entries file.");
				return;
			}
			
			Main.DATA.startAll();
			blockUntilClose();
		} else if (command == Commands.Start) {
			if (Main.DATA.isEmpty()) {
				Main.LOGGER.log(LogSeverity.FATAL, "No valid UPnP services in entries file.");
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
			}
		} else if (command == Commands.List) {
			Main.LOGGER.log(LogSeverity.INFO, "Current entries:");
			for (int i = 0; i < Main.DATA.getSize(); i++) {
				MappingEntry entry = Main.DATA.getEntry(i);
				Main.LOGGER.log(LogSeverity.INFO, String.format("\tIndex: %d, Name: %s", i, entry.getName()));
			}
		}
	}
	
	private void blockUntilClose() {
		// Warn user about closing issue
		Main.LOGGER.log(LogSeverity.INFO, "Type stop, quit, exit or q to stop the mappings.\n"
				+ "IMPORTANT: Make sure to NOT close the console before stopping the mappings or ports WON'T be closed!");
		
		// Wait for user to input any of the stop values
		Scanner scanner = new Scanner(System.in);
		boolean stop = false;
		while (!stop) {
			String input = scanner.next();
			if (parseArg(input) == Commands.Stop) {
				stop = true;
			}
		}
		scanner.close();
	}
}

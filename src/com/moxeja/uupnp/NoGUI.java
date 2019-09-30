package com.moxeja.uupnp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;
import com.moxeja.uupnp.datatypes.Protocols;

public class NoGUI {

	private enum Commands {
		Create,
		Start,
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
		case "stop":
		case "quit":
		case "exit":
		case "q":
			return Commands.Stop;
		default:
			return Commands.Unknown;
		}
	}
	
	public NoGUI(String[] args) {
		Commands command = parseArg(args[0]);
		if (command == Commands.Unknown) {
			Main.LOGGER.log(LogSeverity.FATAL, "Unknown argument: " + args[0]);
			Main.LOGGER.log(LogSeverity.INFO, "Available arguments: -create, -start");
			return;
		}
		
		if (command == Commands.Create) {	// Create a template file for editing
			ArrayList<PortInfo> templatePorts = new ArrayList<PortInfo>();
			templatePorts.add(new PortInfo(new Point(8080, 8080), Protocols.UDP));
			templatePorts.add(new PortInfo(new Point(8081, 8085), Protocols.TCP));
			templatePorts.add(new PortInfo(new Point(8090, 8090), Protocols.UDP_TCP));
			
			Main.LOGGER.log(LogSeverity.INFO, "A template file will be created at: " + FileLocations.getWorkingDir());
			Main.DATA.addEntry(new MappingEntry("Template Entry", templatePorts));
			return;
		} else if (command == Commands.Start) {	// Start all mappings from entries file
			if (Main.DATA.isEmpty()) {
				Main.LOGGER.log(LogSeverity.FATAL, "No valid UPnP services in entries file.");
				return;
			} else {
				Main.DATA.startAll();
				Main.LOGGER.log(LogSeverity.INFO, "Type stop, quit, exit or q to stop the mappings.\n"
						+ "IMPORTANT: Make sure to NOT close the console before stopping the mappings or ports WON'T be closed!");
				
				boolean stop = false;
				Scanner scanner = new Scanner(System.in);
				do {
					String input = scanner.next();
					if (parseArg(input) == Commands.Stop) {
						stop = true;
					}
				} while (!stop);
				scanner.close();
				return;
			}
		} else {
			Main.LOGGER.log(LogSeverity.FATAL, "Available arguments: -create, -start");
			return;
		}
	}
}

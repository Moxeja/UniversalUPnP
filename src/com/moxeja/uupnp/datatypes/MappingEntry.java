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
package com.moxeja.uupnp.datatypes;

import java.awt.Component;
import java.awt.Point;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.stream.IntStream;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.network.Protocols;
import com.moxeja.uupnp.Main;

public class MappingEntry {

	private String name;
	private ArrayList<PortInfo> ports = new ArrayList<PortInfo>();
	private transient boolean running;
	private transient UpnpService upnpservice;
	
	public MappingEntry(String name, ArrayList<PortInfo> ports) {
		this.name = name;
		this.ports = ports;
		running = false;
	}
	
	public String getName() {
		return name;
	}
	
	public Protocols getProtocol(int index) {
		return ports.get(index).protocol;
	}
	
	public ArrayList<PortInfo> getPorts() {
		return ports;
	}
	
	public Point getPort(int index) {
		return ports.get(index).portRange;
	}
	
	private Iterator<Integer> getPortIterator(Point range) {
		IntStream temp = IntStream.rangeClosed(range.x, range.y);
		return temp.boxed().iterator();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void startUPnP(Component parent) throws Exception {
		if (upnpservice != null) {
			running = true;
			return;
		}
		
		// Get local IP and handle multiple network interfaces
		Main.LOGGER.log(LogSeverity.INFO, "Finding all network interfaces...");
		LinkedHashMap<String, InetAddress> addresses = new LinkedHashMap<String, InetAddress>();
		for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			if (ni.isLoopback() || !ni.isUp())
				continue;
			
			for (InetAddress address : Collections.list(ni.getInetAddresses())) {
				if (!(address instanceof Inet4Address))
					continue;
				Main.LOGGER.log(LogSeverity.FOLLOW, "Found interface with IP: "+address.getHostAddress());
				addresses.put(address.getHostAddress()+" ["+ni.getDisplayName()+"]", address);
			}
		}
		
		// Check if any valid addresses were found
		if (addresses.size() == 0) {
			Main.LOGGER.log(LogSeverity.ERROR, "No valid IP addresses were found.");
			throw new Exception("No valid IP addresses were found.");
		}
		
		String selectedIP = null;
		// Ask user what IP they want to use
		String[] ips = addresses.keySet().toArray(new String[addresses.size()]);
		if (parent != null) {
			JComboBox<String> cboIP = new JComboBox<String>(ips);
			Object[] message = {
					"IP to bind ports to:",
					cboIP
			};
			int option = JOptionPane.showConfirmDialog(parent, message, "IP Binding", JOptionPane.OK_CANCEL_OPTION);
			if (option != JOptionPane.OK_OPTION) {
				Main.LOGGER.log(LogSeverity.INFO, "IP binding dialog cancelled.");
				throw new Exception("IP binding dialog cancelled.");
			}
			
			selectedIP = addresses.get(cboIP.getSelectedItem()).getHostAddress();
		} else {
			// Print available IPs
			Main.LOGGER.log(LogSeverity.INFO, "Which of the following IPs should the ports be binded to?:");
			for (int i = 0; i < ips.length; i++) {
				Main.LOGGER.log(LogSeverity.INFO, String.format("%d: %s", i, ips[i]));
			}
			
			// Get user selection
			Scanner scanner = new Scanner(new CloseShieldInputStream(System.in));
			int selection = -1;
			while (selection == -1) {
				try {
					int userSelection = Integer.parseInt(scanner.nextLine());
					if (userSelection >= 0 && userSelection <= (ips.length-1)) {
						selection = userSelection;
					} else {
						Main.LOGGER.log(LogSeverity.INFO, "No entry with that index.");
					}
				} catch (NumberFormatException e) {
					Main.LOGGER.log(LogSeverity.INFO, "Invalid number.");
				}
			}
			scanner.close();
			
			selectedIP = addresses.get(ips[selection]).getHostAddress();
		}
		
		// Set selected IP to use with ports
		String internalIP = selectedIP;
		Main.LOGGER.log(LogSeverity.INFO, "Binding ports to IP: "+internalIP);
		
		// Setup portmapping to use with upnpservice
		PortMapping[] portList = null;
		ArrayList<PortMapping> tempList = new ArrayList<PortMapping>();
		
		for (PortInfo port : ports) {
			if (port.protocol == Protocols.UDP) {
				getPortIterator(port.portRange).forEachRemaining((e) -> {
					tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.UDP, name));
				});
			} else if (port.protocol == Protocols.TCP) {
				getPortIterator(port.portRange).forEachRemaining((e) -> {
					tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.TCP, name));
				});
			} else if (port.protocol == Protocols.UDP_TCP) {
				getPortIterator(port.portRange).forEachRemaining((e) -> {
					tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.UDP, name));
					tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.TCP, name));
				});
			} else {
				throw new Exception("Unknown Protocol Type: "+port.protocol);
			}
		}
		
		portList = tempList.toArray(new PortMapping[tempList.size()]);
		
		// Create UPnP service and open ports
		upnpservice = new UpnpServiceImpl(new PortMappingListener(portList));
		upnpservice.getControlPoint().search();
		running = true;
	}
	
	public void stopUPnP() {
		if (upnpservice != null) {
			upnpservice.shutdown();
			upnpservice = null;
			running = false;
		}
	}
}

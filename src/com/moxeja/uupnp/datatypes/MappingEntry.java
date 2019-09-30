package com.moxeja.uupnp.datatypes;

import java.awt.Component;
import java.awt.Point;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.IntStream;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

import com.moxeja.uupnp.Logger.LogSeverity;
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
	
	// TODO: Allow IP selection in command line mode
	public void startUPnP(Component parent) throws Exception, UnknownHostException {
		if (upnpservice != null) {
			running = true;
			return;
		}
		
		// Get local IP and handle multiple network interfaces
		Main.LOGGER.log(LogSeverity.INFO, "Finding all network interfaces...");
		ArrayList<String> addresses = new ArrayList<String>();
		for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			if (ni.isLoopback() || !ni.isUp())
				continue;
			
			for (InetAddress address : Collections.list(ni.getInetAddresses())) {
				if (!(address instanceof Inet4Address))
					continue;
				Main.LOGGER.log(LogSeverity.FOLLOW, "Found interface with IP: "+address.getHostAddress());
				addresses.add(address.getHostAddress());
			}
		}
		
		// Check if any valid addresses were found
		if (addresses.size() == 0) {
			Main.LOGGER.log(LogSeverity.ERROR, "No valid IP addresses were found.");
			throw new Exception("No valid IP addresses were found.");
		}
		
		String selectedIP = null;
		if (parent != null) {
			// Ask user what IP they want to use
			String[] ips = new String[addresses.size()];
			ips = addresses.toArray(ips);
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
			
			selectedIP = cboIP.getSelectedItem().toString();
		}
		
		// Set selected IP to use with ports
		String internalIP = (parent != null && selectedIP != null) ? selectedIP : addresses.get(0);
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

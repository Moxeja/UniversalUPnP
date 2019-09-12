package com.moxeja.uupnp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

public class MappingEntry {

	private String name;
	private Protocols protocol;
	private int port;
	private transient boolean running;
	private transient UpnpService upnpservice;
	
	public MappingEntry(String name, Protocols protocol, int port) {
		this.name = name;
		this.protocol = protocol;
		this.port = port;
		running = false;
	}
	
	public String getName() {
		return name;
	}
	
	public Protocols getProtocol() {
		return protocol;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean getRunning() {
		return running;
	}
	
	public void startUPnP() throws Exception, UnknownHostException {
		if (upnpservice != null) {
			running = true;
			return;
		}
		
		// Get local IP
		String internalIP;
		internalIP = InetAddress.getLocalHost().getHostAddress();
		
		// Setup portmapping to use with upnpservice
		PortMapping[] portList;
		if (protocol == Protocols.UDP) {
			portList = new PortMapping[] {new PortMapping(port, internalIP, PortMapping.Protocol.UDP, name)};
		} else if (protocol == Protocols.TCP) {
			portList = new PortMapping[] {new PortMapping(port, internalIP, PortMapping.Protocol.TCP, name)};
		} else if (protocol == Protocols.UDP_TCP) {
			portList = new PortMapping[] {new PortMapping(port, internalIP, PortMapping.Protocol.UDP, name),
					new PortMapping(port, internalIP, PortMapping.Protocol.TCP, name)};
		} else {
			throw new Exception("Unknown Protocol Type: "+protocol);
		}
		
		// Use Jetty implementation to stop errors
		upnpservice = new UpnpServiceImpl(new JettyUPnPConfiguration(), new PortMappingListener(portList));
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

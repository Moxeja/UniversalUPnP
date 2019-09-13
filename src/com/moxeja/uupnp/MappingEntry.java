package com.moxeja.uupnp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

public class MappingEntry {

	private String name;
	private Protocols protocol;
	private int portBegin;
	private int portEnd;
	private transient boolean running;
	private transient UpnpService upnpservice;
	
	public MappingEntry(String name, Protocols protocol, int portBegin, int portEnd) {
		this.name = name;
		this.protocol = protocol;
		this.portBegin = portBegin;
		this.portEnd = portEnd;
		running = false;
	}
	
	public String getName() {
		return name;
	}
	
	public Protocols getProtocol() {
		return protocol;
	}
	
	public int getPortBegin() {
		return portBegin;
	}
	
	public int getPortEnd() {
		return portEnd;
	}
	
	private Iterator<Integer> getPorts() {
		IntStream temp = IntStream.rangeClosed(portBegin, portEnd);
		return temp.boxed().iterator();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void startUPnP() throws Exception, UnknownHostException {
		if (upnpservice != null) {
			running = true;
			return;
		}
		
		// Get local IP
		String internalIP = InetAddress.getLocalHost().getHostAddress();
		
		// Setup portmapping to use with upnpservice
		PortMapping[] portList = null;
		ArrayList<PortMapping> tempList = new ArrayList<PortMapping>();
		
		if (protocol == Protocols.UDP) {
			getPorts().forEachRemaining((e) -> {
				tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.UDP, name));
			});
		} else if (protocol == Protocols.TCP) {
			getPorts().forEachRemaining((e) -> {
				tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.TCP, name));
			});
		} else if (protocol == Protocols.UDP_TCP) {
			getPorts().forEachRemaining((e) -> {
				tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.UDP, name));
				tempList.add(new PortMapping(e, internalIP, PortMapping.Protocol.TCP, name));
			});
		} else {
			throw new Exception("Unknown Protocol Type: "+protocol);
		}
		portList = tempList.toArray(new PortMapping[tempList.size()]);
		
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

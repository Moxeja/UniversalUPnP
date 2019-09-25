package com.moxeja.uupnp.datatypes;

import java.awt.Point;
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

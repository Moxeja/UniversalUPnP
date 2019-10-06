package com.moxeja.uupnp;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;

public class MappingList {

	private ArrayList<MappingEntry> entries = new ArrayList<MappingEntry>();
	
	public ArrayList<MappingEntry> getEntryList() {
		return entries;
	}
	
	public int getSize() {
		return entries.size();
	}
	
	public void addEntry(MappingEntry entry) {
		if (entry != null) {
			Main.LOGGER.log(LogSeverity.INFO, "Creating new mapping.");
			for (PortInfo port : entry.getPorts()) {
				Main.LOGGER.log(LogSeverity.FOLLOW, "Name: "+entry.getName()+", Protocol: "+port.protocol+
						", Ports: "+port.portRange.x+"->"+port.portRange.y);
			}
			
			entries.add(entry);
		}
	}
	
	public void deleteEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		Main.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		entries.get(id).stopUPnP();
		
		Main.LOGGER.log(LogSeverity.INFO, "Deleting service with id: "+id);
		MappingEntry temp = entries.get(id);
		for (PortInfo port : temp.getPorts()) {
			Main.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
					", Ports: "+port.portRange.x+"->"+port.portRange.y);
		}
		
		entries.remove(id);
	}
	
	public MappingEntry getEntry(int id) {
		if (id > entries.size() || id < 0)
			return null;
		
		return entries.get(id);
	}
	
	public void startEntry(int id, Component parent) throws ArrayIndexOutOfBoundsException {
		if (id > (entries.size()-1) || id < 0)
			throw new ArrayIndexOutOfBoundsException();
		
		Main.LOGGER.log(LogSeverity.INFO, "Starting service with id: "+id);
		MappingEntry temp = entries.get(id);
		for (PortInfo port : temp.getPorts()) {
			Main.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
					", Ports: "+port.portRange.x+"->"+port.portRange.y);
		}
		
		try {
			entries.get(id).startUPnP(parent);
		} catch (Exception e) {
			Main.LOGGER.log(LogSeverity.ERROR, "Could not start upnpservice!");
			for (PortInfo port : temp.getPorts()) {
				Main.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
						", Ports: "+port.portRange.x+"->"+port.portRange.y);
			}
			e.printStackTrace();
		}
	}
	
	public void stopEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		Main.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		MappingEntry temp = entries.get(id);
		for (PortInfo port : temp.getPorts()) {
			Main.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
					", Ports: "+port.portRange.x+"->"+port.portRange.y);
		}
		
		entries.get(id).stopUPnP();
	}
	
	public void startAll() {
		Main.LOGGER.log(LogSeverity.INFO, "Starting all UPnP services.");
		for (int i = 0; i < entries.size(); i++) {
			try {
				startEntry(i, null);
			} catch (ArrayIndexOutOfBoundsException e) {}
		}
	}
	
	public void stopAll() {
		Main.LOGGER.log(LogSeverity.INFO, "Stopping all UPnP services.");
		LinkedList<Thread> threads = new LinkedList<Thread>();
		
		entries.forEach((e) -> {
			if (e.isRunning()) {
				threads.push(new Thread(() -> {
					e.stopUPnP();
				}));
				threads.peek().start();
			}
		});
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e1) {}
		}
	}
	
	public boolean isEmpty() {
		return entries.isEmpty();
	}
}

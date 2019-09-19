package com.moxeja.uupnp;

import java.util.ArrayList;
import java.util.LinkedList;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;
import com.moxeja.uupnp.ui.MainWindow;

public class MappingList {

	private ArrayList<MappingEntry> entries = new ArrayList<MappingEntry>();
	
	public ArrayList<MappingEntry> getEntryList() {
		return entries;
	}
	
	public void addEntry(MappingEntry entry) {
		if (entry != null) {
			MainWindow.LOGGER.log(LogSeverity.INFO, "Creating new mapping.");
			for (PortInfo port : entry.getPorts()) {
				MainWindow.LOGGER.log(LogSeverity.FOLLOW, "Name: "+entry.getName()+", Protocol: "+port.protocol+
						", Ports: "+port.portRange.x+"->"+port.portRange.y);
			}
			
			entries.add(entry);
		}
	}
	
	public void deleteEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		MainWindow.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		entries.get(id).stopUPnP();
		
		MainWindow.LOGGER.log(LogSeverity.INFO, "Deleting service with id: "+id);
		MappingEntry temp = entries.get(id);
		for (PortInfo port : temp.getPorts()) {
			MainWindow.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
					", Ports: "+port.portRange.x+"->"+port.portRange.y);
		}
		
		entries.remove(id);
	}
	
	public MappingEntry getEntry(int id) {
		if (id > entries.size() || id < 0)
			return null;
		
		return entries.get(id);
	}
	
	public void startEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		MainWindow.LOGGER.log(LogSeverity.INFO, "Starting service with id: "+id);
		MappingEntry temp = entries.get(id);
		for (PortInfo port : temp.getPorts()) {
			MainWindow.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
					", Ports: "+port.portRange.x+"->"+port.portRange.y);
		}
		
		try {
			entries.get(id).startUPnP();
		} catch (Exception e) {
			MainWindow.LOGGER.log(LogSeverity.ERROR, "Could not start upnpservice!");
			for (PortInfo port : temp.getPorts()) {
				MainWindow.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
						", Ports: "+port.portRange.x+"->"+port.portRange.y);
			}
			e.printStackTrace();
		}
	}
	
	public void stopEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		MainWindow.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		MappingEntry temp = entries.get(id);
		for (PortInfo port : temp.getPorts()) {
			MainWindow.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+port.protocol+
					", Ports: "+port.portRange.x+"->"+port.portRange.y);
		}
		
		entries.get(id).stopUPnP();
	}
	
	public void stopAll() {
		MainWindow.LOGGER.log(LogSeverity.INFO, "Stopping all UPnP services.");
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
}

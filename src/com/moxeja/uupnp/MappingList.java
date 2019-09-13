package com.moxeja.uupnp;

import java.util.ArrayList;
import java.util.LinkedList;

import com.moxeja.uupnp.Logger.LogSeverity;

public class MappingList {

	private ArrayList<MappingEntry> entries = new ArrayList<MappingEntry>();
	
	public ArrayList<MappingEntry> getEntryList() {
		return entries;
	}
	
	public void addEntry(MappingEntry entry) {
		if (entry != null) {
			Window.LOGGER.log(LogSeverity.INFO, "Creating new mapping.");
			Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+entry.getName()+", Protocol: "+entry.getProtocol()+
					", Ports: "+entry.getPortBegin()+"->"+entry.getPortEnd());
			
			entries.add(entry);
		}
	}
	
	public void deleteEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		Window.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		entries.get(id).stopUPnP();
		
		Window.LOGGER.log(LogSeverity.INFO, "Deleting service with id: "+id);
		MappingEntry temp = entries.get(id);
		Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+
				", Ports: "+temp.getPortBegin()+"->"+temp.getPortEnd());
		
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
		
		Window.LOGGER.log(LogSeverity.INFO, "Starting service with id: "+id);
		MappingEntry temp = entries.get(id);
		Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+
				", Ports: "+temp.getPortBegin()+"->"+temp.getPortEnd());
		
		try {
			entries.get(id).startUPnP();
		} catch (Exception e) {
			Window.LOGGER.log(LogSeverity.ERROR, "Could not start upnpservice!");
			Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+
					", Ports: "+temp.getPortBegin()+"->"+temp.getPortEnd());
			e.printStackTrace();
		}
	}
	
	public void stopEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		Window.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		MappingEntry temp = entries.get(id);
		Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+
				", Ports: "+temp.getPortBegin()+"->"+temp.getPortEnd());
		
		entries.get(id).stopUPnP();
	}
	
	public void stopAll() {
		Window.LOGGER.log(LogSeverity.INFO, "Stopping all UPnP services.");
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

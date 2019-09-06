package com.moxeja.uupnp;

import java.util.ArrayList;

import com.moxeja.uupnp.Logger.LogSeverity;

public class MappingList {

	private ArrayList<MappingEntry> entries = new ArrayList<MappingEntry>();
	
	public ArrayList<MappingEntry> getEntryList() {
		return entries;
	}
	
	public void addEntry(MappingEntry entry) {
		if (entry != null) {
			Window.LOGGER.log(LogSeverity.INFO, "Creating new mapping.");
			Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+entry.getName()+", Protocol: "+entry.getProtocol()+", Port: "+entry.getPort());
			
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
		Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+", Port: "+temp.getPort());
		
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
		Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+", Port: "+temp.getPort());
		
		try {
			entries.get(id).startUPnP();
		} catch (Exception e) {
			Window.LOGGER.log(LogSeverity.ERROR, "Could not start upnpservice!");
			Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+", Port: "+temp.getPort());
			e.printStackTrace();
		}
	}
	
	public void stopEntry(int id) {
		if (id > entries.size() || id < 0)
			return;
		
		Window.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		MappingEntry temp = entries.get(id);
		Window.LOGGER.log(LogSeverity.FOLLOW, "Name: "+temp.getName()+", Protocol: "+temp.getProtocol()+", Port: "+temp.getPort());
		
		entries.get(id).stopUPnP();
	}
	
	public void stopAll() {
		Window.LOGGER.log(LogSeverity.INFO, "Stopping all UPnP services.");
		
		entries.forEach((e) -> {
			e.stopUPnP();
		});
	}
}

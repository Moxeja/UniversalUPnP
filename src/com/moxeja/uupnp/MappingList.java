package com.moxeja.uupnp;

import java.util.ArrayList;

public class MappingList {

	private ArrayList<MappingEntry> entries = new ArrayList<MappingEntry>();
	
	public ArrayList<MappingEntry> getEntryList() {
		return entries;
	}
	
	public void addEntry(MappingEntry entry) {
		if (entry != null) {
			entries.add(entry);
		}
	}
	
	public void deleteEntry(int id) {
		if (id > entries.size())
			return;
		
		entries.get(id).stopUPnP();
		entries.remove(id);
	}
	
	public MappingEntry getEntry(int id) {
		if (id > entries.size())
			return null;
		
		return entries.get(id);
	}
	
	public void startEntry(int id) {
		if (id > entries.size())
			return;
		
		try {
			entries.get(id).startUPnP();
		} catch (Exception e) {
			System.err.println("Could not start UPnP service!");
			e.printStackTrace();
		}
	}
	
	public void stopEntry(int id) {
		if (id > entries.size())
			return;
		
		entries.get(id).stopUPnP();
	}
	
	public void stopAll() {
		entries.forEach((e) -> {
			e.stopUPnP();
		});
	}
}

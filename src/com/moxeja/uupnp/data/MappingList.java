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
package com.moxeja.uupnp.data;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;

import com.moxeja.uupnp.Main;
import com.moxeja.uupnp.Logger.LogSeverity;

public class MappingList {

	private ArrayList<MappingEntry> entries = new ArrayList<MappingEntry>();
	
	public ArrayList<MappingEntry> getEntryList() {
		return entries;
	}
	
	public int getSize() {
		return entries.size();
	}
	
	public boolean isListCorrupted() {
		// Loop over all entries and see if any have a null ports list
		// (not possible through normal program use)
		for (MappingEntry entry : entries) {
			if (entry.getPorts() == null) {
				return true;
			}
		}
		
		return false;
	}
	
	private void printPortInfo(MappingEntry entry) {
		// Print all port details
		for (PortInfo port : entry.getPorts()) {
			if (port.hasRange) {
				Main.LOGGER.log(LogSeverity.FOLLOW, "Name: "+entry.getName()+", Protocol: "+port.protocol+
						", Ports: "+port.startPort.externalPort+"->"+port.endPort.externalPort);
			} else {
				Main.LOGGER.log(LogSeverity.FOLLOW, "Name: "+entry.getName()+", Protocol: "+port.protocol+
						", Port: "+port.startPort.externalPort);
			}
		}
	}
	
	public void addEntry(MappingEntry entry) {
		if (entry != null) {
			Main.LOGGER.log(LogSeverity.INFO, "Creating new mapping.");
			printPortInfo(entry);
			
			entries.add(entry);
		}
	}
	
	public void deleteEntry(int id) throws ArrayIndexOutOfBoundsException {
		if (id >= entries.size() || id < 0)
			throw new ArrayIndexOutOfBoundsException();
		
		// Stop mapping before deleting to make sure ports are closed
		Main.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		entries.get(id).stopUPnP();
		
		Main.LOGGER.log(LogSeverity.INFO, "Deleting service with id: "+id);
		printPortInfo(entries.get(id));
		
		entries.remove(id);
	}
	
	public MappingEntry getEntry(int id) throws ArrayIndexOutOfBoundsException {
		if (id >= entries.size() || id < 0)
			throw new ArrayIndexOutOfBoundsException();
		
		return entries.get(id);
	}
	
	public void startEntry(int id, Component parent) throws Exception {
		if (id >= entries.size() || id < 0)
			throw new ArrayIndexOutOfBoundsException();
		
		// Print what ports are being opened
		Main.LOGGER.log(LogSeverity.INFO, "Starting service with id: "+id);
		MappingEntry temp = entries.get(id);
		printPortInfo(temp);
		
		try {
			entries.get(id).startUPnP(parent);
		} catch (Exception e) {
			e.printStackTrace();
			Main.LOGGER.log(LogSeverity.ERROR, "Could not start upnpservice!");
			printPortInfo(temp);
			entries.get(id).stopUPnP();
			throw new Exception();
		}
	}
	
	public void stopEntry(int id) throws ArrayIndexOutOfBoundsException {
		if (id >= entries.size() || id < 0)
			throw new ArrayIndexOutOfBoundsException();
		
		// Print what ports are being closed
		Main.LOGGER.log(LogSeverity.INFO, "Stopping service with id: "+id);
		printPortInfo(entries.get(id));
		
		entries.get(id).stopUPnP();
	}
	
	public void startAll() {
		Main.LOGGER.log(LogSeverity.INFO, "Starting all UPnP services.");
		for (int i = 0; i < entries.size(); i++) {
			try {
				startEntry(i, null);
			} catch (Exception e) {
				e.printStackTrace();
				Main.LOGGER.log(LogSeverity.ERROR, "Could not start all entries!");
				stopAll();
			}
		}
	}
	
	public void stopAll() {
		Main.LOGGER.log(LogSeverity.INFO, "Stopping all UPnP services.");
		LinkedList<Thread> threads = new LinkedList<Thread>();
		
		// Stop any running mappings in parallel
		entries.forEach((e) -> {
			if (e.isRunning()) {
				threads.push(new Thread(() -> {
					e.stopUPnP();
				}));
				threads.peek().start();
			}
		});
		
		// Ensure all threads have finished closing mappings
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

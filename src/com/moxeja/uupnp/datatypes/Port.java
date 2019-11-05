package com.moxeja.uupnp.datatypes;

public class Port {
	
	public int internalPort;
	public int externalPort;

	public Port(int port) {
		internalPort = port;
		externalPort = port;
	}
	
	public Port(int internal, int external) {
		internalPort = internal;
		externalPort = external;
	}
}

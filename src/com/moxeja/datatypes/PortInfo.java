package com.moxeja.datatypes;

import java.awt.Point;

import com.moxeja.uupnp.Protocols;

public class PortInfo {

	public Point ports;
	public Protocols protocol;
	
	public PortInfo(Point ports, Protocols protocol) {
		this.ports = ports;
		this.protocol = protocol;
	}
}

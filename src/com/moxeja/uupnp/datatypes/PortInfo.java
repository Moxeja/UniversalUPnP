package com.moxeja.uupnp.datatypes;

import java.awt.Point;

public class PortInfo {

	public Point ports;
	public Protocols protocol;
	
	public PortInfo(Point ports, Protocols protocol) {
		this.ports = ports;
		this.protocol = protocol;
	}
}

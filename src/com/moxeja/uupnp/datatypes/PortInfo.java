package com.moxeja.uupnp.datatypes;

import java.awt.Point;

import com.moxeja.uupnp.network.Protocols;

public class PortInfo {

	public Point portRange;
	public Protocols protocol;
	
	public PortInfo(Point portRange, Protocols protocol) {
		this.portRange = portRange;
		this.protocol = protocol;
	}
}

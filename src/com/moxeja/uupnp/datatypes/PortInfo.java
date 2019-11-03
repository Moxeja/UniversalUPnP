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
package com.moxeja.uupnp.datatypes;

import java.awt.Point;

import com.moxeja.uupnp.network.Protocols;

public class PortInfo {

	public Point portRange;
	public boolean hasRange;
	public Point internalExternalMapping;
	public Protocols protocol;
	
	public PortInfo(Point portRange, Point internalExternalMapping, Protocols protocol, boolean hasRange) {
		this.portRange = portRange;
		this.internalExternalMapping = internalExternalMapping;
		this.protocol = protocol;
		this.hasRange = hasRange;
	}
}

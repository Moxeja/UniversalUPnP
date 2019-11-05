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
package com.moxeja.uupnp;

import java.util.Scanner;

public class InputReader {
	
	public static InputReader READER = new InputReader();
	private Scanner scanner;

	public InputReader() {
		scanner = new Scanner(System.in);
	}
	
	public String next() {
		if (scanner != null) {
			return scanner.next();
		} else {
			return "";
		}
	}
	
	public String nextLine() {
		if (scanner != null) {
			return scanner.nextLine();
		} else {
			return "";
		}
	}
}

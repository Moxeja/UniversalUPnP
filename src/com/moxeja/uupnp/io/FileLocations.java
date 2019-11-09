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
package com.moxeja.uupnp.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.moxeja.uupnp.Main;
import com.moxeja.uupnp.Logger.LogSeverity;

public class FileLocations {
	
	private enum OSType {
		Windows,
		Linux,
		Mac,
		Unknown
	}

	// Load OS type at runtime start
	private static final OSType OSNAME = System.getProperty("os.name").contains("Win") ? OSType.Windows : (
			System.getProperty("os.name").contains("Linux") ? OSType.Linux : (
			System.getProperty("os.name").contains("Mac") ? OSType.Mac : OSType.Unknown));
	
	private static final String APP_DIR = "UniversalUPnP";
	private static final String WARN_FILENAME = "disable-warning";
	
	public static String getWorkingDir() {
		// user.home works on all OS so use it as backup
		Path temp;
		if (OSNAME == OSType.Windows) {
			temp = Paths.get(System.getenv("APPDATA"), APP_DIR);
		} else if (OSNAME == OSType.Linux) {
			temp = Paths.get(System.getProperty("user.home"), APP_DIR);
		} else if (OSNAME == OSType.Mac) {
			temp = Paths.get(System.getProperty("user.home"), APP_DIR);
		} else {
			temp = Paths.get(System.getProperty("user.home"), APP_DIR);
		}
		
		// Create directory if it doesn't exist
		if (Files.notExists(temp)) {
			try {
				Files.createDirectory(temp);
			} catch (IOException e) {
				Main.LOGGER.log(LogSeverity.ERROR, "Could not create configuration directory.");
				e.printStackTrace();
			}
		}
		
		return temp.toString();
	}
	
	public static String getLogFilename(String filename) {
		// Use paths.get to create URI with correct file separators
		return Paths.get(getWorkingDir(), filename+".log").toString();
	}
	
	public static String getEntriesFilename() {
		// Use paths.get to create URI with correct file separators
		return Paths.get(getWorkingDir(), "entries.json").toString();
	}
	
	private static Path getWarningFile() {
		// Use paths.get to create URI with correct file separators
		return Paths.get(getWorkingDir(), WARN_FILENAME);
	}
	
	public static boolean warningDisabled() {
		return Files.exists(getWarningFile());
	}
	
	public static void createWarningFile() {
		try {
			Files.createFile(getWarningFile());
			Main.LOGGER.log(LogSeverity.INFO, "Created file to disable warnings.");
		} catch (IOException e) {
			Main.LOGGER.log(LogSeverity.ERROR, "Could not disable startup warning.");
			e.printStackTrace();
		}
	}
}

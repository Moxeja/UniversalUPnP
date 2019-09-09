package com.moxeja.uupnp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	
	private static String getWorkingDir() {
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
				Window.LOGGER.log(LogSeverity.ERROR, "Could not create configuration directory.");
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
}

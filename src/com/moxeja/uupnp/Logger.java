package com.moxeja.uupnp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

public class Logger {

	public enum LogSeverity {
		INFO,
		WARN,
		ERROR,
		FATAL
	}
	
	private PrintWriter pw = null;
	
	public Logger(String filepath) {
		// Delete previous log if it exists
		try {
			Files.deleteIfExists(Paths.get(filepath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(filepath, false)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pw.println("OS Name: "+System.getProperty("os.name")+", Arch: "+System.getProperty("os.arch"));
		pw.println("Java version: "+System.getProperty("java.version"));
	}
	
	public void log(LogSeverity severity, String message) {
		String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(System.currentTimeMillis());
		
		switch(severity) {
		case INFO:
			pw.print("[INFO]-[");
			break;
		case WARN:
			pw.print("[WARN]-[");
			break;
		case ERROR:
			pw.print("[ERROR]-[");
			break;
		case FATAL:
			pw.print("[FATAL]-[");
			break;
		default:
			pw.print("[UNKNOWN]-[");
			break;
		}
		pw.print(timestamp + "]: ");
		pw.println(message);
	}
	
	public void close() {
		if (pw != null) {
			pw.close();
			pw = null;
		}
	}
}

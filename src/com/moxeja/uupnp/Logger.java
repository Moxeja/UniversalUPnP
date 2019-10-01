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
		FATAL,
		FOLLOW
	}
	
	private PrintWriter pw = null;
	private boolean invalidLogger = false;
	
	public Logger(String filepath) {
		// Delete previous log if it exists
		try {
			Files.deleteIfExists(Paths.get(filepath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR: Could not open logging file stream. Logging to file will be disabled for this session.");
			invalidLogger = true;
		}
		
		// Print basic runtime information
		if (!invalidLogger) {
			pw.println("OS Name: "+System.getProperty("os.name")+", Arch: "+System.getProperty("os.arch"));
			pw.println("Java version: "+System.getProperty("java.version"));
		}
		System.out.println("OS Name: "+System.getProperty("os.name")+", Arch: "+System.getProperty("os.arch"));
		System.out.println("Java version: "+System.getProperty("java.version"));
	}
	
	public synchronized void log(LogSeverity severity, String message) {
		String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(System.currentTimeMillis());
		
		switch(severity) {
		case INFO:
			if (!invalidLogger) pw.print("[INFO]-[");
			System.out.print("[INFO]-[");
			break;
		case WARN:
			if (!invalidLogger) pw.print("[WARN]-[");
			System.out.print("[WARN]-[");
			break;
		case ERROR:
			if (!invalidLogger) pw.print("[ERROR]-[");
			System.out.print("[ERROR]-[");
			break;
		case FATAL:
			if (!invalidLogger) pw.print("[FATAL]-[");
			System.out.print("[FATAL]-[");
			break;
		case FOLLOW:
			if (!invalidLogger) pw.print(">>>> ");
			System.out.print(">>>> ");
			break;
		default:
			if (!invalidLogger) pw.print("[UNKNOWN]-[");
			System.out.print("[UNKNOWN]-[");
			break;
		}
		
		if (severity != LogSeverity.FOLLOW) {
			if (!invalidLogger) pw.println(timestamp + "]: " + message);
			System.out.println(timestamp + "]: " + message);
		} else {
			if (!invalidLogger) pw.println(message);
			System.out.println(message);
		}
	}
	
	public void close() {
		if (pw != null) {
			pw.close();
			pw = null;
		}
	}
}

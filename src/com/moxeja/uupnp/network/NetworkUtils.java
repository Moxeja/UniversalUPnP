package com.moxeja.uupnp.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

public class NetworkUtils {

	public static String getExternalIPAddress() {
		try {
			URL ipchecker = new URL("https://checkip.amazonaws.com");
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(ipchecker.openStream()));
				return reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return "0.0.0.0";
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "0.0.0.0";
		}
	}
	
	private static boolean onlineVersionNewer(String currentVersion, String webVersion) {
		final String delimiter = ".";
		StringTokenizer stCurrent = new StringTokenizer(currentVersion, delimiter);
		StringTokenizer stWeb = new StringTokenizer(webVersion, delimiter);
		
		while (stCurrent.hasMoreTokens()) {
			if (stWeb.hasMoreTokens()) {
				int currentInt = Integer.parseInt(stCurrent.nextToken());
				int webInt = Integer.parseInt(stWeb.nextToken());
				
				if (currentInt > webInt) {
					return false;
				} else if (webInt > currentInt) {
					return true;
				}
			} else {
				return false;
			}
		}
		
		return stWeb.hasMoreTokens();
	}
	
	public static boolean needsUpdate(String currentVersion) {
		String webVersion = null;
		
		// Retrieve online version number
		try {
			URL versionChecker = new URL("https://moxeja.github.io/uupnp-version");
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(versionChecker.openStream()));
				webVersion = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
		
		if (webVersion == null || webVersion.isEmpty()) {
			return false;
		}
		
		return onlineVersionNewer(currentVersion, webVersion);
	}
}

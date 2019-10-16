/*
 * Copyright (C) 2019  Moxeja
 * NetworkUtils.java is part of UniversalUPnP
 *
 * UniversalUPnP is free software: you can redistribute it and/or modify
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
package com.moxeja.uupnp.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class NetworkUtils {

	public static String getExternalIPAddress() {
		try {
			// External IP address provider
			URL ipchecker = new URL("https://checkip.amazonaws.com");
			BufferedReader reader = null;
			try {
				// Open connection and read data
				URLConnection con = ipchecker.openConnection();
				con.setConnectTimeout(4000);
				con.setReadTimeout(3000);
				reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				return reader.readLine();
			} catch (Exception e) {
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
		// Tokenize both strings
		final String delimiter = ".";
		StringTokenizer stCurrent = new StringTokenizer(currentVersion, delimiter);
		StringTokenizer stWeb = new StringTokenizer(webVersion, delimiter);
		
		// Check if webVersion is newer than currentVersion
		while (stCurrent.hasMoreTokens()) {
			if (stWeb.hasMoreTokens()) {
				try {
					int currentInt = Integer.parseInt(stCurrent.nextToken());
					int webInt = Integer.parseInt(stWeb.nextToken());
					
					if (currentInt > webInt) {
						return false;
					} else if (webInt > currentInt) {
						return true;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		}
		
		return stWeb.hasMoreTokens();
	}
	
	public static boolean needsUpdate(String currentVersion) {
		String webVersion = null;
		
		try {
			// Retrieve online version number
			URL versionChecker = new URL("https://moxeja.github.io/app-versions/uupnp-version");
			BufferedReader reader = null;
			try {
				// Open connection and read data
				URLConnection con = versionChecker.openConnection();
				con.setConnectTimeout(4000);
				con.setReadTimeout(3000);
				reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				webVersion = reader.readLine();
			} catch (Exception e) {
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

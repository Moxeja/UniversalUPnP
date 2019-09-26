package com.moxeja.uupnp.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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
}

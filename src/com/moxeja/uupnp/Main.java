package com.moxeja.uupnp;

import java.awt.EventQueue;

import com.moxeja.uupnp.ui.MainWindow;

public class Main {

	public static void main(String[] args) {
		// Start GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new MainWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}

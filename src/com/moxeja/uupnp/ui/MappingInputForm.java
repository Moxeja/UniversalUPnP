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
package com.moxeja.uupnp.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.moxeja.uupnp.Logger.LogSeverity;
import com.moxeja.uupnp.data.MappingEntry;
import com.moxeja.uupnp.data.Port;
import com.moxeja.uupnp.data.PortInfo;
import com.moxeja.uupnp.Main;
import com.moxeja.uupnp.network.Protocols;

@SuppressWarnings("serial")
public class MappingInputForm extends JDialog {
	
	private JPanel contentPane;
	private JTable table;
	private JTextField txtName;
	
	private final int width = 504;
	private final int height = 350;
	
	public ArrayList<PortInfo> ports = new ArrayList<PortInfo>();
	public boolean okClose = false;
	public String name;

	public MappingInputForm(JFrame parent) {
		this(parent, null);
	}
	
	public MappingInputForm(JFrame parent, MappingEntry entry) {
		super(parent, true);
		setResizable(false);
		setTitle("Enter Mapping Details");
		
		// Centre dialog in parent
		if (parent != null) {
			int startX = parent.getX() + (parent.getWidth()/2 - width/2);
			int startY = parent.getY() + (parent.getHeight()/2 - height/2);
			setBounds(startX, startY, width, height);
		}
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 36, 312, 224);
		contentPane.add(scrollPane);
		
		// Main table
		table = new JTable();
		String[] col = { "Internal Port", "External Port", "Protocol" };
		table.setModel(new NoCellEditableModel(col, 0));
		scrollPane.setViewportView(table);
		
		// Add port button
		JButton btnAddPort = new JButton("Add Port (Basic)");
		btnAddPort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddPortClicked();
			}
		});
		btnAddPort.setBounds(332, 36, 156, 37);
		contentPane.add(btnAddPort);
		
		// Add port range button
		JButton btnAddPortRange = new JButton("Add Port Range");
		btnAddPortRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddPortRange();
			}
		});
		btnAddPortRange.setBounds(332, 132, 156, 65);
		contentPane.add(btnAddPortRange);
		
		// Delete port button
		JButton btnDeletePort = new JButton("Delete Port(s)");
		btnDeletePort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnDeletePortClicked();
			}
		});
		btnDeletePort.setBounds(332, 208, 156, 52);
		contentPane.add(btnDeletePort);
		
		// Mapping name UI code
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(10, 11, 46, 14);
		contentPane.add(lblName);
		
		txtName = new JTextField();
		txtName.setHorizontalAlignment(SwingConstants.LEFT);
		txtName.setBounds(48, 8, 440, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		// OK button
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnOkClicked();
			}
		});
		btnOk.setBounds(10, 271, 289, 40);
		contentPane.add(btnOk);
		
		// Cancel button
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btnCancel.setBounds(309, 271, 179, 40);
		contentPane.add(btnCancel);
		
		// Add port (advanced) button
		JButton btnAddPortadvanced = new JButton("Add Port (Advanced)");
		btnAddPortadvanced.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddPortAdvClicked();
			}
		});
		btnAddPortadvanced.setBounds(332, 84, 156, 37);
		contentPane.add(btnAddPortadvanced);
		
		// If being used in edit mode
		if (entry != null) {
			Main.LOGGER.log(LogSeverity.INFO, "Opened edit form.");
			txtName.setText(entry.getName());
			entry.getPorts().forEach((e) -> {
				ports.add(e);
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				Object[] temp = null;
				
				if (!e.hasRange) {
					if (e.startPort.internalPort != e.startPort.externalPort) {
						temp = new Object[] { e.startPort.internalPort, e.startPort.externalPort, e.protocol };
					} else {
						temp = new Object[] { e.startPort.externalPort, e.startPort.externalPort, e.protocol };
					}
				} else {
					temp = new Object[] { e.startPort.externalPort+"->"+e.endPort.externalPort,
							e.startPort.externalPort+"->"+e.endPort.externalPort, e.protocol };
				}
				
				model.addRow(temp);
			});
		}
	}
	
	private void btnOkClicked() {
		// Cannot leave name field empty
		if (txtName.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Cannot leave the name field empty!",
					"Empty Name", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// No point creating a mapping with no ports
		if (ports.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Cannot create entry with no port mappings",
					"No port mappings", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		okClose = true;
		name = txtName.getText();
		dispose();
	}
	
	private void btnAddPortClicked() {
		// Port number input
		JSpinner port = new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1));
		NumberEditor editor = new NumberEditor(port, "#");
		port.setEditor(editor);
		
		JList<Protocols> protocol = new JList<Protocols>(Protocols.values());
		protocol.setSelectedIndex(0);
		
		Object[] message = {
				"Port:", port,
				"Protocol:", protocol
		};
		
		// Prompt user for settings
		int option = JOptionPane.showConfirmDialog(this, message, "Add Port", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			Object[] temp = { (Integer)port.getValue(), (Integer)port.getValue(), protocol.getSelectedValue() };
			model.addRow(temp);
			
			ports.add(new PortInfo(new Port((Integer)port.getValue()), protocol.getSelectedValue()));
		}
	}
	
	private void btnAddPortAdvClicked() {
		// Internal and external port UI inputs
		JSpinner portInternal = new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1));
		NumberEditor editor = new NumberEditor(portInternal, "#");
		portInternal.setEditor(editor);
		
		JSpinner portExternal = new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1));
		NumberEditor editor2 = new NumberEditor(portExternal, "#");
		portExternal.setEditor(editor2);
		
		JList<Protocols> protocol = new JList<Protocols>(Protocols.values());
		protocol.setSelectedIndex(0);
		
		Object[] message = {
				"Internal Port:", portInternal,
				"External Port:", portExternal,
				"Protocol:", protocol
		};
		
		// Prompt user for settings
		int option = JOptionPane.showConfirmDialog(this, message, "Add Port", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {			
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			Object[] temp = { (Integer)portInternal.getValue(), (Integer)portExternal.getValue(), protocol.getSelectedValue() };
			model.addRow(temp);
			
			ports.add(new PortInfo(new Port((Integer)portInternal.getValue(),
					(Integer)portExternal.getValue()), protocol.getSelectedValue()));
		}
	}
	
	private void btnAddPortRange() {
		// Port range UI inputs
		JSpinner portBegin = new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1));
		NumberEditor editor = new NumberEditor(portBegin, "#");
		portBegin.setEditor(editor);
		
		JSpinner portEnd = new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1));
		NumberEditor editor2 = new NumberEditor(portEnd, "#");
		portEnd.setEditor(editor2);
		
		JList<Protocols> protocol = new JList<Protocols>(Protocols.values());
		protocol.setSelectedIndex(0);
		
		Object[] message = {
				"Port Begin:", portBegin,
				"Port End:", portEnd,
				"Protocol:", protocol
		};
		
		// Prompt user for settings
		int option = JOptionPane.showConfirmDialog(this, message, "Add Port", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			if ((Integer)portBegin.getValue() > (Integer)portEnd.getValue()) {
				JOptionPane.showMessageDialog(this, "Begin port must be lower than end port!",
						"Invalid port mapping", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			String portText = (Integer)portBegin.getValue()+"->"+(Integer)portEnd.getValue();
			Object[] temp = { portText, portText, protocol.getSelectedValue() };
			model.addRow(temp);
			
			ports.add(new PortInfo(new Port((Integer)portBegin.getValue()),
					new Port((Integer)portEnd.getValue()),protocol.getSelectedValue()));
		}
	}
	
	private void btnDeletePortClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		// Remove port from list and UI table
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.removeRow(selectedIndex);
		ports.remove(selectedIndex);
	}
}

package com.moxeja.uupnp.ui;

import java.awt.Point;
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
import com.moxeja.uupnp.Main;
import com.moxeja.uupnp.datatypes.MappingEntry;
import com.moxeja.uupnp.datatypes.PortInfo;
import com.moxeja.uupnp.network.Protocols;

@SuppressWarnings("serial")
public class MappingInputForm extends JDialog {
	
	private JPanel contentPane;
	private JTable table;
	private JTextField txtName;
	
	private final int width = 373;
	private final int height = 351;
	
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
		scrollPane.setBounds(10, 36, 181, 224);
		contentPane.add(scrollPane);
		
		table = new JTable();
		String[] col = { "Ports", "Protocol" };
		table.setModel(new NoCellEditableModel(col, 0));
		scrollPane.setViewportView(table);
		
		JButton btnAddPort = new JButton("Add Port");
		btnAddPort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddPortClicked();
			}
		});
		btnAddPort.setBounds(201, 36, 156, 75);
		contentPane.add(btnAddPort);
		
		JButton btnAddPortRange = new JButton("Add Port Range");
		btnAddPortRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddPortRange();
			}
		});
		btnAddPortRange.setBounds(201, 122, 156, 75);
		contentPane.add(btnAddPortRange);
		
		JButton btnDeletePort = new JButton("Delete Port");
		btnDeletePort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnDeletePortClicked();
			}
		});
		btnDeletePort.setBounds(201, 208, 156, 52);
		contentPane.add(btnDeletePort);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(10, 11, 46, 14);
		contentPane.add(lblName);
		
		txtName = new JTextField();
		txtName.setHorizontalAlignment(SwingConstants.LEFT);
		txtName.setBounds(48, 8, 309, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnOkClicked();
			}
		});
		btnOk.setBounds(10, 271, 208, 40);
		contentPane.add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btnCancel.setBounds(228, 271, 129, 40);
		contentPane.add(btnCancel);
		
		// If being used in edit mode
		if (entry != null) {
			Main.LOGGER.log(LogSeverity.INFO, "Opened edit form.");
			txtName.setText(entry.getName());
			entry.getPorts().forEach((e) -> {
				ports.add(e);
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				Object[] temp = null;
				
				if (e.portRange.x == e.portRange.y)
					temp = new Object[] { e.portRange.x, e.protocol };
				else
					temp = new Object[] { e.portRange.x+"->"+e.portRange.y, e.protocol };
					
				model.addRow(temp);
			});
		}
	}
	
	private void btnOkClicked() {
		if (txtName.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Cannot leave the name field empty!",
					"Empty Name", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
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
			Object[] temp = { (Integer)port.getValue(), protocol.getSelectedValue() };
			model.addRow(temp);
			
			int portNumber = (Integer)port.getValue();
			ports.add(new PortInfo(new Point(portNumber, portNumber), protocol.getSelectedValue()));
		}
	}
	
	private void btnAddPortRange() {
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
			Object[] temp = { (Integer)portBegin.getValue()+"->"+(Integer)portEnd.getValue(), protocol.getSelectedValue() };
			model.addRow(temp);
			
			ports.add(new PortInfo(new Point((Integer)portBegin.getValue(), (Integer)portEnd.getValue()),
					protocol.getSelectedValue()));
		}
	}
	
	private void btnDeletePortClicked() {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1)
			return;
		
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.removeRow(selectedIndex);
		ports.remove(selectedIndex);
	}
}

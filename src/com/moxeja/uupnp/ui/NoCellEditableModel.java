package com.moxeja.uupnp.ui;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class NoCellEditableModel extends DefaultTableModel {

	public NoCellEditableModel() {
		super();
	}

	public NoCellEditableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
	}

	public NoCellEditableModel(Vector<Object> columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	public NoCellEditableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	public NoCellEditableModel(Vector<Object> data, Vector<Object> columnNames) {
		super(data, columnNames);
	}

	public NoCellEditableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}

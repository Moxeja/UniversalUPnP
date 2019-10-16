/*
 * Copyright (C) 2019  Moxeja
 * NoCellEditableModel.java is part of UniversalUPnP
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

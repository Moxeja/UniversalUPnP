package com.moxeja.uupnp.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/*
 * Author: Marcel
 * Link: https://stackoverflow.com/users/6256451/marcel
 */

@SuppressWarnings("serial")
public class MultiLineCellRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		int height = c.getPreferredSize().height;
		
		if (table.getRowHeight(row) < height) {
			table.setRowHeight(row, height);
		}
		return c;
	}
	
	@Override
	protected void setValue(Object value) {
		if (value instanceof String) {
			String val = (String)value;
			
			if (val.indexOf('\n') >= 0 && !(val.startsWith("<html>") && val.endsWith("</html>"))) {
				value = "<html><nobr>" + htmlEncodeLines(val) + "</nobr></html>";
			}
		}
		
		super.setValue(value);
	}
	
	protected static String htmlEncodeLines(String s) {
		int i = indexOfAny(s, "<>&\n", 0);
		if (i < 0)
			return s;
		
		StringBuffer sb = new StringBuffer(s.length() + 20);
		int j = 0;
		do {
			sb.append(s, j, i).append(htmlEncode(s.charAt(i)));
			i = indexOfAny(s, "<>&\n", j = i + 1);
		} while (i >= 0);
		
		sb.append(s, j, s.length());
		return sb.toString();
	}
	
	private static String htmlEncode(char c) {
		switch (c) {
		case '<': return "&lt;";
		case '>': return "&gt;";
		case '&': return "&amp;";
		case '\n': return "<br>";
		default: return Character.toString(c);
		} 
	}
	
	private static int indexOfAny(String s, String set, int start) {
		for (int i = start; i < s.length(); ++i) {
			if (set.indexOf(s.charAt(i)) >= 0)
				return i;
		}
		
		return -1;
	}
}

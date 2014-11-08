/**
 * TableModel to handle tables of frequencies used in scale and tuning objects.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.gui.util;

import javax.swing.table.DefaultTableModel;

/**
 * This table model handles tables for which all columns are of class Double,
 * except possibly the first and last columns.
 * @author Burton Patkau
 */
public class NumericTableModel extends DefaultTableModel
{
	Class<?> firstColumnClass;
	Class<?> finalColumnClass;

	/**
	 * Create a table model with specified classes for the first and last
	 * column, and class Double for all other columns.
	 * @param firstClass - the class of objects in the first table column
	 * @param finalClass - the class of objects in the final table column
	 */
	public NumericTableModel(Class<?> firstClass, Class<?> finalClass)
	{
		this.firstColumnClass = firstClass;
		this.finalColumnClass = finalClass;
	}

	/**
	 * Create a table model with String objects in the first column,
	 * and Double objects in all other columns.
	 */
	public NumericTableModel()
	{
		this(String.class,Double.class);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		if (columnIndex == 0)
		{
			return firstColumnClass;
		}
		if (columnIndex == getColumnCount()-1)
		{
			return finalColumnClass;
		}
		return Double.class;
	}
}

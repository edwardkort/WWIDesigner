/**
 * Class to support I/O formatting of an optional Double value.
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
package com.wwidesigner.util;

import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.text.DefaultFormatter;

public class DoubleFormatter extends DefaultFormatter
{
	boolean isOptional;
	DecimalFormat decFormatter;

	public DoubleFormatter()
	{
		super();
		setValueClass(Double.class);
		this.isOptional = false;
	}

	public DoubleFormatter(boolean aIsOptional)
	{
		super();
		setValueClass(Double.class);
		this.isOptional = aIsOptional;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.DefaultFormatter#stringToValue(java.lang.String)
	 */
	@Override
	public Object stringToValue(String string) throws ParseException
	{
		if (string == null || string.isEmpty())
		{
			if (isOptional)
			{
				return null;
			}
			throw new ParseException("Required field", 0);
		}
		return super.stringToValue(string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.DefaultFormatter#valueToString(java.lang.Object)
	 */
	@Override
	public String valueToString(Object value) throws ParseException
	{
		if (value == null)
		{
			if (isOptional)
			{
				return new String("");
			}
			return formatValue(0);
		}
		return formatValue(value);
	}

	public boolean isOptional()
	{
		return isOptional;
	}

	public void setOptional(boolean aIsOptional)
	{
		this.isOptional = aIsOptional;
	}

	@Override
	public DoubleFormatter clone()
	{
		DoubleFormatter myClone;
		try
		{
			myClone = (DoubleFormatter) super.clone();
			myClone.setOptional(isOptional);
			if (decFormatter != null)
			{
				int decDigits = decFormatter.getMaximumFractionDigits();
				myClone.setDecimalPrecision(decDigits);
			}
		}
		catch (CloneNotSupportedException e)
		{
			myClone = new DoubleFormatter(isOptional);
		}
		return myClone;
	}

	public void setDecimalPrecision(int decimalPrecision)
	{
		if (decFormatter == null)
		{
			decFormatter = new DecimalFormat();
		}
		decFormatter.setMinimumFractionDigits(decimalPrecision);
		decFormatter.setMaximumFractionDigits(decimalPrecision);
	}

	protected String formatValue(Object value) throws ParseException
	{
		if (decFormatter != null)
		{
			String text = decFormatter.format(value);
			return text;
		}
		return super.valueToString(value);
	}

}

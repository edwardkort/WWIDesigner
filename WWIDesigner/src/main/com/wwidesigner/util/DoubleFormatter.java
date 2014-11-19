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

import java.text.ParseException;

import javax.swing.text.DefaultFormatter;

public class DoubleFormatter extends DefaultFormatter
{
	boolean isOptional;

	public DoubleFormatter()
	{
		super();
		setValueClass(Double.class);
		this.isOptional = false;
	}
	
	public DoubleFormatter(boolean isOptional)
	{
		super();
		setValueClass(Double.class);
		this.isOptional = isOptional;
	}

	/* (non-Javadoc)
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
			else
			{
				throw new ParseException("Required field", 0);
			}
		}
		return super.stringToValue(string);
	}

	/* (non-Javadoc)
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
			else
			{
				return new String("0");
			}
		}
		return super.valueToString(value);
	}

	public boolean isOptional()
	{
		return isOptional;
	}

	public void setOptional(boolean isOptional)
	{
		this.isOptional = isOptional;
	}

	@Override
	public DoubleFormatter clone()
	{
		DoubleFormatter myClone;
		try
		{
			myClone = (DoubleFormatter) super.clone();
			myClone.setOptional(isOptional);
		}
		catch (CloneNotSupportedException e)
		{
			myClone = new DoubleFormatter(isOptional);
		}
		return myClone;
	}

}

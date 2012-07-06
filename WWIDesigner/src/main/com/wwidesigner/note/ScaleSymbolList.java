/**
 * 
 */
package com.wwidesigner.note;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kort
 * 
 */
public class ScaleSymbolList
{
	protected String name;
	protected String comment;
	protected List<String> scaleSymbol;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the scaleSymbol
	 */
	public List<String> getScaleSymbol()
	{
		if (scaleSymbol == null)
		{
			scaleSymbol = new ArrayList<String>();
		}
		return this.scaleSymbol;
	}

	/**
	 * @param scaleSymbol
	 *            the scaleSymbol to set
	 */
	public void setScaleSymbol(List<String> scaleSymbol)
	{
		this.scaleSymbol = scaleSymbol;
	}

	public void addScaleSymbol(String symbol)
	{
		getScaleSymbol();
		scaleSymbol.add(symbol);
	}
}

/**
 * 
 */
package com.wwidesigner.note.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author kort
 * 
 */
public class XmlTemperamentTest extends AbstractXmlTest<XmlTemperament>
{

	@Override
	public void setVariables()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/Equal_Temperament.xml";
		outputSymbolXML = "Equal_Temperament_test.xml";
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlTemperament#getName()}.
	 */
	@Test
	public final void testGetName()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			String temperamentName = inputElement.getName();
			assertEquals("Temperament name incorrect", "Equal Temperament",
					temperamentName);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlTemperament#getComment()}.
	 */
	@Test
	public final void testGetComment()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			String temperamentComment = inputElement.getComment();
			assertEquals("Temperament comment incorrect",
					"Chromatic, equal temperament", temperamentComment);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlTemperament#getInterval()}.
	 */
	@Test
	public final void testGetInterval()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			double interval = inputElement.getInterval().get(1);
			assertEquals("Temperament interval incoorect", 1.059, interval, 0.001);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}

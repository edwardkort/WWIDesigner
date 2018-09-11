/**
 * 
 */
package com.wwidesigner.note.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.util.AbstractXmlTest;

/**
 * @author kort
 * 
 */
public class TemperamentTest extends AbstractXmlTest<Temperament>
{

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.Temperament#getName()}.
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
	 * {@link com.wwidesigner.note.bind.Temperament#getComment()}.
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
	 * {@link com.wwidesigner.note.bind.Temperament#getRatio()}.
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
			double interval = inputElement.getRatio().get(1);
			assertEquals("Temperament interval incoorect", 1.059, interval,
					0.001);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Override
	protected void setInputSymbolXML()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/Equal_Temperament.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "Equal_Temperament_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = NoteBindFactory.getInstance();

	}

}

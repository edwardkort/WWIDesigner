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
public class XmlScaleSymbolListTest extends AbstractXmlTest<XmlScaleSymbolList>
{

	@Override
	public void setVariables()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/Western_Chromatic_Symbols.xml";
		outputSymbolXML = "Western_Chromatic_Symbols_test.xml";
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlScaleSymbolList#getName()}.
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
			String symbolListName = inputElement.getName();
			assertEquals("List name incoorect", "Western Chromatic Symbols",
					symbolListName);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlScaleSymbolList#getComment()}.
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
			String symbolListComment = inputElement.getComment();
			assertEquals("List comment incoorect",
					"A symbol set with no indication of octave",
					symbolListComment);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlScaleSymbolList#getScaleSymbol()}.
	 */
	@Test
	public final void testGetScaleSymbol()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			String firstSymbol = inputElement.getScaleSymbol().get(0);
			assertEquals("List symbol incoorect", "C", firstSymbol);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}

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
public class ScaleSymbolListTest extends AbstractXmlTest<ScaleSymbolList>
{

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.ScaleSymbolList#getName()}.
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
	 * {@link com.wwidesigner.note.bind.ScaleSymbolList#getComment()}.
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
	 * {@link com.wwidesigner.note.bind.ScaleSymbolList#getScaleSymbol()}.
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

	@Override
	protected void setInputSymbolXML()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/Western_Chromatic_Symbols.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "Western_Chromatic_Symbols_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = NoteBindFactory.getInstance();

	}

}

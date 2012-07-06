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
public class ScaleTest extends AbstractXmlTest<Scale>
{

	/**
	 * Test method for {@link com.wwidesigner.note.bind.XmlScale#getName()}.
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
			String scaleName = inputElement.getName();
			assertEquals("Scale name incorrect", "A pentatonic minor scale",
					scaleName);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.wwidesigner.note.bind.XmlScale#getComment()}.
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
			String scaleComment = inputElement.getComment();
			assertEquals(
					"Scale comment incorrect",
					"Key of A, equal temperament, only the pentatonic minor notes",
					scaleComment);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.wwidesigner.note.bind.XmlScale#getNote()}.
	 */
	@Test
	public final void testGetNote()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			Note note = inputElement.getNote().get(2);
			assertEquals("Note name incorrect", "D", note.getName());
			assertEquals("Note frequency incorrect", 587.33,
					note.getFrequency(), 0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Override
	protected void setInputSymbolXML()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/A_pentatonic_minor_scale.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "A_pentatonic_minor_scale_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = new NoteBindFactory();

	}

}

/**
 * 
 */
package com.wwidesigner.note.bind;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author kort
 * 
 */
public class XmlScaleTest extends AbstractXmlTest<XmlScale>
{

	@Override
	public void setVariables()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/A_pentatonic_minor_scale.xml";
		outputSymbolXML = "A_pentatonic_minor_scale_test.xml";
	}

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
			XmlNote note = inputElement.getNote().get(2);
			assertEquals("Note name incorrect", "D", note.getName());
			assertEquals("Note frequency incorrect", 587.33,
					note.getFrequency(), 0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}

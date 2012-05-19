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
public class XmlFingeringPatternTest extends
		AbstractXmlTest<XmlFingeringPattern>
{

	@Override
	public void setVariables()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/5-hole_NAF_standard_fingering.xml";
		outputSymbolXML = "5-hole_NAF_standard_fingering_test.xml";
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlFingeringPattern#getName()}.
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
			String patternName = inputElement.getName();
			assertEquals("pattern name incorrect",
					"5-hole NAF standard fingering", patternName);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlFingeringPattern#getComment()}.
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
			String patternComment = inputElement.getComment();
			assertEquals("Pattern comment incorrect",
					"Only the pentatonic minor notes in Nakai tab",
					patternComment);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlFingeringPattern#getNumberOfHoles()}.
	 */
	@Test
	public final void testGetNumberOfHoles()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			int numberOfHoles = inputElement.getNumberOfHoles().intValue();
			assertEquals("Pattern hole count incorrect", 5, numberOfHoles);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.XmlFingeringPattern#getFingering()}.
	 */
	@Test
	public final void testGetFingering()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			XmlFingering fingering = inputElement.getFingering().get(3);
			assertEquals("Fingering note name incorrect", "C#", fingering
					.getNote().getName());
			assertEquals("Hole 4 status incorrect", false, fingering
					.getOpenHole().get(3));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}

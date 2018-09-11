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
public class TuningTest extends AbstractXmlTest<Tuning>
{

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.FingeringPattern#getName()}.
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
					"A 5-hole NAF standard tuning", patternName);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.FingeringPattern#getComment()}.
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
			assertEquals(
					"Pattern comment incorrect",
					"Key of A, equal temperament, only the pentatonic minor notes",
					patternComment);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.wwidesigner.note.bind.FingeringPattern#getNumberOfHoles()}.
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
	 * {@link com.wwidesigner.note.bind.FingeringPattern#getFingering()}.
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
			Fingering fingering = inputElement.getFingering().get(3);
			assertEquals("Fingering note name incorrect", "E", fingering
					.getNote().getName());
			assertEquals("Fingering note frequency incorrect", 659.25,
					fingering.getNote().getFrequency(), 0.01);
			assertEquals("Hole 4 status incorrect", false, fingering
					.getOpenHole().get(3));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Override
	protected void setInputSymbolXML()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/A_5-hole_NAF_standard_tuning.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "A_5-hole_NAF_standard_tuning_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = NoteBindFactory.getInstance();

	}

}

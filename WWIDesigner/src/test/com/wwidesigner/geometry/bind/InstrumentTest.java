/**
 * 
 */
package com.wwidesigner.geometry.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.util.AbstractXmlTest;

/**
 * @author kort
 * 
 */
public class InstrumentTest extends AbstractXmlTest<Instrument>
{
	@Test
	public void testGetName()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			String scaleName = inputElement.getName();
			assertEquals("Instrument name incorrect", "D NAF", scaleName);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

	}

	@Test
	public final void testGetDescription()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			String instrumentDescription = inputElement.getDescription();
			assertEquals(
					"Instrument description incorrect",
					"7-hole NAF, key of D, A=432",
					instrumentDescription);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Override
	protected void setInputSymbolXML()
	{
		inputSymbolXML = "com/wwidesigner/geometry/bind/example/D_NAF.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "D_NAF_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = new GeometryBindFactory();
	}

}

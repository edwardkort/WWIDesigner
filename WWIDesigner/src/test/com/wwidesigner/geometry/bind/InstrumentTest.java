/**
 * 
 */
package com.wwidesigner.geometry.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

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
			String instrumentName = inputElement.getName();
			assertEquals("Instrument name incorrect", "D NAF", instrumentName);
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
			assertEquals("Instrument description incorrect",
					"7-hole NAF, key of D, A=432", instrumentDescription);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testGetBorePoints()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			List<BorePoint> borePoints = inputElement.getBorePoint();
			int numberOfBorePoints = borePoints.size();
			assertEquals("Number of bore points incorrect", 3,
					numberOfBorePoints);
			BorePoint borePoint2 = borePoints.get(1);
			double position = borePoint2.getBorePosition();
			assertEquals("Bore point 2 position incorrect", 9.8, position,
					0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testGetFippleWindowLength()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			double windowLength = inputElement.getMouthpiece().getFipple()
					.getWindowLength();
			assertEquals("Window length incorrect", 0.15, windowLength, 0.01);
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
		bindFactory = GeometryBindFactory.getInstance();
	}

}

/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.InstrumentConfigurator;
import com.wwidesigner.geometry.Termination;

/**
 * @author kort
 * 
 */
public class WhistleConfigurator extends InstrumentConfigurator
{

	@Override
	protected void setMouthpieceCalculator()
	{
		this.mouthpieceCalculator = new SimpleFippleMouthpieceCalculator(
				instrument.getMouthpiece());
	}

	@Override
	protected void setTerminationCalculator()
	{
		Termination termination = instrument.getTermination();
		this.terminationCalculator = new FlangedEndCalculator(termination);
		if (termination.getBoreDiameter() <= 0.0)
		{
			// Retrieve bore diameter from final bore section.
			List<BorePoint> borePoints = instrument.getBorePoint();
			termination.setBoreDiameter(borePoints.get(borePoints.size() - 1)
					.getBoreDiameter());
		}
	}

	@Override
	protected void setHoleCalculator()
	{
		this.holeCalculatorClass = WhistleHoleCalculator.class;
	}

	@Override
	protected void setBoreSectionCalculator()
	{
		this.boreSectionCalculatorClass = DefaultBoreSectionCalculator.class;
	}
}

package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for total bore length.
 * @author Burton
 *
 */
public class LengthObjectiveFunction extends BaseObjectiveFunction
{

	public LengthObjectiveFunction(InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer;		// UnivariateOptimizer
		
		// Length cannot be shorter than position of lowest hole.

		PositionInterface[] holeList = Instrument.sortList(calculator.getInstrument().getHole());
		PositionInterface endHole = holeList[holeList.length-1];
		lowerBounds = new double[1];
		lowerBounds[0] = endHole.getBorePosition();
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];
		PositionInterface[] boreList = Instrument.sortList(calculator.getInstrument().getBorePoint());
		BorePoint endPoint = (BorePoint) boreList[boreList.length-1];
		geometry[0] = endPoint.getBorePosition();
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if ( point.length != nrDimensions ) {
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		PositionInterface[] boreList = Instrument.sortList(calculator.getInstrument().getBorePoint());
		BorePoint endPoint = (BorePoint) boreList[boreList.length-1];
		endPoint.setBorePosition(point[0]);
		calculator.getInstrument().updateComponents();
	}

}

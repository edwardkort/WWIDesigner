/**
 * Class to graph instrument playing characteristics for a given tuning.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.modelling;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.complex.Complex;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.PointShape;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.util.Constants;

/**
 * Class to plot the impedance pattern of an instrument when played with a specified tuning.
 * Marks minimum, maximum, and nominal playing frequencies if the instrument
 * provides them, and the position of the target tuning frequency
 * within the range of a note.
 * Call buildGraph(), then plotGraph().
 */
public class PlotPlayingRanges
{
	protected String mName;
	protected Chart chart;
	
	protected static final Color darkGreen = new Color(0,192,0);
	protected static final Color darkYellow = new Color(255,192,0);

	public PlotPlayingRanges()
	{
		mName = null;
	}

	public PlotPlayingRanges(String title)
	{
		mName = title;
	}
	
	/**
	 * Ensure a value falls within a specified range.
	 * @param x
	 * @param xMin
	 * @param xMax
	 * @return value of x, clamped to range xMin..xMax.
	 */
	protected static double clamp(double x, double xMin, double xMax)
	{
		if ( xMin >= xMax )
		{
			// We have no proper bounds.  No clamping.
			return x;
		}
		if ( x < xMin )
		{
			return xMin;
		}
		if ( x > xMax )
		{
			return xMax;
		}
		return x;
	}
	
	protected static final String Y_VALUE_NAME = "Reactance Ratio, X/R";
	/**
	 * Calculate a y value for a point on the graph.
	 * @param calculator - instrument calculator to calculate y value.
	 * @param freq - frequency at which to calculate y value.
	 * @return y value.
	 */
	protected static double yValue(InstrumentCalculator calculator, double freq)
	{
		Complex z = calculator.calcZ(freq);
		return z.getImaginary()/z.getReal();
	}

	/**
	 * Return true if f is a tonic or dominant in the key fLow.
	 * @param f
	 * @param fLow
	 */
	protected static boolean isMarker(double f, double fLow )
	{
		double logNote = Math.log(f/fLow)/Constants.LOG2;
		int octaves = (int)logNote;
		double semitones = 12.0 * ( logNote - (double)octaves );
		if ( semitones < 0.5 || semitones > 11.5 )
		{
			return true;
		}
		if ( 6.5 < semitones && semitones < 7.5 )
		{
			return true;
		}
		return false;
	}
	/**
	 * Collect the data necessary to graph the predicted tuning for an instrument.
	 * Following this call, use plotGraph() to display the graph.
	 * @param calculator - an impedance calculator for the instrument
	 * @param target - target tuning
	 * @param predicted - predicted tuning from the specified calculator,
	 * 			for each note in target tuning.
	 */
	public void buildGraph(InstrumentCalculator calculator, Tuning target, Tuning predicted)
	{
		if ( mName == null )
		{
			if ( calculator.instrument.getName() != null )
			{
				mName = calculator.instrument.getName();
			}
			else 
			{
				mName = "Instrument";
			}
		}
		chart = new Chart();
		chart.setTitle("Impedance Pattern");
		chart.setAutoRanging(true);
		chart.getXAxis().setLabel("Frequency");
		chart.getYAxis().setLabel(Y_VALUE_NAME);
//		Legend legend = new Legend(chart);
//		chart.addDrawable(legend);
//		legend.setLocation(200, 50);

		int idx;						// Index into notes of target and predicted.
		
		// Find bounds of graph quantities.

		double lowestF = Double.POSITIVE_INFINITY;	// Frequency of lowest target note.
		Fingering predFingering;		// Predicted fingering at index idx.
		Note tgt;						// Target note at index idx.
		Note pred;						// Predicted note at index idx.
		double f;						// Frequency.
		double y;						// y (vertical axis) value at a particular frequency.
		double minY = 0.0;				// Minimum y value.
		double maxY = 0.0;				// Maximum y value.

		for (idx = 0; idx < target.getFingering().size(); idx++)
		{
			tgt  = target.getFingering().get(idx).getNote();
			predFingering = predicted.getFingering().get(idx);
			pred = predFingering.getNote();
			if ( tgt.getFrequency() != null && tgt.getFrequency() < lowestF )
			{
				lowestF = tgt.getFrequency();
			}
			calculator.setFingering(predFingering);
			if ( pred.getFrequencyMin() != null )
			{
				y = yValue(calculator, pred.getFrequencyMin());
				if ( y < minY )
				{
					minY = y;
				}
				if ( y > maxY )
				{
					maxY = y;
				}
			}
			if ( pred.getFrequencyMax() != null )
			{
				y = yValue(calculator, pred.getFrequencyMax());
				if ( y < minY )
				{
					minY = y;
				}
				if ( y > maxY )
				{
					maxY = y;
				}
			}
		}
		if (maxY > minY)
		{
			// Add a 10% margin outside of the bounds found.
			double range = maxY - minY;
			maxY += 0.10 * range;
			minY -= 0.10 * range;
		}

		ChartStyle styleTarget  = new ChartStyle(darkGreen, PointShape.DISC, 7);
		ChartStyle styleTargetOver  = new ChartStyle(Color.red, PointShape.UP_TRIANGLE, 9);
		ChartStyle styleTargetHigh  = new ChartStyle(darkYellow, PointShape.UP_TRIANGLE, 9);
		ChartStyle styleTargetLow   = new ChartStyle(darkYellow, PointShape.DOWN_TRIANGLE, 9);
		ChartStyle styleTargetUnder = new ChartStyle(Color.red, PointShape.DOWN_TRIANGLE, 9);
		ChartStyle styleNominal = new ChartStyle(Color.blue, PointShape.CIRCLE);
		ChartStyle styleMinmax  = new ChartStyle(Color.black, PointShape.DIAMOND);
		ChartStyle styleRange   = new ChartStyle(Color.black, false, true);
		ChartStyle styleMinmaxMarked = new ChartStyle(Color.blue, PointShape.DIAMOND);
		ChartStyle styleRangeMarked  = new ChartStyle(Color.blue, false, true);

		DefaultChartModel targetModel  = new DefaultChartModel();
		DefaultChartModel targetModelOver  = new DefaultChartModel();
		DefaultChartModel targetModelHigh  = new DefaultChartModel();
		DefaultChartModel targetModelLow   = new DefaultChartModel();
		DefaultChartModel targetModelUnder = new DefaultChartModel();
		DefaultChartModel nominalModel = new DefaultChartModel();
		DefaultChartModel minmaxModel  = new DefaultChartModel();
		DefaultChartModel minmaxModelMarked = new DefaultChartModel();
		boolean isMarkerNote;		// True if target note is tonic or dominant.

		for (idx = 0; idx < target.getFingering().size(); idx++)
		{
			tgt  = target.getFingering().get(idx).getNote();
			predFingering = predicted.getFingering().get(idx);
			pred = predFingering.getNote();
			calculator.setFingering(predFingering);
			if ( tgt.getFrequency() != null )
			{
				f = tgt.getFrequency();
				y = yValue(calculator, f);
				y = clamp(y,minY,maxY);
				if (pred.getFrequencyMax() != null && f > pred.getFrequencyMax())
				{
					targetModelOver.addPoint(f, y);
				}
				else if  (pred.getFrequencyMin() != null && f < pred.getFrequencyMin())
				{
					targetModelUnder.addPoint(f, y);
				}
				else if ( pred.getFrequencyMin() != null && pred.getFrequencyMax() != null )
				{
					double ratio = (f - pred.getFrequencyMin())
							/ (pred.getFrequencyMax() - pred.getFrequencyMin());
					if ( ratio > 0.9 )
					{
						targetModelHigh.addPoint(f, y);
					}
					else if ( ratio < 0.1 )
					{
						targetModelLow.addPoint(f, y);
					}
					else
					{
						targetModel.addPoint(f, y);
					}
				}
				else
				{
					targetModel.addPoint(f, y);
				}
				isMarkerNote = isMarker(f, lowestF);
			}
			else
			{
				isMarkerNote = false;
			}
			if ( pred.getFrequency() != null
				&& ( tgt.getFrequency() == null || pred.getFrequency() != tgt.getFrequency() ) )
			{
				y = yValue(calculator, pred.getFrequency());
				y = clamp(y,minY,maxY);
				nominalModel.addPoint(pred.getFrequency(), y);
			}
			if ( pred.getFrequencyMin() != null )
			{
				f = pred.getFrequencyMin();
				y = yValue(calculator, f);
				y = clamp(y,minY,maxY);
				if (isMarkerNote)
				{
					minmaxModelMarked.addPoint(f, y );
				}
				else
				{
					minmaxModel.addPoint(f, y );
				}
			}
			if ( pred.getFrequencyMax() != null )
			{
				f = pred.getFrequencyMax();
				y = yValue(calculator, f);
				y = clamp(y,minY,maxY);
				if (isMarkerNote)
				{
					minmaxModelMarked.addPoint(f, y );
				}
				else
				{
					minmaxModel.addPoint(f, y );
				}
			}
			if ( pred.getFrequencyMin() != null && pred.getFrequencyMax() != null )
			{
				DefaultChartModel rangeModel  = new DefaultChartModel();
				double step = (pred.getFrequencyMax() - pred.getFrequencyMin())/32.0;
				f = pred.getFrequencyMin();
				for (int i = 0; i <= 32; i++ )
				{
					y = yValue(calculator, f);
					rangeModel.addPoint(f, y);
					f += step;
				}
				if (isMarkerNote)
				{
					chart.addModel(rangeModel, styleRangeMarked);
				}
				else
				{
					chart.addModel(rangeModel, styleRange);
				}
			}
			else if ( tgt.getFrequency() != null && pred.getFrequency() != null
					&& tgt.getFrequency() != pred.getFrequency() )
			{
				DefaultChartModel rangeModel  = new DefaultChartModel();
				double step = (tgt.getFrequency() - pred.getFrequency())/32.0;
				f = pred.getFrequency();
				for (int i = 0; i <= 32; i++ )
				{
					y = yValue(calculator, f);
					y = clamp(y,minY,maxY);
					rangeModel.addPoint(f, y);
					f += step;
				}
				if (isMarkerNote)
				{
					chart.addModel(rangeModel, styleRangeMarked);
				}
				else
				{
					chart.addModel(rangeModel, styleRange);
				}
			}
		}

		if (minmaxModel.getPointCount() > 0)
		{
			chart.addModel(minmaxModel, styleMinmax);
			chart.addModel(minmaxModelMarked, styleMinmaxMarked);
		}
		if (nominalModel.getPointCount() > 0)
		{
			chart.addModel(nominalModel, styleNominal);
		}

		if (targetModel.getPointCount() > 0)
		{
			chart.addModel(targetModel, styleTarget);
		}
		if (targetModelOver.getPointCount() > 0)
		{
			chart.addModel(targetModelOver, styleTargetOver);
		}
		if (targetModelHigh.getPointCount() > 0)
		{
			chart.addModel(targetModelHigh, styleTargetHigh);
		}
		if (targetModelLow.getPointCount() > 0)
		{
			chart.addModel(targetModelLow, styleTargetLow);
		}
		if (targetModelUnder.getPointCount() > 0)
		{
			chart.addModel(targetModelUnder, styleTargetUnder);
		}
	}

	/**
	 * Display the graph generated in buildGraph().
	 * @param exitOnClose - If true, the application will exit when the user closes the plot window.
	 */
	public void plotGraph(final boolean exitOnClose)
	{
		final Chart graph = chart;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Impedance Pattern for " + mName);
				if (exitOnClose)
				{
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
				frame.setSize(800, 600);
				
				frame.setContentPane(graph);
				frame.setVisible(true);
			}
		});
	}


}

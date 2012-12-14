/**
 * 
 */
package com.wwidesigner.modelling;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.PointShape;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.util.Constants;

/**
 * Representation of a complex spectrum, along with information about its
 * extreme points.
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
		chart.setTitle("Reactance Pattern");
		chart.setAutoRanging(true);
		chart.getXAxis().setLabel("Frequency");
		chart.getYAxis().setLabel("Reactance");
//		Legend legend = new Legend(chart);
//		chart.addDrawable(legend);
//		legend.setLocation(200, 50);

		int idx;						// Index into notes of target and predicted.
		
		// Find bounds of graph quantities.

		double lowestF = Double.POSITIVE_INFINITY;	// Frequency of lowest target note.
		double minX = 0.0;				// Minimum value of X found.
		double maxX = 0.0;				// Maximum value of X found.
		Fingering predFingering;		// Predicted fingering at index idx.
		Note tgt;						// Target note at index idx.
		Note pred;						// Predicted note at index idx.
		double f;
		double x;						// Reactance at a particular frequency.

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
				x = calculator.calcZ(pred.getFrequencyMin()).getImaginary();
				if ( x < minX )
				{
					minX = x;
				}
				if ( x > maxX )
				{
					maxX = x;
				}
			}
			if ( pred.getFrequencyMax() != null )
			{
				x = calculator.calcZ(pred.getFrequencyMax()).getImaginary();
				if ( x < minX )
				{
					minX = x;
				}
				if ( x > maxX )
				{
					maxX = x;
				}
			}
		}
		if (maxX > minX)
		{
			// Add a 10% margin outside of the bounds found.
			double range = maxX - minX;
			maxX += 0.10 * range;
			minX -= 0.10 * range;
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
				x = calculator.calcZ(f).getImaginary();
				x = clamp(x,minX,maxX);
				if (pred.getFrequencyMax() != null && f > pred.getFrequencyMax())
				{
					targetModelOver.addPoint(f, x);
				}
				else if  (pred.getFrequencyMin() != null && f < pred.getFrequencyMin())
				{
					targetModelUnder.addPoint(f, x);
				}
				else if ( pred.getFrequencyMin() != null && pred.getFrequencyMax() != null )
				{
					double ratio = (f - pred.getFrequencyMin())
							/ (pred.getFrequencyMax() - pred.getFrequencyMin());
					if ( ratio > 0.9 )
					{
						targetModelHigh.addPoint(f, x);
					}
					else if ( ratio < 0.1 )
					{
						targetModelLow.addPoint(f, x);
					}
					else
					{
						targetModel.addPoint(f, x);
					}
				}
				else
				{
					targetModel.addPoint(f, x);
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
				x = calculator.calcZ(pred.getFrequency()).getImaginary();
				x = clamp(x,minX,maxX);
				nominalModel.addPoint(pred.getFrequency(), x);
			}
			if ( pred.getFrequencyMin() != null )
			{
				f = pred.getFrequencyMin();
				x = calculator.calcZ(f).getImaginary();
				x = clamp(x,minX,maxX);
				if (isMarkerNote)
				{
					minmaxModelMarked.addPoint(f, x );
				}
				else
				{
					minmaxModel.addPoint(f, x );
				}
			}
			if ( pred.getFrequencyMax() != null )
			{
				f = pred.getFrequencyMax();
				x = calculator.calcZ(f).getImaginary();
				x = clamp(x,minX,maxX);
				if (isMarkerNote)
				{
					minmaxModelMarked.addPoint(f, x );
				}
				else
				{
					minmaxModel.addPoint(f, x );
				}
			}
			if ( pred.getFrequencyMin() != null && pred.getFrequencyMax() != null )
			{
				DefaultChartModel rangeModel  = new DefaultChartModel();
				double step = (pred.getFrequencyMax() - pred.getFrequencyMin())/32.0;
				f = pred.getFrequencyMin();
				for (int i = 0; i <= 32; i++ )
				{
					x = calculator.calcZ(f).getImaginary();
					rangeModel.addPoint(f, x);
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
					x = calculator.calcZ(f).getImaginary();
					x = clamp(x,minX,maxX);
					rangeModel.addPoint(f, x);
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

	public void plotGraph(final boolean exitOnClose )
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

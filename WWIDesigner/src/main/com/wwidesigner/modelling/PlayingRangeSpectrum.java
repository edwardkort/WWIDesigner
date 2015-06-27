/**
 * 
 */
package com.wwidesigner.modelling;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.complex.Complex;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.Legend;
import com.jidesoft.chart.PointShape;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * Class to plot the impedance and playing ranges of an instrument
 * over a range of frequencies with specified fingering.
 * Includes the ratio of X/R, and the estimated loop gain.
 */
public class PlayingRangeSpectrum
{
	protected String mName;
	protected List<Double> actuals;
	/**
	 * Holds impedance spectrum (created by calcImpedance()).
	 */
	protected Map<Double, Complex> mImpedance;

	/**
	 * Holds loop gain spectrum (created by calcImpedance()).
	 */
	protected Map<Double, Double> mGain;

	/**
	 * Add or replace a point in the spectrum.
	 */
	protected void setDataPoint(double frequency, Complex impedance, Double loopGain)
	{
		mImpedance.put(frequency, impedance);
		mGain.put(frequency, loopGain);
	}

	protected void calcImpedance(InstrumentCalculator calculator, Fingering fingering,
			double freqStart, double freqEnd, int nfreq)
	{
		Note myNote = fingering.getNote();
		if ( myNote.getName() != null )
		{
			mName = myNote.getName();
		}
		else if ( calculator.instrument.getName() != null )
		{
			mName = calculator.instrument.getName();
		}
		else 
		{
			mName = "Instrument";
		}
		actuals = new ArrayList<Double>();

		if ( myNote.getFrequencyMin() != null )
		{
			actuals.add(myNote.getFrequencyMin());
		}
		if ( myNote.getFrequencyMax() != null )
		{
			actuals.add(myNote.getFrequencyMax());
		}
		if ( actuals.size() < 2 && myNote.getFrequency() != null )
		{
			actuals.add(myNote.getFrequency());
		}
		calculator.setFingering(fingering);
		mImpedance = new TreeMap<Double, Complex>();
		mGain = new TreeMap<Double, Double>();
		double freqStep = (freqEnd - freqStart) / (nfreq - 1);
		for (int i = 0; i < nfreq; ++i)
		{
			double freq = freqStart + i * freqStep;
			Complex zAc = calculator.calcZ(freq);
			Double gain = calculator.calcGain(freq, zAc);
			setDataPoint(freq, zAc, gain );
		}
	}

	protected Map<Double, Complex> getSpectrum()
	{
		return mImpedance;
	}

	protected void setSpectrum(Map<Double, Complex> spectrum)
	{
		mImpedance = spectrum;
	}

	protected void plotImpedanceSpectrum(final boolean exitOnClose)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Impedance Spectrum for " + mName);
				if (exitOnClose)
				{
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
				frame.setSize(800, 600);
				DefaultChartModel modelReal = new DefaultChartModel("Real");
				DefaultChartModel modelImag = new DefaultChartModel("Imaginary");
				for (Map.Entry<Double, Complex> point : mImpedance.entrySet())
				{
					double x = point.getKey();
					double r = point.getValue().getReal();
					double i = point.getValue().getImaginary();
					modelReal.addPoint(x, r);
					modelImag.addPoint(x, i);
				}
				Chart chart = new Chart();
				chart.setAutoRanging(true);
				ChartStyle styleReal = new ChartStyle(Color.blue, false, true);
				ChartStyle styleImag = new ChartStyle(Color.red, false, true);
				chart.addModel(modelReal, styleReal);
				chart.addModel(modelImag, styleImag);
				chart.getXAxis().setLabel("Frequency");
				chart.getYAxis().setLabel("Impedance");
				chart.setTitle("Impedance Spectrum");
				Legend legend = new Legend(chart);
				chart.addDrawable(legend);
				legend.setLocation(200, 50);
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}

	protected void plotPlayingRange(final boolean exitOnClose)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Note Spectrum for " + mName);
				if (exitOnClose)
				{
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
				frame.setSize(800, 600);
				DefaultChartModel modelRatio = new DefaultChartModel(
						"Impedance Imag/Real");
				DefaultChartModel modelGain = new DefaultChartModel(
						"Loop Gain >= 1");
				DefaultChartModel modelGainLow = new DefaultChartModel(
						"Loop Gain < 1");
				DefaultChartModel modelActuals = new DefaultChartModel(
						"Actual Frequency");
				for (Map.Entry<Double, Complex> point : mImpedance.entrySet())
				{
					double x = point.getKey();
					double r = point.getValue().getReal();
					double i = point.getValue().getImaginary();
					modelRatio.addPoint(x, i / r);
				}
				for (Map.Entry<Double, Double> point : mGain.entrySet())
				{
					double x = point.getKey();
					double g = point.getValue();
					if ( g >= 1.0 )
					{
						modelGain.addPoint(x, g);
					}
					else
					{
						modelGainLow.addPoint(x, g);
					}
				}
				for ( Double freq : actuals )
				{
					modelActuals.addPoint(freq,-0.4);
				}

				Chart chart = new Chart();
				chart.setAutoRanging(true);
				ChartStyle styleRatio = new ChartStyle(Color.black, false, true);
				ChartStyle styleGain  = new ChartStyle(Color.green, PointShape.CIRCLE);
				ChartStyle styleGainLow  = new ChartStyle(Color.red, PointShape.CIRCLE);
				chart.addModel(modelRatio, styleRatio);
				chart.addModel(modelGain, styleGain);
				chart.addModel(modelGainLow, styleGainLow);
				if ( actuals.size() > 0 )
				{
					ChartStyle styleActuals  = new ChartStyle(Color.yellow, PointShape.DIAMOND, Color.yellow);
					chart.addModel(modelActuals,styleActuals);
				}
				chart.getXAxis().setLabel("Frequency");
				chart.getYAxis().setLabel("Impedance Ratio, Gain");
				chart.setTitle("Note Spectrum");
				Legend legend = new Legend(chart);
				chart.addDrawable(legend);
				legend.setLocation(200, 50);
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Plot the impedance and playing ranges for a given calculator
	 * and fingering, using default plot parameters.
	 * @param calculator
	 * @param fingering
	 */
	public void plot(InstrumentCalculator calculator, Fingering fingering)
	{
		plot(calculator, fingering, 4./3., 600, true);
	}

	/**
	 * Plot the impedance and playing ranges for a given calculator
	 * and fingering, using default number of points.
	 * @param calculator
	 * @param fingering
	 * @param freqRange - Relative range of frequencies to plot above and below fingering.
	 */
	public void plot(InstrumentCalculator calculator, Fingering fingering, 
			double freqRange)
	{
		plot(calculator, fingering, freqRange, 600, true);
	}

	/**
	 * Plot the impedance and playing ranges for a given calculator and fingering.
	 * @param calculator
	 * @param fingering
	 * @param freqRange - Relative range of frequencies to plot above and below fingering.
	 * @param numberPoints - number of points to calculate for plotting.
	 */
	public void plot(InstrumentCalculator calculator, Fingering fingering, 
			double freqRange, int numberPoints, final boolean exitOnClose)
	{
		double targetFreq;
		if ( fingering.getNote().getFrequency() != null )
		{
			targetFreq = fingering.getNote().getFrequency();
		}
		else if ( fingering.getNote().getFrequencyMax() != null )
		{
			targetFreq = fingering.getNote().getFrequencyMax();
		}
		else {
			targetFreq = 1000.0;
		}
		double freqStart = targetFreq / freqRange;
		double freqEnd = targetFreq * freqRange;
		if ( freqEnd > 4000.0 )
		{
			freqEnd = 4000.0;
		}
		calcImpedance(calculator, fingering, freqStart, freqEnd,
				numberPoints);
		// plotImpedanceSpectrum();
		plotPlayingRange(exitOnClose);

	}
	
	/**
	 * Plot the impedance and playing range for each note in a given list of notes,
	 * for a given calculator and fingering.
	 * @param calculator
	 * @param tuning
	 * @param noteList
	 */
	public static void plotNotes( InstrumentCalculator calculator, Tuning tuning,
			int[] noteList )
	{
		for ( int noteIndex : noteList )
		{
			PlayingRangeSpectrum spectrum = new PlayingRangeSpectrum();
			spectrum.plot(calculator, tuning.getFingering().get(noteIndex));
		}
	}

}

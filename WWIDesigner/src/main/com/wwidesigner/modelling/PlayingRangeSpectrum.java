/**
 * 
 */
package com.wwidesigner.modelling;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.Legend;
import com.jidesoft.chart.PointShape;
import com.jidesoft.chart.ZeroAlignedAutoRanger;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.AxisPlacement;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * Class to plot the impedance and playing ranges of an instrument over a range
 * of frequencies with specified fingering. Includes the ratio of X/R, and the
 * estimated loop gain.
 */
public class PlayingRangeSpectrum
{
	protected String mName;
	protected List<Double> actuals; // min/max frequency if available, nominal
									// frequency otherwise.
	protected boolean hasMinMax;	// false if actuals contains min/max, true if
									// actuals contains nominal.
	protected List<Double> harmonics;	// Harmonics of target or nominal frequency.
	/**
	 * Holds impedance spectrum (created by calcImpedance()).
	 */
	protected Map<Double, Complex> mImpedance;

	/**
	 * Holds loop gain spectrum (created by calcImpedance()).
	 */
	protected Map<Double, Double> mGain;

	/**
	 * Holds loop gain maxima
	 */
	public Map<Double, Double> mGainMaxima;

	private int dataPointIndex;
	private double prevFreq;
	private double prevLoopGain;
	private double prevPrevLoopGain;

	/**
	 * Add or replace a point in the spectrum.
	 */
	protected void setDataPoint(double frequency, Complex impedance,
			Double loopGain)
	{
		mImpedance.put(frequency, impedance);
		mGain.put(frequency, loopGain);
		findLoopGainMaximum(frequency, loopGain);
	}

	private void findLoopGainMaximum(double frequency, Double loopGain)
	{
		if ((dataPointIndex >= 2) && (prevLoopGain > loopGain)
				&& (prevLoopGain > prevPrevLoopGain))
		{
			// We have found a loop gain maximum.
			mGainMaxima.put(prevFreq, prevLoopGain);
		}

		dataPointIndex++;
		prevPrevLoopGain = prevLoopGain;
		prevLoopGain = loopGain;
		prevFreq = frequency;
	}

	protected void printLoopGainMaxima()
	{
		Set<Entry<Double, Double>> entries = mGainMaxima.entrySet();
		if (entries.size() > 0)
		{
			System.out.println("\nLoop gain maxima for " + mName);
			DecimalFormat decFormat = new DecimalFormat("0.00");
			for (Entry<Double, Double> entry : entries)
			{
				System.out.println(decFormat.format(entry.getKey()) + " Hz, gain "
						+ decFormat.format(entry.getValue()));
			}
		}
	}

	protected void calcImpedance(InstrumentCalculator calculator,
			Fingering fingering, double freqStart, double freqEnd, int nfreq)
	{
		Note myNote = fingering.getNote();
		mName = "Note";
		String holeString = fingering.toString();
		if (myNote.getName() != null && !myNote.getName().isEmpty())
		{
			mName += " " + myNote.getName();
			if (holeString != null && !holeString.isEmpty())
			{
				mName += " (" + holeString + ")";
			}
		}
		else
		{
			mName += holeString;
		}
		String instrName = calculator.instrument.getName();
		if (instrName != null && !instrName.isEmpty())
		{
			mName += " on " + instrName;
		}
		actuals = new ArrayList<Double>();
		harmonics = new ArrayList<Double>();
		hasMinMax = false;

		if (myNote.getFrequencyMin() != null)
		{
			actuals.add(myNote.getFrequencyMin());
			hasMinMax = true;
		}
		if (myNote.getFrequencyMax() != null)
		{
			actuals.add(myNote.getFrequencyMax());
			hasMinMax = true;
		}
		if (!hasMinMax && myNote.getFrequency() != null)
		{
			actuals.add(myNote.getFrequency());
		}
		if (myNote.getFrequency() != null)
		{
			// Build list of harmonics of target up to freqEnd.
			double freqTarget = myNote.getFrequency();
			double freqHarmonic = 2.0 * freqTarget;
			while (freqHarmonic <= freqEnd)
			{
				harmonics.add(freqHarmonic);
				freqHarmonic += freqTarget;
			}
		}
		mImpedance = new TreeMap<Double, Complex>();
		mGain = new TreeMap<Double, Double>();
		double freqStep = (freqEnd - freqStart) / (nfreq - 1);
		for (int i = 0; i < nfreq; ++i)
		{
			double freq = freqStart + i * freqStep;
			Complex zAc = calculator.calcZ(freq, fingering);
			Double gain = calculator.calcGain(freq, zAc);
			setDataPoint(freq, zAc, gain);
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
				legend.setLocation(540, 420);
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}

	protected void plotImpedanceMagnitude(final boolean exitOnClose)
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
				DefaultChartModel modelMagnitude = new DefaultChartModel("Magnitude");
				DefaultChartModel modelPhase = new DefaultChartModel("Phase");
				for (Map.Entry<Double, Complex> point : mImpedance.entrySet())
				{
					double x = point.getKey();
					double z = point.getValue().abs();
					double phi = point.getValue().getArgument();
					modelMagnitude.addPoint(x, FastMath.log10(z));
					modelPhase.addPoint(x, phi);
				}
				Chart chart = new Chart();
				chart.setAutoRanging(true);
				chart.setAutoRanger(new ZeroAlignedAutoRanger());
				Axis magnitudeAxis = chart.getYAxis();
				magnitudeAxis.setLabel("Impedance");
				Axis phaseAxis = new Axis("Phase");
				phaseAxis.setPlacement(AxisPlacement.TRAILING);
				chart.addYAxis(phaseAxis);
				ChartStyle styleMagnitude = new ChartStyle(Color.blue, false, true);
				ChartStyle stylePhase = new ChartStyle(Color.red, false, true);
				chart.addModel(modelMagnitude, magnitudeAxis, styleMagnitude);
				chart.addModel(modelPhase, phaseAxis, stylePhase);
				chart.getXAxis().setLabel("Frequency");
				chart.setTitle("Impedance Spectrum");
				Legend legend = new Legend(chart);
				chart.addDrawable(legend);
				legend.setLocation(540, 420);
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
				DefaultChartModel modelActuals;
				if (hasMinMax)
				{
					modelActuals = new DefaultChartModel("Actual Frequency");
				}
				else
				{
					modelActuals = new DefaultChartModel("Target Frequency");
				}
				DefaultChartModel modelHarmonics = new DefaultChartModel("Harmonics");
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
					if (g >= 1.0)
					{
						modelGain.addPoint(x, g);
					}
					else
					{
						modelGainLow.addPoint(x, g);
					}
				}
				for (Double freq : actuals)
				{
					modelActuals.addPoint(freq, -0.4);
				}
				for (Double freq : harmonics)
				{
					modelHarmonics.addPoint(freq, -0.4);
				}

				Chart chart = new Chart();
				chart.setAutoRanging(true);
				chart.setAutoRanger(new ZeroAlignedAutoRanger());
				Axis impedanceAxis = chart.getYAxis();
				impedanceAxis.setLabel("Impedance Ratio");
				Axis gainAxis = new Axis("Loop Gain");
				gainAxis.setPlacement(AxisPlacement.TRAILING);
				chart.addYAxis(gainAxis);
				ChartStyle styleRatio = new ChartStyle(Color.black, false, true);
				ChartStyle styleGain = new ChartStyle(Color.green,
						PointShape.CIRCLE);
				ChartStyle styleGainLow = new ChartStyle(Color.red,
						PointShape.CIRCLE);
				chart.addModel(modelRatio, impedanceAxis, styleRatio);
				chart.addModel(modelGain, gainAxis, styleGain);
				chart.addModel(modelGainLow, gainAxis, styleGainLow);
				if (actuals.size() > 0)
				{
					ChartStyle styleActuals = new ChartStyle(Color.gray,
							PointShape.DIAMOND, Color.gray);
					styleActuals.setPointSize(8);
					chart.addModel(modelActuals, impedanceAxis, styleActuals);
				}
				if (harmonics.size() > 0)
				{
					ChartStyle styleHarmonics = new ChartStyle(Color.gray,
							PointShape.UP_TRIANGLE);
					styleHarmonics.setPointSize(6);
					chart.addModel(modelHarmonics, impedanceAxis, styleHarmonics);
				}
				chart.getXAxis().setLabel("Frequency");
				chart.setTitle("Note Spectrum");
				Legend legend = new Legend(chart);
				chart.addDrawable(legend);
				legend.setLocation(560, 410);
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Plot the impedance and playing ranges for a given calculator and
	 * fingering, using default plot parameters.
	 * 
	 * @param calculator
	 * @param fingering
	 */
	public void plot(InstrumentCalculator calculator, Fingering fingering)
	{
		plot(calculator, fingering, 0.5, 2.0, 2000, true);
	}

	/**
	 * Plot the impedance and playing ranges for a given calculator and
	 * fingering.
	 * 
	 * @param calculator
	 * @param fingering
	 * @param freqRangeBelow
	 *            - Range of frequencies to plot below fingered note, as a
	 *            fraction < 1 of the note.
	 * @param freqRangeAbove
	 *            - Range of frequencies to plot above fingered note, as a
	 *            multiple > 1 of the note.
	 * @param numberPoints
	 *            - number of points to calculate for plotting.
	 */
	public void plot(InstrumentCalculator calculator, Fingering fingering,
			double freqRangeBelow, double freqRangeAbove, int numberPoints,
			final boolean exitOnClose)
	{
		double targetFreq;
		if (fingering.getNote().getFrequency() != null)
		{
			targetFreq = fingering.getNote().getFrequency();
		}
		else if (fingering.getNote().getFrequencyMax() != null)
		{
			targetFreq = fingering.getNote().getFrequencyMax();
		}
		else
		{
			targetFreq = 1000.0;
		}
		double freqStart = targetFreq * 0.5;
		if (freqRangeBelow < 1.0)
		{
			freqStart = targetFreq * freqRangeBelow;
		}
		double freqEnd = targetFreq * 2.0;
		if (freqRangeAbove > 1.0)
		{
			freqEnd = targetFreq * freqRangeAbove;
		}

		// Set up loop gain maxima find
		mGainMaxima = new TreeMap<Double, Double>();
		dataPointIndex = 0;
		prevFreq = 0.;
		prevLoopGain = 0.;
		prevPrevLoopGain = 0.;

		calcImpedance(calculator, fingering, freqStart, freqEnd, numberPoints);
		// plotImpedanceMagnitude(exitOnClose);
		printLoopGainMaxima();
		plotPlayingRange(exitOnClose);

	}

	/**
	 * Plot the impedance and playing range for each note in a given list of
	 * notes, for a given calculator and fingering.
	 * 
	 * @param calculator
	 * @param tuning
	 * @param noteList
	 */
	public static void plotNotes(InstrumentCalculator calculator,
			Tuning tuning, int[] noteList)
	{
		for (int noteIndex : noteList)
		{
			PlayingRangeSpectrum spectrum = new PlayingRangeSpectrum();
			spectrum.plot(calculator, tuning.getFingering().get(noteIndex));
		}
	}

}

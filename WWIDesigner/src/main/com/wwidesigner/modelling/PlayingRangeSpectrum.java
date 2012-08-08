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
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.wwidesigner.geometry.InstrumentInterface;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Representation of a complex spectrum, along with information about its
 * extreme points.
 */
public class PlayingRangeSpectrum
{

	/**
	 * Holds impedance spectrum (created by calcImpedance()).
	 */
	private Map<Double, Complex> mSpectrum;

	/**
	 * Holds impedance minima.
	 */
	private List<Double> mMinima;

	/**
	 * Holds impedance maxima.
	 */
	private List<Double> mMaxima;

	/**
	 * Add or replace a point in the spectrum.
	 */
	public void setDataPoint(double frequency, Complex impedance)
	{
		mSpectrum.put(frequency, impedance);
	}

	public void calcImpedance(InstrumentInterface flute, 
			InstrumentCalculator calculator,
			double freqStart, double freqEnd, int nfreq, Fingering fingering,
			PhysicalParameters physicalParams)
	{
		mSpectrum = new TreeMap<Double, Complex>();
		mMinima = new ArrayList<Double>();
		mMaxima = new ArrayList<Double>();
		Complex prevZ = Complex.ZERO;
		double absPrevPrevZ = 0;
		double prevFreq = 0;
		double freqStep = (freqEnd - freqStart) / (nfreq - 1);
		for (int i = 0; i < nfreq; ++i)
		{
			double freq = freqStart + i * freqStep;
			Complex zAc = calculator.calcZ(freq, fingering,
					physicalParams);
			double absZAc = zAc.abs();

			setDataPoint(freq, zAc);

			double absPrevZ = prevZ.abs();

			if ((i >= 2) && (absPrevZ < absZAc) && (absPrevZ < absPrevPrevZ))
			{
				// We have found an impedance minimum.
				getMinima().add(prevFreq);
			}

			if ((i >= 2) && (absPrevZ > absZAc) && (absPrevZ > absPrevPrevZ))
			{
				// We have found an impedance maximum.
				getMaxima().add(prevFreq);
			}

			absPrevPrevZ = absPrevZ;
			prevZ = zAc;
			prevFreq = freq;
		}
	}

	public List<Double> getMaxima()
	{
		return mMaxima;
	}

	public void setMaxima(List<Double> maxima)
	{
		mMaxima = maxima;
	}

	public List<Double> getMinima()
	{
		return mMinima;
	}

	public void setMinima(List<Double> minima)
	{
		mMinima = minima;
	}

	public Map<Double, Complex> getSpectrum()
	{
		return mSpectrum;
	}

	public void setSpectrum(Map<Double, Complex> spectrum)
	{
		mSpectrum = spectrum;
	}

	public Double getClosestMinimumFrequency(double frequency)
	{
		Double closestFreq = null;
		double deviation = Double.MAX_VALUE;
		for (double minVal : mMinima)
		{
			double thisDeviation = Math.abs(frequency - minVal);
			if (thisDeviation < deviation)
			{
				closestFreq = minVal;
				deviation = thisDeviation;
			}
		}

		return closestFreq;
	}

	public Double getClosestMaximumFrequency(double frequency)
	{
		Double closestFreq = null;
		double deviation = Double.MAX_VALUE;
		for (double maxVal : mMaxima)
		{
			double thisDeviation = Math.abs(frequency - maxVal);
			if (thisDeviation < deviation)
			{
				closestFreq = maxVal;
				deviation = thisDeviation;
			}
		}

		return closestFreq;
	}

	public void plotImpedanceSpectrum()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Impedance Spectrum");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(800, 600);
				DefaultChartModel model1 = new DefaultChartModel("Real");
				DefaultChartModel model2 = new DefaultChartModel("Imaginary");
				for (Map.Entry<Double, Complex> point : mSpectrum.entrySet())
				{
					double x = point.getKey();
					double r = point.getValue().getReal();
					double i = point.getValue().getImaginary();
					model1.addPoint(x, r);
					model2.addPoint(x, i);
				}
				Chart chart = new Chart();
				chart.setAutoRanging(true);
				ChartStyle style1 = new ChartStyle(Color.blue, false, true);
				ChartStyle style2 = new ChartStyle(Color.red, false, true);
				chart.addModel(model1, style1);
				chart.addModel(model2, style2);
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

	public void plotPlayingRange()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Playing Ranges");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(800, 600);
				DefaultChartModel model1 = new DefaultChartModel(
						"Impedance Im/Re");
				// DefaultChartModel model2 = new DefaultChartModel(
				// "Loop Gain");
				for (Map.Entry<Double, Complex> point : mSpectrum.entrySet())
				{
					double x = point.getKey();
					double r = point.getValue().getReal();
					double i = point.getValue().getImaginary();
					model1.addPoint(x, i / r);
					// model2.addPoint(x, i);
				}
				Chart chart = new Chart();
				chart.setAutoRanging(true);
				ChartStyle style1 = new ChartStyle(Color.black, false, true);
				// ChartStyle style2 = new ChartStyle(Color.red, false, true);
				chart.addModel(model1, style1);
				// chart.addModel(model2, style2);
				chart.getXAxis().setLabel("Frequency");
				chart.getYAxis().setLabel("Impedance");
				chart.setTitle("Playing Ranges");
				Legend legend = new Legend(chart);
				chart.addDrawable(legend);
				legend.setLocation(200, 50);
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}

}

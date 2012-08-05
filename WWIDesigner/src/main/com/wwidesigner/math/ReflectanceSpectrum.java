/**
 * 
 */
package com.wwidesigner.math;

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
 * @author kort
 * 
 */
public class ReflectanceSpectrum
{
	/**
	 * Holds reflectance spectrum (created by calcReflectance().
	 */
	private Map<Double, Complex> mSpectrum;

	/**
	 * Holds squared reflectance angle minima.
	 */
	private List<Double> mMinima;

	/**
	 * Holds squared reflectance angle maxima.
	 */
	private List<Double> mMaxima;

	/**
	 * Add or replace a point in the spectrum.
	 */
	public void setDataPoint(double frequency, Complex value)
	{
		mSpectrum.put(frequency, value);
	}

	public void calcReflectance(InstrumentInterface flute, double freqStart,
			double freqEnd, int nfreq, Fingering fingering,
			PhysicalParameters physicalParams)
	{
		mSpectrum = new TreeMap<Double, Complex>();
		mMinima = new ArrayList<Double>();
		mMaxima = new ArrayList<Double>();
		double prevReflAngle = 0.;
		double prevPrevReflAngle = 0.;
		double prevFreq = 0.;
		double freqStep = (freqEnd - freqStart) / (nfreq - 1);
		for (int i = 0; i < nfreq; ++i)
		{
			double freq = freqStart + i * freqStep;
			Complex reflectance = flute.calcRefOrImpCoefficient(freq,
					fingering, physicalParams);

			setDataPoint(freq, reflectance);

			double reflectAngle = reflectance.getArgument();
			reflectAngle *= reflectAngle;

			if ((i >= 2) && (prevReflAngle < reflectAngle)
					&& (prevReflAngle < prevPrevReflAngle))
			{
				// We have found an impedance minimum.
				getMinima().add(prevFreq);
			}

			if ((i >= 2) && (prevReflAngle > reflectAngle)
					&& (prevReflAngle > prevPrevReflAngle))
			{
				// We have found an impedance maximum.
				getMaxima().add(prevFreq);
			}

			prevPrevReflAngle = prevReflAngle;
			prevReflAngle = reflectAngle;
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

	public void plotReflectanceSpectrum()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Reflectance Spectrum");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(800, 600);
				DefaultChartModel model1 = new DefaultChartModel(
						"Absolute Value");
				DefaultChartModel model2 = new DefaultChartModel(
						"Reflectance angle, squared");
				for (Map.Entry<Double, Complex> point : mSpectrum.entrySet())
				{
					double x = point.getKey();
					Complex cy = point.getValue();
					double ra = cy.getArgument();
					ra *= ra;
					double y = cy.abs();
					model1.addPoint(x, y);
					model2.addPoint(x, ra);
				}
				Chart chart = new Chart();
				chart.setAutoRanging(true);
				ChartStyle style1 = new ChartStyle(Color.black, false, true);
				ChartStyle style2 = new ChartStyle(Color.red, false, true);
				chart.addModel(model1, style1);
				chart.addModel(model2, style2);
				chart.getXAxis().setLabel("Frequency");
				chart.getYAxis().setLabel("Reflectance");
				chart.setTitle("Reflectance Spectrum");
				Legend legend = new Legend(chart);
				chart.addDrawable(legend);
				legend.setLocation(200, 50);
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}

}

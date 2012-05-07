/**
 * 
 */
package com.wwidesigner.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.InstrumentInterface;

/**
 * Representation of a complex spectrum, along with information about its
 * extreme points.
 */
public class ImpedanceSpectrum
{

    private Map<Double, Complex> mSpectrum = new TreeMap<Double, Complex>();
    private List<Double> mMinima = new ArrayList<Double>();
    private List<Double> mMaxima = new ArrayList<Double>();

    /**
     * Add or replace a point in the spectrum.
     */
    public void setDataPoint( double frequency, Complex impedance )
    {
        mSpectrum.put( frequency, impedance );
    }

    public void calcImpedance( InstrumentInterface flute, double freqStart, double freqEnd, int nfreq )
    {
        Complex prevZ = Complex.ZERO;
        double absPrevPrevZ = 0;
        double prevFreq = 0;
        double freqStep = ( freqEnd - freqStart ) / ( nfreq - 1 );
        for ( int i = 0; i < nfreq; ++i )
        {
            double freq = freqStart + i * freqStep;
            Complex zAc = flute.calcZ( freq );
            double absZAc = zAc.abs();

            setDataPoint( freq, zAc );

            double absPrevZ = prevZ.abs();

            if ( ( i >= 2 ) && ( absPrevZ < absZAc ) && ( absPrevZ < absPrevPrevZ ) )
            {
                // We have found an impedance minimum.
                getMinima().add( prevFreq );
            }

            if ( ( i >= 2 ) && ( absPrevZ > absZAc ) && ( absPrevZ > absPrevPrevZ ) )
            {
                // We have found an impedance maximum.
                getMaxima().add( prevFreq );
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

    public void setMaxima( List<Double> maxima )
    {
        mMaxima = maxima;
    }

    public List<Double> getMinima()
    {
        return mMinima;
    }

    public void setMinima( List<Double> minima )
    {
        mMinima = minima;
    }

    public Map<Double, Complex> getSpectrum()
    {
        return mSpectrum;
    }

    public void setSpectrum( Map<Double, Complex> spectrum )
    {
        mSpectrum = spectrum;
    }
}

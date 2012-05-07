/**
 * 
 */
package com.wwidesigner.note;

/**
 * The name of a note plus its octave, plus a cents deviation from its
 * (unspecified) nominal pitch.
 */
public class DeviatedNominalNote extends NominalNote
{

    private double mCentsDeviation;

    public DeviatedNominalNote()
    {
        mCentsDeviation = 0.0;
    }

    public DeviatedNominalNote( String name, int octave, double cents )
    {
        super( name, octave );
        mCentsDeviation = cents;
    }

    public DeviatedNominalNote( NominalNote noteSpec, double cents )
    {
        super( noteSpec );
        mCentsDeviation = cents;
    }

    /**
     * @return the centsDeviation
     */
    public double getCentsDeviation()
    {
        return mCentsDeviation;
    }
}

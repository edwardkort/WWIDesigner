/**
 * 
 */
package com.wwidesigner.note;

/**
 * Representation of a note in an octave - it's name, and its deviation from the
 * reference note. For example, in equal temperament, A# is 100 cents above the
 * reference note A.
 */
public class Note
{

    private String mName;
    private double mCents;

    public Note( String name, double cents )
    {
        mName = name;
        mCents = cents;
    }

    public Note( String name, int cents )
    {
        this( name, (double) cents );
    }

    /**
     * @return the cents
     */
    public double getCents()
    {
        return mCents;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return mName;
    }

}

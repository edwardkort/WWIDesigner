/**
 * 
 */
package com.wwidesigner.impedance.note;

import java.util.HashMap;

/**
 * The name of a note plus its octave.
 */
public class NominalNote implements Comparable<Object>
{

    private String mName;
    private int mOctave;
    private int mMidiCode;
    private Integer mSemitone;
    
    private static HashMap<String, Integer> mSemitoneLookup;
    private static boolean mIsInit = false;

    public NominalNote()
    {
        this( "", 0 );
    }

    public NominalNote( String name, int octave )
    {
    	if (!mIsInit)
    	{
    		mIsInit = true;
    		makeLookup();
    	}
        mName = name;
        mOctave = octave;
        setMidiCode();
    }

    public NominalNote( NominalNote note )
    {
        this( note.mName, note.mOctave );
    }
    
    private void setMidiCode()
    {
    	mSemitone = mSemitoneLookup.get(mName);
    	if (mSemitone != null)
    	{
    		mMidiCode = mSemitone + (mOctave + 1) * 12;
    	}
    }
    
    public int getMidiCode()
    {
    	return mMidiCode;
    }
    
    public Integer getSemitone()
    {
    	return mSemitone;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo( Object other )
    {
        NominalNote otherNote = (NominalNote) other;
        if ( mName.equals( otherNote.mName ) && mOctave == otherNote.mOctave )
        {
            return 0;
        }
        else if ( ( mName.compareTo( otherNote.mName ) == -1 )
                  || ( mName.equals( otherNote.mName ) && mOctave < otherNote.mOctave ) )
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @return the octave
     */
    public int getOctave()
    {
        return mOctave;
    }
    
    private static void makeLookup()
    {
    	mSemitoneLookup = new HashMap<String, Integer>();
    	mSemitoneLookup.put("C", 0);
    	mSemitoneLookup.put("C#", 1);
    	mSemitoneLookup.put("Db", 1);
    	mSemitoneLookup.put("D", 2);
    	mSemitoneLookup.put("D#", 3);
    	mSemitoneLookup.put("Eb", 3);
    	mSemitoneLookup.put("E", 4);
    	mSemitoneLookup.put("F", 5);
    	mSemitoneLookup.put("F#", 6);
    	mSemitoneLookup.put("Gb", 6);
    	mSemitoneLookup.put("G", 7);
    	mSemitoneLookup.put("G#", 8);
    	mSemitoneLookup.put("Ab", 8);
    	mSemitoneLookup.put("A", 9);
    	mSemitoneLookup.put("A#", 10);
    	mSemitoneLookup.put("Bb", 10);
    	mSemitoneLookup.put("B", 11);
    }
}

/**
 * 
 */
package com.wwidesigner.impedance.note;

/**
 * Configuration of open and closed holes corresponding to a given nominal note.
 */
public class NoteConfig extends NominalNote implements NoteConfigInterface
{

    private boolean[] mConfig;

    public NoteConfig( NominalNote nominalNote, boolean[] config )
    {
        super( nominalNote );
        mConfig = config;
    }

    /* (non-Javadoc)
	 * @see com.edkort.flute.impedance.note.NoteConfigInterface#getConfig()
	 */
    public boolean[] getConfig()
    {
        return mConfig;
    }

    /* (non-Javadoc)
	 * @see com.edkort.flute.impedance.note.NoteConfigInterface#setConfig(boolean[])
	 */
    public void setConfig( boolean[] config )
    {
        mConfig = config;
    }

}

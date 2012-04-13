/**
 * 
 */
package com.wwidesigner.impedance.geometry;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.complex.Complex;

import com.wwidesigner.impedance.math.ImpedanceSpectrum;
import com.wwidesigner.impedance.math.TransferMatrix;
import com.wwidesigner.impedance.note.DeviatedNominalNote;
import com.wwidesigner.impedance.note.NominalNote;
import com.wwidesigner.impedance.note.NoteConfig;
import com.wwidesigner.impedance.note.Temperament;
import com.wwidesigner.impedance.util.Constants;

/**
 * Class representing a flute. Contains a linear chain of {@link Component Components},
 * terminated by a {@link Termination}.
 */
public class Instrument implements InstrumentInterface
{

	private String mDescription;
    private List<Component> mComponents;
    private EmbouchureInterface mEmbouchure;
    private List<BoreSection> mBoreSections;
    private List<Hole> mHoles;
    private TerminationInterface mTermination;
    private List<NoteConfig> mNoteConfigs;
    private transient Temperament mTemperament;
    private transient double mPitchStandard;
    private NominalNote mKey;

    public Instrument()
    {
        mComponents = new ArrayList<Component>();
        mHoles = new ArrayList<Hole>();
        mTemperament = Temperament.getEqualTemperament();
        mKey = new NominalNote("C", 4);
        mPitchStandard = 440.0;
        mBoreSections = new ArrayList<BoreSection>();
        mNoteConfigs = new ArrayList<NoteConfig>();
    }

    /**
	 * @return the mKey
	 */
	public NominalNote getKey()
	{
		return mKey;
	}

	/**
	 * @return the mDescription
	 */
	protected String getDescription()
	{
		return mDescription;
	}

	/**
	 * @param mDescription the mDescription to set
	 */
	protected void setDescription(String mDescription)
	{
		this.mDescription = mDescription;
	}

	/**
	 * @param key the mKey to set
	 */
	public void setKey(NominalNote key)
	{
		mKey = key;
	}

	/**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#validate()
	 */
    public void validate()
    {
        assert mTermination != null;

        for ( ComponentInterface comp : mComponents )
        {
            assert comp != null;
            comp.validate();
        }
    }

    /**
     * Add a nominal note and the configuration of open and closed holes that
     * produce it.
     */
    void addNote( NominalNote nominalNote, String xoConfig )
    {
        boolean[] config = new boolean[xoConfig.length()];
        char[] chars = xoConfig.toCharArray();
        int index = 0;
        for ( char ch : chars )
        {
            if ( ch == 'x' )
            {
                config[index] = true;
            }
            else if ( ch == 'o' )
            {
                config[index] = false;
            }
            index++;
        }
        mNoteConfigs.add( new NoteConfig( nominalNote, config ) );
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#setNote(com.wwidesigner.impedance.note.NominalNote)
	 */
    public void setNote( NominalNote nominalNote )
    {
        for ( NoteConfig noteConfig : mNoteConfigs )
        {
            if ( noteConfig.compareTo( nominalNote ) == 0 )
            {
                boolean[] config = noteConfig.getConfig();
                for ( int i = 0; i < config.length; i++ )
                {
                    Hole hole = mHoles.get( i );
                    hole.setIsClosed( config[i] );
                    hole.validate();
                }
            }
        }
    }

    /**
     * Add a {@link BoreSect} to the right (foot) end.
     */
    void addBoreSection( BoreSection boreSection )
    {
        mComponents.add( boreSection );
        mBoreSections.add( boreSection );
    }

    /**
     * Add a Hole to the right (foot) end.
     */
    void addHole( Hole hole )
    {
        mComponents.add( hole );
        mHoles.add( hole );
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#calcZ(double)
	 */
    public Complex calcZ( double freq )
    {
        TransferMatrix fluteMatrix = new TransferMatrix( Complex.ONE, Complex.ZERO, Complex.ZERO,
                                                         Complex.ONE );
        TransferMatrix compMatrix = new TransferMatrix( Complex.ONE, Complex.ZERO, Complex.ZERO,
                                                        Complex.ONE );
        for ( ComponentInterface component : mComponents )
        {
            component.calcT( compMatrix, freq );
            fluteMatrix = TransferMatrix.multiply( fluteMatrix, compMatrix );
        }
        Complex zL = mTermination.calcZL( freq );
        Complex result = zL.multiply( fluteMatrix.getPP() ).add( fluteMatrix.getPU() )
                .divide( zL.multiply( fluteMatrix.getUP() ).add( fluteMatrix.getUU() ) );
        return result;
    }

    public List<DeviatedNominalNote> calcTuning(int nFreq)
    {
    	List<DeviatedNominalNote> calcNotes = new ArrayList<DeviatedNominalNote>();
    	for (NoteConfig noteConfig : mNoteConfigs)
    	{
    		setNote( noteConfig );
    		double matchingFreq = mTemperament.getFreq(noteConfig, mPitchStandard, mKey);

    		double deltaFactor =
    			Math.pow(Constants.CENT_FACTOR, Constants.CENTS_IN_SEMITONE * 4.0);
    		double freqStart = matchingFreq / deltaFactor;
    		double freqEnd = matchingFreq * deltaFactor;

    		ImpedanceSpectrum imp = new ImpedanceSpectrum();
    		imp.calcImpedance(this, freqStart, freqEnd, nFreq);

    		double minDeviationFreq = 0;
    		double minDeviation = Constants.BIG_DBL;

    		List<Double>minima = imp.getMinima();
    		for (Double minVal : minima)
    		{
    			double deviation = Math.abs(minVal - matchingFreq);
    			if (deviation < minDeviation)
    			{
    				minDeviation = deviation;
    				minDeviationFreq = minVal;
    			}
    		}

    		if (minDeviation < Constants.BIG_DBL)
    		{
    			double centsFromNominal = Constants.CENTS_IN_OCTAVE *
    				Math.log10(minDeviationFreq / matchingFreq) / Constants.LOG2;

    			calcNotes.add(new DeviatedNominalNote(noteConfig, centsFromNominal));
    		}
    		else
    		{
    			calcNotes.add(new DeviatedNominalNote(noteConfig, Constants.BIG_DBL));
    		}
    	}
    	
    	return calcNotes;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#getTemperament()
	 */
    public Temperament getTemperament()
    {
        return mTemperament;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#setTemperament(com.wwidesigner.impedance.note.Temperament)
	 */
    public void setTemperament( Temperament temperament )
    {
        mTemperament = temperament;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#getEmbouchure()
	 */
    public EmbouchureInterface getEmbouchure()
    {
        return mEmbouchure;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#getBoreSections()
	 */
    public List<BoreSection> getBoreSections()
    {
        return mBoreSections;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#getHoles()
	 */
    public List<Hole> getHoles()
    {
        return mHoles;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#getTermination()
	 */
    public TerminationInterface getTermination()
    {
        return mTermination;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#getNoteConfigs()
	 */
    public List<NoteConfig> getNoteConfigs()
    {
        return mNoteConfigs;
    }

	/**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#setEmbouchure(com.wwidesigner.impedance.geometry.Embouchure)
	 */
    public void setEmbouchure( Embouchure embouchure )
    {
        mComponents.add( 0, embouchure );
        mEmbouchure = embouchure;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#setTermination(com.edkort.flute.impedance.geometry.Termination)
	 */
    public void setTermination( TerminationInterface termination )
    {
        assert termination != null;
        mTermination = termination;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#getPitchStandard()
	 */
    public double getPitchStandard()
    {
        return mPitchStandard;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.InstrumentInterface#setPitchStandard(double)
	 */
    public void setPitchStandard( double pitchStandard )
    {
        mPitchStandard = pitchStandard;
    }
}
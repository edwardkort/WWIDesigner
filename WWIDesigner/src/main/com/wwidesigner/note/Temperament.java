/**
 * 
 */
package com.wwidesigner.note;

import java.util.ArrayList;
import java.util.List;

import com.wwidesigner.util.Constants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Temperament complex type.
 * Representation of a temperament relative to a root of C. Basically, a list of \a Notes. Extra
 * information in the form of a reference pitch is needed to find the actual
 * frequency of the notes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Temperament">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;sequence maxOccurs="12" minOccurs="2">
 *           &lt;element name="temperamentNote" type="{http://www.example.org/Temperament/}TemperamentNote"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Temperament", propOrder = {
    "mName",
    "mNotes"
})
@XmlRootElement
public class Temperament
{
	static private boolean mIsInitEqualT;
    static private boolean mIsInitJustT;
    static private String[] mDiatonicNotes = new String[12];
    static private Temperament mEqualTemp;
    static private Temperament mJustTemp;

    @XmlElement(name="note", required = true)
    private List<TemperamentNote> mNotes = new ArrayList<TemperamentNote>();
    @XmlElement(name="name", required = true)
    private String mName;
    
    public Temperament()
    {
    	
    }
    
    public Temperament(String name)
    {
    	mName = name;
    }

    public static Temperament getEqualTemperament()
    {
        if ( !mIsInitEqualT)
        {
            staticInitEqualT();
        }

        return mEqualTemp;
    }
    
    public static Temperament getJustIntonation()
    {
    	if (!mIsInitJustT)
    	{
    		staticInitJustT();
    	}
    	
    	return mJustTemp;
    }

	/**
     * The \a Notes of the \a Temperament.
     */
    public List<TemperamentNote> getNotes()
    {
        return mNotes;
    }

    /**
	 * @return the mName
	 */
	public String getName()
	{
		return mName;
	}

	/**
     * Utility function giving the note nearest to \a freq, if we are at a
     * reference pitch of \a ref.
     */
    public DeviatedNominalNote nearestNote( double freq, double ref )
    {
        assert ( freq > 0 );

        double centsFromRef = Constants.CENTS_IN_OCTAVE * Math.log( freq / ref ) / Math.log( 2 );

        TemperamentNote minNote = null;
        double minDeviation = Constants.CENTS_IN_OCTAVE;
        int minOctave = 0;
        for ( TemperamentNote note : mNotes )
        {
            double shiftedCents = centsFromRef - note.getCents();
            double deviation = shiftedCents % Constants.CENTS_IN_OCTAVE;
            if ( Math.abs( deviation ) < Math.abs( minDeviation ) )
            {
                minOctave = (int) ( shiftedCents / Constants.CENTS_IN_OCTAVE );
                minDeviation = deviation;
                minNote = note;
            }
        }

        return new DeviatedNominalNote( minNote.getName(), minOctave, minDeviation );
    }

    /**
     * Return the frequency of a given nominal note, given that we are at a
     * reference pitch of ref for A4.
     */
    public double getFreq( NominalNote nominalNote, double ref, NominalNote root )
    {
    	// Get ratio from reference A down to C
    	// Remember, C has a semitone of 0
    	TemperamentNote note = mNotes.get(Constants.A_SEMITONE);
    	double ratioToC = 1. / note.getRatio();
    	
    	// Get ratio from C up to root
    	note = mNotes.get(root.getSemitone());
    	double ratioToRoot = note.getRatio();
    	
    	// Get ratio from root to nominalNote
    	// Root is now considered a C
    	int semitoneDiff = nominalNote.getSemitone() - root.getSemitone();
    	if (semitoneDiff < 0)
    	{
    		semitoneDiff += 12;
    	}
    	note = mNotes.get(semitoneDiff);
    	double ratioToNote = note.getRatio();
    	
    	double ratioFromA = ratioToC * ratioToRoot * ratioToNote;    	
        double freq = ref * ratioFromA;
        // Handle octave
        // Remember reference A is in octave 4
        freq *= Math.pow(2, nominalNote.getOctave() - 4);

        return freq;
    }
    
    private static void staticInitEqualT()
    {
    	TemperamentNote note;
        mEqualTemp = new Temperament("Equal Temperament");
        note = new TemperamentNote("C", 0);
        note.setCents(0 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("C#", 1);
        note.setCents(1 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("D", 2);
        note.setCents(2 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("D#", 3);
        note.setCents(3 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("E", 4);
        note.setCents(4 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("F", 5);
        note.setCents(5 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("F#", 6);
        note.setCents(6 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("G", 7);
        note.setCents(7 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("G#", 8);
        note.setCents(8 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("A", 9);
        note.setCents(9 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("A#", 10);
        note.setCents(10 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );
        note = new TemperamentNote("B", 11);
        note.setCents(11 * Constants.CENTS_IN_SEMITONE);
        mEqualTemp.mNotes.add( note );

        mIsInitEqualT = true;
    }

    private static void staticInitJustT()
	{
		mJustTemp = new Temperament("Just Intonation");
		TemperamentNote note;
		note = new TemperamentNote("C", 0);
		note.setRatio(1.);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("C#", 1);
		note.setRatio(16./15);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("D", 2);
		note.setRatio(9./8);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("D#", 3);
		note.setRatio(6./5);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("E", 4);
		note.setRatio(5./4);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("F", 5);
		note.setRatio(4./3);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("F#", 6);
		note.setRatio(45./32);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("G", 7);
		note.setRatio(3./2);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("G#", 8);
		note.setRatio(8./5);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("A", 9);
		note.setRatio(5./3);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("A#", 10);
		note.setRatio(9./5);
		mJustTemp.mNotes.add(note);
		note = new TemperamentNote("B", 11);
		note.setRatio(15./8);
		mJustTemp.mNotes.add(note);
		
		mIsInitJustT = true;
	}
    
    public static void main(String[] args) throws Exception
    {
    	Temperament temperament = Temperament.getJustIntonation();
    	for (TemperamentNote note : temperament.getNotes())
    	{
    		System.out.println(note.getName() + ": " + note.getCents());
    	}
    	
        JAXBContext jc = JAXBContext.newInstance( "com.wwidesigner.impedance.note" );
        Marshaller m = jc.createMarshaller();
        m.setProperty("jaxb.formatted.output", true);
        m.marshal(temperament, System.out);
    }

}

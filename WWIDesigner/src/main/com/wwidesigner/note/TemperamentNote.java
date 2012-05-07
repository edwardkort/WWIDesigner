package com.wwidesigner.note;

import com.wwidesigner.util.Constants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TemperamentNote complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TemperamentNote">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="ratio" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *           &lt;element name="cents" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TemperamentNote", propOrder = {
    "mName",
    "mSemitone",
    "mRatio",
    "mCents"
})

public class TemperamentNote
{
	@XmlElement(name="semitone", required=true)
	private int mSemitone;
	private transient boolean doCalc = true;
    @XmlElement(name="name", required = true)
	private String mName;
    @XmlElement(name="ratio")
	private Double mRatio;
    @XmlElement(name="cents")
	private Double mCents;
	
	public TemperamentNote()
	{
		
	}
	
	public TemperamentNote(String name, int semitone)
	{
		mName = name;
		setSemitone(semitone);
	}
	
	public String getName()
	{
		return mName;
	}
	
	/**
	 * @return the mRatio
	 */
	public Double getRatio()
	{
		return mRatio;
	}

	/**
	 * @param ratio the mRatio to set
	 */
	public void setRatio(double ratio)
	{
		mRatio = ratio;
		
		if (doCalc)
		{
			doCalc = false;
			setCents(ratioToCents(ratio));
		}
		
		doCalc = true;
	}

	/**
	 * @return the mCents
	 */
	public Double getCents()
	{
		return mCents;
	}

	/**
	 * @param cents the mCents to set
	 */
	public void setCents(double cents)
	{
		mCents = cents;
		
		if (doCalc)
		{
			doCalc = false;
			setRatio(centsToRatio(cents));
		}
		
		doCalc = true;
	}
	
    /**
     * Return the number of cents difference between 2 notes represented as a ratio
     * of frequencies. 
     */
    public static double ratioToCents(double ratio)
    {
    	return Constants.CENTS_IN_OCTAVE * Math.log10(ratio) / Constants.LOG2;
    }
    
    /**
     * Return the ratio of frequencies between two notes cents apart.
     */
    public static double centsToRatio(double cents)
    {
    	return Math.pow(2, cents/Constants.CENTS_IN_OCTAVE);
    }

	/**
	 * @param semitone the mSemitone to set
	 */
	public void setSemitone(int semitone)
	{
		mSemitone = semitone;
	}

	/**
	 * @return the mSemitone
	 */
	public int getSemitone()
	{
		return mSemitone;
	}

}

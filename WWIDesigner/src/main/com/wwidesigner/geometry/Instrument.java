/**
 * 
 */
package com.wwidesigner.geometry;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.bind.XmlBorePoint;
import com.wwidesigner.geometry.bind.XmlEndBoreSection;
import com.wwidesigner.geometry.bind.XmlHole;
import com.wwidesigner.geometry.bind.XmlInstrument;
import com.wwidesigner.geometry.bind.XmlLengthType;
import com.wwidesigner.geometry.bind.XmlMouthpiece;

/**
 * @author kort
 * 
 */
public class Instrument implements InstrumentInterface
{

	protected XmlInstrument xmlInstrumtent;

	/**
	 * @return the xmlInstrumtent
	 */
	public XmlInstrument getXmlInstrumtent()
	{
		return xmlInstrumtent;
	}

	/**
	 * @param xmlInstrumtent
	 *            the xmlInstrumtent to set
	 */
	public void setXmlInstrumtent(XmlInstrument xmlInstrumtent)
	{
		this.xmlInstrumtent = xmlInstrumtent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getName()
	 */
	@Override
	public String getName()
	{
		return xmlInstrumtent.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setName(java.lang.String)
	 */
	@Override
	public void setName(String value)
	{
		xmlInstrumtent.setName(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return xmlInstrumtent.getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setDescription(java.lang
	 * .String)
	 */
	@Override
	public void setDescription(String value)
	{
		xmlInstrumtent.setDescription(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getLengthType()
	 */
	@Override
	public XmlLengthType getLengthType()
	{
		return xmlInstrumtent.getLengthType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setLengthType(com.wwidesigner
	 * .geometry.bind.XmlLengthType)
	 */
	@Override
	public void setLengthType(XmlLengthType value)
	{
		xmlInstrumtent.setLengthType(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getBorePoint()
	 */
	@Override
	public List<XmlBorePoint> getBorePoint()
	{
		return xmlInstrumtent.getBorePoint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getMouthpiece()
	 */
	@Override
	public XmlMouthpiece getMouthpiece()
	{
		return xmlInstrumtent.getMouthpiece();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setMouthpiece(com.wwidesigner
	 * .geometry.bind.XmlMouthpiece)
	 */
	@Override
	public void setMouthpiece(XmlMouthpiece value)
	{
		xmlInstrumtent.setMouthpiece(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getHole()
	 */
	@Override
	public List<XmlHole> getHole()
	{
		return xmlInstrumtent.getHole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getTermination()
	 */
	@Override
	public XmlEndBoreSection getTermination()
	{
		return xmlInstrumtent.getTermination();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setTermination(com.wwidesigner
	 * .geometry.bind.XmlEndBoreSection)
	 */
	@Override
	public void setTermination(XmlEndBoreSection value)
	{
		xmlInstrumtent.setTermination(value);
	}

	/*
	 * Included for possible compatibility to Dan Gordon's code.
	 */
	public Complex calcZ(double freq)
	{
		return null;
	}

}

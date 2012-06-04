/**
 * 
 */
package com.wwidesigner.note;

import java.math.BigInteger;
import java.util.List;

import com.wwidesigner.note.bind.XmlFingering;
import com.wwidesigner.note.bind.XmlTuning;

/**
 * @author kort
 * 
 */
public class Tuning implements TuningInterface
{
	protected XmlTuning xmlTuning;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.note.TuningInterface#setXmlTuning(com.wwidesigner.note
	 * .bind.XmlTuning)
	 */
	@Override
	public void setXmlTuning(XmlTuning value)
	{
		this.xmlTuning = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#getXmlTuning()
	 */
	@Override
	public XmlTuning getXmlTuning()
	{
		return xmlTuning;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#getName()
	 */
	@Override
	public String getName()
	{
		return xmlTuning.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#setName(java.lang.String)
	 */
	@Override
	public void setName(String value)
	{
		xmlTuning.setName(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#getComment()
	 */
	@Override
	public String getComment()
	{
		return xmlTuning.getComment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#setComment(java.lang.String)
	 */
	@Override
	public void setComment(String value)
	{
		xmlTuning.setComment(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#getNumberOfHoles()
	 */
	@Override
	public BigInteger getNumberOfHoles()
	{
		return xmlTuning.getNumberOfHoles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.note.TuningInterface#setNumberOfHoles(java.math.BigInteger
	 * )
	 */
	@Override
	public void setNumberOfHoles(BigInteger value)
	{
		xmlTuning.setNumberOfHoles(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#getFingering()
	 */
	@Override
	public List<XmlFingering> getFingering()
	{
		return xmlTuning.getFingering();
	}

}

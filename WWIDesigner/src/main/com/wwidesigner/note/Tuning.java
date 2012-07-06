/**
 * 
 */
package com.wwidesigner.note;

import java.math.BigInteger;
import java.util.List;


/**
 * @author kort
 * 
 */
public class Tuning implements TuningInterface
{
	protected com.wwidesigner.note.bind.Tuning xmlTuning;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.note.TuningInterface#setXmlTuning(com.wwidesigner.note
	 * .bind.XmlTuning)
	 */
	@Override
	public void setXmlTuning(com.wwidesigner.note.bind.Tuning value)
	{
		this.xmlTuning = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.note.TuningInterface#getXmlTuning()
	 */
	@Override
	public com.wwidesigner.note.bind.Tuning getXmlTuning()
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
	public List<com.wwidesigner.note.bind.Fingering> getFingering()
	{
		return xmlTuning.getFingering();
	}

}

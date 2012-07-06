package com.wwidesigner.note;

import java.math.BigInteger;
import java.util.List;

//import com.wwidesigner.note.bind.XmlFingering;
//import com.wwidesigner.note.bind.XmlTuning;

public interface TuningInterface
{

	/**
	 * Sets the {@link XmlTuning}
	 */
	public void setXmlTuning(com.wwidesigner.note.bind.Tuning value);

	/**
	 * Gets the {@link XmlTuning}
	 */
	public com.wwidesigner.note.bind.Tuning getXmlTuning();

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName();

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value);

	/**
	 * Gets the value of the comment property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getComment();

	/**
	 * Sets the value of the comment property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setComment(String value);

	/**
	 * Gets the value of the numberOfHoles property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getNumberOfHoles();

	/**
	 * Sets the value of the numberOfHoles property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setNumberOfHoles(BigInteger value);

	/**
	 * Gets the value of the fingering property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the fingering property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFingering().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link XmlFingering }
	 * 
	 * 
	 */
	public List<com.wwidesigner.note.bind.Fingering> getFingering();

}
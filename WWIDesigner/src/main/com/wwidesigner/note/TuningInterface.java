package com.wwidesigner.note;

import java.util.List;


public interface TuningInterface
{

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
	 * @return possible object is {@link int }
	 * 
	 */
	public int getNumberOfHoles();

	/**
	 * Sets the value of the numberOfHoles property.
	 * 
	 * @param value
	 *            allowed object is {@link int }
	 * 
	 */
	public void setNumberOfHoles(int value);

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
	 * {@link Fingering }
	 * 
	 * 
	 */
	public List<Fingering> getFingering();

}
package com.wwidesigner.geometry;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.bind.XmlBorePoint;
import com.wwidesigner.geometry.bind.XmlEndBoreSection;
import com.wwidesigner.geometry.bind.XmlHole;
import com.wwidesigner.geometry.bind.XmlLengthType;
import com.wwidesigner.geometry.bind.XmlMouthpiece;

public interface InstrumentInterface
{

	/**
	 * Gets the value of the name property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public String getName();

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public void setName(String value);

	/**
	 * Gets the value of the description property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public String getDescription();

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public void setDescription(String value);

	/**
	 * Gets the value of the lengthType property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XmlLengthType }
	 *     
	 */
	public XmlLengthType getLengthType();

	/**
	 * Sets the value of the lengthType property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XmlLengthType }
	 *     
	 */
	public void setLengthType(XmlLengthType value);

	/**
	 * Gets the value of the borePoint property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the borePoint property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getBorePoint().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link XmlBorePoint }
	 * 
	 * 
	 */
	public List<XmlBorePoint> getBorePoint();

	/**
	 * Gets the value of the mouthpiece property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XmlMouthpiece }
	 *     
	 */
	public XmlMouthpiece getMouthpiece();

	/**
	 * Sets the value of the mouthpiece property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XmlMouthpiece }
	 *     
	 */
	public void setMouthpiece(XmlMouthpiece value);

	/**
	 * Gets the value of the hole property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the hole property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getHole().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link XmlHole }
	 * 
	 * 
	 */
	public List<XmlHole> getHole();

	/**
	 * Gets the value of the termination property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link XmlEndBoreSection }
	 *     
	 */
	public XmlEndBoreSection getTermination();

	/**
	 * Sets the value of the termination property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link XmlEndBoreSection }
	 *     
	 */
	public void setTermination(XmlEndBoreSection value);
	
	/*
	 * Added for possible compatibility with Dan Gordon's code.
	 */
	public Complex calcZ(double freq);


}
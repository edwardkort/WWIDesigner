/**
 * 
 */
package com.wwidesigner.impedance.geometry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for BorePoint complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="BorePoint">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="borePosition" type="{http://www.wwidesigner.com/Geometry}zeroOrMore"/>
 *         &lt;element name="boreDiameter" type="{http://www.wwidesigner.com/Geometry}zeroOrMore"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BorePoint", propOrder = { "mBorePosition", "mBoreDiameter" })
public class BorePoint
{

	@XmlElement(name = "borePosition", required = true)
	protected double mBorePosition;
	@XmlElement(name = "boreDiameter", required = true)
	protected double mBoreDiameter;
	private transient HoleInterface mHole = null;
	private transient BoreSectionInterface mBoreSection = null;

	public BorePoint()
	{

	}

	public BorePoint(double diameter, HoleInterface hole,
			BoreSectionInterface boreSection)
	{
		mBoreDiameter = diameter;
		mHole = hole;
		mBoreSection = boreSection;
	}

	/**
	 * Gets the value of the mBorePosition property.
	 * 
	 */
	public double getBorePosition()
	{
		return mBorePosition;
	}

	/**
	 * Sets the value of the mBorePosition property.
	 * 
	 */
	public void setBorePosition(double value)
	{
		this.mBorePosition = value;
	}

	/**
	 * Gets the value of the mBoreDiameter property.
	 * 
	 */
	public double getBoreDiameter()
	{
		return mBoreDiameter;
	}

	/**
	 * Sets the value of the mBoreDiameter property.
	 * 
	 */
	public void setBoreDiameter(double value)
	{
		this.mBoreDiameter = value;
	}

	/**
	 * @see
	 * com.wwidesigner.impedance.geometry.BorePointInterface#getBoreSection()
	 */
	public BoreSectionInterface getBoreSection()
	{
		return mBoreSection;
	}

	/**
	 * @see
	 * com.wwidesigner.impedance.geometry.BorePointInterface#setBoreSection
	 * (com.wwidesigner.impedance.geometry.BoreSection)
	 */
	public void setBoreSection(BoreSectionInterface boreSection)
	{
		mBoreSection = boreSection;
	}

	/**
	 * @see com.wwidesigner.impedance.geometry.BorePointInterface#getHole()
	 */
	public HoleInterface getHole()
	{
		return mHole;
	}

	/**
	 * @see
	 * com.wwidesigner.impedance.geometry.BorePointInterface#setHole
	 * (com.wwidesigner.impedance.geometry.Hole)
	 */
	public void setHole(HoleInterface hole)
	{
		mHole = hole;
	}

}

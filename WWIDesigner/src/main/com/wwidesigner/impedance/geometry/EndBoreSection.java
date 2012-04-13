/**
 * 
 */
package com.wwidesigner.impedance.geometry;

import org.apache.commons.math.complex.Complex;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EndBoreSection complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EndBoreSection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="flangeDiameter" type="{http://www.wwidesigner/Geometry}moreThanZero"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndBoreSection", propOrder = { "mFlangeDiameter" })
public class EndBoreSection extends BoreSection implements
		TerminationInterface, EndBoreSectionInterface
{

	@XmlElement(name = "flangeDiameter", required = true)
	protected double mFlangeDiameter;
	protected transient double mRFlange; // The radius of the outer edge of the

	// flange.

	public EndBoreSection()
	{

	}

	public EndBoreSection(BoreSection bs, double rFlange)
	{
		super(bs);
		setRFlange(rFlange);
	}

	/**
	 * Gets the value of the flangeDiameter property.
	 * 
	 */
	public double getFlangeDiameter()
	{
		return mFlangeDiameter;
	}

	/**
	 * Sets the value of the flangeDiameter property.
	 * 
	 */
	public void setFlangeDiameter(double value)
	{
		this.mFlangeDiameter = value;
	}

	/**
	 * @see
	 * com.wwidesigner.impedance.geometry.EndBoreSectionInterface#getRFlange()
	 */
	public double getRFlange()
	{
		return mRFlange;
	}

	/**
	 * @see
	 * com.wwidesigner.impedance.geometry.EndBoreSectionInterface#setRFlange
	 * (double)
	 */
	public void setRFlange(double rFlange)
	{
		mRFlange = rFlange;
		mFlangeDiameter = mRFlange * 2.;
	}

	/**
	 * @see com.wwidesigner.impedance.geometry.Termination#calcZL(double)
	 * @see
	 * com.wwidesigner.impedance.geometry.EndSectionInterface#calcZL(double)
	 */
	public Complex calcZL(double freq)
	{
		// Start with z_l for a cylindrical tube or radius mRBR.

		// For now, just use a crude piecewise constant resistance plus
		// frequency dependent reactance from Scavone's thesis.
		// See Fletcher and Rossing.
		double kr = 2.0 * Math.PI * freq * mRBR / mParams.getSpecificHeat();
		double z0 = mParams.calcZ0(mRBR);
		double flange_factor = mRBR / mRFlange;
		double length_corr_factor = 0.821 - 0.135 * flange_factor - 0.073
				* Math.pow(flange_factor, 4);
		double rea = z0 * length_corr_factor * kr;
		// Reactance (im part of Z_L).
		double res = z0 * (kr < 2.0 ? 0.25 * kr * kr : 1.0);
		// Resistance (re part of Z_L).
		Complex result = new Complex(res, -rea);

		// The result is now scaled by s_p / s_s, where s_p is the cross
		// sectional
		// surface area at the end of the cone, s_s is the spherical wave
		// surface
		// area at the end of the cone; see Scavone, PhD Thesis.

		// For present, assume a scaling factor of 1 which should be sufficient
		// for the kinds of conic sections found in realistic flutes.

		// double scale = 1.0;

		// result *= scale;

		return result;
	}

}

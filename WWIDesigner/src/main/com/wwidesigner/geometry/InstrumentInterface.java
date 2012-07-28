package com.wwidesigner.geometry;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;

public interface InstrumentInterface
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
	 * Gets the value of the description property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDescription();

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDescription(String value);

	/**
	 * Gets the value of the borePoint property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present. This is why there is not a <CODE>set</CODE> method for the
	 * borePoint property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getBorePoint().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link BorePoint }
	 * 
	 * 
	 */
	public List<BorePoint> getBorePoint();

	public void addBorePoint(BorePoint borePoint);

	/**
	 * Gets the value of the mouthpiece property.
	 * 
	 * @return possible object is {@link Mouthpiece }
	 * 
	 */
	public Mouthpiece getMouthpiece();

	/**
	 * Sets the value of the mouthpiece property.
	 * 
	 * @param value
	 *            allowed object is {@link Mouthpiece }
	 * 
	 */
	public void setMouthpiece(Mouthpiece value);

	/**
	 * Gets the value of the hole property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present. This is why there is not a <CODE>set</CODE> method for the
	 * hole property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getHole().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Hole }
	 * 
	 * 
	 */
	public List<Hole> getHole();

	public void addHole(Hole hole);

	/**
	 * Gets the value of the termination property.
	 * 
	 * @return possible object is {@link XmlEndBoreSection }
	 * 
	 */
	public Termination getTermination();

	/**
	 * Sets the value of the termination property.
	 * 
	 * @param value
	 *            allowed object is {@link XmlEndBoreSection }
	 * 
	 */
	public void setTermination(Termination value);

	/**
	 * Creates the instrument Components (BoreSection and filled-out Holes) from
	 * the raw BorePoints and Holes.
	 */
	public void updateComponents();

	public Complex calculateReflectionCoefficient(Fingering fingering,
			PhysicalParameters physicalParams);
	
	public Complex calculateReflectionCoefficient(double frequency, Fingering fingering,
			PhysicalParameters physicalParams);

	public Complex calculateReflectionCoefficient(double frequency, 
			PhysicalParameters physicalParams);

	public Complex calcZ(double freq, Fingering fingering,
			PhysicalParameters physicalParams);

}
/**
 * Interface to a class describing an instrument.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.geometry;

import java.util.List;

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
}
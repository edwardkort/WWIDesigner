/**
 * Class to encapsulate the sounding mechanism of an instrument.
 * Includes detail classes for different types of instruments.
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

import com.wwidesigner.util.InvalidFieldHandler;

/**
 * Main class for the sounding mechanism of an instrument.
 * @author kort
 * 
 */
public class Mouthpiece implements ComponentInterface, MouthpieceInterface,
		BorePointInterface
{
	protected double position;
	protected Double beta;
	protected Mouthpiece.EmbouchureHole embouchureHole;
	protected Mouthpiece.Fipple fipple;
	protected Mouthpiece.SingleReed singleReed;
	protected Mouthpiece.DoubleReed doubleReed;
	protected Mouthpiece.LipReed lipReed;

	// Values not part of the binding framework
	protected Double gainFactor;
	// List of bore sections with positions less than the mouthpiece
	// (above the mouthpiece), from smallest position to largest position.
	protected List<BoreSection> headspace;
	protected double boreDiameter;

	/**
	 * Gets the value of the position property.
	 * 
	 */
	public double getPosition()
	{
		return position;
	}

	/**
	 * Sets the value of the position property.
	 * 
	 * @param value
	 *            allowed object is {@link XmlZeroOrMore }
	 * 
	 */
	public void setPosition(double value)
	{
		this.position = value;
	}
	
	/**
	 * As yet, mouthpieces are not named.
	 */
	public String getName()
	{
		return "";
	}

	/**
	 * @return the beta
	 */
	public Double getBeta()
	{
		return beta;
	}

	/**
	 * Set the jet amplification factor, and the instrument-specific loop gain
	 * factor, after Auvray, 2012. Loop gain G = gainFactor * freq * rho /
	 * abs(Z).
	 * 
	 * @param aBeta
	 *            the jet amplification factor to set
	 */
	public void setBeta(Double aBeta)
	{
		this.beta = aBeta;
		// For instruments without beta, calculate a gain factor with a default beta.
		double nominalBeta = 0.35d;
		if (this.beta != null)
		{
			nominalBeta = this.beta;
		}

		if (this.fipple != null && this.fipple.windwayHeight != null)
		{
			this.gainFactor = (8.0
					* this.fipple.windwayHeight
					* Math.sqrt(2.0 * this.fipple.windwayHeight
							/ this.fipple.windowLength)
					* Math.exp(nominalBeta * this.fipple.windowLength
							/ this.fipple.windwayHeight)
					/ (this.fipple.windowLength * this.fipple.windowWidth));
		}
		else if (this.embouchureHole != null)
		{
			this.gainFactor = (8.0
					* this.embouchureHole.airstreamHeight
					* Math.sqrt(2.0 * this.embouchureHole.airstreamHeight
							/ this.embouchureHole.airstreamLength)
					* Math.exp(nominalBeta * this.embouchureHole.airstreamLength
							/ this.embouchureHole.airstreamHeight)
					/ (this.embouchureHole.length * this.embouchureHole.airstreamLength));
		}
		else
		{
			// Cannot calculate gain.
			this.gainFactor = null;
		}
	}

	/**
	 * @return the gain factor.
	 */
	public Double getGainFactor()
	{
		return gainFactor;
	}

	/**
	 * Set the jet amplification factor, and the instrument-specific loop gain
	 * factor, after Auvray, 2012. Loop gain G = gainFactor * freq * rho /
	 * abs(Z).
	 * 
	 * @param aGainFactor
	 *            the gain factor to set
	 */
	public void setGainFactor(Double aGainFactor)
	{
		this.gainFactor = aGainFactor;
		if (this.fipple != null && this.fipple.windwayHeight != null
			&& this.gainFactor != null)
		{
			this.beta = this.fipple.windwayHeight
					/ this.fipple.windowLength
					* Math.log(this.gainFactor
							/ (8.0 * this.fipple.windwayHeight)
							* Math.sqrt(0.5 * this.fipple.windowLength
									/ this.fipple.windwayHeight)
							* (this.fipple.windowLength * this.fipple.windowWidth));
		}
		else if (this.embouchureHole != null
				&& this.gainFactor != null)
			{
				this.beta = this.embouchureHole.airstreamHeight
						/ this.embouchureHole.airstreamLength
						* Math.log(this.gainFactor
								/ (8.0 * this.embouchureHole.airstreamHeight)
								* Math.sqrt(0.5 * this.embouchureHole.airstreamLength
										/ this.embouchureHole.airstreamHeight)
								* (this.embouchureHole.length * this.embouchureHole.airstreamLength));
			}
		else
		{
			this.beta = null;
		}
	}
	
	/**
	 * Test the type of mouthpiece for a pressure node.
	 * @return true for cane or lip reed mouthpiece, false for air reed (flute) mouthpiece.
	 */
	public boolean isPressureNode()
	{
		if (singleReed != null || doubleReed != null || lipReed != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Gets the value of the embouchureHole property.
	 * 
	 * @return possible object is {@link Mouthpiece.EmbouchureHole }
	 * 
	 */
	public Mouthpiece.EmbouchureHole getEmbouchureHole()
	{
		return embouchureHole;
	}

	/**
	 * Sets the value of the embouchureHole property.
	 * 
	 * @param value
	 *            allowed object is {@link Mouthpiece.EmbouchureHole }
	 * 
	 */
	public void setEmbouchureHole(Mouthpiece.EmbouchureHole value)
	{
		this.embouchureHole = value;
		if (value != null)
		{
			this.fipple = null;
			this.singleReed = null;
			this.doubleReed = null;
			this.lipReed = null;
		}
		// Recalculate gain factor.
		setBeta(this.beta);
	}

	/**
	 * Gets the value of the fipple property.
	 * 
	 * @return possible object is {@link Mouthpiece.Fipple }
	 * 
	 */
	public Mouthpiece.Fipple getFipple()
	{
		return fipple;
	}

	/**
	 * Sets the value of the fipple property.
	 * 
	 * @param value
	 *            allowed object is {@link Mouthpiece.Fipple }
	 * 
	 */
	public void setFipple(Mouthpiece.Fipple value)
	{
		this.fipple = value;
		if (value != null)
		{
			this.embouchureHole = null;
			this.singleReed = null;
			this.doubleReed = null;
			this.lipReed = null;
		}
		// Re-calculate gainFactor when the fipple changes.
		this.setBeta(this.beta);
	}

	/**
	 * Gets the single-reed mouthpiece.
	 * 
	 * @return possible object is {@link Mouthpiece.SingleReed }
	 * 
	 */
	public Mouthpiece.SingleReed getSingleReed()
	{
		return singleReed;
	}

	/**
	 * Sets the single-reed mouthpiece.
	 * 
	 * @param value
	 *            allowed object is {@link Mouthpiece.SingleReed }
	 * 
	 */
	public void setSingleReed(Mouthpiece.SingleReed value)
	{
		this.singleReed = value;
		if (value != null)
		{
			this.embouchureHole = null;
			this.fipple = null;
			this.doubleReed = null;
			this.lipReed = null;
		}
		// Re-calculate gainFactor when the reed changes.
		this.setBeta(this.beta);
	}
	
	/**
	 * Gets the double-reed mouthpiece.
	 * 
	 * @return possible object is {@link Mouthpiece.DoubleReed }
	 * 
	 */
	public Mouthpiece.DoubleReed getDoubleReed()
	{
		return doubleReed;
	}

	/**
	 * Sets the double-reed mouthpiece.
	 * 
	 * @param value
	 *            allowed object is {@link Mouthpiece.DoubleReed }
	 * 
	 */
	public void setDoubleReed(Mouthpiece.DoubleReed value)
	{
		this.doubleReed = value;
		if (value != null)
		{
			this.embouchureHole = null;
			this.fipple = null;
			this.singleReed = null;
			this.lipReed = null;
		}
		// Re-calculate gainFactor when the reed changes.
		this.setBeta(this.beta);
	}
	
	/**
	 * Gets the lip-reed (brass) mouthpiece.
	 * 
	 * @return possible object is {@link Mouthpiece.LipReed }
	 * 
	 */
	public Mouthpiece.LipReed getLipReed()
	{
		return lipReed;
	}

	/**
	 * Sets the lip-reed (brass) mouthpiece.
	 * 
	 * @param value
	 *            allowed object is {@link Mouthpiece.LipReed }
	 * 
	 */
	public void setLipReed(Mouthpiece.LipReed value)
	{
		this.lipReed = value;
		if (value != null)
		{
			this.embouchureHole = null;
			this.fipple = null;
			this.singleReed = null;
			this.doubleReed = null;
		}
		// Re-calculate gainFactor when the reed changes.
		this.setBeta(this.beta);
	}
	
	public double getAirstreamLength()
	{
		if (this.fipple != null )
		{
			return this.fipple.windowLength;
		}
		if (this.embouchureHole != null)
		{
			return this.embouchureHole.airstreamLength;
		}
		// Return an arbitrary length, of a plausible magnitude.
		return 0.5 * this.boreDiameter;
	}

	public void convertDimensions(double multiplier)
	{
		position *= multiplier;
		if (gainFactor != null)
		{
			// The gain factor has dimensions 1/length.
			gainFactor /= multiplier;
		}

		if (embouchureHole != null)
		{
			embouchureHole.convertDimensions(multiplier);
		}

		if (fipple != null)
		{
			fipple.convertDimensions(multiplier);
		}

		if (singleReed != null)
		{
			singleReed.convertDimensions(multiplier);
		}

		if (doubleReed != null)
		{
			doubleReed.convertDimensions(multiplier);
		}

		if (lipReed != null)
		{
			lipReed.convertDimensions(multiplier);
		}
	}

	public void checkValidity(InvalidFieldHandler handler, Double minPosition, Double maxPosition)
	{
		if (Double.isNaN(position))
		{
			handler.logError("The mouthpiece/splitting-edge position must be specified");
		}
		else if (isPressureNode())
		{
			if (minPosition != null && (minPosition > position || minPosition + 0.0001 < position))
			{
				handler.logError("The mouthpiece position must be the lowest bore position.");
			}
		}
		else
		{
			if (minPosition != null && minPosition > position)
			{
				handler.logError("The mouthpiece/splitting-edge position must not be less than lowest bore position.");
			}
			if (maxPosition != null && maxPosition <= position)
			{
				handler.logError("The mouthpiece/splitting-edge position must be less than highest bore position.");
			}
		}
		if (fipple != null)
		{
			fipple.checkValidity(handler);
		}
		else if (embouchureHole != null)
		{
			embouchureHole.checkValidity(handler);
		}
		else if (singleReed != null)
		{
			singleReed.checkValidity(handler);
		}
		else if (doubleReed != null)
		{
			doubleReed.checkValidity(handler);
		}
		else if (lipReed != null)
		{
			lipReed.checkValidity(handler);
		}
		else
		{
			handler.logError("The type of mouthpiece is not specified");
		}
		if (beta != null && beta < 0.0)
		{
			// For now, allow negative beta.
			// handler.logError("Beta, if specified, must be zero or more.");
		}
	}

	/**
	 * Detail class for transverse flute embouchure hole.
	 * 
	 */
	public static class EmbouchureHole
	{
		protected double length;
		protected double width;
		protected double height;
		protected double airstreamLength;
		protected double airstreamHeight;

		/**
		 * Gets the embouchure hole length (size in longitudinal direction).
		 */
		public double getLength()
		{
			return length;
		}

		/**
		 * Sets the embouchure hole length (size in longitudinal direction).
		 */
		public void setLength(double aLength)
		{
			this.length = aLength;
		}

		/**
		 * Gets the embouchure hole width (size in transverse direction,
		 * direction of air stream).
		 */
		public double getWidth()
		{
			return width;
		}

		/**
		 * Sets the embouchure hole width (size in transverse direction,
		 * direction of air stream).
		 */
		public void setWidth(double aWidth)
		{
			this.width = aWidth;
		}

		/**
		 * @return the height of the embouchure hole
		 */
		public double getHeight()
		{
			return height;
		}

		/**
		 * Set the height of the embouchure hole.
		 * @param aHeight
		 *            the height to set
		 */
		public void setHeight(double aHeight)
		{
			this.height = aHeight;
		}

		/**
		 * @return the length of the air stream from the player's lips
		 * to the edge of the embouchure hole
		 */
		public double getAirstreamLength()
		{
			return airstreamLength;
		}

		/**
		 * Set the length of the air stream from the player's lips
		 * to the edge of the embouchure hole.
		 * @param aLength
		 *            the new value of airstreamLength
		 */
		public void setAirstreamLength(double aLength)
		{
			this.airstreamLength = aLength;
		}

		/**
		 * @return the height of the air stream from the player's lips
		 */
		public double getAirstreamHeight()
		{
			return airstreamHeight;
		}

		/**
		 * Set the height of the air stream from the player's lips.
		 * @param aHeight
		 *            the height to set
		 */
		public void setAirstreamHeight(double aHeight)
		{
			this.airstreamHeight = aHeight;
		}

		public void convertDimensions(double multiplier)
		{
			length *= multiplier;
			width *= multiplier;
			height *= multiplier;
			airstreamLength *= multiplier;
			airstreamHeight *= multiplier;
		}

		public void checkValidity(InvalidFieldHandler handler)
		{
			if (Double.isNaN(length))
			{
				handler.logError("Embouchure hole length must be specified.");
			}
			else if (length <= 0.0)
			{
				handler.logError("Embouchure hole length must be positive.");
			}
			if (Double.isNaN(width))
			{
				handler.logError("Embouchure hole width must be specified.");
			}
			else if (width <= 0.0)
			{
				handler.logError("Embouchure hole width must be positive.");
			}
			if (Double.isNaN(height))
			{
				handler.logError("Embouchure hole height must be specified.");
			}
			else if (height <= 0.0)
			{
				handler.logError("Embouchure hole height must be positive.");
			}
			if (Double.isNaN(airstreamLength))
			{
				handler.logError("Air stream length must be specified.");
			}
			else if (airstreamLength <= 0.0)
			{
				handler.logError("Air stream length must be positive.");
			}
			if (Double.isNaN(airstreamHeight))
			{
				handler.logError("Air stream (player's embouchure) height must be specified.");
			}
			else if (airstreamHeight <= 0.0)
			{
				handler.logError("Air stream (player's embouchure) height must be positive.");
			}
		}
	}

	/**
	 * Detail class for the window and windway of a fipple flute.
	 */
	public static class Fipple
	{
		protected double windowWidth;
		protected double windowLength;
		protected Double fippleFactor;
		protected Double windowHeight;
		protected Double windwayLength;
		protected Double windwayHeight;

		/**
		 * @return the windowWidth
		 */
		public double getWindowWidth()
		{
			return windowWidth;
		}

		/**
		 * @param aWindowWidth
		 *            the windowWidth to set
		 */
		public void setWindowWidth(double aWindowWidth)
		{
			this.windowWidth = aWindowWidth;
		}

		/**
		 * @return the windowLength
		 */
		public double getWindowLength()
		{
			return windowLength;
		}

		/**
		 * @param aWindowLength
		 *            the windowLength to set
		 */
		public void setWindowLength(double aWindowLength)
		{
			this.windowLength = aWindowLength;
		}

		/**
		 * @return the fippleFactor
		 */
		public Double getFippleFactor()
		{
			return fippleFactor;
		}

		/**
		 * @param aFippleFactor
		 *            the fippleFactor to set
		 */
		public void setFippleFactor(Double aFippleFactor)
		{
			this.fippleFactor = aFippleFactor;
		}

		/**
		 * @return the windowHeight
		 */
		public Double getWindowHeight()
		{
			return windowHeight;
		}

		/**
		 * @param aWindowHeight
		 *            the windowHeight to set
		 */
		public void setWindowHeight(Double aWindowHeight)
		{
			this.windowHeight = aWindowHeight;
		}

		/**
		 * @return the windwayLength
		 */
		public Double getWindwayLength()
		{
			return windwayLength;
		}

		/**
		 * @param aWindwayLength
		 *            the windwayLength to set
		 */
		public void setWindwayLength(Double aWindwayLength)
		{
			this.windwayLength = aWindwayLength;
		}

		/**
		 * @return the windwayHeight
		 */
		public Double getWindwayHeight()
		{
			return windwayHeight;
		}

		/**
		 * @param aWindwayHeight
		 *            the windwayHeight to set
		 */
		public void setWindwayHeight(Double aWindwayHeight)
		{
			this.windwayHeight = aWindwayHeight;
		}

		public void convertDimensions(double multiplier)
		{
			windowWidth *= multiplier;
			windowLength *= multiplier;
			if (windowHeight != null)
			{
				windowHeight *= multiplier;
			}
			if (windwayLength != null)
			{
				windwayLength *= multiplier;
			}
			if (windwayHeight != null)
			{
				windwayHeight *= multiplier;
			}
		}

		public void checkValidity(InvalidFieldHandler handler)
		{
			if (Double.isNaN(windowLength))
			{
				handler.logError("Window or TSH length must specified.");
			}
			else if (windowLength <= 0.0)
			{
				handler.logError("Window or TSH length must be positive.");
			}
			if (Double.isNaN(windowWidth))
			{
				handler.logError("Window or TSH width must be specified.");
			}
			else if (windowWidth <= 0.0)
			{
				handler.logError("Window or TSH width must be positive.");
			}
			if (windowHeight != null && windowHeight <= 0.0)
			{
				handler.logError("Window height, if specified, must be positive.");
			}
			if (windwayHeight != null && windwayHeight <= 0.0)
			{
				handler.logError("Windway height or flue depth, if specified, must be positive.");
			}
			if (windwayLength != null && windwayLength <= 0.0)
			{
				handler.logError("Windway length, if specified, must be positive.");
			}
			if (fippleFactor != null && fippleFactor <= 0.0)
			{
				handler.logError("Fipple factor, if specified, must be positive.");
			}
		}
	}


	/**
	 * Detail class for a single-reed mouthpiece.
	 */
	public static class SingleReed
	{
		protected double alpha;

		/**
		 * @return the alpha
		 */
		public double getAlpha()
		{
			return alpha;
		}

		/**
		 * @param aAlpha
		 *            the alpha to set
		 */
		public void setAlpha(double aAlpha)
		{
			this.alpha = aAlpha;
		}

		public void convertDimensions(double multiplier)
		{
		}

		public void checkValidity(InvalidFieldHandler handler)
		{
			if (Double.isNaN(alpha))
			{
				handler.logError("Alpha factor must specified.");
			}
			else if (alpha < 0.0)
			{
				// For now, allow negative alpha.
				// handler.logError("Alpha factor must be zero or more.");
			}
		}
	}

	/**
	 * Detail class for a double-reed mouthpiece.
	 */
	public static class DoubleReed
	{
		protected double alpha;
		protected double crowFreq;

		/**
		 * @return the alpha
		 */
		public double getAlpha()
		{
			return alpha;
		}

		/**
		 * @param aAlpha
		 *            the alpha to set
		 */
		public void setAlpha(double aAlpha)
		{
			this.alpha = aAlpha;
		}


		/**
		 * @return the crowFreq
		 */
		public double getCrowFreq()
		{
			return crowFreq;
		}

		/**
		 * @param aCrowFreq
		 *            the crowFreq to set
		 */
		public void setCrowFreq(double aCrowFreq)
		{
			this.crowFreq = aCrowFreq;
		}

		public void convertDimensions(double multiplier)
		{
		}

		public void checkValidity(InvalidFieldHandler handler)
		{
			if (Double.isNaN(alpha))
			{
				handler.logError("Alpha factor must specified.");
			}
			else if (alpha < 0.0)
			{
				// For now, allow negative alpha.
				// handler.logError("Alpha factor must be zero or more.");
			}
			if (Double.isNaN(crowFreq))
			{
				handler.logError("Crow frequency must specified.");
			}
			else if (crowFreq <= 0.0)
			{
				handler.logError("Crow frequency must be positive.");
			}
		}
	}

	/**
	 * Detail class for a lip-reed (brass) mouthpiece
	 * details as yet unspecified.
	 */
	public static class LipReed
	{
		protected double alpha;

		/**
		 * @return the alpha
		 */
		public double getAlpha()
		{
			return alpha;
		}

		/**
		 * @param aAlpha
		 *            the alpha to set
		 */
		public void setAlpha(double aAlpha)
		{
			this.alpha = aAlpha;
		}

		public void convertDimensions(double multiplier)
		{
		}

		public void checkValidity(InvalidFieldHandler handler)
		{
			if (Double.isNaN(alpha))
			{
				handler.logError("Alpha factor must specified.");
			}
		}
	}

	@Override
	public double getBorePosition()
	{
		return position;
	}

	@Override
	public void setBorePosition(double aPosition)
	{
		this.position = aPosition;
	}

	@Override
	public void setBoreDiameter(double aBoreDiameter)
	{
		this.boreDiameter = aBoreDiameter;
	}

	@Override
	public double getBoreDiameter()
	{
		return boreDiameter;
	}

	/**
	 * @return the headspace
	 */
	public List<BoreSection> getHeadspace()
	{
		return headspace;
	}

	/**
	 * @param aHeadspace
	 *            the headspace to set
	 */
	public void setHeadspace(List<BoreSection> aHeadspace)
	{
		this.headspace = aHeadspace;
	}

}

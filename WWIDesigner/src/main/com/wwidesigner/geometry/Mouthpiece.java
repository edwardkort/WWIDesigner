/**
 * 
 */
package com.wwidesigner.geometry;

import java.util.List;

import com.wwidesigner.util.InvalidFieldHandler;

/**
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
	 * @param beta
	 *            the jet amplification factor to set
	 */
	public void setBeta(Double beta)
	{
		this.beta = beta;
		if (this.fipple != null && this.fipple.windwayHeight != null
				&& beta != null)
		{
			this.gainFactor = (8.0
					* this.fipple.windwayHeight
					* Math.sqrt(2.0 * this.fipple.windwayHeight
							/ this.fipple.windowLength)
					* Math.exp(this.beta * this.fipple.windowLength
							/ this.fipple.windwayHeight) / (this.fipple.windowLength * this.fipple.windowWidth));
		}
		else
		{
			this.gainFactor = 1.0;
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
	 * @param gainFactor
	 *            the gain factor to set
	 */
	public void setGainFactor(Double gainFactor)
	{
		this.gainFactor = gainFactor;
		if (this.fipple.windwayHeight != null && this.gainFactor != null)
		{
			this.beta = this.fipple.windwayHeight
					/ this.fipple.windowLength
					* Math.log(this.gainFactor
							/ (8.0 * this.fipple.windwayHeight)
							* Math.sqrt(0.5 * this.fipple.windowLength
									/ this.fipple.windwayHeight)
							* (this.fipple.windowLength * this.fipple.windowWidth));
		}
		else
		{
			this.beta = 0.3;
		}
	}

	/**
	 * Gets the value of the embouchureHole property.
	 * 
	 * @return possible object is {@link XmlMouthpiece.EmbouchureHole }
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
	 *            allowed object is {@link XmlMouthpiece.EmbouchureHole }
	 * 
	 */
	public void setEmbouchureHole(Mouthpiece.EmbouchureHole value)
	{
		this.embouchureHole = value;
	}

	/**
	 * Gets the value of the fipple property.
	 * 
	 * @return possible object is {@link XmlMouthpiece.Fipple }
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
	 *            allowed object is {@link XmlMouthpiece.Fipple }
	 * 
	 */
	public void setFipple(Mouthpiece.Fipple value)
	{
		this.fipple = value;
		// Re-calculate gainFactor when the fipple changes.
		this.setBeta(this.beta);
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
	}

	public void checkValidity(InvalidFieldHandler handler, Double minPosition, Double maxPosition)
	{
		if (Double.isNaN(position))
		{
			handler.logError("The mouthpiece/splitting-edge position must be specified");
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
		else
		{
			handler.logError("The type of mouthpiece is not specified");
		}
		if (beta != null && (beta <= 0.0 || beta >= 1.0))
		{
			handler.logError("Beta, if specified, must be positive and less than 1.");
		}
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 */
	public static class EmbouchureHole
	{

		protected double innerDiameter;
		protected double outerDiameter;
		protected double height;

		/**
		 * Gets the value of the innerDiameter property.
		 * 
		 */
		public double getInnerDiameter()
		{
			return innerDiameter;
		}

		/**
		 * Sets the value of the innerDiameter property.
		 * 
		 */
		public void setInnerDiameter(double value)
		{
			this.innerDiameter = value;
		}

		/**
		 * Gets the value of the outerDiameter property.
		 * 
		 */
		public double getOuterDiameter()
		{
			return outerDiameter;
		}

		/**
		 * Sets the value of the outerDiameter property.
		 * 
		 */
		public void setOuterDiameter(double value)
		{
			this.outerDiameter = value;
		}

		/**
		 * @return the height
		 */
		public double getHeight()
		{
			return height;
		}

		/**
		 * @param height
		 *            the height to set
		 */
		public void setHeight(double height)
		{
			this.height = height;
		}

		public void convertDimensions(double multiplier)
		{
			innerDiameter *= multiplier;
			outerDiameter *= multiplier;
			height *= multiplier;
		}

		public void checkValidity(InvalidFieldHandler handler)
		{
			if (Double.isNaN(innerDiameter))
			{
				handler.logError("Embouchure hole inner diameter must be specified.");
			}
			else if (innerDiameter <= 0.0)
			{
				handler.logError("Embouchure hole inner diameter must be positive.");
			}
			if (Double.isNaN(outerDiameter))
			{
				handler.logError("Embouchure hole outer diameter must be specified.");
			}
			else if (outerDiameter <= 0.0)
			{
				handler.logError("Embouchure hole outer diameter must be positive.");
			}
			if (Double.isNaN(height))
			{
				handler.logError("Embouchure hole height must be specified.");
			}
			else if (height <= 0.0)
			{
				handler.logError("Embouchure hole height must be positive.");
			}
		}
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
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
		 * @param windowWidth
		 *            the windowWidth to set
		 */
		public void setWindowWidth(double windowWidth)
		{
			this.windowWidth = windowWidth;
		}

		/**
		 * @return the windowLength
		 */
		public double getWindowLength()
		{
			return windowLength;
		}

		/**
		 * @param windowLength
		 *            the windowLength to set
		 */
		public void setWindowLength(double windowLength)
		{
			this.windowLength = windowLength;
		}

		/**
		 * @return the fippleFactor
		 */
		public Double getFippleFactor()
		{
			return fippleFactor;
		}

		/**
		 * @param fippleFactor
		 *            the fippleFactor to set
		 */
		public void setFippleFactor(Double fippleFactor)
		{
			this.fippleFactor = fippleFactor;
		}

		/**
		 * @return the windowHeight
		 */
		public Double getWindowHeight()
		{
			return windowHeight;
		}

		/**
		 * @param windowHeight
		 *            the windowHeight to set
		 */
		public void setWindowHeight(Double windowHeight)
		{
			this.windowHeight = windowHeight;
		}

		/**
		 * @return the windwayLength
		 */
		public Double getWindwayLength()
		{
			return windwayLength;
		}

		/**
		 * @param windwayLength
		 *            the windwayLength to set
		 */
		public void setWindwayLength(Double windwayLength)
		{
			this.windwayLength = windwayLength;
		}

		/**
		 * @return the windwayHeight
		 */
		public Double getWindwayHeight()
		{
			return windwayHeight;
		}

		/**
		 * @param windwayHeight
		 *            the windwayHeight to set
		 */
		public void setWindwayHeight(Double windwayHeight)
		{
			this.windwayHeight = windwayHeight;
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

	@Override
	public double getBorePosition()
	{
		return position;
	}

	@Override
	public void setBorePosition(double position)
	{
		this.position = position;
	}

	@Override
	public void setBoreDiameter(double boreDiameter)
	{
		this.boreDiameter = boreDiameter;
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
	 * @param headspace
	 *            the headspace to set
	 */
	public void setHeadspace(List<BoreSection> headspace)
	{
		this.headspace = headspace;
	}

}

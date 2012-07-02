/**
 * 
 */
package com.wwidesigner.geometry;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class Mouthpiece implements ComponentInterface, MouthpieceInterface
{
	protected double position;
	protected double characteristicLength;
	protected Mouthpiece.EmbouchureHole embouchureHole;
	protected Mouthpiece.Fipple fipple;

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
	 * Gets the value of the characteristicLength property.
	 * 
	 * @return possible object is {@link XmlMoreThanZero }
	 * 
	 */
	public double getCharacteristicLength()
	{
		return characteristicLength;
	}

	/**
	 * Sets the value of the characteristicLength property.
	 * 
	 * @param value
	 *            allowed object is {@link XmlMoreThanZero }
	 * 
	 */
	public void setCharacteristicLength(double value)
	{
		this.characteristicLength = value;
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
		protected double length;

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
		 * Gets the value of the length property.
		 * 
		 */
		public double getLength()
		{
			return length;
		}

		/**
		 * Sets the value of the length property.
		 * 
		 */
		public void setLength(double value)
		{
			this.length = value;
		}

	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 */
	public static class Fipple
	{

		protected double width;
		protected double length;
		protected double fippleFactor;

		/**
		 * Gets the value of the width property.
		 * 
		 */
		public double getWidth()
		{
			return width;
		}

		/**
		 * Sets the value of the width property.
		 * 
		 */
		public void setWidth(double value)
		{
			this.width = value;
		}

		/**
		 * Gets the value of the length property.
		 * 
		 */
		public double getLength()
		{
			return length;
		}

		/**
		 * Sets the value of the length property.
		 * 
		 */
		public void setLength(double value)
		{
			this.length = value;
		}

		/**
		 * Gets the value of the fippleFactor property.
		 * 
		 */
		public double getFippleFactor()
		{
			return fippleFactor;
		}

		/**
		 * Sets the value of the fippleFactor property.
		 * 
		 */
		public void setFippleFactor(double value)
		{
			this.fippleFactor = value;
		}

	}

	@Override
	public TransferMatrix calcTransferMatrix(double waveNumber,
			PhysicalParameters parameters)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int calcReflectanceMultiplier()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}

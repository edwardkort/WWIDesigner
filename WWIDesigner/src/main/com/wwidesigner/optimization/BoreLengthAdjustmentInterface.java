/**
 * 
 */
package com.wwidesigner.optimization;

/**
 * @author Edward Kort
 *
 */
public interface BoreLengthAdjustmentInterface
{
	public enum BoreLengthAdjustmentType
	{
		/**
		 * Change the position of the bottom bore point, adjusting the diameter
		 * to keep the taper angle unchanged.
		 */
		PRESERVE_TAPER,
		/**
		 * Change position of all bore points below longest bore segment,
		 * leaving bore diameters unchanged.
		 */
		PRESERVE_BELL,
		/**
		 * Change position of the bottom bore point, leaving bore diameters
		 * unchanged.
		 */
		MOVE_BOTTOM,
		/**
		 * As in PRESERVE_TAPER, but throw an Exception if an intermediate bore
		 * point is moved.
		 */
		PRESERVE_BORE
	}
	
	public double MINIMUM_BORE_POINT_SPACING = 0.00001d;

	public void setBore(double[] point);
}

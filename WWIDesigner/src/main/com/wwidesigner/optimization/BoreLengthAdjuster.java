/**
 * 
 */
package com.wwidesigner.optimization;


/**
 * @author Edward Kort
 *
 */
public class BoreLengthAdjuster implements BoreLengthAdjustmentInterface
{
	protected BaseObjectiveFunction mParent;
	protected BoreLengthAdjustmentInterface adjustmentWorker;

	public BoreLengthAdjuster(BaseObjectiveFunction parent,
			BoreLengthAdjustmentType adjustmentType)
	{
		mParent = parent;
		setAdjustmentWorker(adjustmentType);
	}

	protected void setAdjustmentWorker(BoreLengthAdjustmentType adjustmentType)
	{
		switch (adjustmentType)
		{
			case MOVE_BOTTOM:
				adjustmentWorker = new BoreLengthAdjusterMoveBottom(mParent);
				break;
			case PRESERVE_BELL:
				adjustmentWorker = new BoreLengthAdjusterPreserveBell(mParent);
				break;
			case PRESERVE_BORE:
				adjustmentWorker = new BoreLengthAdjusterPreserveBore(mParent);
				break;
			case PRESERVE_TAPER:
				adjustmentWorker = new BoreLengthAdjusterPreserveTaper(mParent);
				break;
			default:
				adjustmentWorker = null;
				break;
		}
	}

	public void setBore(double[] point)
	{
		adjustmentWorker.setBore(point);
	}
}

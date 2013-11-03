/**
 * 
 */
package com.wwidesigner.optimization.run;

import com.wwidesigner.optimization.HoleGroupSpacingOptimizer;

/**
 * @author kort
 * 
 */
@Deprecated
public class HoleGroupSpacingOptimizationRunnner extends BaseOptimizationRunner
{
	protected int[][] holeGroups;

	public void setHoleGroups(int[][] groups)
	{
		holeGroups = groups;
	}

	@Override
	protected void setupCustomOptimizer() throws Exception
	{
		((HoleGroupSpacingOptimizer) optimizer).setHoleGroups(holeGroups);
	}

}

package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.wwidesigner.gui.WhistleStudyModel;
import com.wwidesigner.gui.StudyModel.Category;

public class WhistleOptimizationTest extends PerturbedInstrumentOptimization
{
	protected String instrumentFile = "com/wwidesigner/optimization/example/Optimized-D-Whistle.xml";
	protected String tuningFile = "com/wwidesigner/optimization/example/D-Tuning.xml";
	protected double initialNorm = 0.0;

	@Test
	public final void optimizationTest() throws Exception
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		
		// Set up the study model to be tested.

		WhistleStudyModel myStudy = new WhistleStudyModel();
		myStudy.getParams().setProperties(27.0, 98.4, 100, 0.040);
		myStudy.setBlowingLevel(4);
		Category optimizerCat = myStudy.getCategory(WhistleStudyModel.OPTIMIZER_CATEGORY_ID);
		myStudy.setCategorySelection(optimizerCat, WhistleStudyModel.HOLE_OPT_SUB_CATEGORY_ID);
		
		setStudyModel(myStudy);
		setTuning(tuningFile);
		setInstrument(instrumentFile, 1.0, 1.0, 1.0);
		testOptimization("Re-optimize the un-perturbed instrument...", 0.5);
		initialNorm = study.getInitialNorm();
		assertEquals("Residual error incorrect", 1.0, study.getResidualErrorRatio(), 0.02);

		perturbInstrument(1.05,1.05,0.95);
		testOptimization("Optimize instrument after 5% stretch...", 0.5);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/initialNorm, 0.02);

		perturbInstrument(0.96,0.96,1.04);
		testOptimization("Optimize instrument after 4% shrink...", 0.5);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/initialNorm, 0.02);

		myStudy.setCategorySelection(optimizerCat, WhistleStudyModel.HOLESIZE_OPT_SUB_CATEGORY_ID);
		perturbInstrument(1.0,1.0,1.10);
		testOptimization("Optimize instrument from 10% larger holes...", 0.5);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/initialNorm, 0.02);
	}
}

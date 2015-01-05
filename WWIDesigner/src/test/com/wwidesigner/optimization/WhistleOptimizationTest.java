package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.gui.StudyModel;
import com.wwidesigner.gui.WhistleStudyModel;

public class WhistleOptimizationTest extends PerturbedInstrumentOptimization
{
	protected String instrumentFile = "com/wwidesigner/optimization/example/Optimized-D-Whistle.xml";
	protected String tuningFile = "com/wwidesigner/optimization/example/D-Tuning.xml";
	protected String remoteTuningFile = "com/wwidesigner/optimization/example/A4-Just-Tuning.xml";
	protected double finalNorm = 0.0;

	public WhistleOptimizationTest()
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
	}

//	@Test
	public final void optimizationTest() throws Exception
	{
		// Set up the study model to be tested.

		WhistleStudyModel myStudy = new WhistleStudyModel();
		myStudy.getParams().setProperties(27.0, 98.4, 100, 0.040);
		myStudy.setBlowingLevel(4);
		myStudy.setCategorySelection(WhistleStudyModel.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.HOLE_OPT_SUB_CATEGORY_ID);
		
		setStudyModel(myStudy);
		setTuning(tuningFile);
		setInstrument(instrumentFile, 1.0, 1.0, 1.0);
		testOptimization("Re-optimize the un-perturbed instrument...", 0.5);
		finalNorm = study.getFinalNorm();
		assertEquals("Residual error incorrect", 1.0, study.getResidualErrorRatio(), 0.05);

		perturbInstrument(1.05,1.05,0.95);
		testOptimization("Optimize instrument after 5% stretch...", 0.5);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/finalNorm, 0.01);

		perturbInstrument(0.95,0.95,1.05);
		testOptimization("Optimize instrument after 5% shrink...", 0.5);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/finalNorm, 0.01);

		myStudy.setCategorySelection(WhistleStudyModel.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.HOLESIZE_OPT_SUB_CATEGORY_ID);
		perturbInstrument(1.0,1.0,1.10);
		testOptimization("Optimize instrument from 10% larger holes...", 0.5);
		assertEquals("Residual error incorrect", 1.0, study.getFinalNorm()/finalNorm, 0.01);
	}

	@Test
	public final void roughCutTest() throws Exception
	{
		// Try to optimize the same instrument against a totally different tuning file,
		// A4 pitch instead of D5.

		WhistleStudyModel myStudy = new WhistleStudyModel();
		myStudy.getParams().setProperties(27.0, 98.4, 100, 0.040);
		myStudy.setBlowingLevel(4);
		myStudy.setCategorySelection(WhistleStudyModel.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.ROUGH_CUT_OPT_SUB_CATEGORY_ID);
		
		setStudyModel(myStudy);
		setTuning(remoteTuningFile);
		setInstrument(instrumentFile, 1.0, 1.0, 1.0);

		// Optimize the instrument against the new tuning file.
		System.out.println();
		System.out.println("Optimize for A4 tuning instead of D5...");
		String xmlOptimized = study.optimizeInstrument();
		replaceInstrument(originalInstrument.getName(), xmlOptimized);

		study.setCategorySelection(WhistleStudyModel.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.HOLE_OPT_SUB_CATEGORY_ID);
		Instrument optimizedInstrument = StudyModel.getInstrument(study.optimizeInstrument());
		
		// Test final error norm.
		assertEquals("Final error norm incorrect", 836.8, study.getFinalNorm(), 1.0);

		// Test bore length

		List<BorePoint> newBorePoints = optimizedInstrument.getBorePoint();
		PositionInterface[] sortedPoints;
		sortedPoints = Instrument.sortList(newBorePoints);
		double newBoreLength = sortedPoints[sortedPoints.length - 1].getBorePosition();
		assertEquals("Bore length incorrect", 362.1, newBoreLength, 3.0);
	}
}

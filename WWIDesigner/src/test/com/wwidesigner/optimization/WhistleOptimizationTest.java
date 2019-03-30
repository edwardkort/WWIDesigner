package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.gui.CategoryType;
import com.wwidesigner.gui.StudyModel;
import com.wwidesigner.gui.WhistleStudyModel;

public class WhistleOptimizationTest extends PerturbedInstrumentOptimization
{
	protected String instrumentFile = "com/wwidesigner/optimization/example/Optimized-D-Whistle.xml";
	protected String tuningFile = "com/wwidesigner/optimization/example/Optimized-D-Tuning.xml";
	protected String remoteTuningFile = "com/wwidesigner/optimization/example/A4-Just-Tuning.xml";
	protected double finalNorm = 0.0;

	public WhistleOptimizationTest()
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
	}

	@Test
	public final void optimizationTest() throws Exception
	{
		// Set up the study model to be tested.

		WhistleStudyModel myStudy = new WhistleStudyModel();
		myStudy.getParams().setProperties(27.0, 98.4, 100.0, 0.040);
		myStudy.setBlowingLevel(4);
		myStudy.setCategorySelection(CategoryType.OPTIMIZER_CATEGORY_ID,
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

		myStudy.setCategorySelection(CategoryType.OPTIMIZER_CATEGORY_ID,
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
		myStudy.getParams().setProperties(27.0, 98.4, 100.0, 0.040);
		myStudy.setBlowingLevel(4);
		myStudy.setCategorySelection(CategoryType.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.GLOBAL_HOLESPACE_OPT_SUB_CATEGORY_ID);
		
		setStudyModel(myStudy);
		setTuning(remoteTuningFile);
		setInstrument(instrumentFile, 1.0, 1.0, 1.0);

		// Optimize the instrument against the new tuning file.
		System.out.println();
		System.out.println("Optimize for A4 tuning instead of D5...");
		String xmlOptimized = study.optimizeInstrument();
		replaceInstrument(originalInstrument.getName(), xmlOptimized);

		study.setCategorySelection(CategoryType.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.HOLE_OPT_SUB_CATEGORY_ID);
		Instrument optimizedInstrument = StudyModel.getInstrument(study.optimizeInstrument());
		
		// Test final error norm.
		assertEquals("Final error norm incorrect", 866.7, study.getFinalNorm(), 1.0);

		// Test bore length

		List<BorePoint> newBorePoints = optimizedInstrument.getBorePoint();
		PositionInterface[] sortedPoints;
		sortedPoints = Instrument.sortList(newBorePoints);
		double newBoreLength = sortedPoints[sortedPoints.length - 1].getBorePosition();
		assertEquals("Bore length incorrect", 362.1, newBoreLength, 3.0);
	}

	@Test
	public final void calibrationTest() throws Exception
	{
		// Set up the study model to be tested.

		WhistleStudyModel myStudy = new WhistleStudyModel();
		myStudy.getParams().setProperties(27.0, 98.4, 100.0, 0.040);
		myStudy.setBlowingLevel(4);
		myStudy.setCategorySelection(CategoryType.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.WHISTLE_CALIB_SUB_CATEGORY_ID);
		
		setStudyModel(myStudy);
		setTuning(tuningFile);
		setInstrument(instrumentFile, 1.0, 1.0, 1.0);
		// Slight change in tonehole model changes the window height.
		getOriginalInstrument().getMouthpiece().getFipple().setWindowHeight(2.75);
		getOriginalInstrument().getMouthpiece().setBeta(0.385);
		testOptimization("Re-calibrate the instrument...", 0.01);
		finalNorm = study.getFinalNorm();
		assertEquals("Final error norm incorrect", 3948.3, study.getFinalNorm(), 1.0);
	}

	@Test
	public final void boreOptimizationTest() throws Exception
	{
		// Optimize the head-joint bore to improve the intonation.

		WhistleStudyModel myStudy = new WhistleStudyModel();
		myStudy.getParams().setProperties(27.0, 98.4, 100.0, 0.040);
		myStudy.setBlowingLevel(4);
		myStudy.setCategorySelection(CategoryType.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.BORE_DIA_TOP_OPT_SUB_CATEGORY_ID);
		
		setStudyModel(myStudy);
		setTuning(tuningFile);
		setInstrument(instrumentFile, 1.0, 1.0, 1.0);

		// Optimize the instrument against the new tuning file.
		System.out.println();
		System.out.println("Optimize headjoint bore diameters...");
		Instrument optimizedInstrument = StudyModel.getInstrument(study.optimizeInstrument());
		
		// Test final error norm.
		assertEquals("Final error norm incorrect", 515.2, study.getFinalNorm(), 1.0);

		List<BorePoint> newBorePoints = optimizedInstrument.getBorePoint();
		assertEquals("Upper bore diameter incorrect", 11.04, newBorePoints.get(0).getBoreDiameter(), 0.1);
		assertEquals("Bore point 2 diameter incorrect", 11.90, newBorePoints.get(2).getBoreDiameter(), 0.1);
		assertEquals("Bore point 5 diameter incorrect", 11.90, newBorePoints.get(5).getBoreDiameter(), 0.1);

		// Optimize holes and headjoint bore profile to improve intonation even more.

		System.out.println();
		System.out.println("Optimize holes and headjoint bore diameters...");
		myStudy.setCategorySelection(CategoryType.OPTIMIZER_CATEGORY_ID,
				WhistleStudyModel.HOLE_BORE_DIA_TOP_OPT_SUB_CATEGORY_ID);
		optimizedInstrument = StudyModel.getInstrument(study.optimizeInstrument());
		
		// Test final error norm.
		assertEquals("Final error norm incorrect", 302.8, study.getFinalNorm(), 1.0);

		newBorePoints = optimizedInstrument.getBorePoint();
		assertEquals("Upper bore diameter incorrect", 10.82, newBorePoints.get(0).getBoreDiameter(), 0.1);
		assertEquals("Bore point 2 diameter incorrect", 11.90, newBorePoints.get(2).getBoreDiameter(), 0.1);
		assertEquals("Bore point 5 diameter incorrect", 11.90, newBorePoints.get(5).getBoreDiameter(), 0.1);
	}

}

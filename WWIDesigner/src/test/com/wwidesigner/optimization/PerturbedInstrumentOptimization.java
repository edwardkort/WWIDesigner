/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.gui.StudyModel;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.util.SortedPositionList;

/**
 * Base class for testing optimization functions in a StudyModel class.
 * Given an already-optimized instrument, applies a perturbation factor
 * on the dimensions of the instrument, re-optimizes the instrument,
 * and confirms that the result is (close to) the same as the
 * original instrument.
 * @author Burton Patkau
 * 
 */
public class PerturbedInstrumentOptimization
{
	// Basic variables that derived class should initialize
	// before calling base setup() function.
	protected StudyModel  study;
	protected Instrument  originalInstrument;

	/**
	 * Assign the study model to be tested.
	 * @param studyModel
	 */
	public void setStudyModel( StudyModel  studyModel )
	{
		this.study = studyModel;
	}

	/**
	 * Load the study model with the target tuning from a file.
	 * @param tuningFile - name of file containing tuning XML.
	 * @throws Exception 
	 */
	public void setTuning(String tuningFile) throws Exception
	{
		// Read the tuning from the tuning file.
		Tuning tuning = StudyModel.getTuningFromFile(tuningFile);
		// Extract the XML for the tuning, and load it into the study model.
		FileDataModel fileData = new FileDataModel();
		fileData.setData(StudyModel.marshal(tuning));
		fileData.setName(tuning.getName());
		study.addDataModel(fileData);
	}

	/**
	 * Load the study model with the target tuning from a file.
	 * @param tuningFile - name of file containing tuning XML.
	 * @throws Exception 
	 */
	public void setDefaultConstraints() throws Exception
	{
		// Extract the XML for the constraints, and load it into the study model.
		FileDataModel fileData = new FileDataModel();
		fileData.setData(study.getDefaultConstraints());
		fileData.setName("Default");
		study.addDataModel(fileData);
	}

	/**
	 * Load the study model with an instrument from a file,
	 * after applying specified perturbations.
	 * Pre:  testOptimization() assumes that the instrument in instrumentFile is already optimal.
	 * 		 setStudyModel() has been called.
	 * @param instrumentFile - name of file containing XML for original instrument.
	 * @param boreLengthFactor - Multiply the final bore length by this factor.
	 * @param spacingFactor - Multiply hole positions by this factor.
	 * @param diameterFactor - Multiply hole sizes by this factor.
	 * @throws Exception
	 */
	public void setInstrument(String instrumentFile,
			double boreLengthFactor, double spacingFactor, double diameterFactor) throws Exception
	{
		originalInstrument = StudyModel.getInstrumentFromFile(instrumentFile);
		perturbInstrument(boreLengthFactor, spacingFactor, diameterFactor);
	}
	
	/**
	 * Apply specified perturbations to the current instrument.
	 * Pre:  setStudyModel() and setInstrument() have been called.
	 * @param boreLengthFactor - Multiply the final bore length by this factor.
	 * @param spacingFactor - Multiply hole positions by this factor.
	 * @param diameterFactor - Multiply hole sizes by this factor.
	 * @throws Exception
	 */
	public void perturbInstrument(double boreLengthFactor, double spacingFactor, double diameterFactor) throws Exception
	{
		// Create a new instrument from the XML,

		Instrument perturbedInstrument = StudyModel.getInstrument(StudyModel.marshal(originalInstrument));
		
		// Perturb the dimensions of perturbedInstrument.

		// Alter bore length
		List<BorePoint> oldBorePoints = perturbedInstrument.getBorePoint();
		PositionInterface[] sortedPoints;
		sortedPoints = Instrument.sortList(oldBorePoints);
		double oldBoreLength = sortedPoints[sortedPoints.length - 1].getBorePosition();
		sortedPoints[sortedPoints.length - 1].setBorePosition(oldBoreLength * boreLengthFactor);

		// Alter hole sizes and positions
		for (Hole oldHole: perturbedInstrument.getHole())
		{
			oldHole.setBorePosition(oldHole.getBorePosition() * spacingFactor);
			oldHole.setDiameter(oldHole.getDiameter() * diameterFactor);
		}

		// Load the perturbed instrument into the study model.
		
		replaceInstrument(perturbedInstrument.getName(),
				StudyModel.marshal(perturbedInstrument));

	}

	public void replaceInstrument(String instrumentName, String xmlString) throws Exception
	{
		FileDataModel fileData = new FileDataModel();
		fileData.setData(xmlString);
		fileData.setName(instrumentName);
		if (! study.replaceDataModel(fileData))
		{
			study.addDataModel(fileData);
		}
	}

	/**
	 * Optimize the current instrument, and compare it to the original instrument.
	 * Pre:  setStudyModel() and setInstrument() have been called.
	 * @param description - Explanatory text for this test.
	 * @throws Exception
	 */
	public void testOptimization(String description, double tolerance) throws Exception
	{
		// Optimize the perturbed instrument.
		System.out.println();
		System.out.println(description);
		Instrument optimizedInstrument = StudyModel.getInstrument(study.optimizeInstrument());

		// Test bore length

		List<BorePoint> oldBorePoints = originalInstrument.getBorePoint();
		List<BorePoint> newBorePoints = optimizedInstrument.getBorePoint();
		PositionInterface[] sortedPoints;
		sortedPoints = Instrument.sortList(oldBorePoints);
		double oldBoreLength = sortedPoints[sortedPoints.length - 1].getBorePosition();
		sortedPoints = Instrument.sortList(newBorePoints);
		double newBoreLength = sortedPoints[sortedPoints.length - 1].getBorePosition();
		assertEquals("Bore length incorrect", oldBoreLength, newBoreLength, tolerance);

		// Test hole sizes and positions

		SortedPositionList<Hole> oldHoles = new SortedPositionList<Hole>(
				originalInstrument.getHole());
		SortedPositionList<Hole> newHoles = new SortedPositionList<Hole>(
				optimizedInstrument.getHole());
		for (int holeNr = 0; holeNr < oldHoles.size(); ++ holeNr)
		{
			String assertName;
			assertName = String.format("Hole %d size incorrect", holeNr+1);
			assertEquals(assertName,
					oldHoles.get(holeNr).getDiameter(),
					newHoles.get(holeNr).getDiameter(), tolerance);
			assertName = String.format("Hole %d position incorrect", holeNr+1);
			assertEquals(assertName,
					oldHoles.get(holeNr).getBorePosition(),
					newHoles.get(holeNr).getBorePosition(), tolerance);
			
		}
	}

}

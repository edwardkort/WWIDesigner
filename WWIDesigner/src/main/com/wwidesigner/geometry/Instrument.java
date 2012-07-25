/**
 * 
 */
package com.wwidesigner.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 * 
 */
public class Instrument implements InstrumentInterface
{

	protected String name;
	protected LengthType lengthType;
	protected Mouthpiece mouthpiece;
	protected List<BorePoint> borePoint;
	protected String description;
	protected List<Hole> hole;
	protected List<ComponentInterface> components;
	protected Termination termination;

	private boolean convertedToMetres = false;
	protected InstrumentConfigurator configurator;

	public Instrument()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setName(java.lang.String)
	 */
	@Override
	public void setName(String value)
	{
		this.name = value;
	}

	/**
	 * @return the lengthType
	 */
	public LengthType getLengthType()
	{
		return lengthType;
	}

	/**
	 * @param lengthType
	 *            the lengthType to set
	 */
	public void setLengthType(LengthType lengthType)
	{
		this.lengthType = lengthType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setDescription(java.lang
	 * .String)
	 */
	@Override
	public void setDescription(String value)
	{
		description = value;
	}

	/**
	 * @param borePoint
	 *            the borePoint to set
	 */
	public void setBorePoint(List<BorePoint> borePoint)
	{
		this.borePoint = borePoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getBorePoint()
	 */
	@Override
	public List<BorePoint> getBorePoint()
	{
		return borePoint;
	}

	@Override
	public void addBorePoint(BorePoint borePoint)
	{
		getBorePoint().add(borePoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getMouthpiece()
	 */
	@Override
	public Mouthpiece getMouthpiece()
	{
		return mouthpiece;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setMouthpiece(com.wwidesigner
	 * .geometry.bind.XmlMouthpiece)
	 */
	@Override
	public void setMouthpiece(Mouthpiece value)
	{
		mouthpiece = value;
	}

	/**
	 * @param hole
	 *            the hole to set
	 */
	public void setHole(List<Hole> hole)
	{
		this.hole = hole;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getHole()
	 */
	@Override
	public List<Hole> getHole()
	{
		return hole;
	}

	@Override
	public void addHole(Hole hole)
	{
		getHole().add(hole);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.InstrumentInterface#getTermination()
	 */
	@Override
	public Termination getTermination()
	{
		return termination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentInterface#setTermination(com.wwidesigner
	 * .geometry.bind.XmlEndBoreSection)
	 */
	@Override
	public void setTermination(Termination value)
	{
		termination = value;
	}

	public void setConfiguration(InstrumentConfigurator configurator)
	{
		this.configurator = configurator;
		configurator.configureInstrument(this);
		convertToMetres();
	}

	public void convertToMetres()
	{
		if (convertedToMetres)
		{
			return;
		}

		double multiplier;
		switch (lengthType)
		{
			case MM:
				multiplier = 0.001;
				break;
			case IN:
				multiplier = 0.0254;
				break;
			default:
				multiplier = 1.;
		}

		convertDimensions(multiplier);
		convertedToMetres = true;
	}

	public void convertToLengthType()
	{
		if (!convertedToMetres)
		{
			return;
		}

		double multiplier;
		switch (lengthType)
		{
			case MM:
				multiplier = 1000.;
				break;
			case IN:
				multiplier = 39.3701;
				break;
			default:
				multiplier = 1.;
		}

		convertDimensions(multiplier);
		convertedToMetres = false;
	}

	protected void convertDimensions(double multiplier)
	{
		if (mouthpiece != null)
		{
			mouthpiece.convertDimensions(multiplier);
		}

		if (borePoint != null)
		{
			for (BorePoint aPoint : borePoint)
			{
				aPoint.convertDimensions(multiplier);
			}
		}

		if (hole != null)
		{
			for (Hole aHole : hole)
			{
				aHole.convertDimensions(multiplier);
			}
		}

		if (termination != null)
		{
			termination.convertDimensions(multiplier);
		}
	}

	@Override
	public void updateComponents()
	{
		// TODO Write recursive validation method and call it here.
		// Then take out the error checking in all the other methods.
		components = new ArrayList<ComponentInterface>();

		if (borePoint != null && !borePoint.isEmpty())
		{
			SortedPositionList<BorePoint> borePointList = makePositionList(borePoint);
			// Add the mouthpiece reference position to the map.
			// I don't believe the optimization routines should care that this
			// offset
			// is not subtracted, since the calculations are performed on the
			// components, which are offset agnostic.
			// borePointMap.put(mouthpieceOrigin.getBorePosition(),

			processMouthpiece(borePointList);
			components.add(mouthpiece);

			processTermination(borePointList);

			SortedPositionList<Hole> holeList = makePositionList(hole);

			// TODO Deal with the Mouthpiece and the start of the bore:

			// Process the holes, making sections as needed to include the hole
			// on the right
			if (holeList.size() > 0)
			{
				for (Hole currentHole : holeList)
				{
					double rightPosition = currentHole.getBorePosition();
					makeSections(borePointList, rightPosition);
					processPosition(borePointList, currentHole);

					components.add(currentHole);
				}
			}

			// Process the rest of the sections. There must be at least one
			double lastPosition = borePointList.getLast().getBorePosition() + 1.;
			makeSections(borePointList, lastPosition);
		}
	}

	protected void processTermination(
			SortedPositionList<BorePoint> borePointList)
	{
		BorePoint lastPoint = borePointList.getLast();
		termination.setBoreDiameter(lastPoint.getBoreDiameter());
		termination.setBorePosition(lastPoint.getBorePosition());
	}

	protected void processMouthpiece(SortedPositionList<BorePoint> borePointList)
	{
		double mouthpiecePosition = mouthpiece.getBorePosition();

		// Make the bore sections above mouthpiece
		makeSections(borePointList, mouthpiecePosition);

		// Make bore section that ends with mouthpiece
		// Set mouthpiece boreDiameter
		processPosition(borePointList, mouthpiece);

		List<BoreSection> headspace = new ArrayList<BoreSection>();
		for (Iterator<ComponentInterface> it = components.iterator(); it
				.hasNext();)
		{
			ComponentInterface component = it.next();
			if (component instanceof BoreSection)
			{
				BoreSection section = (BoreSection) component;
				if (section.getRightBorePosition() <= mouthpiecePosition)
				{
					headspace.add((BoreSection) component);
					it.remove();
				}
			}
		}

		mouthpiece.setHeadspace(headspace);
	}

	protected void processPosition(SortedPositionList<BorePoint> borePointList,
			BorePointInterface currentPosition)
	{
		// Update bore radius at hole
		// At this stage, the hole must be between the first and second bore
		// point
		Iterator<BorePoint> points = borePointList.iterator();
		BorePoint leftPoint = points.next();
		BorePoint rightPoint = points.next();

		double leftPosition = leftPoint.getBorePosition();
		double rightPosition = rightPoint.getBorePosition();
		double thisPosition = currentPosition.getBorePosition();
		double holeRelativePosition = (rightPosition - thisPosition)
				/ (rightPosition - leftPosition);

		double leftDiameter = leftPoint.getBoreDiameter();
		double rightDiameter = rightPoint.getBoreDiameter();
		double holeBoreDiameter = leftDiameter + (rightDiameter - leftDiameter)
				* holeRelativePosition;
		currentPosition.setBoreDiameter(holeBoreDiameter);

		// Make new bore section
		if ((rightPosition - thisPosition) > 0.00001d)
		{
			rightPoint = new BorePoint();
			rightPoint.setBoreDiameter(holeBoreDiameter);
			rightPoint.setBorePosition(thisPosition);
			borePointList.add(rightPoint);
		}
		addSection(leftPoint, rightPoint);
		borePointList.remove(leftPoint);
	}

	protected double makeSections(SortedPositionList<BorePoint> borePointList,
			double rightPosition)
	{
		SortedPositionList<BorePoint> unprocessedPoints = borePointList
				.headList(rightPosition);
		if (unprocessedPoints.size() > 1)
		{
			Iterator<BorePoint> points = unprocessedPoints.iterator();
			BorePoint leftPoint = points.next();
			for (; points.hasNext();)
			{
				BorePoint rightPoint = points.next();
				addSection(leftPoint, rightPoint);
				borePointList.remove(leftPoint);
				leftPoint = rightPoint;
			}
		}

		return rightPosition;
	}

	protected void addSection(BorePoint leftPoint, BorePoint rightPoint)

	{
		BoreSection section = new BoreSection();
		section.setLength(rightPoint.getBorePosition()
				- leftPoint.getBorePosition());
		section.setLeftRadius(leftPoint.getBoreDiameter() / 2);
		section.setRightRadius(rightPoint.getBoreDiameter() / 2);
		section.setRightBorePosition(rightPoint.getBorePosition());

		configurator.configureBoreSectionCalculator(section);

		components.add(section);
	}

	public static <P extends PositionInterface> SortedPositionList<P> makePositionList(
			Collection<P> positions)
	{
		SortedPositionList<P> sortedList = new SortedPositionList<P>();
		sortedList.addAll(positions);

		return sortedList;
	}

	/**
	 * Sorts a list of BorePointInterface objects (BorePoints and Holes) by
	 * their position.
	 * 
	 * @param positions
	 * @return
	 */
	public static <P extends PositionInterface> PositionInterface[] sortList(
			List<P> positions)
	{
		int numberOfPositions = positions.size();
		PositionInterface[] sortedPositions = new PositionInterface[numberOfPositions];
		int i = 0;
		for (P position : positions)
		{
			sortedPositions[i++] = position;
		}
		Arrays.sort(sortedPositions, new Comparator<PositionInterface>()
		{
			public int compare(PositionInterface first, PositionInterface second)
			{
				if (first.getBorePosition() < second.getBorePosition())
				{
					return -1;
				}
				if (first.getBorePosition() > second.getBorePosition())
				{
					return 1;
				}
				return 0;
			}
		});

		return sortedPositions;
	}

	@Override
	public Complex calculateReflectionCoefficient(Fingering fingering,
			PhysicalParameters physicalParams)
	{
		double frequency = fingering.getNote().getFrequency();

		setOpenHoles(fingering);

		Complex reflectance = calculateReflectionCoefficient(frequency,
				physicalParams);

		int reflectanceMultiplier = mouthpiece.calcReflectanceMultiplier();

		Complex result = reflectance.multiply(reflectanceMultiplier);

		return result;
	}

	public Complex calculateReflectionCoefficient(double frequency,
			PhysicalParameters physicalParams)
	{
		double waveNumber = 2 * Math.PI * frequency
				/ physicalParams.getSpeedOfSound();

		updateComponents();

		TransferMatrix transferMatrix = TransferMatrix.makeIdentity();

		for (ComponentInterface component : components)
		{
			transferMatrix = TransferMatrix.multiply(transferMatrix,
					component.calcTransferMatrix(waveNumber, physicalParams));
		}

		StateVector sv = TransferMatrix.multiply(transferMatrix,
				termination.calcStateVector(waveNumber, physicalParams));

		// TODO This mouthpiece calculation will change
		double headRadius = mouthpiece.getBoreDiameter() / 2.;
		double characteristic_impedance = physicalParams.calcZ0(headRadius);
		Complex reflectance = sv.Reflectance(characteristic_impedance);
		return reflectance;
	}

	public void setOpenHoles(Fingering fingering)
	{
		List<Boolean> openHoles = fingering.getOpenHole();
		Iterator<Boolean> openHoleIterator = openHoles.iterator();
		for (Hole iHole : hole)
		{
			boolean isOpen = openHoleIterator.next();
			iHole.setOpenHole(isOpen);
		}
	}

	@Override
	public Complex calcZ(double freq, Fingering fingering,
			PhysicalParameters physicalParams )
	{
		setOpenHoles(fingering);
		updateComponents();

		double waveNumber = physicalParams.calcWaveNumber(freq);

		// Start with the state vector of the termination,
		// and multiply by transfer matrices of each hole and bore segment
		// from the termination up to, but not including the mouthpiece.

		StateVector sv = termination.calcStateVector(waveNumber, physicalParams);
		TransferMatrix tm;
		Complex Zresonator = sv.Impedance();
		for ( int componentNr = components.size() - 1; componentNr > 0; -- componentNr )
		{
			tm = components.get(componentNr).calcTransferMatrix(waveNumber, physicalParams);
			sv = tm.multiply( sv );
			Zresonator = sv.Impedance();
		}
		
		Complex Zwindow = mouthpiece.mouthpieceCalculator.calcZ(freq, physicalParams);
		
		return Zresonator.add(Zwindow);
	}
}

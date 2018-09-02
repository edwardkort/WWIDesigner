/**
 * Class to describe the geometry of a woodwind instrument.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.wwidesigner.geometry.calculation.Tube;
import com.wwidesigner.util.Constants.LengthType;
import com.wwidesigner.util.InvalidFieldException;
import com.wwidesigner.util.InvalidFieldHandler;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 * 
 */
public class Instrument implements InstrumentInterface
{

	// Fundamental properties of the instrument.

	protected String name;
	protected LengthType lengthType;
	protected Mouthpiece mouthpiece;
	protected List<BorePoint> borePoint;
	protected String description;
	protected List<Hole> hole;
	protected Termination termination;

	// Derived properties.

	// List of components, holes and bore sections, with positions greater
	// than the mouthpiece (below the mouthpiece), from smallest position to
	// largest position. Does not include the mouthpiece.
	protected List<ComponentInterface> components;

	// mouthpiece.headspace contains the list of bore sections with positions
	// less than the mouthpiece (above the mouthpiece).

	private boolean convertedToMetres = false;

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

	/**
	 * Converts the instrument, if not already done, to metric.
	 * 
	 * @return True if a conversion was performed.
	 */
	public boolean convertToMetres()
	{
		if (convertedToMetres)
		{
			return false;
		}

		double multiplier = lengthType.getMultiplierToMetres();

		convertDimensions(multiplier);
		convertedToMetres = true;
		return true;
	}

	/**
	 * Converts the instrument, if not already done, to the specified
	 * lengthType.
	 * 
	 * @param lengthType
	 * @return True is a conversion is performed.
	 */
	public boolean convertToLengthType(LengthType lengthType)
	{
		LengthType originalLengthType = getLengthType();
		if (!originalLengthType.equals(lengthType))
		{
			convertToMetres();
			setLengthType(lengthType);
			convertToLengthType();
			return true;
		}

		return false;
	}

	/**
	 * Converts the instrument, if necessary, to the LengthType already set for
	 * this instrument.
	 * 
	 * @return True if conversion is performed.
	 */
	public boolean convertToLengthType()
	{
		if (!convertedToMetres)
		{
			return false;
		}

		double multiplier = lengthType.getMultiplierFromMetres();

		convertDimensions(multiplier);
		convertedToMetres = false;
		return true;
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

	public void checkValidity() throws InvalidFieldException
	{
		InvalidFieldHandler handler = new InvalidFieldHandler("Instrument");
		if (name.isEmpty())
		{
			handler.logError("Enter a name for the instrument.");
		}
		if (borePoint.size() < 2)
		{
			handler.logError("Instrument must have at least two bore points.");
		}
		Double minimumPosition = null;
		Double maximumPosition = null;
		BorePoint terminalBorePoint = null;
		for (BorePoint bore : borePoint)
		{
			bore.checkValidity(handler);
			if (minimumPosition == null
					|| minimumPosition > bore.getBorePosition())
			{
				minimumPosition = bore.getBorePosition();
			}
			if (maximumPosition == null
					|| maximumPosition < bore.getBorePosition())
			{
				maximumPosition = bore.getBorePosition();
				terminalBorePoint = bore;
			}
		}
		if (minimumPosition != null && maximumPosition != null
				&& minimumPosition >= maximumPosition)
		{
			handler.logError("Bore length must not be zero.");
		}
		if (mouthpiece == null)
		{
			handler.logError("A mouthpiece description is required.");
		}
		else
		{
			mouthpiece.checkValidity(handler, minimumPosition, maximumPosition);
			if (minimumPosition < mouthpiece.getPosition())
			{
				// Holes cannot be above the mouthpiece position, in the
				// headspace.
				minimumPosition = mouthpiece.getPosition();
			}
		}
		for (Hole currentHole : hole)
		{
			currentHole
					.checkValidity(handler, minimumPosition, maximumPosition);
		}
		termination.checkValidity(handler, terminalBorePoint);
		handler.reportErrors(false);
	}

	/**
	 * Creates the instrument Components (BoreSection and filled-out Holes) from
	 * the raw BorePoints and Holes. <br>
	 * Pre: this instrument is valid. <br>
	 * Post: getComponents returns a list of bore sections, holes and the
	 * mouthpiece, sorted by increasing position.
	 */
	public void updateComponents()
	{
		components = new ArrayList<ComponentInterface>();

		if (borePoint != null && !borePoint.isEmpty())
		{
			// Sort the bore points from lowest (left-most) to highest
			// (right-most)
			// position.
			SortedPositionList<BorePoint> borePointList = makePositionList(borePoint);

			// Put any bore sections to the left of the mouthpiece position
			// into the mouthpiece headspace.
			processMouthpiece(borePointList);

			// Set the termination to be at the end of the bore.
			processTermination(borePointList);

			// Sort the holes from lowest to highest.
			SortedPositionList<Hole> holeList = makePositionList(hole);

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

	/**
	 * Pre: this instrument is valid and updateComponents has been called since
	 * the last change to the geometry. <br>
	 * Post: getComponents returns a list of bore sections, holes and the
	 * mouthpiece, sorted by increasing position.
	 * 
	 * @return the components
	 */
	public List<ComponentInterface> getComponents()
	{
		return components;
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

		// Move the bore sections above the mouthpiece into the mouthpiece
		// headspace.

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

		// Move the first borepoint to top of TSH
		// BorePoint firstPoint = borePointList.getFirst();
		// double newPosition = firstPoint.getBorePosition() -
		// mouthpiece.getFipple().getWindowLength();
		// firstPoint.setBorePosition(newPosition);
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
		double leftDiameter = leftPoint.getBoreDiameter();
		double rightPosition = rightPoint.getBorePosition();
		double rightDiameter = rightPoint.getBoreDiameter();
		double thisPosition = currentPosition.getBorePosition();

		double holeBoreDiameter;
		if (rightDiameter == leftDiameter || thisPosition == leftPosition)
		{
			// Bore is cylindrical, or hole is at left end.
			holeBoreDiameter = leftDiameter;
		}
		else if (rightPosition > leftPosition)
		{
			// Interpolate bore diameter at the hole.
			holeBoreDiameter = leftDiameter + (thisPosition - leftPosition)
					* (rightDiameter - leftDiameter)
					/ (rightPosition - leftPosition);
		}
		else
		{
			// Bore section has zero length. Use average bore diameter.
			holeBoreDiameter = 0.5 * (leftDiameter + rightDiameter);
		}
		currentPosition.setBoreDiameter(holeBoreDiameter);

		// Make new bore section up to the hole.
		if (rightPosition > thisPosition)
		{
			rightPoint = new BorePoint();
			rightPoint.setBoreDiameter(holeBoreDiameter);
			rightPoint.setBorePosition(thisPosition);
			borePointList.add(rightPoint);
		}
		addSection(leftPoint, rightPoint);
		borePointList.remove(leftPoint);
	}

	/**
	 * Add bore sections to components from the first point in borePointList
	 * through rightPosition.
	 * 
	 * @param borePointList
	 * @param rightPosition
	 */
	protected void makeSections(SortedPositionList<BorePoint> borePointList,
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
	}

	protected void addSection(BorePoint leftPoint, BorePoint rightPoint)

	{
		BoreSection section = new BoreSection();
		double length = rightPoint.getBorePosition()
				- leftPoint.getBorePosition();
		double rightPosition = rightPoint.getBorePosition();
		// Ensure that the section length > 0
		if (length == 0.)
		{
			length = Tube.MINIMUM_CONE_LENGTH;
			rightPosition += Tube.MINIMUM_CONE_LENGTH;
			rightPoint.setBorePosition(rightPosition);
		}
		section.setLength(length);
		section.setLeftRadius(leftPoint.getBoreDiameter() / 2);
		section.setRightRadius(rightPoint.getBoreDiameter() / 2);
		section.setRightBorePosition(rightPosition);

		components.add(section);
	}

	/**
	 * Sort positioned components.
	 * 
	 * @param positions
	 *            - Collection of positioned components
	 * @return List containing the supplied positions, sorted by increasing
	 *         position.
	 */
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
}

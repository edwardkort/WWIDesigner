/**
 * Mouthpiece calculator for transverse flute embouchure hole.
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
package com.wwidesigner.geometry.calculation;

import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.ComponentInterface;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Mouthpiece calculation for a transverse flute embouchure hole, modeling
 * the embouchure hole as a (short) tube with area equal to the window area
 * and flanged open end.
 * @author Burton Patkau
 * 
 */
public class FluteMouthpieceCalculator extends MouthpieceCalculator
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	protected TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		double freq = parameters.calcFrequency(waveNumber);
		
		Complex Zwindow = calcZ(mouthpiece, freq, parameters);
		return new TransferMatrix(Complex.ONE, Zwindow, Complex.ZERO, Complex.ONE);		
	}
	
	@Override
	public StateVector calcStateVector(StateVector boreState,
			Mouthpiece mouthpiece, double waveNumber,
			PhysicalParameters parameters)
	{
		StateVector sv = new StateVector(boreState);

		List<BoreSection> headspace = mouthpiece.getHeadspace();
		if (headspace.size() > 0)
		{
			StateVector headspaceState = calcHeadspace_transmission(headspace, waveNumber, parameters);

			// Assume the mouthpiece sees the bore impedance in parallel with
			// the headspace impedance.
			sv = boreState.parallel(headspaceState);
		}

		sv = calcTransferMatrix(mouthpiece, waveNumber, parameters)
				.multiply(sv);
		return sv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcReflectanceMultiplier()
	 */
	@Override
	public int calcReflectanceMultiplier()
	{
		return -1;
	}

	/**
	 * Calculate the impedance of the embouchure hole, at specified frequency.
	 * @param mouthpiece
	 * @param freq
	 * @param physicalParams
	 * @return Complex impedance of embouchure hole.
	 */
	public Complex calcZ(Mouthpiece mouthpiece,
			double freq, PhysicalParameters physicalParams)
	{
		// Prototype reactance model taken from empirical whistle model
		// in SimpleFippleMouthpieceCalculator.
		double holeWidth = mouthpiece.getEmbouchureHole().getWidth();
		if (mouthpiece.getEmbouchureHole().getAirstreamLength() < holeWidth)
		{
			holeWidth = mouthpiece.getEmbouchureHole().getAirstreamLength();
		}
		double effSize = FastMath.sqrt(holeWidth
				* mouthpiece.getEmbouchureHole().getLength() );
		// Model for use in absence of blade height measurement.
		double windowHeight = mouthpiece.getEmbouchureHole().getHeight();
		double Xw = physicalParams.getRho() * freq/effSize
				* ( 4.30 + 2.87 * windowHeight/effSize );
		
		// Resistance modeled as radiation resistance from end of bore,
		// plus short cylindrical tube with same area as window.
		double radius = 0.5 * mouthpiece.getBoreDiameter();
		double Rw = Tube.calcR(freq, radius, physicalParams)
			  + physicalParams.getRho() * 0.0184 * FastMath.sqrt(freq)*windowHeight
				/ (effSize*effSize*effSize);
		return new Complex(Rw,Xw);
	}

	/**
	 * Calculate a state vector for the headspace, assuming it is long enough
	 * to act as a duct with a closed upper end.
	 * @param headspace - the bore sections of the headspace
	 * @param waveNumber - 2 pi f / c
	 * @param physicalParams
	 * @return state vector for the headspace
	 */
	protected StateVector calcHeadspace_transmission(List<BoreSection> headspace,
			double waveNumber, PhysicalParameters physicalParams)
	{
		StateVector headspaceState = StateVector.ClosedEnd();
		TransferMatrix tm;
		for (int componentNr = 0; componentNr < headspace.size(); ++componentNr)
		{
			ComponentInterface component = headspace.get(componentNr);
			assert component instanceof BoreSection;
			BoreSection section = (BoreSection) component;
			tm = Tube.calcConeMatrix(waveNumber, section.getLength(),
					section.getRightRadius(), section.getLeftRadius(),
					physicalParams);
			headspaceState = tm.multiply(headspaceState);
		}
		return headspaceState;
	}

	/**
	 * Calculate a state vector for the headspace, assuming it is small enough
	 * to act only as an acoustic compliance.
	 * @param headspace
	 * @param waveNumber
	 * @param physicalParams
	 * @return
	 */
	protected StateVector calcHeadspace_compliance(List<BoreSection> headspace,
			double waveNumber, PhysicalParameters physicalParams)
	{
		double freq = physicalParams.calcFrequency(waveNumber);
		double compliance = calcHeadspaceVolume(headspace)
				/ (physicalParams.getGamma()*physicalParams.getPressure()*1.0e3);
		return new StateVector(Complex.ONE, new Complex(0.0, 2.0*Math.PI*freq*compliance));
	}

	protected double calcHeadspaceVolume(List<BoreSection> headspace)
	{
		double volume = 0.;
		for (BoreSection section : headspace)
		{
			volume += getSectionVolume(section);
		}
		return volume;
	}

	protected double getSectionVolume(BoreSection section)
	{
		double leftRadius = section.getLeftRadius();
		double rightRadius = section.getRightRadius();
		double volume = Math.PI / 3.0
				* section.getLength()
				* (leftRadius * leftRadius + leftRadius * rightRadius + rightRadius
						* rightRadius);
		return volume;
	}
}

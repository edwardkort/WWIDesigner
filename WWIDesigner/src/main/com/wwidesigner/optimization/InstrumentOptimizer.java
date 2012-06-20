package com.wwidesigner.optimization;

import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;

import java.util.List;
import java.util.ArrayList;

public class InstrumentOptimizer extends BOBYQAOptimizer
{	
	public InstrumentOptimizer(OptimizableInstrument inst, List<OptimizationTarget> targets) 
	{
		super( 20 ); // the number of interpolation point should be set according to the number of variables in the optimization problem, 
		             // which depends on the OptimizableInstrument
	}
	
	private static OptimizableInstrument makeCylinderWithSixHoles(double length, double radius)
	{
		List<Section> sections = new ArrayList<Section>(2);
		sections.add(new Section(length, radius, radius));
		
		List<Hole> holes = new ArrayList<Hole>();
		holes.add(new Hole(0.49*length, 0.65*radius, 0.008));
		holes.add(new Hole(0.58*length, 0.65*radius, 0.008));
		holes.add(new Hole(0.65*length, 0.65*radius, 0.008));
		holes.add(new Hole(0.73*length, 0.65*radius, 0.008));
		holes.add(new Hole(0.81*length, 0.65*radius, 0.008));
		holes.add(new Hole(0.9*length, 0.65*radius, 0.008));
				
		return new OptimizableInstrument(sections, holes);	
	}
	public static void testInstrument()
	{
		OptimizableInstrument inst = makeCylinderWithSixHoles(0.3, 0.0075);

		List<OptimizationTarget> targets = new ArrayList<OptimizationTarget>();
		
		double f0 = 440.;
		
		boolean[] pattern1 = new boolean[6];
		pattern1[0] = false;
		pattern1[1] = false;		
		pattern1[2] = false;
		pattern1[3] = false;		
		pattern1[4] = false;
		pattern1[5] = false;		
		targets.add( new OptimizationTarget(f0, new Fingering(pattern1) ));
		
		boolean[] pattern2 = new boolean[6];
		pattern2[0] = false;
		pattern2[1] = false;
		pattern2[2] = false;
		pattern2[3] = false;		
		pattern2[4] = false;
		pattern2[5] = true;
		targets.add( new OptimizationTarget(f0*Math.pow(2., 2./12.), new Fingering(pattern2)) );
		
		boolean[] pattern3 = new boolean[6];
		pattern3[0] = false;
		pattern3[1] = false;
		pattern3[2] = false;
		pattern3[3] = false;		
		pattern3[4] = true;
		pattern3[5] = true;
		targets.add( new OptimizationTarget(f0*Math.pow(2., 4./12.), new Fingering(pattern3)) );

		boolean[] pattern4 = new boolean[6];
		pattern4[0] = false;
		pattern4[1] = false;
		pattern4[2] = false;
		pattern4[3] = true;		
		pattern4[4] = true;
		pattern4[5] = true;
		targets.add( new OptimizationTarget(f0*Math.pow(2., 5./12.), new Fingering(pattern4)) );

		boolean[] pattern5 = new boolean[6];
		pattern5[0] = false;
		pattern5[1] = false;
		pattern5[2] = true;
		pattern5[3] = true;		
		pattern5[4] = true;
		pattern5[5] = true;
		targets.add( new OptimizationTarget(f0*Math.pow(2., 7./12.), new Fingering(pattern5)) );

		boolean[] pattern6 = new boolean[6];
		pattern6[0] = false;
		pattern6[1] = true;
		pattern6[2] = true;
		pattern6[3] = true;		
		pattern6[4] = true;
		pattern6[5] = true;
		targets.add( new OptimizationTarget(f0*Math.pow(2., 9./12.), new Fingering(pattern6)) );

		boolean[] pattern7 = new boolean[6];
		pattern7[0] = true;
		pattern7[1] = true;
		pattern7[2] = true;
		pattern7[3] = true;		
		pattern7[4] = true;
		pattern7[5] = true;
		targets.add( new OptimizationTarget(f0*Math.pow(2., 11./12.), new Fingering(pattern7)) );
		
		OptimizationFunction func = new OptimizationFunction(inst, targets);
			
		InstrumentOptimizer optimizer = new InstrumentOptimizer(inst, targets);

		double[] startPoint = inst.getStateVector();

		System.out.print("start point: ( ");
		for (double x: startPoint)
        	System.out.print(x + " ");
   	    System.out.println(")" );

   	    
		double[] lB = new double[7];
		double[] uB = new double[7];
		
		lB[0] = 0.25;
		uB[0] = 0.40;
		lB[1] = 0.01;
		uB[1] = 0.035;
		lB[2] = 0.01;
		uB[2] = 0.035;
		lB[3] = 0.01;
		uB[3] = 0.035;
		lB[4] = 0.01;
		uB[4] = 0.035;
		lB[5] = 0.01;
		uB[5] = 0.05;
		lB[6] = 0.01;
		uB[6] = 0.08;
		
		PointValuePair result = optimizer.optimize(5000, func, GoalType.MINIMIZE, startPoint, lB, uB);

		System.out.print("( ");
		for (double x: result.getPoint())
        	System.out.print(x + " ");
   	    System.out.println(") = " +  result.getValue());
		
	}
	
	public static void main(String[] args)
	{
		try {
		    testInstrument();
		}
		catch (Exception e) {
			System.err.println("Caught Exception: " + e.getMessage());
		}
	}
}
	
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.geometry.TerminationCalculator;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

public class ThickFlangedOpenEndCalculator  extends TerminationCalculator
{

	public ThickFlangedOpenEndCalculator(Termination termination)
	{
		super(termination);
	}

	@Override
	public StateVector calcStateVector(double wave_number,
			PhysicalParameters params)
	{	
		Complex P = Z(wave_number, params).multiply( params.calcZ0(0.015/2.)); //termination.getBoreDiameter()/2) ); 
				
		return new StateVector(P, Complex.ONE);
	}

	private Complex Z(double wave_number, PhysicalParameters params)
	{
        double a = 0.015/2.; //termination.getBoreDiameter()/2;
        double b = termination.getFlangeDiameter()/2;
                
        //double t = b-a;
        double a_b = a/b;
        
		double ka = wave_number*a;
		//double kb = wave_number*b;
        double delta_inf = 0.8216 * a;
        double delta_0 = 0.6133 * a;
		
        double delta_circ = delta_inf + a_b*(delta_0 - delta_inf) + 0.057*a_b*(1-Math.pow(a_b, 5))*a;
        double R0 = (1 + 0.2*ka - 0.084*ka*ka)/(1 + 0.2*ka + (0.5 - 0.084)*ka*ka);	
        Complex R = Complex.I.multiply( -2*delta_circ*ka ).exp().multiply(-R0);
		return R.add(1).divide( R.negate().add(1) );

	
        
      /*  
        double delta_0_tilde = delta_0 * (1 + 0.044*ka*ka)/(1 + 0.19*ka*ka);
        
        double R0 = (1 + 0.2*ka - 0.084*ka*ka)/(1 + 0.2*ka + (0.5 - 0.084)*ka*ka);
		
        Complex delta_0_tilde_star = Complex.valueOf(delta_0_tilde, Math.log(R0)/(2*wave_number));

		double delta_inf_tilde = delta_inf / (1 + (0.77*ka)*(0.77*ka)/(1+0.77*ka));
		double Rinf = (1. + 0.323*ka - 0.077*ka*ka)/(1. + 0.323*ka + (1-0.077)*ka*ka);
		
		Complex delta_inf_tilde_star = Complex.valueOf(delta_inf_tilde, Math.log(Rinf)/(2*wave_number));

		Complex delta_norefl_tilde_star = delta_inf_tilde_star.add( delta_0_tilde_star.subtract(delta_inf_tilde_star).multiply(a_b) ).add(0.057*a_b*(1-Math.pow(a_b, 5))*a);
		Complex R_norefl = Complex.I.negate().multiply(wave_number).multiply(delta_norefl_tilde_star).exp().negate();

		Complex Redge = Complex.valueOf(0., -kb*(1. + a_b*(2.3 - a_b - 0.3*ka*ka))).exp().multiply(-0.43*((b-a)*a/(b*b))*Math.pow(Math.sin(kb/(1.85-a_b)), 2));
				
		Complex Rcirc = R_norefl.add(Redge);

		Complex Zcirc = Rcirc.add(1).divide( Rcirc.negate().add(1) );
		
        return Zcirc;
     */
	}
}

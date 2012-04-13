/**
 * 
 */
package com.wwidesigner.impedance.geometry;

import org.apache.commons.math.complex.Complex;

import com.wwidesigner.impedance.math.TransferMatrix;
import com.wwidesigner.impedance.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class BoreSection extends Component implements BoreSectionInterface
{

    protected double mLB; // Bore length.
    protected double mBoreLeftDiameter;
    protected double mBoreRightDiameter;
    protected transient double mRBL; // Bore radius at left (head) end.
    protected transient double mRBR; // Bore radius at right (foot) end.

    // The following are calculated at validation:
    // True if the section is converging towards the right (foot) end.
    protected transient boolean mIsConv;
    protected transient double mRBSmall; // Bore radius at small end.
    protected transient double mRBLarge; // Bore radius at large end.
    // Inverse of the 'missing length' - the length from the hypothetical
    // apex of the cone to the small end of the conic section.
    protected transient double mX0Inv;
    
    public BoreSection()
    {
    	super(new PhysicalParameters());
    }

    /**
     * @param params
     */
    public BoreSection( PhysicalParameters params, double lB, double rBL, double rBR )
    {
        super( params );
        mLB = lB;
        mRBL = rBL;
        mRBR = rBR;
        mIsConv = false;
        mRBSmall = 0.0;
        mRBLarge = 0.0;
        mX0Inv = 0.0;
    }

    public BoreSection( BoreSection bs )
    {
        this( bs.mParams, bs.getLB(), bs.getRBL(), bs.getRBR() );
    }

    /**
     * @see com.wwidesigner.impedance.geometry.Component#calcT(com.wwidesigner.impedance.math.TransferMatrix,
     *      double)
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#calcT(com.wwidesigner.impedance.math.TransferMatrix, double)
	 */
	@Override
    public void calcT( TransferMatrix t, double freq )
    {
        // See Scavone, PhD Thesis.

        double omega = 2.0 * Math.PI * freq;
        double k = omega / mParams.getSpecificHeat();

        double z0 = mParams.calcZ0( mRBSmall );
        // Wave impedance of cylindrical bore, using radius at small end.

        double l_c_inv = mX0Inv / ( 1.0 + mLB * mX0Inv );

        double l_c_on_x0 = 1.0 + mLB * mX0Inv;
        double x0_on_l_c = 1.0 / l_c_on_x0;

        Complex gamma;
        // Complex propagation wavenumber.
        Complex z_c;
        // Characteristic impedance.

        boolean use_losses = true;
        if ( use_losses )
        {
            double r_ave = 0.5 * ( mRBL + mRBR );
            // Use ave. radius for calculation of viscous and thermal
            // losses.

            double r_v_m1 = 1.0 / ( Math.sqrt( omega * mParams.getRho() / mParams.getEta() ) * r_ave );
            double r_v_m2 = r_v_m1 * r_v_m1;
            double r_v_m3 = r_v_m2 * r_v_m1;

            double omega_on_v_p = k * ( 1.0 + 1.045 * r_v_m1 );
            // omega times inverse of phase velocity of the complex
            // propagation wavenumber.

            double alpha = k * ( 1.045 * r_v_m1 + 1.080 * r_v_m2 + 0.750 * r_v_m3 );
            // Attenuation coefficient of the complex
            // propagation wavenumber.

            gamma = new Complex( alpha, -omega_on_v_p );

            z_c = new Complex( ( 1.0 + 0.369 * r_v_m1 ),
                               -( 0.369 * r_v_m1 + 1.149 * r_v_m2 + 0.303 * r_v_m3 ) );
            z_c = z_c.multiply( new Complex( z0, 0.0 ) );
        }
        else
        {
            // For lossless case the following hold:
            gamma = new Complex( 0.0, -k );
            z_c = new Complex( z0, 0.0 );
        }

        Complex gamma_lb = gamma.multiply( new Complex( mLB, 0.0 ) );
        Complex cosh_gamma_lb = gamma_lb.cosh();
        Complex sinh_gamma_lb = gamma_lb.sinh();

        Complex gamma_x0_inv = new Complex( mX0Inv, 0.0 ).divide( gamma );

        Complex a = new Complex( l_c_on_x0, 0.0 ).multiply( cosh_gamma_lb )
                .subtract( gamma_x0_inv.multiply( sinh_gamma_lb ) );
        Complex b = new Complex( x0_on_l_c, 0.0 ).multiply( z_c ).multiply( sinh_gamma_lb );
        Complex c = ( Complex.ONE.divide( z_c ) ).multiply( ( ( new Complex( l_c_on_x0, 0.0 )
                .subtract( gamma_x0_inv.multiply( gamma_x0_inv ) ) ).multiply( sinh_gamma_lb )
                .add( new Complex( mX0Inv, 0.0 ).multiply( gamma_x0_inv )
                        .multiply( new Complex( mLB, 0.0 ) ).multiply( cosh_gamma_lb ) ) ) );
        Complex d = new Complex( x0_on_l_c, 0.0 ).multiply( cosh_gamma_lb )
                .add( new Complex( l_c_inv, 0.0 ).multiply( sinh_gamma_lb ).divide( gamma ) );

        if ( mIsConv )
        {
            t.setPP( d );
            t.setPU( b );
            t.setUP( c );
            t.setUU( a );
        }
        else
        {
            t.setPP( a );
            t.setPU( b );
            t.setUP( c );
            t.setUU( d );
        }
    }

    /**
     * @see com.wwidesigner.impedance.geometry.Component#validate()
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#validate()
	 */
	@Override
    public void validate()
    {
        mIsConv = ( mRBL > mRBR );
        mRBSmall = ( mIsConv ? mRBR : mRBL );
        mRBLarge = ( mIsConv ? mRBL : mRBR );
        mX0Inv = ( mRBLarge - mRBSmall ) / ( mLB * mRBSmall );
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#getLB()
	 */
    @Override
	public double getLB()
    {
        return mLB;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#setLB(double)
	 */
    @Override
	public void setLB( double lb )
    {
        mLB = lb;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#getRBL()
	 */
    @Override
	public double getRBL()
    {
        return mRBL;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#setRBL(double)
	 */
    @Override
	public void setRBL( double rbl )
    {
        mRBL = rbl;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#getRBR()
	 */
    @Override
	public double getRBR()
    {
        return mRBR;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.BoreSectionInterface#setRBR(double)
	 */
    @Override
	public void setRBR( double rbr )
    {
        mRBR = rbr;
    }

}

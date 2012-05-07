/**
 * 
 */
package com.wwidesigner.geometry;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.Constants;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class Hole extends Component implements HoleInterface
{

	protected double mHoleDiameter;
    protected transient double mRB; // The radius of the bore at the position of the
    // hole.
    protected double mRHExt; // The external radius of the hole;
    protected double mLH; // The physical length of the hole.
    protected boolean mIsClosed; // Is the hole open or closed?
    protected double mRC; // Effective radius of curvature of the internal
    // and external ends of the tonehole wall, see
    // Keefe (1990). I think this refers to the
    // transition between the tonehole wall and the
    // bore or exterior of the flute.

    // The following values are calculated at validation and cached.
    protected double mLHG; // The geometric length of the hole, which
    // takes into accout the effects of the hole
    // deviating from a perfect cylinder.
    protected double mRHG; // The geometric radius of the hole, which
    // takes into account the effects of the hole
    // deviating from a perfect cylinder.
    protected double mOHLB; // Effective acoustic length along the axis of the
    // bore, when the hole is open.
    protected double mCHLB; // Effective acoustic length along the axis of
    // the bore, when the hole is closed.

    public Hole( PhysicalParameters params, double rB, double rHExt, double lH, boolean isClosed,
                 double rC )
    {
        super( params );
        mRB = rB;
        mRHExt = rHExt;
        mLH = lH;
        mIsClosed = isClosed;
        mRC = rC;
        mLHG = 0.0;
        mRHG = 0.0;
        mOHLB = 0.0;
        mCHLB = 0.0;
    }

    /**
     * The bore radius at the position of the hole.
     */
    double getRB()
    {
        return mRB;
    }

    /**
     * The bore radius at the position of the hole.
     */
    void setRB( double rB )
    {
        mRB = rB;
    }

    /**
     * The radius of the hole.
     */
    double getRHExt()
    {
        return mRHExt;
    }

    /**
     * The radius of the hole.
     */
    void setRHExt( double rHExt )
    {
        mRHExt = rHExt;
    }

    /**
     * The actual depth of the hole.
     */
    double getLH()
    {
        return mLH;
    }

    /**
     * The actual depth of the hole.
     */
    void setLH( double l )
    {
        mLH = l;
    }

    /**
     * Is the hole closed?
     */
    boolean getIsClosed()
    {
        return mIsClosed;
    }

    /**
     * Is the hole closed?
     */
    void setIsClosed( boolean isClosed )
    {
        mIsClosed = isClosed;
    }

    /**
     * The effective radius of curvature of the transition between the hole wall
     * and the bore or exterior of the flute.
     */
    double getRC()
    {
        return mRC;
    }

    /**
     * The effective radius of curvature of the transition between the hole wall
     * and the bore or exterior of the flute.
     */
    void setRC( double rC )
    {
        mRC = rC;
    }

    /**
     * Effective accoustic length along the bore, when the hole is open.
     */
    double getOHLB()
    {
        return mOHLB;
    }

    /**
     * Effective accoustic length along the bore, when the hole is closed.
     */
    double getCHLB()
    {
        return mCHLB;
    }

    /**
     * @see com.wwidesigner.geometry.Component#calcT(com.wwidesigner.math.TransferMatrix,
     *      double)
	 * @see com.wwidesigner.geometry.HoleInterface#calcT(com.wwidesigner.math.TransferMatrix, double)
	 */
    @Override
    public void calcT( TransferMatrix t, double freq )
    {
        double omega = 2.0 * Math.PI * freq;
        double k = omega / mParams.getSpecificHeat(); // Wavenumber.
        double z0 = mParams.getRho() * mParams.getSpecificHeat() / ( Math.PI * mRB * mRB ); // Wave
        // impedance
        // of
        // the
        // main
        // bore.
        double rb_on_rh = mRB / mRHG;
        double rb_on_rh_2 = rb_on_rh * rb_on_rh;

        t.setPP( Complex.ONE );
        t.setUU( Complex.ONE );
        if ( mIsClosed )
        {
            t.setPU( new Complex( 0.0, z0 * rb_on_rh_2 * k * mCHLB ) );
            t.setUP( new Complex( 0.0, -Math.tan( k * mLHG ) / ( z0 * rb_on_rh_2 ) ) );
        }
        else
        {
            t.setPU( new Complex( 0.0, z0 * rb_on_rh_2 * k * mOHLB ) );
            t.setUP( Complex.ONE.divide( new Complex( z0 * rb_on_rh_2, 0.0 )
                    .multiply( ( new Complex( calcXi( freq ), -k * calcHLE( freq ) ) ) ) ) );
        }
    }

    /**
     * @see com.wwidesigner.geometry.Component#validate()
	 * @see com.wwidesigner.geometry.HoleInterface#validate()
	 */
    @Override
    public void validate()
    {
        super.validate();

        assert ( mRB > 0.0 );
        assert ( mRHExt > 0.0 );
        assert ( mLH > 0.0 );

        calcAndCacheRLG();

        // Calculate and cache the series effective lengths of the hole:

        // See Keefe 1990

        double rh_on_rb = mRHG / mRB;
        double rh_on_rb_2 = rh_on_rb * rh_on_rb;
        double rh_on_rb_4 = rh_on_rb_2 * rh_on_rb_2;

        double term1 = 0.47 * mRHG * rh_on_rb_4;
        double term2 = 0.62 * rh_on_rb_2 + 0.64 * rh_on_rb;
        double term3 = Math.tanh( 1.84 * mLHG / mRHG );

        // From eq. (8) in Keefe (1990):
        mOHLB = term1 / ( term2 + term3 );
        // From eq. (9) in Keefe (1990):
        mCHLB = term1 / ( term2 + ( 1.0 / term3 ) );
    }

    // Effective accoustic length of the hole when it is open.
    protected double calcHLE( double freq )
    {
        // See Keefe 1990

        double k = 2.0 * Math.PI * freq / mParams.getSpecificHeat(); // Wavenumber.
        double k_inv = 1.0 / k;

        double tan_k_l = Math.tan( k * mLHG );

        double rh_on_rb = mRHG / mRB;

        double result = ( k_inv * tan_k_l + mRHG * ( 1.40 - 0.58 * rh_on_rb * rh_on_rb ) )
                        / ( 1.0 - 0.61 * k * mRHG * tan_k_l );
        // From eq. (5) in Keefe (1990):
        assert ( result > 0.0 );

        return result;
    }

    // Specific resistance along the bore, when the hole is open.
    protected double calcXi( double freq )
    {
        double omega = 2.0 * Math.PI * freq;
        double k = omega / mParams.getSpecificHeat(); // Wavenumber.

        double d_v = Math.sqrt( 2.0 * mParams.getEta() / ( mParams.getRho() * omega ) );
        // Viscous boundary layer thickness.

        double alpha = ( Math.sqrt( 2 * mParams.getEta() * omega / mParams.getRho() ) + ( Constants.GAMMA - 1 )
                                                                                        * Math
                                                                                                .sqrt( 2
                                                                                                       * Constants.KAPPA
                                                                                                       * omega
                                                                                                       / ( mParams
                                                                                                               .getRho() * Constants.C_P ) ) )
                       / ( 2 * mRHG * mParams.getSpecificHeat() );

        double result = 0.25 * ( k * mRHG ) * ( k * mRHG ) + alpha * mLHG + 0.25 * k * d_v
                        * Math.log( 2 * mRHG / mRC );

        return result;
    }

    protected void calcAndCacheRLG()
    {
        mRHG = mRHExt;
        mLHG = mLH;
    }

}

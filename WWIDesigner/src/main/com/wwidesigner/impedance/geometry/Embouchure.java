/**
 * 
 */
package com.wwidesigner.impedance.geometry;

import org.apache.commons.math.complex.Complex;

import com.wwidesigner.impedance.math.TransferMatrix;
import com.wwidesigner.impedance.util.Constants;
import com.wwidesigner.impedance.util.PhysicalParameters;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Embouchure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Embouchure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="backSet" type="{http://www.wwidesigner/Geometry}zeroOrMore"/>
 *         &lt;choice>
 *           &lt;element name="characteristicLength" type="{http://www.wwidesigner/Geometry}moreThanZero"/>
 *           &lt;sequence>
 *             &lt;element name="tshWidth" type="{http://www.wwidesigner/Geometry}moreThanZero"/>
 *             &lt;element name="tshLength" type="{http://www.wwidesigner/Geometry}moreThanZero"/>
 *             &lt;element name="birdFactor" type="{http://www.wwidesigner/Geometry}moreThanZero"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Embouchure", propOrder = {
    "mBackset",
    "mCharacteristicLength",
    "mTshWidth",
    "mTshLength",
    "mBirdFactor"
})
public class Embouchure extends Component implements EmbouchureInterface {

	@XmlElement(name="backSet", required=true)
	protected double mBackset;
	@XmlElement(name="characteristicLength")
    protected Double mCharacteristicLength; // The characteristic length for the
    // embouchure, equal to the surface area of
    // the hole divided by the effective length of
    // the hole.
	@XmlElement(name="tshWidth")
    protected Double mTshWidth;
	@XmlElement(name="tshLength")
    protected Double mTshLength;
	@XmlElement(name="birdFactor")
    protected Double mBirdFactor;
    private transient double mRB; // The bore radius.
    
    public Embouchure()
    {
    }

    /**
     * @param params
     */
    public Embouchure( PhysicalParameters params, double RB, double LChar, double LCav )
    {
        super( params );
        mRB = RB;
        mCharacteristicLength = LChar;
        mBackset = LCav;
    }


    /**
     * Gets the value of the mBackset property.
     * 
     */
    public double getBackSet() {
        return mBackset;
    }

    /**
     * Sets the value of the mBackset property.
     * 
     */
    public void setBackSet(double value) {
        this.mBackset = value;
    }

    /**
     * Gets the value of the mCharacteristicLength property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getCharacteristicLength() {
        return mCharacteristicLength;
    }

    /**
     * Sets the value of the mCharacteristicLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setCharacteristicLength(Double value) {
        this.mCharacteristicLength = value;
    }

    /**
     * Gets the value of the mTshWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTshWidth() {
        return mTshWidth;
    }

    /**
     * Sets the value of the mTshWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTshWidth(Double value) {
        this.mTshWidth = value;
    }

    /**
     * Gets the value of the mTshLength property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTshLength() {
        return mTshLength;
    }

    /**
     * Sets the value of the mTshLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTshLength(Double value) {
        this.mTshLength = value;
    }

    /**
     * Gets the value of the mBirdFactor property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getBirdFactor() {
        return mBirdFactor;
    }

    /**
     * Sets the value of the mBirdFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setBirdFactor(Double value) {
        this.mBirdFactor = value;
    }


    /**
     * The local radius of the bore.
     */
    double getRB()
    {
        return mRB;
    }

    /**
     * The local radius of the bore.
     */
    void setRB( double rB )
    {
        mRB = rB;
    }

    /**
     * @see com.wwidesigner.impedance.geometry.Component#calcT(com.wwidesigner.impedance.math.TransferMatrix,
     *      double)
	 * @see com.wwidesigner.impedance.geometry.EmbouchureInterface#calcT(com.wwidesigner.impedance.math.TransferMatrix, double)
	 */
    @Override
    public void calcT( TransferMatrix t, double freq )
    {
        double z0 = mParams.calcZ0( mRB );
        double k_delta_l = calcKDeltaL( freq );
        t.setPP( new Complex( Math.cos( k_delta_l ), 0.0 ) );
        t.setUU( new Complex( Math.cos( k_delta_l ), 0.0 ) );
        t.setPU( new Complex( 0.0, -Math.sin( k_delta_l ) * z0 ) );
        t.setUP( new Complex( 0.0, -Math.sin( k_delta_l ) / z0 ) );
    }

    private double calcKDeltaL( double freq )
    {
        double z0 = mParams.calcZ0( mRB );
        double result = Math.atan( 1.0 / ( z0 * ( calcJYE( freq ) + calcJYC( freq ) ) ) );

        return result;
    }

    private double calcJYE( double freq )
    {
        double omega = 2.0 * Math.PI * freq;

        double result = mCharacteristicLength / ( Constants.GAMMA * omega );

        return result;
    }

    private double calcJYC( double freq )
    {
        double omega = 2.0 * Math.PI * freq;
        double v = 2.0 * Math.PI * mRB * mRB * mBackset;

        double result = -( omega * v )
                        / ( Constants.GAMMA * mParams.getSpecificHeat() * mParams.getSpecificHeat() );

        return result;
    }

    /**
     * @see com.wwidesigner.impedance.geometry.Component#validate()
	 * @see com.wwidesigner.impedance.geometry.EmbouchureInterface#validate()
	 */
    @Override
    public void validate()
    {
        assert ( mRB > 0.0 );
        assert ( mCharacteristicLength > 0.0 );
        assert ( mBackset >= 0.0 );
    }

}

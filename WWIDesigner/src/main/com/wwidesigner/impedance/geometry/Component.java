/**
 * 
 */
package com.wwidesigner.impedance.geometry;

import com.wwidesigner.impedance.math.TransferMatrix;
import com.wwidesigner.impedance.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class Component implements ComponentInterface
{

    protected transient boolean mIsValid;
    protected PhysicalParameters mParams;
    
    public Component()
    {
    	this(new PhysicalParameters());
    }

    public Component( PhysicalParameters params )
    {
        mIsValid = false;
        mParams = params;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.ComponentInterface#validate()
	 */
    public void validate()
    {
        mIsValid = true;
    }

    /**
	 * @see com.wwidesigner.impedance.geometry.ComponentInterface#calcT(com.wwidesigner.impedance.math.TransferMatrix, double)
	 */
    abstract public void calcT( TransferMatrix t, double freq );
}

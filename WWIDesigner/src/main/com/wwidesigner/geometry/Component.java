/**
 * 
 */
package com.wwidesigner.geometry;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

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
	 * @see com.wwidesigner.geometry.ComponentInterface#validate()
	 */
    public void validate()
    {
        mIsValid = true;
    }

    /**
	 * @see com.wwidesigner.geometry.ComponentInterface#calcT(com.wwidesigner.math.TransferMatrix, double)
	 */
    abstract public void calcT( TransferMatrix t, double freq );
}

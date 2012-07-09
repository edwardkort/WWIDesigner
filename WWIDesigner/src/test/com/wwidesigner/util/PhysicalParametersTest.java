/**
 * 
 */
package com.wwidesigner.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.Constants.TemperatureType;

/**
 * @author kort
 * 
 */
public class PhysicalParametersTest
{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * Test method for
     * {@link com.wwidesigner.util.PhysicalParameters#PhysicalParameters(double, com.wwidesigner.impedance.util.PhysicalParameters.TemperatureType)}.
     */
    @Test
    public void testPhysicalParameters()
    {
        // Celcius temp calculation
        double temp = 20.;
        PhysicalParameters phyPar = new PhysicalParameters( temp,
                                                            TemperatureType.C );
        assertEquals( temp + 273.15, phyPar.getTemperature(), 0.001 );

        // Fahrenheit temp calculation
        temp = 32.;
        phyPar = new PhysicalParameters( temp, TemperatureType.F );
        assertEquals( 5. * ( temp + 40 ) / 9 + 233.15, phyPar.getTemperature(), 0.001 );
    }

    /**
     * Test method for
     * {@link com.wwidesigner.util.PhysicalParameters#CalcZ0(double)}.
     */
    @Test
    public void testCalcZ0()
    {
        double temp = 20.;
        PhysicalParameters phyPar = new PhysicalParameters( temp,
                                                            TemperatureType.C );
        assertEquals( 32.8686, phyPar.calcZ0( 2. ), 0.01 );
    }

}

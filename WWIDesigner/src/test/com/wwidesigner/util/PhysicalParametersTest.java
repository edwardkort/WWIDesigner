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
     * {@link com.wwidesigner.util.PhysicalParameters#PhysicalParameters(double, com.wwidesigner.util.Constants.TemperatureType)}.
     */
    @Test
    public void testTemperature()
    {
        // Celsius temp calculation
        double temp = 20.;
        PhysicalParameters phyPar = new PhysicalParameters( temp,
                                                            TemperatureType.C );
        assertEquals( temp, phyPar.getTemperature(), 0.001 );

        // Fahrenheit temp calculation
        temp = 32.;
        phyPar = new PhysicalParameters( temp, TemperatureType.F );
        assertEquals( 5. * ( temp + 40 ) / 9 - 40, phyPar.getTemperature(), 0.001 );
    }

    /**
     * Test method for
     * {@link com.wwidesigner.util.PhysicalParameters#PhysicalParameters(double, com.wwidesigner.util.Constants.TemperatureType)}.
     */
    @Test
    public void testProperties()
    {
    	// Dry air, sea level.
        PhysicalParameters phyPar = new PhysicalParameters( 0.0, TemperatureType.C, 101.325, 0.0, 0.000390 );
        assertEquals( 331.34, phyPar.getSpeedOfSound(), 0.02 );
        assertEquals( 1.293, phyPar.getDensity(), 0.001 );
        assertEquals( 1.517, phyPar.getEpsilonFromF(1.0, 0.001), 0.001 );
        phyPar.setProperties(20.0, 101.325, 0.0, 0.000390);
        assertEquals( 343.23, phyPar.getSpeedOfSound(), 0.02 );
        assertEquals( 1.205, phyPar.getDensity(), 0.001 );
        assertEquals( 1.616, phyPar.getEpsilonFromF(1.0, 0.001), 0.001 );
        
        // Saturated air.
        phyPar.setProperties(20.0, 101.325, 100.0, 0.000390);
        assertEquals( 344.47, phyPar.getSpeedOfSound(), 0.02 );
        assertEquals( 1.194, phyPar.getDensity(), 0.001 );
        assertEquals( 1.608, phyPar.getEpsilonFromF(1.0, 0.001), 0.001 );
 
        // Saturated, exhaled air.
        phyPar.setProperties(37.0, 101.325, 100.0, 0.040);
        assertEquals( 353.22, phyPar.getSpeedOfSound(), 0.02 );
        assertEquals( 1.129, phyPar.getDensity(), 0.001 );
        assertEquals( 1.662, phyPar.getEpsilonFromF(1.0, 0.001), 0.001 );
        
        // At 1 km.
        assertEquals( 89.996, PhysicalParameters.pressureAt(1000), 0.001);
        phyPar.setProperties(20.0, 90.0, 100.0, 0.000390);
        assertEquals( 344.64, phyPar.getSpeedOfSound(), 0.02 );
        assertEquals( 1.059, phyPar.getDensity(), 0.001 );
        assertEquals( 1.706, phyPar.getEpsilonFromF(1.0, 0.001), 0.001 );
    }

    /**
     * Test method for
     * {@link com.wwidesigner.util.PhysicalParameters#calcZ0(double)}.
     */
    @Test
    public void testCalcZ0()
    {
        double temp = 20.;
        PhysicalParameters phyPar = new PhysicalParameters( temp,
                                                            TemperatureType.C );
        assertEquals( 3.647e6, phyPar.calcZ0( 0.006 ), 1e3 );
    }

}

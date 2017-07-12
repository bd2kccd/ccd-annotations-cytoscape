package edu.pitt.cs.admt;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for CyActivator
 */
public class CyActivatorTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CyActivatorTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CyActivatorTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}

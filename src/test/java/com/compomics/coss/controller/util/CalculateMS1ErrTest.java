/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class CalculateMS1ErrTest {
    
    public CalculateMS1ErrTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getMS1Err method, of class CalculateMS1Err.
     */
    @Test
    public void testGetMS1Err() {
        System.out.println("getMS1Err");
        boolean isPPM = false;
        double theoreticalPrecursorMass = 0.0;
        double measuredPrecusorMass = 0.0;
        double expResult = 0.0;
        double result = CalculateMS1Err.getMS1Err(isPPM, theoreticalPrecursorMass, measuredPrecusorMass);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

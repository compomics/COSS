/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class MSRobinTest {
    
    public MSRobinTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of calculateCumulativeBinominalProbability method, of class MSRobin.
     */
    @Test
    public void testCalculateCumulativeBinominalProbability() throws Exception {
        System.out.println("calculateCumulativeBinominalProbability");
        MSRobin instance = null;
        double expResult = 0.0;
        double result = instance.calculateCumulativeBinominalProbability();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateScore method, of class MSRobin.
     */
    @Test
    public void testCalculateScore() {
        System.out.println("calculateScore");
        MSRobin instance = null;
        instance.calculateScore();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIntensity_part method, of class MSRobin.
     */
    @Test
    public void testGetIntensity_part() {
        System.out.println("getIntensity_part");
        MSRobin instance = null;
        double expResult = 0.0;
        double result = instance.getIntensity_part();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntensity_part method, of class MSRobin.
     */
    @Test
    public void testSetIntensity_part() {
        System.out.println("setIntensity_part");
        double intensity_part = 0.0;
        MSRobin instance = null;
        instance.setIntensity_part(intensity_part);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProbability_part method, of class MSRobin.
     */
    @Test
    public void testGetProbability_part() throws Exception {
        System.out.println("getProbability_part");
        MSRobin instance = null;
        double expResult = 0.0;
        double result = instance.getProbability_part();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.featureExtraction;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class DivideAndTopNPeaksTest {
    
    public DivideAndTopNPeaksTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of process method, of class DivideAndTopNPeaks.
     */
    @Test
    public void testProcess() {
        System.out.println("process");
        DivideAndTopNPeaks instance = null;
        instance.process();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTopN method, of class DivideAndTopNPeaks.
     */
    @Test
    public void testGetTopN() {
        System.out.println("getTopN");
        DivideAndTopNPeaks instance = null;
        int expResult = 0;
        int result = instance.getTopN();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTopN method, of class DivideAndTopNPeaks.
     */
    @Test
    public void testSetTopN() {
        System.out.println("setTopN");
        int topN = 0;
        DivideAndTopNPeaks instance = null;
        instance.setTopN(topN);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWindowMassSize method, of class DivideAndTopNPeaks.
     */
    @Test
    public void testGetWindowMassSize() {
        System.out.println("getWindowMassSize");
        DivideAndTopNPeaks instance = null;
        double expResult = 0.0;
        double result = instance.getWindowMassSize();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWindowMassSize method, of class DivideAndTopNPeaks.
     */
    @Test
    public void testSetWindowMassSize() {
        System.out.println("setWindowMassSize");
        double windowMassSize = 0.0;
        DivideAndTopNPeaks instance = null;
        instance.setWindowMassSize(windowMassSize);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

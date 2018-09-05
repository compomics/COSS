/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class GetMatchedPeaksTest {
    
    public GetMatchedPeaksTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of printClosest method, of class GetMatchedPeaks.
     */
    @Test
    public void testPrintClosest() {
        System.out.println("printClosest");
        List<Double> ar1 = null;
        List<Double> ar2 = null;
        int m = 0;
        int n = 0;
        double fragTolerance = 0.0;
        GetMatchedPeaks instance = new GetMatchedPeaks();
        int[] expResult = null;
        int[] result = instance.printClosest(ar1, ar2, m, n, fragTolerance);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

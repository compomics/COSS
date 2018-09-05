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
public class MathsUtilTest {
    
    public MathsUtilTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of log method, of class MathsUtil.
     */
    @Test
    public void testLog() {
        System.out.println("log");
        double x = 0.0;
        int base = 0;
        double expResult = 0.0;
        double result = MathsUtil.log(x, base);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateCombination method, of class MathsUtil.
     */
    @Test
    public void testCalculateCombination() throws Exception {
        System.out.println("calculateCombination");
        int n = 0;
        int r = 0;
        long expResult = 0L;
        long result = MathsUtil.calculateCombination(n, r);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

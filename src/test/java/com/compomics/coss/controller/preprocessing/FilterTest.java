/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class FilterTest {
    
    public FilterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of filter method, of class Filter.
     */
    @Test
    public void testFilter() {
        System.out.println("filter");
        double[] originalData = null;
        Filter instance = new FilterImpl();
        double[] expResult = null;
        double[] result = instance.filter(originalData);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class FilterImpl extends Filter {

        public double[] filter(double[] originalData) {
            return null;
        }
    }
    
}

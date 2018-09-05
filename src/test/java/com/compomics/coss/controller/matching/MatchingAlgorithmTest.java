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
public class MatchingAlgorithmTest {
    
    public MatchingAlgorithmTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of values method, of class MatchingAlgorithm.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        MatchingAlgorithm[] expResult = null;
        MatchingAlgorithm[] result = MatchingAlgorithm.values();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of valueOf method, of class MatchingAlgorithm.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "";
        MatchingAlgorithm expResult = null;
        MatchingAlgorithm result = MatchingAlgorithm.valueOf(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

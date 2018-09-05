/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ComparisonResult;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class MeanSquareErrorTest {
    
    public MeanSquareErrorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of InpArgs method, of class MeanSquareError.
     */
    @Test
    public void testInpArgs() {
        System.out.println("InpArgs");
        String[] args = null;
        MeanSquareError instance = null;
        instance.InpArgs(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopMatching method, of class MeanSquareError.
     */
    @Test
    public void testStopMatching() {
        System.out.println("stopMatching");
        MeanSquareError instance = null;
        instance.stopMatching();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dispatcher method, of class MeanSquareError.
     */
    @Test
    public void testDispatcher() {
        System.out.println("dispatcher");
        Logger log = null;
        MeanSquareError instance = null;
        List<ComparisonResult> expResult = null;
        List<ComparisonResult> result = instance.dispatcher(log);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

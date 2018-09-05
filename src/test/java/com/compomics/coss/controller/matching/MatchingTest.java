/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ComparisonResult;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
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
public class MatchingTest {
    
    public MatchingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of InpArgs method, of class Matching.
     */
    @Test
    public void testInpArgs() {
        System.out.println("InpArgs");
        String[] args = null;
        Matching instance = new MatchingImpl();
        instance.InpArgs(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dispatcher method, of class Matching.
     */
    @Test
    public void testDispatcher() {
        System.out.println("dispatcher");
        Logger log = null;
        Matching instance = new MatchingImpl();
        List<ComparisonResult> expResult = null;
        List<ComparisonResult> result = instance.dispatcher(log);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopMatching method, of class Matching.
     */
    @Test
    public void testStopMatching() {
        System.out.println("stopMatching");
        Matching instance = new MatchingImpl();
        instance.stopMatching();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of meanSqrError method, of class Matching.
     */
    @Test
    public void testMeanSqrError() {
        System.out.println("meanSqrError");
        List<Peak> v1 = null;
        List<Peak> v2 = null;
        Matching instance = new MatchingImpl();
        double expResult = 0.0;
        double result = instance.meanSqrError(v1, v2);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of logTransform method, of class Matching.
     */
    @Test
    public void testLogTransform() {
        System.out.println("logTransform");
        Spectrum spec = null;
        Matching instance = new MatchingImpl();
        instance.logTransform(spec);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class MatchingImpl extends Matching {

        public void InpArgs(String[] args) {
        }

        public List<ComparisonResult> dispatcher(Logger log) {
            return null;
        }

        public void stopMatching() {
        }
    }
    
}

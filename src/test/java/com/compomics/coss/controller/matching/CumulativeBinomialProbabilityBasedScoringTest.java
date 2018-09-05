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
public class CumulativeBinomialProbabilityBasedScoringTest {
    
    public CumulativeBinomialProbabilityBasedScoringTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of calculateScore method, of class CumulativeBinomialProbabilityBasedScoring.
     */
    @Test
    public void testCalculateScore() {
        System.out.println("calculateScore");
        CumulativeBinomialProbabilityBasedScoring instance = new CumulativeBinomialProbabilityBasedScoringImpl();
        instance.calculateScore();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getScore method, of class CumulativeBinomialProbabilityBasedScoring.
     */
    @Test
    public void testGetScore() {
        System.out.println("getScore");
        CumulativeBinomialProbabilityBasedScoring instance = new CumulativeBinomialProbabilityBasedScoringImpl();
        double expResult = 0.0;
        double result = instance.getScore();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateCumulativeBinominalProbability method, of class CumulativeBinomialProbabilityBasedScoring.
     */
    @Test
    public void testCalculateCumulativeBinominalProbability() throws Exception {
        System.out.println("calculateCumulativeBinominalProbability");
        CumulativeBinomialProbabilityBasedScoring instance = new CumulativeBinomialProbabilityBasedScoringImpl();
        double expResult = 0.0;
        double result = instance.calculateCumulativeBinominalProbability();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class CumulativeBinomialProbabilityBasedScoringImpl extends CumulativeBinomialProbabilityBasedScoring {

        public void calculateScore() {
        }
    }
    
}

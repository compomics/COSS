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
public class WaveletTest {
    
    public WaveletTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getFeatures method, of class Wavelet.
     */
    @Test
    public void testGetFeatures() {
        System.out.println("getFeatures");
        double[] data = null;
        Wavelet instance = new Wavelet();
        double[] expResult = null;
        double[] result = instance.getFeatures(data);
       // assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

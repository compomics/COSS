/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing.Transformation;

import com.compomics.ms2io.model.Spectrum;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class LogTransformNGTest {
    
    public LogTransformNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of transform method, of class LogTransform.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        Spectrum spec = null;
        LogTransform instance = null;
        Spectrum expResult = null;
        Spectrum result = instance.transform(spec);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

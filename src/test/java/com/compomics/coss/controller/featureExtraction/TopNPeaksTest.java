/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.featureExtraction;

import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class TopNPeaksTest {
    
    public TopNPeaksTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of process method, of class TopNPeaks.
     */
    @Test
    public void testProcess() {
        System.out.println("process");
        TopNPeaks instance = new TopNPeaksImpl();
        instance.process();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExpSpectrum method, of class TopNPeaks.
     */
    @Test
    public void testGetExpSpectrum() {
        System.out.println("getExpSpectrum");
        TopNPeaks instance = new TopNPeaksImpl();
        Spectrum expResult = null;
        Spectrum result = instance.getExpSpectrum();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setExpSpectrum method, of class TopNPeaks.
     */
    @Test
    public void testSetExpSpectrum() {
        System.out.println("setExpSpectrum");
        Spectrum expMSnSpectrum = null;
        TopNPeaks instance = new TopNPeaksImpl();
        instance.setExpSpectrum(expMSnSpectrum);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFilteredPeaks method, of class TopNPeaks.
     */
    @Test
    public void testGetFilteredPeaks() {
        System.out.println("getFilteredPeaks");
        TopNPeaks instance = new TopNPeaksImpl();
        ArrayList<Peak> expResult = null;
        ArrayList<Peak> result = instance.getFilteredPeaks();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class TopNPeaksImpl extends TopNPeaks {

        public void process() {
        }
    }
    
}

package com.compomics.coss.controller.featureExtraction;

import java.util.ArrayList;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class DivideAndTopNPeaksNGTest {
    
    public DivideAndTopNPeaksNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getFeatures method, of class DivideAndTopNPeaks.
     */
    @Test
    public void testGetFeatures() {
        System.out.println("getFeatures");
        DivideAndTopNPeaks instance = null;
        ArrayList expResult = null;
        ArrayList result = null;// instance.getFeatures();
        assertEquals(result, expResult);
    }
    
}

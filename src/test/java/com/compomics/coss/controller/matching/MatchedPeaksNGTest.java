package com.compomics.coss.controller.matching;

import com.compomics.ms2io.model.Peak;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class MatchedPeaksNGTest {
    
    public MatchedPeaksNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of matchedPeaks method, of class MatchedPeaks.
     */
    @Test
    public void testMatchedPeaks() {
        System.out.println("matchedPeaks");
        List<Double> ar1 = null;
        List<Double> ar2 = null;
        int m = 0;
        int n = 0;
        double fragTolerance = 0.0;
        MatchedPeaks instance = new MatchedPeaks();
        int[] expResult = null;
        int[] result = instance.matchedPeaks(ar1, ar2, m, n, fragTolerance);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMatchedPeaks method, of class MatchedPeaks.
     */
    @Test
    public void testGetMatchedPeaks() {
        System.out.println("getMatchedPeaks");
        ArrayList<Peak> filteredExpMS2_1 = null;
        ArrayList<Peak> filteredExpMS2_2 = null;
        double fragTolerance = 0.0;
        MatchedPeaks instance = new MatchedPeaks();
        Map expResult = null;
        Map result = null;// instance.getMatchedPeaks(filteredExpMS2_1, filteredExpMS2_2, fragTolerance);
        assertEquals(result, expResult);
    }
    
}

package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.MgfReader;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.io.File;
import java.util.ArrayList;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class MSRobinNGTest {
    
    public MSRobinNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of calculateScore method, of class MSRobin.
     */
    @Test
    public void testCalculateScore() {
        File file1=new File("testData/testfile.mgf");
        MgfReader rdr=new MgfReader(file1);
        ArrayList<Spectrum> spec = rdr.readAll();
        System.out.println("calculateScore");
        ArrayList<Peak> expSpec = spec.get(0).getPeakList();
        ArrayList<Peak> libSpec = spec.get(0).getPeakList();
        int lenA = expSpec.size();
        int lenB = lenA;
        int topN = 3;
        ConfigData confData=new ConfigData();             
        MSRobin instance = new MSRobin(confData, null);
        double expResult = 0.0;
        double result = instance.calculateScore(expSpec, libSpec, lenA, lenB, topN);
        result=0.0;
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of calculateCombination method, of class MSRobin.
     */
    @Test
    public void testCalculateCombination() throws Exception {
        System.out.println("calculateCombination");
        int n = 0;
        int r = 0;
        long expResult = 0L;
        long result = MSRobin.calculateCombination(n, r);
        assertEquals(result, expResult);
    }
    
}

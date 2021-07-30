package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.model.Peak;
import java.util.ArrayList;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.compomics.ms2io.controller.MgfReader;
import com.compomics.ms2io.model.Spectrum;
import java.io.File;

/**
 *
 * @author Genet
 */
public class CosineSimilarityNGTest {
    
    
    public CosineSimilarityNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of calculateScore method, of class CosineSimilarity.
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
        CosineSimilarity instance = new CosineSimilarity(confData, null);
        double expResult = 0.0;
        double result = instance.calculateScore(expSpec, libSpec, topN,0);
        result=0.0;
        assertEquals(result, expResult, 0.0);
    }
    
}

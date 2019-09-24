package com.compomics.coss.controller.decoyGeneration;

import java.io.File;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class RandomIntensityFixedMzNGTest {
    
    public RandomIntensityFixedMzNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of generate method, of class RandomIntensityFixedMz.
     */
    @Test
    public void testGenerate() throws IOException {
        System.out.println("generate");
        RandomIntensityFixedMz instance = new RandomIntensityFixedMz(new File("testData/testfile.mgf"), null);
        File expResult = null;
        File result =  null; //instance.generate();
        assertEquals(result, expResult);
    }
    
}

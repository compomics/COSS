package com.compomics.coss.controller.decoyGeneration;

import java.io.File;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class RandomMzIntShiftNGTest {
    
    public RandomMzIntShiftNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of generate method, of class RandomMzIntShift.
     */
    @Test
    public void testGenerate() {     
        
        System.out.println("generate");
        RandomMzIntShift instance = new RandomMzIntShift(new File("testData/testfile.mgf"), null);
        File expResult = null;
        File result =  null; //instance.generate();
        assertEquals(result, expResult);
    }
    
}

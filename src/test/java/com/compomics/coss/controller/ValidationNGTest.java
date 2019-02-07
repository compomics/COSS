package com.compomics.coss.controller;

import com.compomics.coss.model.ComparisonResult;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class ValidationNGTest {
    
    public ValidationNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of validate method, of class Validation.
     */
    @Test
    public void testValidate() {
        System.out.println("validate");
        List<ComparisonResult> result_2 = null;
        double fdr_given = 0.0;
        Validation instance = new Validation();
        List expResult = null;
        List result = null;//instance.validate(result_2, fdr_given);
        assertEquals(result, expResult);
    }

    
}

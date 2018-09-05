/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class ITransformTest {
    
    public ITransformTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of transform method, of class ITransform.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        EnTransform tr = null;
        ITransform instance = new ITransformImpl();
        instance.transform(tr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class ITransformImpl implements ITransform {

        public void transform(EnTransform tr) {
        }
    }
    
}

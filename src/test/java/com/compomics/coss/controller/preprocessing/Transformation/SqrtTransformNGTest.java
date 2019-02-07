package com.compomics.coss.controller.preprocessing.Transformation;

import com.compomics.ms2io.MgfReader;
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
public class SqrtTransformNGTest {
    
    public SqrtTransformNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of transform method, of class SqrtTransform.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        File file1=new File("testData/testfile.mgf");
        MgfReader rdr=new MgfReader(file1);
        ArrayList<Spectrum> specs = rdr.readAll();
        Spectrum spec = specs.get(0);
        SqrtTransform instance = new SqrtTransform();
        Spectrum expResult = null;
        Spectrum result = null;// instance.transform(spec);
        assertEquals(result, expResult);
    }
    
}

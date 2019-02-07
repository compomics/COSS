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
public class MergeFilesNGTest {
    
    public MergeFilesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of Merge method, of class MergeFiles.
     */
    @Test
    public void testMerge() {
        System.out.println("Merge");
        File file1=new File("testData/testfile.mgf");
        File file2=new File("testData/testfile.msp");
        
        MergeFiles instance = new MergeFiles(file1, file2);
        instance.Merge();
        
    }
    
}

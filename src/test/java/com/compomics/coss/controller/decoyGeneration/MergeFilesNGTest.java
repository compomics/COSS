package com.compomics.coss.controller.decoyGeneration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        try {
            System.out.println("Merge");
            File file1=new File("testData/testfile.mgf");
            File file2=new File("testData/testfile.msp");
            
            MergeFiles instance = new MergeFiles(file1, file2);
            instance.Merge();
        } catch (InterruptedException ex) {
            Logger.getLogger(MergeFilesNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

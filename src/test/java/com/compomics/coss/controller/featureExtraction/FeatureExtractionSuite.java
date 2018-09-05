/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.featureExtraction;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Genet
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({com.compomics.coss.controller.featureExtraction.DivideAndTopNPeaksTest.class, com.compomics.coss.controller.featureExtraction.WaveletTest.class, com.compomics.coss.controller.featureExtraction.TopNPeaksTest.class})
public class FeatureExtractionSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}

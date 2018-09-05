/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Genet
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({com.compomics.coss.controller.preprocessing.AbRemovePrecursorTest.class, com.compomics.coss.controller.preprocessing.IFilterTest.class, com.compomics.coss.controller.preprocessing.ITransformTest.class, com.compomics.coss.controller.preprocessing.RemovePCursorTest.class, com.compomics.coss.controller.preprocessing.HighPassTest.class, com.compomics.coss.controller.preprocessing.EnTransformTest.class, com.compomics.coss.controller.preprocessing.FilterTest.class, com.compomics.coss.controller.preprocessing.TransformSpectrumTest.class})
public class PreprocessingSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Genet
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({com.compomics.coss.controller.matching.MatchingTest.class, com.compomics.coss.controller.matching.MatchingAlgorithmTest.class, com.compomics.coss.controller.matching.ScoreNameTest.class, com.compomics.coss.controller.matching.MSRobinTest.class, com.compomics.coss.controller.matching.MeanSquareErrorTest.class, com.compomics.coss.controller.matching.CosineSimilarityTest.class, com.compomics.coss.controller.matching.Score_InterfaceTest.class, com.compomics.coss.controller.matching.GetMatchedPeaksTest.class, com.compomics.coss.controller.matching.CumulativeBinomialProbabilityBasedScoringTest.class, com.compomics.coss.controller.matching.UseMsRobenTest.class})
public class MatchingSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}

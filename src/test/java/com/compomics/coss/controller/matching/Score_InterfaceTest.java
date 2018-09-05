/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Genet
 */
public class Score_InterfaceTest {
    
    public Score_InterfaceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of calculateScore method, of class Score_Interface.
     */
    @Test
    public void testCalculateScore() {
        System.out.println("calculateScore");
        Score_Interface instance = new Score_InterfaceImpl();
        instance.calculateScore();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class Score_InterfaceImpl implements Score_Interface {

        public void calculateScore() {
        }
    }
    
}

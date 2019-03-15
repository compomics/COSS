/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class MainConsolControlerNGTest {
    
    public MainConsolControlerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of startRunning method, of class MainConsoleController.
     */
    @Test
    public void testStartRunning() {
        System.out.println("startRunning");
        String[] args = {"C:/pandyDS/SpecA.msp", "C:/pandyDS/SpecB.msp"};
        MainConsoleController instance = new MainConsoleController();
        instance.startRunning(args);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of validateResult method, of class MainConsoleController.
     */
    @Test
    public void testValidateResult() {
        System.out.println("validateResult");
        MainConsoleController instance = new MainConsoleController();
        instance.validateResult();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

   
    
}

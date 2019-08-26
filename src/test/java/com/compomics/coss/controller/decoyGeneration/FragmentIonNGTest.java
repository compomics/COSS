/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.decoyGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Genet
 */
public class FragmentIonNGTest {

    public FragmentIonNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getFragmentIon method, of class FragmentIon.
     */
    @Test
    public void testGetFragmentIon() {
        System.out.println("getFragmentIon");
        String aa_sequence = "AAAAAAAVSGNNASDEPSR";
        Map modifications = new HashMap<Integer, List<String>>();
        List mods = new ArrayList<String>();
                   double[] massA = {43.04219, 114.07931, 185.11642, 256.15353, 327.19063, 398.22775,
                469.26486, 568.33326, 655.36530, 712.38675, 826.42968, 940.47262, 1011.50972,
                1098.54175, 1213.56869, 1342.61128, 1439.66404, 1526.69608};

            double[] massB = {71.03711, 142.07422, 213.11133, 284.14844, 355.18555, 426.22266, 497.25977,
                596.32818, 683.36021, 740.38167, 854.42460, 968.46753, 1039.50464, 1126.53667,
                1241.56361, 1370.60620, 1467.65896, 1554.69099};

            double[] massY = {174.1117, 261.1437, 358.1965, 487.2391, 602.2660, 689.2980, 760.3351, 874.3781,
                988.4210, 1045.4425, 1132.4745, 1231.5429, 1302.5800, 1373.6171, 1444.6542, 1515.6913,
                1586.7284, 1657.7656};


//            double[] massAm = {43.04219, 114.07931, 185.11642, 256.15353, 384.21211, 499.23905, 628.28164,
//                741.36569, 842.41337, 899.43484, 1014.46177, 1071.48323, 1172.53092, 1273.57859,
//                1374.62628, 1473.69468, 1572.76310, 1732.79375, 1845.87781, 1944.94622, 2001.96768,
//                2131.01027, 2244.09433, 2357.17839};
//            double[] massBm = {71.03711, 142.07422, 213.11133, 284.14844, 412.20702, 527.23396, 656.27655,
//                769.36061, 870.40829, 927.42975, 1042.45669, 1099.47815, 1200.52583, 1301.57351,
//                1402.62119, 1501.68960, 1600.75801, 1760.78866, 1873.87272, 1972.94113, 2029.96259,
//                2159.00518, 2272.08924, 2385.17330};
//            double[] massYm = {174.1117, 287.1957, 400.2798, 529.3224, 586.3438, 685.4123, 798.4963, 958.5270,
//                1057.5954, 1156.6638, 1257.7115, 1358.7591, 1459.8068, 1516.8283, 1631.8552, 1688.8767,
//                1789.9244, 1903.0084, 2032.0510, 2147.0780, 2275.1365, 2346.1736, 2417.2108, 2488.2479};
            
        
        System.out.println(massA.length);
        System.out.println(massB.length);
        System.out.println(massY.length);

        Map expResult = new HashMap<>();
        for (int i = 0; i < 18; i++) {
            String suffix = Integer.toString(i);
            expResult.put("a" + suffix, massA[i]);
            expResult.put("b" + suffix, massB[i]);
            expResult.put("y" + suffix, massY[i]);
        }
        
        
        System.out.println(aa_sequence);
        FragmentIon instance = new FragmentIon(aa_sequence, modifications);

        Map result = instance.getFragmentIon();
        
//        for (int i = 0; i < result.size()/3 ; i++) {
//            System.out.println(result.get("a" + Integer.toString(i)) + "    \t");
//            System.out.println(result.get("b" + Integer.toString(i)) + "    \t");
//            System.out.println(result.get("y" + Integer.toString(i)) + "    \t");
//            System.out.println("\n");
//        }
        
        
        assertEquals(result, expResult);

    }

}

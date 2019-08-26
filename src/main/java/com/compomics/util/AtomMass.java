
package com.compomics.util;

/**
 * holds mass of atoms
 * @author Genet
 */
public final class AtomMass {
    
    private AtomMass(){
        
    }
    
    private static  final double proton = 1.007276; // monoisotopic mass of H+
    private static final double H1 = 1.007825;
    private static final double C12 = 12;
    private static final double C13 = 13.003355;
    private static final double N14 = 14.003074;
    private static final double N15 = 15.000109;
    private static final double O16 = 15.994915;
    
    /**
     * return mass of the atom passed to the function
     * @param atom
     * @return 
     */
    public static double getAtomMass(String atom){
        double mass=0.0;
        switch(atom){
            case "proton": mass= proton;
            break;
            case "H1": mass = H1;
            break;
            case "C12": mass = C12;
            break;
            case "C13": mass = C13;
            break;    
            case "N14": mass = N14;
            break;
            case "N15": mass = N15;
            break;    
            case "O16": mass = O16;
            break;
              
              
        }
        return mass;
    }
    
}

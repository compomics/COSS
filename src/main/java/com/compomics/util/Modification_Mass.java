/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util;

/**
 *
 * @author Genet
 */
public final class Modification_Mass {

    private static final double CAM = 57.021464;
    private static final double Oxidation = 15.994915;
    private static final double Pyro_glu_E = -18.010565;	// Pyro-glu from E (Glu -> pyro-Glu) //modified
    private static final double Pyro_glu_Q = -17.026549;	// Pyro-glu from Q (Gln -> pyro-Glu) //modified
    private static final double Pyro_carbamidomethyl = 39.994915; //modified
    private static final double Glu_pyro_Glu = -18.010565;//name modified
    private static final double Gln_pyro_Glu = -17.026549;
    private static final double iTRAQ = 144.102063;
    private static final double iTRAQ8 = 304.205360;
    private static final double TMT = 229.162932;
    private static final double Acetyl = 42.010565;
    private static final double Amidated = -0.984016;
    private static final double Biotin = 226.077598;
    private static final double Carbamidomethyl = 57.021464;
    private static final double Carbamyl = 43.005814;
    private static final double Carboxymethyl = 58.005479;
    private static final double Deamidated = 0.984016;
    private static final double Dehydrated = -18.010565;
    private static final double Dimethyl = 28.0313;
    private static final double Farnesyl = 204.187801;
    private static final double FormylMet = 159.035399;
    private static final double GeranylGeranyl = 272.250401;
    private static final double Glucuronyl = 176.032088;
    private static final double Glutathione = 305.068156;
    private static final double Guanidinyl = 42.021798;
    private static final double HexNAc = 203.079373;
    private static final double HNE = 156.11503;
    private static final double Iminobiotin = 225.093583;
    private static final double Lipoyl = 188.032956;
    private static final double Methyl = 14.01565;
    private static final double Methylthio = 45.987721;
    private static final double Myristoyl = 210.198366;
    private static final double Nethylmaleimide = 125.047679;
    private static final double Palmitoyl = 238.229666;
    private static final double Phospho = 79.966331;
    private static final double Propionamide = 71.037114;
    private static final double Propionyl = 56.026215;
    private static final double Pyridylacetyl = 119.037114;
    private static final double Pyridylethyl = 105.057849;
    private static final double Succinyl = 100.016044;
    private static final double Sulfo = 79.956815;
    private static final double Trimethyl = 42.04695;
    
    
    public static double getMass(String mod){
        
        double mass=0.0;
        switch(mod){
            case "CAM": mass = 57.021464;
            break;
            case "Oxidation": mass = 15.994915;
            break;
            case "Pyro_glu_E": mass = -18.010565;
            break;
            case "Pyro_glu_Q": mass = -17.026549;
            break;
            case "Pyro_carbamidomethyl":  mass = 39.994915; 
            break;
            case "Glu_pyro_Glu": mass = -18.010565;//name modified
            break;
            case "Gln_pyro_Glu": mass = -17.026549;
            break;
            case "iTRAQ": mass = 144.102063;
            break;
            case "TMT": mass = 229.162932;
            break;
            case "Acetyl": mass = 42.010565;
            break;
            case "Amidated": mass = -0.984016;
            break;
            case "Biotin": mass = 226.077598;
            break;
            case "Carbamidomethyl": mass = 57.021464;
            break;
            case "Carbamyl": mass = 43.005814;
            break;
            case "Carboxymethyl": mass = 58.005479;
            break;
            case "Deamidated": mass = 0.984016;
            break;
            case "Dehydrated": mass = -18.010565;
            break;
            case "Dimethyl": mass = 28.0313;
            break;
            case "Farnesyl": mass = 204.187801;
            break;
            case "FormylMet": mass = 159.035399;
            break;                
            case "GeranylGeranyl": mass = 272.250401;
            break;
            case "Glucuronyl": mass = 176.032088;
            break;
            case "Glutathione": mass = 305.068156;
            break;
            case "Guanidinyl": mass = 42.021798;           
            break;           
            case "HexNAc":  mass = 203.079373; 
            break;
            case "HNE": mass = 156.11503;
            break;
            case "Iminobiotin": mass = 225.093583;
            break;
            case "Lipoyl": mass = 188.032956;
            break;
            case "Methyl": mass = 14.01565;
            break;
            
            case "Methylthio": mass = 45.987721;
            break;
            case "Myristoyl": mass = 210.198366;
            break;
            case "Nethylmaleimide": mass = 125.047679;
            break;
            case "Palmitoyl": mass = 238.229666;
            break;
            case "Phospho": mass = 79.966331;
            break;
            case "Propionamide": mass = 71.037114;
            break;
            case "Propionyl": mass = 56.026215;
            break;
            case "Pyridylacetyl": mass = 119.037114;
            break;
            case "Pyridylethyl": mass = 105.057849;
            break;
            case "Succinyl": mass = 100.016044;
            break;
            case "Sulfo": mass = 79.956815;
            break;  
            case "Trimethyl": mass = 42.04695;
            break;   

            
        }
        return mass;
    }

}

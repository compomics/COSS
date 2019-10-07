package com.compomics.util;

/**
 * constant class holding mass of amino acids
 * @author Genet
 */
public final class AA_Mass {

    /*
    no initialization, singletone class
    */
    private AA_Mass(){
        
    }
    private static final double A = 71.03711;
    private static final double C = 103.00919;
    private static final double D = 115.02694;
    private static final double E = 129.04259;
    private static final double F = 147.06841;
    private static final double G = 57.02146;
    private static final double H = 137.05891;
    private static final double I = 113.08406;
    private static final double K = 128.09496;
    private static final double L = 113.08406;
    private static final double M = 131.04049;
    private static final double N = 114.04293;
    private static final double P = 97.05276;
    private static final double Q = 128.05858;
    private static final double R = 156.10111;
    private static final double S = 87.03203;
    private static final double T = 101.04768;
    private static final double V = 99.06841;
    private static final double W = 186.07931;
    private static final double Y = 163.06332;
    
    public static double getAA_mass(char aa){
        double mass=0.0;
        switch(aa){
            case 'A': mass = A;
            break;
            case 'C': mass = C;
            break;
            case 'D': mass = D;
            break;
            case 'E': mass = E;
            break;
            case 'F': mass = F;
            break;
            case 'G': mass = G;
            break;
            case 'H': mass = H;
            break;
            case 'I': mass = I;
            break;
            case 'K': mass = K;
            break;
            case 'L': mass = L;
            break;
            case 'M': mass = M;
            break;
            case 'N': mass = N;
            break;
            case 'P': mass = P;
            break;
            case 'Q': mass = Q;
            break;
            case 'R': mass = R;
            break;
            case 'S': mass = S;
            break;
            case 'T': mass = T;
            break;
            case 'V': mass = V;
            break;
            case 'W': mass = W;
            break;
            case 'Y': mass = Y;
            break;            
            
        }
        return mass;
    }

}

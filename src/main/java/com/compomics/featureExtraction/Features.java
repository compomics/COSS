/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.featureExtraction;

/**
 *
 * @author Genet
 */
public abstract class Features {
    
    /**
     *
     * @param data the original data from which features extracted
     * @return
     */
    public abstract double[] getFeatures(double[] data);
    
}

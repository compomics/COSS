/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.featureExtraction;

import com.compomics.ms2io.model.Peak;
import com.compomics.ms2io.model.Spectrum;
import java.util.ArrayList;

/**
 *
 * @author Genet
 */
public interface Features {
    
    ArrayList<Peak>  getFeatures(Spectrum spectrum, int topN);
    
}

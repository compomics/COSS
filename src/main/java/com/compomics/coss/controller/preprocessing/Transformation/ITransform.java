/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing.Transformation;

import com.compomics.ms2io.model.Spectrum;


/**
 *
 * @author Genet
 */
public interface ITransform {
     /**
     * This method transforms intensities on a spectrum
     * @param originalSpec spectrum to be transformed
     * @return return transformed spectrum
     */
    public Spectrum transform(Spectrum originalSpec);
    
}

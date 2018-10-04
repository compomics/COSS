/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing.Filter;


import com.compomics.ms2io.Spectrum;

/**
 *
 * @author Genet
 */
public abstract class LowPass implements IFilter {

    @Override
    public Spectrum filter(Spectrum spec, double threshold) {
       return null;
    }
    
}

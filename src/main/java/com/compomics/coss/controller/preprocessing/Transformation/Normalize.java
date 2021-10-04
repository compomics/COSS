/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing.Transformation;

import com.compomics.ms2io.model.Peak;
import com.compomics.ms2io.model.Spectrum;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Genet
 */
public class Normalize implements ITransform{
    
     Spectrum normalizedSpec = null;
    Spectrum normalizedPeaks = null;

    public Normalize() {

    }

    @Override
    public Spectrum transform(Spectrum spec) {
        
        ArrayList<Peak> peaks = spec.getPeakList();
        //find the maximum peak intensity
        double max = 0;
        for (Peak p : peaks) {
            if (max < p.getIntensity()) {
                max = p.getIntensity();
            }
        }

        //Normalize peaks
        if (max > 0) {
            for (Peak p : peaks) {
                p.setIntensity(p.getIntensity() / max);
            }
        }
        spec.setPeakList(peaks);
        normalizedSpec = spec;
        return normalizedSpec;
        
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.preprocessing.Transformation;

import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Genet
 */
public class SqrtTransform implements ITransform {

    @Override
    public Spectrum transform(Spectrum spec) {
        Spectrum trnsSpec=null;
        try{            
            List<Peak> peaks = spec.getPeakList();
            int indx=0;
            for (Peak p : peaks) {
                double intensity = p.getIntensity();
                intensity = Math.sqrt(intensity);
                p.setIntensity(intensity);
                peaks.set(indx, p);
                indx++;
            
            }
            trnsSpec.setPeakList(new ArrayList(peaks));
        }catch(IllegalArgumentException ex){
            
            throw new IllegalArgumentException();
        }
          
        return trnsSpec;
    }
    
}

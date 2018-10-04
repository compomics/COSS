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
public class LogTransform implements ITransform {

    private final int logBase;
    Spectrum trnsSpec=null;

    public LogTransform(int base) {
        this.logBase = base;
    }

    @Override
    public Spectrum transform(Spectrum spec) {
        trnsSpec = spec;
        if(logBase==2){
            log2Transform(spec);
        }else if(logBase==10){
            log10Transform(spec);
        }
        
        return trnsSpec;
    }
    
    private void log2Transform(Spectrum spec){
         try{            
            List<Peak> peaks = spec.getPeakList();
            int indx=0;
            for (Peak p : peaks) {
                double intensity = p.getIntensity();
                intensity = Math.log(intensity)/Math.log(2.0);
                p.setIntensity(intensity);
                peaks.set(indx, p);
                indx++;
            
            }
            trnsSpec.setPeakList(new ArrayList(peaks));
        }catch(IllegalArgumentException ex){
            
            throw new IllegalArgumentException();
        }
            
    }
    
     private void log10Transform(Spectrum spec){
         try{            
            List<Peak> peaks = spec.getPeakList();
            int indx=0;
            for (Peak p : peaks) {
                double intensity = p.getIntensity();
                intensity = Math.log10(intensity)/Math.log(10.0);
                p.setIntensity(intensity);
                peaks.set(indx, p);
                indx++;
            
            }
            trnsSpec.setPeakList(new ArrayList(peaks));
        }catch(IllegalArgumentException ex){
            
            throw new IllegalArgumentException();
        }
            
    }

}

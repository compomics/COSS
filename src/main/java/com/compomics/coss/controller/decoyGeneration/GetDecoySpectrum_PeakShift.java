package com.compomics.coss.controller.decoyGeneration;

import com.compomics.ms2io.model.Peak;
import com.compomics.ms2io.model.Spectrum;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 *
 * @author Genet
 */
public class GetDecoySpectrum_PeakShift implements Callable<Spectrum> {

    Spectrum spectrum;

    public GetDecoySpectrum_PeakShift(Spectrum spec) {
        this.spectrum = spec;
    }

    @Override
    public Spectrum call() throws Exception {
        ArrayList<Peak> peaks_d = spectrum.getPeakList();
        for (Peak p : peaks_d) {     

            p.setMz(p.getMz() + 20);
        }
        
        this.spectrum.setPeakList(peaks_d);
        
         this.spectrum.setPeakList(peaks_d);
        //for mgf format
        if(!spectrum.getTitle().equals("") && spectrum.getComment().equals("")){
            spectrum.setTitle(spectrum.getTitle() + " _Decoy");
        }
        //for msp format
        if(spectrum.getTitle().equals("") && !spectrum.getComment().equals("")){
            spectrum.setComment(spectrum.getComment() + " _Decoy");
        }
        
        return this.spectrum;

    }

}

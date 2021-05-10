
package com.compomics.oglycans;
import com.compomics.ms2io.model.*;
import com.compomics.ms2io.controller.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Genet
 */
public class Generate_spectra {
    
    private void start() throws IOException{
        File mgf_file=new File("D:/text.mgf");
        String[] digested_peptides = getPeptides();
        String name="o_glycan";
        double mw = 0;
        int num_peaks =0;
        String comment = "o_glycan";
        double pcmass=0;
        FragmentIon_glycan frag;
        Spectrum spec=new Spectrum();
        SpectraWriter spw=new MspWriter(mgf_file);
        
        for(String peptide : digested_peptides){
            frag=new FragmentIon_glycan(peptide);
            ArrayList<Double> mz_values = frag.getFragmentIon();
            Peak pk;
            ArrayList<Peak> peaks = new ArrayList<>();
            for(Double d : mz_values){
                pk=new Peak(d, 600, "");
                peaks.add(pk);                
            }
            
            spec.setComment(comment);
            spec.setMW(mw);;
            spec.setSequence(peptide);
            spec.setPCMass(0);
            spec.setTitle(name);
            spec.setNumPeaks(mz_values.size());
            spec.setPeakList(peaks);            
            spw.write(spec);   
            
        }
    
    
    
}
    

    private String[] getPeptides() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

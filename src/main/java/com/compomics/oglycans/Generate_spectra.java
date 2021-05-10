
package com.compomics.oglycans;
import com.compomics.ms2io.model.*;
import com.compomics.ms2io.controller.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Genet
 */
public class Generate_spectra {
    public Generate_spectra(){
        
    }
    
    public void start(List<String> peptides, File mgf_file) throws IOException{      
       
        
        ArrayList<String> digested_peptides = (ArrayList)peptides;      
        double pcmass=0;
        FragmentIon_glycan frag;
        FragmentIon_glycan frag_decoy;
        Spectrum spec=new Spectrum();
        Spectrum spec_decoy=new Spectrum();
        SpectraWriter spw=new MspWriter(mgf_file);
        
        for(String peptide : digested_peptides){
            //get fragment ion of the peptide
            frag=new FragmentIon_glycan(peptide);
            
            
           //Generatin decoy fragment ions from reverse sequence
            //reverse sequence except the last aa             
             char[] tempseq = peptide.substring(0, peptide.length() - 1).toCharArray();
             ArrayUtils.reverse(tempseq);
             String rev_sequence = new String(tempseq);             
            //add last aa to reversed sequence
            rev_sequence += peptide.charAt( peptide.length() - 1);            
            frag_decoy=new FragmentIon_glycan(rev_sequence);
            
            
            
            ArrayList<Double> mz_values = frag.getFragmentIon();
            ArrayList<Double> mz_values_decoy = frag_decoy.getFragmentIon();
            
            Peak pk;
            ArrayList<Peak> peaks = new ArrayList<>();
            
            Peak pk_d;
            ArrayList<Peak> peaks_d = new ArrayList<>();
            
            //peak list
            for(Double d : mz_values){
                pk=new Peak(d, 600, "");
                peaks.add(pk);                
            }
            
            //peak list of decoy
            for(Double dd : mz_values_decoy){
                pk_d=new Peak(dd, 600, "");
                peaks_d.add(pk_d);                
            }
            
            spec.setComment("Comment: " + "Parent="+pcmass + " " + "Mods=o_glycan, S&T");
            spec.setMW(0);;
            spec.setSequence(peptide);
            spec.setPCMass(pcmass);
            spec.setTitle("Name: " + peptide);
            spec.setNumPeaks(mz_values.size());
            spec.setPeakList(peaks);            
            spw.write(spec);   
            
            spec_decoy.setComment("Comment: " + "Parent="+pcmass + " " + "Mods=o_glycan, S&T");
            spec_decoy.setMW(0);;
            spec_decoy.setSequence(rev_sequence);
            spec_decoy.setPCMass(pcmass);
            spec_decoy.setTitle("Name: " + rev_sequence);
            spec_decoy.setNumPeaks(mz_values_decoy.size());
            spec_decoy.setPeakList(peaks_d);            
            spw.write(spec_decoy);  
            
        }
        spw.closeWriter();
        
    
    
    
}
    

    private ArrayList<String> getPeptides() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

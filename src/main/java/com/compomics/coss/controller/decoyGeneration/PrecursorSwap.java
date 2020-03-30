package com.compomics.coss.controller.decoyGeneration;

import com.compomics.ms2io.model.Spectrum;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Genet
 */
public class PrecursorSwap extends GenerateDecoy {
    
     public PrecursorSwap(File f, org.apache.log4j.Logger log) throws IOException {
        super(f, log);
    }

    @Override
    public void generate() {
        int len_index = indxList.size();
        Spectrum spectrum;
        Spectrum spectrum2;
       // GetDecoySpectrum_RandomPeaks getDecoy;

        String temp_pm = "";
        
      //  Future<Spectrum> future;
        int half_index=len_index/2;
        
        ExecutorService executor = Executors.newFixedThreadPool(8);
     
        for (int i = 0; i < half_index ; i++) {
            try {
                spectrum = specReader.readAt(indxList.get(i).getPos());
                spectrum2 = specReader.readAt(indxList.get(i + half_index).getPos());
                temp_pm= "Comments: " + "Parent=" + Double.toString(spectrum2.getPCMass()) + " " + "Mods=" + spectrum.getModifications_asStr() + "_decoy";
                spectrum.setComment(temp_pm);
                temp_pm= "Comments: " + "Parent=" + Double.toString(spectrum.getPCMass()) + " " + "Mods=" + spectrum2.getModifications_asStr() + "_decoy";
                spectrum2.setComment(temp_pm);
                
                spectrum.setTitle(spectrum.getSequence() + "_decoy" + "/" + Integer.toString(spectrum.getCharge().getChargeValue()));
                spectrum2.setTitle(spectrum2.getSequence() + "_decoy" + "/" + Integer.toString(spectrum2.getCharge().getChargeValue()));
               
                specWriter.write(spectrum);
                specWriter.write(spectrum2);
                System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));
      
                
            } catch (Exception ex) {
                Logger.getLogger(ReverseSequence.class.getName()).log(Level.SEVERE, null, ex);
            } 

        }
        
        specWriter.closeWriter();
 
        
        MergeFiles merg = new MergeFiles(file, decoyFile);
        try {
            merg.Merge();
            decoyFile.delete();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(RandomSequene.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } finally {

        }
    }
    
}

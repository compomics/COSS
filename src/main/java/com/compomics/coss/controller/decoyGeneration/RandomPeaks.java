package com.compomics.coss.controller.decoyGeneration;

import com.compomics.coss.controller.UpdateListener;
import com.compomics.ms2io.model.Spectrum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class RandomPeaks extends GenerateDecoy {

    File randomIntShift;

    public RandomPeaks(File f, org.apache.log4j.Logger log) throws IOException {
        super(f, log);
    }

    @Override
    public void generate() {
        
        int len_index = indxList.size();
        Spectrum spectrum;
        GetDecoySpectrum_RandomPeaks getDecoy;

        Future<Spectrum> future;
        ExecutorService executor = Executors.newFixedThreadPool(8);
     
        for (int i = 0; i < len_index ; i++) {
            try {
                spectrum = specReader.readAt(indxList.get(i).getPos());
               
                
                getDecoy = new GetDecoySpectrum_RandomPeaks(spectrum);
                
                future = executor.submit(getDecoy);                
                spectrum = future.get();    
                synchronized(this){
                    specWriter.write(spectrum);
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));
                }
                
            } catch (InterruptedException ex) {
                Logger.getLogger(ReverseSequence.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ReverseSequence.class.getName()).log(Level.SEVERE, null, ex);
            } 

        }
        
        specWriter.closeWriter();
        executor.shutdown();   
        
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

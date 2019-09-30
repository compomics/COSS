
package com.compomics.coss.controller.decoyGeneration;

import com.compomics.coss.controller.SpectrumAnnotation.Annotator;
import com.compomics.ms2io.model.Spectrum;
import java.io.File;
import java.io.IOException;
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
public class RandomSequene extends GenerateDecoy {
    
    public RandomSequene(File f, double  fragTol, org.apache.log4j.Logger log) throws IOException {
        super(f, fragTol, log);
    }

    @Override
    public void generate() {
        int len_index = indxList.size();
        Spectrum spectrum;
        GetDecoySpectrum getDecoy;

        Future<Spectrum> future;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        String sequence="";
        String shuffle_sequence="";
     
        for (int i = 0; i < len_index ; i++) {
            try {
                spectrum = specReader.readAt(indxList.get(i).getPos());
                sequence = spectrum.getSequence();
                shuffle_sequence = shuffle(sequence.substring(0, sequence.length() - 1));
                shuffle_sequence += sequence.charAt(sequence.length() - 1);
                
                getDecoy = new GetDecoySpectrum(spectrum, shuffle_sequence);
                
               // getDecoy = new GetDecoySpectrum(spectrum, "");
                future = executor.submit(getDecoy);                
                spectrum = future.get();    
                synchronized(this){
                    specWriter.write(spectrum);
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));
                }
                
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomSequene.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(RandomSequene.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        specWriter.closeWriter();
        executor.shutdown(); 
    
    }
    
   
    
}

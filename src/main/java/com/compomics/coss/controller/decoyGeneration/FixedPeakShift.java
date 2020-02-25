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

/**
 *
 * @author Genet
 */
public class FixedPeakShift extends GenerateDecoy {

    File mzShift = null;

    public FixedPeakShift(File f, org.apache.log4j.Logger log) throws IOException {
        super(f, log);
    }

    @Override
    public void generate() {
        
        int len_index = indxList.size();
        Spectrum spectrum;
        GetDecoySpectrum_PeakShift getDecoy;

        Future<Spectrum> future;
        ExecutorService executor = Executors.newFixedThreadPool(8);
     
        for (int i = 0; i < len_index ; i++) {
            try {
                spectrum = specReader.readAt(indxList.get(i).getPos());
               
                
                getDecoy = new GetDecoySpectrum_PeakShift(spectrum);
                
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
        
        
        
        
        
        
        
        
//        BufferedWriter bw = null;
//        BufferedReader br = null;
//        String filename;
//        String fileExtension = "";
//
//        if (file.getName().endsWith("mgf")) {
//            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
//            mzShift = new File(file.getParent(), filename + "_mzshift" + ".mgf");
//            fileExtension = "mgf";
//
//        } else if (file.getName().endsWith("msp")) {
//            filename = file.getName().substring(0, file.getName().lastIndexOf("."));
//            mzShift = new File(file.getParent(), filename + "_mzshift" + ".msp");
//            fileExtension = "msp";
//
//        }
//
//        try {
//            int count = 1;
//
//            br = new BufferedReader(new FileReader(file));
//            bw = new BufferedWriter(new FileWriter(mzShift));
//            
//            String line = br.readLine();
//            while (line != null) {
//
//                if (!"".equals(line) && Character.isDigit(line.charAt(0))) {
//                    String fline = line.replaceAll("\\s+", " ");
//                    String[] p = fline.split(" ");
//                    double pcm = Double.parseDouble(p[0]) + 20;
//                    p[0] = Double.toString(pcm);
//                    int len = p.length;
//                    line = "";
//                    for (int s = 0; s < len; s++) {
//                        line += p[s] + " ";
//                    }
//
//                } else if ((line.startsWith("Name") && fileExtension.equals("msp")) || (line.startsWith("TITLE") && fileExtension.equals("mgf"))) {
//                    line += "_decoy";
//                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
//                    Thread.sleep(1);
//                    System.out.print("Current decoy spectrum index generated :  " + Integer.toString(count));
//                    count++;
//                }
//
//                bw.write(line + "\n");
//                line = br.readLine();
//
//            }
//
//
//        } catch (IOException ex) {
//            Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedPeakShift.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(FixedPeakShift.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                br.close();
//                bw.close();
//            } catch (IOException ex) {
//                Logger.getLogger(com.compomics.coss.controller.decoyGeneration.FixedPeakShift.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

      
    }


}

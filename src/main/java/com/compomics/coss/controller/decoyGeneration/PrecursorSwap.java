package com.compomics.coss.controller.decoyGeneration;

import com.compomics.ms2io.controller.SpectraReader;
import com.compomics.ms2io.controller.SpectraWriter;
import com.compomics.ms2io.model.IndexKey;
import com.compomics.ms2io.model.Spectrum;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        int midIndex=len_index/2;
        
        Spectrum spectrum;
        Spectrum spectrum2;
        String temp_pm = "";

        for (int i = 0; i < midIndex; i++) {
            try {
               
                    spectrum = specReader.readAt(indxList.get(i).getPos());
                    spectrum2 = specReader.readAt(indxList.get(i + midIndex).getPos());

                    temp_pm = "Comments: " + "Parent=" + Double.toString(spectrum2.getPCMass()) + " " + "Mods=" + spectrum.getModifications_asStr() + " _decoy";
                    spectrum.setComment(temp_pm);
                    temp_pm = "Comments: " + "Parent=" + Double.toString(spectrum.getPCMass()) + " " + "Mods=" + spectrum2.getModifications_asStr() + " _decoy";
                    spectrum2.setComment(temp_pm);



                    specWriter.write(spectrum);
                    specWriter.write(spectrum2);
                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));
               

            } catch (Exception ex) {
                Logger.getLogger(ReverseSequence.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
//        int div = len_index / 4;
//
//        // GetDecoySpectrum_RandomPeaks getDecoy;
//        ExecutorService executor = Executors.newFixedThreadPool(4);
//
//        Swapspectrum swap1 = new Swapspectrum(indxList.subList(0, div), specReader, specWriter);
//        Swapspectrum swap2 = new Swapspectrum(indxList.subList(div, 2 * div), specReader, specWriter);
//        Swapspectrum swap3 = new Swapspectrum(indxList.subList(2 * div, 3 * div), specReader, specWriter);
//        Swapspectrum swap4 = new Swapspectrum(indxList.subList(3 * div, len_index), specReader, specWriter);
//
//        CompletableFuture[] futures = new CompletableFuture[4];
//        executor.execute(swap1);
//        futures[0] = CompletableFuture.runAsync(swap1, executor);
//        executor.execute(swap2);
//        futures[1] = CompletableFuture.runAsync(swap2, executor);
//        executor.execute(swap3);
//        futures[2] = CompletableFuture.runAsync(swap3, executor);
//        executor.execute(swap4);
//        futures[3] = CompletableFuture.runAsync(swap4, executor);
//
//        //wait for all threads to finish
//        CompletableFuture.allOf(futures).join();
        specWriter.closeWriter();

        //merge generated decoy file to the original library
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

class Swapspectrum implements Runnable {

    final List<IndexKey> specIndex;
    SpectraReader rd;
    SpectraWriter wr;

    public Swapspectrum(List<IndexKey> specIndex, SpectraReader rd, SpectraWriter wr) {
        this.specIndex = specIndex;
        this.rd = rd;
        this.wr = wr;
    }

    @Override
    public void run() {

//        Spectrum spectrum;
//        Spectrum spectrum2;
//        int midIdx = this.specIndex.size() / 2;
//        String temp_pm = "";
//
//        for (int i = 0; i < midIdx; i++) {
//            try {
//                synchronized (this) {
//                    spectrum = this.rd.readAt(this.specIndex.get(i).getPos());
//                    spectrum2 = this.rd.readAt(this.specIndex.get(i + midIdx).getPos());
//
//                    temp_pm = "Comments: " + "Parent=" + Double.toString(spectrum2.getPCMass()) + " " + "Mods=" + spectrum.getModifications_asStr() + "_decoy";
//                    spectrum.setComment(temp_pm);
//                    temp_pm = "Comments: " + "Parent=" + Double.toString(spectrum.getPCMass()) + " " + "Mods=" + spectrum2.getModifications_asStr() + "_decoy";
//                    spectrum2.setComment(temp_pm);
//
//                    spectrum.setTitle(spectrum.getSequence() + "_decoy" + "/" + Integer.toString(spectrum.getCharge().getChargeValue()));
//                    spectrum2.setTitle(spectrum2.getSequence() + "_decoy" + "/" + Integer.toString(spectrum2.getCharge().getChargeValue()));
//
//                    this.wr.write(spectrum);
//                    this.wr.write(spectrum2);
//                    //System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));
//                }
//
//            } catch (Exception ex) {
//                Logger.getLogger(ReverseSequence.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
    }

}

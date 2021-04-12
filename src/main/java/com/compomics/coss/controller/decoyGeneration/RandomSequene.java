package com.compomics.coss.controller.decoyGeneration;

import com.compomics.ms2io.model.Spectrum;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

    public RandomSequene(File f, org.apache.log4j.Logger log) throws IOException {
        super(f, log);
    }

    @Override
    public void generate() {
        int len_index = indxList.size();
        Spectrum spectrum;
        GetDecoySpectrum getDecoy;

        Future<Spectrum> future;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        String sequence = "";
        String shuffle_sequence = "";

        
        for (int i = 0; i < len_index; i++) {
            spectrum = specReader.readAt(indxList.get(i).getPos());
            sequence = spectrum.getSequence();
            //shuffle_sequence = shuffle(sequence.substring(0, sequence.length() - 1));
            int seqLen = sequence.length();
            //new sequence positions after randomiz
            int[] newPos = new int[seqLen];
            Arrays.fill(newPos, -1);
            int k = 0;
            boolean found;
            shuffle_sequence = "";
            while (k<seqLen-1) {
                int randomIndex = (int) (Math.random() * seqLen - 1);
                found = false;
                for (int val : newPos) {
                    if (val == randomIndex) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    shuffle_sequence += sequence.charAt(randomIndex);
                    newPos[k] = randomIndex;
                    k++;
                }
            }
            newPos[seqLen - 1] = seqLen - 1;
            shuffle_sequence += sequence.charAt(seqLen - 1);
            getDecoy = new GetDecoySpectrum(spectrum, shuffle_sequence, newPos, specWriter);
            executor.submit(getDecoy);
            
            // getDecoy = new GetDecoySpectrum(spectrum, "");
//                future = executor.submit(getDecoy);
//                spectrum = future.get();
//                if(file.getName().endsWith("msp")){
//                    spectrum.setTitle(shuffle_sequence + "/" + spectrum.getCharge().getCharge());
//                    if (!spectrum.getModifications().isEmpty()) {
//                        String comments=spectrum.getComment();
//                        String[] splitcomm = comments.split(" ");
//                        int ln = splitcomm.length;
//                        for (int j = 0; j < ln; j++) {
//                            if (splitcomm[k].contains("Mods")) {
//                                splitcomm[k] = "Mods=" + spectrum.getModifications_asStr();
//                                break;
//                            }
//                        }
//                        String newcomments = String.join(" ", splitcomm);
//                        spectrum.setComment(newcomments);
//                    }
//                }


//               // synchronized (this) {
//                    specWriter.write(spectrum);
//                    System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));
//               // }

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

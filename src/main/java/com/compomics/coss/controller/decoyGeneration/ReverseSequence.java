package com.compomics.coss.controller.decoyGeneration;

import com.compomics.coss.controller.SpectrumAnnotation.Annotation;
import com.compomics.ms2io.model.Spectrum;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class ReverseSequence extends GenerateDecoy {

    public ReverseSequence(File f, org.apache.log4j.Logger log) throws IOException {

        super(f, log);
    }

    @Override
    public void generate() {

        int len_index = indxList.size();
        Spectrum spectrum;
        GetDecoySpectrum getDecoy;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        String sequence = "";
        String rev_sequence = "";

        for (int i = 0; i < len_index; i++) {
            spectrum = specReader.readAt(indxList.get(i).getPos());
            sequence = spectrum.getSequence();
            int seqLen = sequence.length();
            //reverse sequence except the last aa
            rev_sequence = reverse(sequence.substring(0, seqLen - 1));
            //add last aa to reversed sequence
            rev_sequence += sequence.charAt(seqLen - 1);

            //new sequence positions after reverse
            int[] newPos = new int[seqLen];
            int base = seqLen - 2;//-1 for 0 based len and -1 for unmodified last aa position
            for (int k = 0; k < seqLen - 1; k++) {
                newPos[k] = base - k;
            }
            newPos[seqLen - 1] = seqLen - 1;//add the last index unmodified
            getDecoy = new GetDecoySpectrum(spectrum, rev_sequence, newPos, specWriter);
            executor.submit(getDecoy);
            //Thread.sleep(5000);
            System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(i));

        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("decoy file generated ... wait until it is appended to the original file ");
            
            MergeFiles merg = new MergeFiles(file, decoyFile);
            try {
                merg.Merge();
                decoyFile.delete();
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(ReverseSequence.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } finally {

            }

        } catch (InterruptedException e) {

        } finally {
            specWriter.closeWriter();

        }

    }

}

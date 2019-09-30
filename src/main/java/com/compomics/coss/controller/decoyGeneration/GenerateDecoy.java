package com.compomics.coss.controller.decoyGeneration;

import com.compomics.ms2io.controller.Indexer;
import com.compomics.ms2io.controller.MgfReader;
import com.compomics.ms2io.controller.MspReader;
import com.compomics.ms2io.controller.MspWriter;
import com.compomics.ms2io.model.IndexKey;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import com.compomics.ms2io.model.Peak;
import com.compomics.ms2io.controller.SpectraReader;
import com.compomics.ms2io.controller.SpectraWriter;
import java.io.IOException;

/**
 *
 * @author Genet
 */
public abstract class GenerateDecoy {

    protected File file;
    // protected final UpdateListener lstnr;
    protected File decoyFile;
    protected SpectraReader specReader;
    protected SpectraWriter specWriter;
    protected List<IndexKey> indxList;
    protected double fragTol;
    protected org.apache.log4j.Logger log;

    public GenerateDecoy(File f, double fragTol, org.apache.log4j.Logger lg) throws IOException {
        this.log = lg;
        this.file = f;
        set_spectrum_ReadWrite();
        this.fragTol=fragTol;
        // this.lstnr=lr;
    }
    
    private void set_spectrum_ReadWrite() throws IOException{
        
        specReader = null;
        specWriter=null;
        
        Indexer giExp = new Indexer(this.file);
        indxList = giExp.generate();
      
        if (this.file.getName().endsWith("mgf")) {
            specReader = new MgfReader(this.file, indxList);

        } else if (this.file.getName().endsWith("msp") || this.file.getName().endsWith("sptxt")) {
            specReader = new MspReader(this.file, indxList);

        }
        String filename = file.getName().substring(0, file.getName().lastIndexOf("."));
        File f_decoy = new File(this.file.getParent(), filename + "_decoy.msp");
        specWriter = new MspWriter(f_decoy);
        
    }

    public abstract void generate();

    protected String shuffle(String aaSequence) {

        char[] shuffledSequence = aaSequence.toCharArray();
        ArrayUtils.shuffle(shuffledSequence);
        return new String(shuffledSequence);
    }

    protected Map<Double, Double> shuffle(LinkedHashMap<Double, Double> spectrum) {

        List<Double> list = new ArrayList<>(spectrum.keySet());
        Collections.shuffle(list);

        Map<Double, Double> shuffleMap = new LinkedHashMap<>();
        list.forEach(k -> shuffleMap.put(k, spectrum.get(k)));

        return shuffleMap;

    }

    /**
     * reverse amino acid sequence
     *
     * @param aaSequence the amino acid sequence to be reversed
     * @return reversed sequence
     */
    protected String reverse(String aaSequence) {
        char[] reversedSeq = aaSequence.toCharArray();

        ArrayUtils.reverse(reversedSeq);
        return (new String(reversedSeq));
    }

    /**
     * reverse the spectrum given
     *
     * @param spectrum the spectrum to be reversed
     * @return reversed spectrum
     */
    protected Map<Double, Double> reverse(Map<Double, Double> spectrum) {

        Map reversedMap = MapUtils.invertMap(spectrum);
        return reversedMap;

    }

    /**
     * reverse the spectrum given as peak list
     *
     * @param peakList the spectrum to be reversed
     *
     */
    protected void reverse(ArrayList<Peak> peakList) {

        Collections.reverse(peakList);

    }

    /**
     * shuffles the spectrum given as peak list
     *
     * @param peakList the spectrum to be reversed
     *
     */
    protected void shuffle(ArrayList<Peak> peakList) {

        ArrayList<Double> intensity = new ArrayList<>();
        peakList.stream().forEach((pk) -> {
            intensity.add(pk.getIntensity());
        });

        Collections.shuffle(intensity);
        int count = 0;
        for (Peak pk : peakList) {
            pk.setIntensity(intensity.get(count));
            count++;
        }

    }

  
}

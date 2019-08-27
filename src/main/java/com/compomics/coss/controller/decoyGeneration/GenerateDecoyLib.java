package com.compomics.coss.controller.decoyGeneration;

import com.compomics.ms2io.IndexKey;
import com.compomics.ms2io.Indexer;
import com.compomics.ms2io.MgfReader;
import com.compomics.ms2io.MgfWriter;
import com.compomics.ms2io.MspReader;
import com.compomics.ms2io.MspWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.SpectraReader;
import com.compomics.ms2io.SpectraWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Genet
 */
public abstract class GenerateDecoyLib {

    protected final File file;
    // protected final UpdateListener lstnr;
    protected File decoyFile;
    protected SpectraReader rd;
    protected SpectraWriter wr;
    protected List<IndexKey> indxList;
    protected org.apache.log4j.Logger log;

    public GenerateDecoyLib(File f, org.apache.log4j.Logger lg) {
        this.log = lg;
        this.file = f;
        // this.lstnr=lr;
    }

    public abstract File generate();

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

    /**
     * parse charge from given string
     *
     * @param comment string containing charge information
     * @return
     */
    protected int getCharge(String comment) {
        int charge = 1;
        try {

            charge = Integer.parseInt(comment.substring(comment.indexOf("Charge") + 6, comment.indexOf("Charge") + 7));

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return charge;
    }

    /**
     * parse modifications, if present, for the given string
     *
     * @param comment string containing modification information
     * @return
     */
    public Map getModifications(String comment) {

        Map<Integer, List<String>> modifications = new HashMap<>();

        try {
            int start = comment.indexOf("Mods");

            String mods_str = comment.substring(start);
            int end = mods_str.indexOf(" ");
            mods_str = mods_str.substring(0, end);

            if (!"Mods=0".equals(mods_str)) {
                String[] strAr = mods_str.split("[/()]");
                int num_mods = strAr.length - 1; //first string represents number of modifications

                //if (num_mods == Integer.parseInt(strAr[0])) { //splited string array might have white space
                List l = new ArrayList<String>();
                for (int p = 1; p < num_mods; p++) {
                    if (!"".equals(strAr[p])) {
                        strAr[p] = strAr[p].replaceAll("\\s", ""); //remove all white space
                        String[] m = strAr[p].split(",");
                        int pos = Integer.parseInt(m[0]);
                        if (!modifications.containsKey(pos)) {
                            l = new ArrayList<String>();
                            l.add(m[2]);
                            modifications.put(pos, l);
                        } else {
                            l = new ArrayList<String>();
                            l = modifications.get(pos);
                            l.add(m[2]);
                            modifications.put(pos, l);
                        }
                    }

                }
                //}
            }
        } catch (Exception ex) {

            System.out.println("decoyGeneration->GenerateDecoyLib->getModification: Error" + ex.toString());
        }

        return modifications;
    }

    /**
     * generates decoy peak list from library peaks based on the fragment ion
     *
     * @param lib_peaks original peaks from which decoy generated
     * @param fragment_ion generated decoy peaks
     * @return
     */
    protected List<Peak> getDecoyPeak(List<Peak> lib_peaks, Map fragment_ion_actual, Map fragment_ion_rev) {

        List<Peak> decoy_peaks = new ArrayList<>();
        boolean isAnnotated = false;

        for (Peak p : lib_peaks) {
            //copy peak of the library to decoy initially
            Peak dp = p;
            String ann = p.getPeakAnnotation();

            //alter decoy peak m/z value if annotaion not empty and annotation doesn't contain NH3 and H2O loss
            if (!"".equals(ann) && !"?".equals(ann) && !"\"?\"".equals(ann)) {
                //&& !p.getPeakAnnotation().contains("NH") && !ann.contains("H2O") && !ann.contains("CO2")
                String strAnn = ann.substring(0, ann.indexOf("/"));//sub-string before the first occurence of '/'that contains ion type

                if (!strAnn.contains("y1")) {
                    int ion_charge = 1;
                    strAnn = strAnn.trim(); //remove white spaces, leading and trailing
                    if (strAnn.contains("^")) {
                        ion_charge = Integer.parseInt(strAnn.substring(strAnn.indexOf("^") + 1));
                        strAnn = strAnn.substring(0, strAnn.indexOf("^"));
                    }

//                    String[] annArray = null;
//                    if(strAnn.contains("[\\w']+")){
//                       annArray = strAnn.split("[^\\w']+");
//                       strAnn=annArray[0];
//                    }
                    if(strAnn.contains("-")){
                        strAnn = strAnn.substring(0, strAnn.indexOf("-"));
                    }
                    
                    strAnn = strAnn.replaceAll("[^aby0-9]", "");//remove characters except letters a,b,y and numbers                          

                    double mass_frag_actual = (double) fragment_ion_actual.get(strAnn);//return mass of srtAnn ion
                    double mass_frag_rev = (double) fragment_ion_rev.get(strAnn);//return mass of srtAnn ion
                    mass_frag_rev = p.getMz() + (mass_frag_rev - mass_frag_actual) / (double) ion_charge;
                    dp.setMz(mass_frag_rev); //update decoy peak mz value with the new 

                }

                isAnnotated = true;
            }

            decoy_peaks.add(dp);
            //dealing with redundant peaks should be considered here

        }

        if (!isAnnotated) {
            System.out.println("Spectrun not annotated and hence decoy spectra can't be generated for this spectrum");
            decoy_peaks.clear();
        }

        return decoy_peaks;
    }

    /**
     * Configures reader and writer for the library and decoy library
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected void configureReadWriter() throws IOException, ClassNotFoundException {
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        File indxfile = new File(file.getParent(), fileName + ".idx");

        if (indxfile.exists()) {

            Indexer indxer = new Indexer();
            indxList = indxer.readFromFile(indxfile);

        } else {

            Indexer gi = new Indexer(file);
            indxList = gi.generate();

        }

        if (file.getName().endsWith("mgf")) {
            decoyFile = new File(file.getParent(), fileName + "_shuffledSeq" + ".mgf");
            rd = new MgfReader(file, indxList);
            wr = new MgfWriter(decoyFile);

        } else if (file.getName().endsWith("msp")) {
            decoyFile = new File(file.getParent(), fileName + "shuffledSeq" + ".msp");
            rd = new MspReader(file, indxList);
            wr = new MspWriter(decoyFile);

        }

    }
}

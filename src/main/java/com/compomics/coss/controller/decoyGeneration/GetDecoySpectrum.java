package com.compomics.coss.controller.decoyGeneration;

import com.compomics.ms2io.model.Modification;
import com.compomics.ms2io.model.Peak;
import com.compomics.ms2io.model.Spectrum;
import com.compomics.util.FragmentIon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 * @author Genet
 */
public class GetDecoySpectrum implements Callable<Spectrum> {

    Spectrum spectrum;
    String rev_or_shuffle_seq;

    public GetDecoySpectrum(Spectrum spec, String rev_or_shuff_sequence) {
        this.spectrum = spec;
        this.rev_or_shuffle_seq = rev_or_shuff_sequence;
    }

    @Override
    public Spectrum call() throws Exception {
        ArrayList<Peak> peaks_d;
        Map<Integer, Modification> modifications = new HashMap<>();
        for (Modification m : this.spectrum.getModifications()) {
            modifications.put(m.getModificationPosition(), m);
        }
        FragmentIon ions = new FragmentIon(this.spectrum.getSequence(), modifications);
        Map frag_ion_actual = ions.getFragmentIon();

        ions = new FragmentIon(this.rev_or_shuffle_seq, modifications);
        Map frag_ion_reverse = ions.getFragmentIon();

        peaks_d = getDecoyPeak(this.spectrum.getPeakList(), frag_ion_actual, frag_ion_reverse);
        Collections.sort(peaks_d);
        this.spectrum.setPeakList(peaks_d);
        spectrum.setComment(spectrum.getComment() + " _Decoy");
        return this.spectrum;

    }

    /**
     * generates decoy peak list from library peaks based on the fragment ion
     *
     * @param lib_peaks original peaks from which decoy generated
     * @param fragment_ion generated decoy peaks
     * @return
     */
    private ArrayList<Peak> getDecoyPeak(List<Peak> lib_peaks, Map fragment_ion_actual, Map fragment_ion_rev) {
        HashMap<Double, Peak> d_peaks = new HashMap<>();

        ArrayList<Peak> decoy_peaks = new ArrayList<>();

        boolean annotated;
        for (Peak p : lib_peaks) {
            //copy peak of the library to decoy initially
            Peak dp = p;
            String ann = p.getPeakAnnotation();

            annotated = false;

            //alter decoy peak m/z value if annotaion not empty and annotation doesn't contain NH3 and H2O loss
            if (!"".equals(ann) && !"?".equals(ann) && !"\"?\"".equals(ann)) {
                //&& !p.getPeakAnnotation().contains("NH") && !ann.contains("H2O") && !ann.contains("CO2")
                String strAnn = ann;
                if (ann.contains("/")) {
                    strAnn = ann.substring(0, ann.indexOf("/"));//sub-string before the first occurence of '/'that contains ion type
                }

                if (!strAnn.contains("[") && !strAnn.contains("]") && (strAnn.contains("a") || strAnn.contains("b") || strAnn.contains("y") )) {
                   
                    int ion_charge = 1;
                    strAnn = strAnn.trim(); //remove white spaces, leading and trailing
                    if (strAnn.contains("^")) {
                        //strAnn = strAnn.substring(0, strAnn.indexOf("^"));
                        String st = strAnn.substring(strAnn.indexOf("^") + 1);
                        st = st.replaceAll("[^\\d]", "");
                        ion_charge = Integer.parseInt(st);

                    }

//                    String[] annArray = null;
//                    if(strAnn.contains("[\\w']+")){
//                       annArray = strAnn.split("[^\\w']+");
//                       strAnn=annArray[0];
//                    }
                    if (strAnn.contains("-")) {
                        strAnn = strAnn.substring(0, strAnn.indexOf("-"));
                    }
                    if (strAnn.contains("^")) {
                        strAnn = strAnn.substring(0, strAnn.indexOf("^"));
                    }

                    strAnn = strAnn.replaceAll("[^aby0-9]", "");//remove characters except letters a,b,y and numbers                          

                    if (fragment_ion_actual.containsKey(strAnn) && fragment_ion_rev.containsKey(strAnn) && !"y1".endsWith(strAnn)) {
                        double mass_frag_actual = (double) fragment_ion_actual.get(strAnn);//return mass of srtAnn ion
                        double mass_frag_rev = (double) fragment_ion_rev.get(strAnn);//return mass of srtAnn ion
                        mass_frag_rev = p.getMz() + (mass_frag_rev - mass_frag_actual) / (double) ion_charge;
                        mass_frag_rev = (double) Math.round(mass_frag_rev * 1000d) / 1000d;

                        dp.setMz(Math.abs(mass_frag_rev)); //update decoy peak mz value with the new 
                        annotated = true;
                    }

                }

                // isAnnotated = true;
            }

          
            if (!d_peaks.containsKey(dp.getMz())) {
                d_peaks.put(dp.getMz(), dp);

            } else if (d_peaks.containsKey(dp.getMz())) {
                if (annotated) {
                    d_peaks.put(dp.getMz(), dp);
                }

            }

        }

        for (Map.Entry e : d_peaks.entrySet()) {

            decoy_peaks.add((Peak) e.getValue());

        }
        
        
        return decoy_peaks;
    }
    
    public ArrayList<Peak> getDecoyPeaks_mzShift(Spectrum spec){
     return null;   
    }

}

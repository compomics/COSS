//package com.compomics.coss.controller.featureExtraction;
//
//import com.compomics.ms2io.Spectrum;
//import com.compomics.ms2io.Peak;
//import java.util.ArrayList;
//import org.apache.log4j.Logger;
//
///**
// *
// * @author Genet
// */
//public class SelectedPeaks implements Features {
//
//    private final Spectrum spectrum;
//    private final Logger LOGGER;
//
//    /**
//     * This constructs an object to filter out spectra intensity value:
//     *peaks with intensity value equal and bellow 5 will be discarded
//     * @param spec is an experimental spectrum
//     * @param log
//     */
//    public SelectedPeaks(Spectrum spec, Logger log) {
//        this.spectrum = spec;
//        this.LOGGER = log;
//
//    }
//
//    @Override
//    public ArrayList<Peak> getFeatures() {
//        ArrayList<Peak> filteredPeaks = new ArrayList<>();
//        int len = 0;
//        double threshold = 5;
//
//        try {
//            len = spectrum.getPeakList().size();
//
//            for (int i = 0; i < len; i++) {
//                double tempP=spectrum.getPeakList().get(i).getIntensity();
//                if (tempP > threshold) {
//                    filteredPeaks.add(spectrum.getPeakList().get(i));
//                }
//            }
//               
//            }catch (IndexOutOfBoundsException ex) {
//            String srtLen = Integer.toString(len);
//            LOGGER.info("Len : " + srtLen);//  + ": Error Location " + errorIndx);           
//            throw ex;
//        }
//
//            return filteredPeaks;
//        }
//
//    }

package com.compomics.coss.controller.featureExtraction;

import com.compomics.ms2io.model.Spectrum;
import com.compomics.ms2io.model.Peak;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.apache.log4j.Logger;

/**
 *
 * @author Genet
 */
public class DivideAndTopNPeaks implements Features {

   // private int topN;
    private int windowMassSize = 10;
//    private int windowMassSize = 100;
    private final Logger LOGGER;
//    ArrayList<Peak> cPeaks;
//    ArrayList<Peak> filteredPeaks;

    /**
     * This constructs an object to filter out spectra based on 100Da window and
     * it selects X peaks with highest intensities for each window.
     *
     * @param spec is an experimental spectrum
     * @param topN is picked peak numbers with highest intensities
     * @param windowmassSize moving window size to filter spectrum
     * @param log
     */
    public DivideAndTopNPeaks(int windowmassSize, Logger log) {
       
        this.LOGGER = log;
     

    }

   

//    public void setTopN(int topN) {
//        this.topN = topN;
//    }

    @Override
    public ArrayList<Peak> getFeatures(Spectrum spectrum, int topN) {

        int len = 0;
        double startMz = 0;
        double limitMz = 0;
        ArrayList<Peak> cPeaks = new ArrayList<>(1000);
        ArrayList<Peak> filteredPeaks = new ArrayList<>(5000);

      //  synchronized (this) {

           // cPeaks.clear();
           // filteredPeaks.clear();
            try {
                
                startMz = spectrum.getMinMZ();
                limitMz = startMz + windowMassSize;
                len = spectrum.getPeakList().size();
                Collections.sort(spectrum.getPeakList());

                double tmpMZ;
                double tempInt;
//                double maxInt_Percent = 100.0/spectrum.getMaxIntensity();
//                double int_ratio;
//                String anntn="";
                for (int k = 0; k < len; k++) {

                    Peak p= spectrum.getPeakList().get(k);
                    tmpMZ = p.getMz();
//                    tempInt= p.getIntensity();
//                    int_ratio = tempInt * maxInt_Percent;
//                    anntn = p.getPeakAnnotation();
//                    if(int_ratio < 2 && anntn.equals("\"?\"")){
//                        continue;//ignote this peak
//                    }

                    //Peak tmpPeak = spectrum.getPeakMap().get(tmpMZ);
                    if (tmpMZ < limitMz) {

                        cPeaks.add(spectrum.getPeakList().get(k));

                    } else if (cPeaks != null && !cPeaks.isEmpty()) {
                        //cPeaks already sort as spectrum.PeakList is sorted
                        //Collections.sort(cPeaks, Peak.DescendingIntensityComparator);
                        //int tmp_num = topN;
                        int len_c = cPeaks.size();
                        if (topN > len_c) {
                            //tmp_num = cPeaks.size(); //only for first peaks selection
                            filteredPeaks.addAll(cPeaks); //only for intense peak selection
                        } //new else ... selecting most intense peaks
                        else {
                            while (len_c > topN) {
                                double min = cPeaks.get(0).getIntensity();
                                int pos = 0;
                                for (int i = 1; i < len_c; i++) {
                                    if (min > cPeaks.get(i).getIntensity()) {
                                        min = cPeaks.get(i).getIntensity();
                                        pos = i;
                                    }
                                }
                                cPeaks.remove(pos);
                                len_c = cPeaks.size();
                            }
                            filteredPeaks.addAll(cPeaks);
                        }

                        cPeaks.clear();
                        limitMz = limitMz + windowMassSize;
                        k = k - 1;
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                String srtLen = Integer.toString(len);
                String srtMzMin = Double.toString(startMz);
                LOGGER.info("Len, minMz and Temp : " + srtLen + ", " + srtMzMin);//  + ": Error Location " + errorIndx);           
                throw ex;
            }

            if (!cPeaks.isEmpty()) {
                //Collections.sort(cPeaks, Peak.DescendingIntensityComparator);
                //int tmp_num = topN; //only for first peaks
                int len_c = cPeaks.size();
                if (topN > len_c) {
                    //tmp_num = cPeaks.size(); //only for first peaks
                    filteredPeaks.addAll(cPeaks);//only for intense peaks
                } else { //only for intense peaks, the whole else statment not the if

                    while (len_c > topN) {
                        double min = cPeaks.get(0).getIntensity();
                        int pos = 0;
                        for (int i = 1; i < len_c; i++) {
                            if (min > cPeaks.get(i).getIntensity()) {
                                min = cPeaks.get(i).getIntensity();
                                pos = i;
                            }
                        }
                        cPeaks.remove(pos);
                        len_c = cPeaks.size();
                    }
                    filteredPeaks.addAll(cPeaks);
                }
            }
       // }

        return filteredPeaks;
    }

}

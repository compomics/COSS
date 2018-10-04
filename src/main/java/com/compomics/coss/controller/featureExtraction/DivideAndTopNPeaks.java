package com.compomics.coss.controller.featureExtraction;

import com.compomics.ms2io.Spectrum;
import com.compomics.ms2io.Peak;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.log4j.Logger;

/**
 *
 * @author Genet
 */
public class DivideAndTopNPeaks implements Features {

    private final int topN;
    private double windowMassSize = 100;    
    private final Spectrum expSpectrum;
    private final Logger LOGGER;

    /**
     * This constructs an object to filter out spectra based on 100Da window and
     * it selects X peaks with highest intensities for each window.
     *
     * @param expSpectrum is an experimental spectrum
     * @param topN is picked peak numbers with highest intensities
     */
    public DivideAndTopNPeaks(Spectrum expSpec, int topN, double windowmassSize, Logger log) {
        this.expSpectrum = expSpec;
        this.topN = topN;
        this.windowMassSize=windowmassSize;        
        this.LOGGER=log;
        
    }

  
   
    @Override
    public ArrayList<Peak>  getFeatures() {
        ArrayList<Peak> cPeaks = new ArrayList<>();
        ArrayList<Peak> filteredPeaks=new ArrayList<>();
        int len = 0;
        double startMz = 0;
        double limitMz = 0;
        int temp = 0;
        try {
            startMz = expSpectrum.getMinMZ();
            limitMz = startMz + windowMassSize;
            len = expSpectrum.getNumPeaks();
            Collections.sort(expSpectrum.getPeakList());
            temp += 1;
            for (int k = 0; k < len; k++) {

                double tmpMZ = expSpectrum.getPeakList().get(k).getMz();
                temp += 1;
                //Peak tmpPeak = expSpectrum.getPeakMap().get(tmpMZ);
                if (tmpMZ < limitMz) {
                    cPeaks.add(expSpectrum.getPeakList().get(k));
                } else {
                    //cPeaks already sort as expSpectrum.PeakList is sorted
                    //Collections.sort(cPeaks, Peak.DescendingIntensityComparator);
                    int tmp_num = topN;
                    if (topN > cPeaks.size()) {
                        tmp_num = cPeaks.size();
                    }
                    for (int num = 0; num < tmp_num; num++) {
                        Peak tmpCPeakToAdd = cPeaks.get(num);
                        filteredPeaks.add(tmpCPeakToAdd);
                    }
                    cPeaks.clear();
                    limitMz = limitMz + windowMassSize;
                    k = k - 1;
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            String srtLen = Integer.toString(len);
            String srtMzMin = Double.toString(startMz);
            String srtTemp = Integer.toString(temp);
            LOGGER.info("Len, minMz and Temp : " + srtLen + ", " + srtMzMin + ", " + srtTemp);           
            throw ex;
        }

        if (!cPeaks.isEmpty()) {
            //Collections.sort(cPeaks, Peak.DescendingIntensityComparator);
            int tmp_num = topN;
            if (topN > cPeaks.size()) {
                tmp_num = cPeaks.size();
            }
            for (int num = 0; num < tmp_num; num++) {
                Peak tmpCPeakToAdd = cPeaks.get(num);
                filteredPeaks.add(tmpCPeakToAdd);
            }
        }
        
        return filteredPeaks;
    }

}

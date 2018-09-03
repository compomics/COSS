/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.featureExtraction;

import com.compomics.ms2io.Spectrum;
import com.compomics.ms2io.Peak;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Genet
 */
public class DivideAndTopNPeaks extends TopNPeaks {

    private int topN;
    private double windowMassSize = 100;

    /**
     * This constructs an object to filter out spectra based on 100Da window and
     * it selects X peaks with highest intensities for each window.
     *
     * @param expSpectrum is an experimental spectrum
     * @param topN is picked peak numbers with highest intensities
     */
    public DivideAndTopNPeaks(Spectrum expSpectrum, int topN) {
        super.expSpectrum = expSpectrum;
        this.topN = topN;
//        LOGGER = Logger.getLogger(ConfigHolder.class);
    }

    /**
     * This constructs an object with a given window size instead of a default
     * value.
     *
     * The default window size is 100Da
     *
     * @param expSpectrum is an experimental spectrum
     * @param topN is picked peak numbers with highest intensities
     * @param windowMassSize size of window, based on this a given spectrum is
     * divided into smaller parts.
     *
     */
    public DivideAndTopNPeaks(Spectrum expSpectrum, int topN, double windowMassSize) {
        super.expSpectrum = expSpectrum;
        this.topN = topN;
        this.windowMassSize = windowMassSize;
//        LOGGER = Logger.getLogger(ConfigHolder.class);
    }

    @Override
    protected void process() {
        ArrayList<Peak> cPeaks = new ArrayList<>();
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

            System.out.println("Len, minMz and Temp : " + srtLen + ", " + srtMzMin + ", " + srtTemp);
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
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public double getWindowMassSize() {
        return windowMassSize;
    }

    public void setWindowMassSize(double windowMassSize) {
        this.windowMassSize = windowMassSize;
    }

}

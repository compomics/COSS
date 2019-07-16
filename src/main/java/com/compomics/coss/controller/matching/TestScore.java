/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.Peak;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class TestScore extends Score{
    
    
    /**
     * @param confData
     * @param log
     */
    public TestScore(ConfigData confData, org.apache.log4j.Logger log) {
        super(confData, log);

    }
    /**
     *
     * @param lenA number of peaks in experimental spectrum
     * @param lenB number of peaks in library spectrum
     * @param topN Top intense peaks selected from each spectrum
     * @return returns score of the comparison
     */
    @Override
    public double calculateScore(ArrayList<Peak> expSpec, ArrayList<Peak> libSpec, int lenA, int lenB, int topN) {

        double intensity_part = 0;
        Map<String, ArrayList<Peak>> map = new TreeMap<>();
      
        ArrayList<Peak> mPeaksExp;
        ArrayList<Peak> mPeaksLib;
        if (lenB < lenA) {
            map = prepareData(expSpec, libSpec);
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks1");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks2");
         

        } else {

            double temp = sumTotalIntExp;//swap value if order if spetrua given is reversed
            sumTotalIntExp = sumTotalIntLib;
            sumTotalIntLib = temp;
            map = prepareData(libSpec, expSpec);        
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks2");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks1");

        }

        matchedNumPeaks = mPeaksExp.size();
        calculateSumIntensity(mPeaksExp, mPeaksLib);
        intensity_part = (sumMatchedIntExp*sumMatchedIntLib)/(sumTotalIntExp*sumTotalIntLib);
        double mzPart = matchedNumPeaks / lenA;
        
        double finalScore=mzPart*intensity_part;
        return finalScore;
    }
    private void calculateSumIntensity(ArrayList<Peak> mPeaksExp, ArrayList<Peak> mPeaksLib) {   
        double expSpecMatchedInt = 0;
        double libSpecMatchedInt = 0;

        for (int k = 0; k < matchedNumPeaks; k++) {
            expSpecMatchedInt += mPeaksExp.get(k).getIntensity();
            libSpecMatchedInt += mPeaksLib.get(k).getIntensity();

        }

        sumMatchedIntExp = expSpecMatchedInt;
        sumMatchedIntLib = libSpecMatchedInt;
   
       
    }   
    
}

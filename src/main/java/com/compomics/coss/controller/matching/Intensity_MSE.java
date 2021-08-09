/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.model.Peak;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
/**
 *
 * @author Genet
 */
public class Intensity_MSE extends Score {

    /**
     * @param confData
     * @param log
     */
    public Intensity_MSE(ConfigData confData, org.apache.log4j.Logger log) {
        super(confData, log);

    }

    /**
     * @param topN Top intense peaks selected from each spectrum
     * @return returns score of the comparison
     */
    @Override
    public double calculateScore(ArrayList<Peak> expSpec, ArrayList<Peak> libSpec, int topN, int transform) {
        Map<String, ArrayList<Peak>> map = new TreeMap<>();
        ArrayList<Peak> mPeaksExp;
        ArrayList<Peak> mPeaksLib;
        if (libSpec.size() < expSpec.size()) {
            map = prepareData(expSpec, libSpec);
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks1");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks2");
           

        } else {

//            double temp = sumTotalIntExp;//swap value if order if spetrua given is reversed
//            sumTotalIntExp = sumTotalIntLib;
//            sumTotalIntLib = temp;

            map = prepareData(libSpec, expSpec);
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks2");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks1");
         

        }

        matchedNumPeaks = mPeaksExp.size();
        sumMatchedIntExp = getSumIntensity(mPeaksExp);
        sumMatchedIntLib = getSumIntensity(mPeaksLib);
        double intScore = meanSquareError(normalizePeaks(mPeaksExp), normalizePeaks(mPeaksLib));  
        return (intScore);
    }

    private double meanSquareError(List<Peak> v1, List<Peak> v2) {

        double sumSqrError = 0;
        for (int a = 0; a < matchedNumPeaks; a++) {

            double err = v1.get(a).getIntensity() - v2.get(a).getIntensity();
            sumSqrError += err * err;
        }

        double mse = Double.MAX_VALUE;
        if (matchedNumPeaks != 0) {
            mse = sumSqrError / (double) matchedNumPeaks;
        }
        return mse;
    }

}

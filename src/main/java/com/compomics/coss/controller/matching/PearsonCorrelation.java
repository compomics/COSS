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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author Genet
 */
public class PearsonCorrelation extends Score {

    /**
     * @param confData
     * @param log
     */
    public PearsonCorrelation(ConfigData confData, org.apache.log4j.Logger log) {
        super(confData, log);

    }

    /**
     *
     *
     * @param topN Top intense peaks selected from each spectrum
     * @return returns intScore of the comparison
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

            map = prepareData(libSpec, expSpec);
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks2");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks1");

        }

        matchedNumPeaks = mPeaksExp.size();
        sumMatchedIntExp = getSumIntensity(mPeaksExp);
        sumMatchedIntLib = getSumIntensity(mPeaksLib);

        double score = -1;
        try {
            score = pearsonCorrelation(normalizePeaks(mPeaksExp), normalizePeaks(mPeaksLib), transform);

        } catch (ArithmeticException ex) {
            System.out.println(ex.toString());
        }

        return score;
    }

    private double pearsonCorrelation(List<Peak> v1, List<Peak> v2, int transform) { // parameters vector1 and vector2
        double cor_coef = -1;
        if (matchedNumPeaks >= 2) {
            double[] v1_intensity = new double[matchedNumPeaks];
            double[] v2_intensity = new double[matchedNumPeaks];

            try {
                if (transform == 0) {
                    
                    for (int i = 0; i < matchedNumPeaks; i++) {
                        v1_intensity[i] = v1.get(i).getIntensity();
                        v2_intensity[i] = v2.get(i).getIntensity();

                    }
                } else {
                    for (int i = 0; i < matchedNumPeaks; i++) {
                        v1_intensity[i] = Math.log(v1.get(i).getIntensity()) / Math.log(2.0);
                        v2_intensity[i] = Math.log(v2.get(i).getIntensity()) / Math.log(2.0);

                    }
                }

                cor_coef = new PearsonsCorrelation().correlation(v1_intensity, v2_intensity);
            } catch (Exception ex) {
                Logger.getLogger(PearsonCorrelation.class.getName()).log(Level.SEVERE, null, ex);

            }

        }
        return cor_coef;
    }
}

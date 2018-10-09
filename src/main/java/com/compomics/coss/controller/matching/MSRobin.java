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
 * This class calculates cumulative binominal probability based scores with
 * considering intensities from experimental spectra to
 * cumulativeBinomialProbability these.
 *
 * n: number of matched peaks
 *
 * N: number of peaks of a spectrum with a bigger peak list
 *
 * p: probability,topN/windowSize from a Filter object. Note that topN is [1-10]
 *
 * Note that cumulative binominal probability function calculates the score as
 * inclusive (not exclusive)
 *
 * Intensity_part(IP): [(0.5*(Explained_Intensities A/All_Intensities
 * A))+(0.5*(Explained_Intensities B/All_intensitiesB))]
 *
 * For each filtered peakList with a given topN parameter, p is calculated as
 * explained and cumulative binominal probability based scoring function part is
 * calculated as:
 *
 * Probability_Part (PP) = -10*[-log(P)]
 *
 * Later on, intensity part is introduced as with two options:
 *
 * (option0) Final_score = PP*Sqrt(IP)
 *
 * (option1) Final_score = PP*IP *
 *
 * @author Sule
 */
public class MSRobin extends Score {

    /**
     * @param confData
     * @param log
     */
    public MSRobin(ConfigData confData, org.apache.log4j.Logger log) {
        super(confData,log);

    }

    

    /**
     * To calculate CumulativeBinominalProbability with given n,N and p values.
     * n is inclusive during cumulative binominal probability function
     *
     * @return
     * @throws Exception
     */
    // @Override
    private double calculateCumulativeBinominalProbability(int N, double p) throws Exception {
        double probability = 0;
        for (int k = matchedNumPeaks; k < N + 1; k++) {
            double factorial_part = calculateCombination(N, k);// Num.combination(N, k);
            double tmp_probability = factorial_part * (Math.pow(p, k)) * (Math.pow((1 - p), (N - k)));
            probability += tmp_probability;
        }
        return probability;
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
        double probability = (double) topN / (double) confData.getMassWindow();

        // double[] results;
        Map<String, ArrayList<Peak>> map = new TreeMap<>();
        int totalN = 0;
        ArrayList<Peak> mPeaksExp;
        ArrayList<Peak> mPeaksLib;
        if (lenB < lenA) {
            map = prepareData(expSpec, libSpec);
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks1");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks2");
            totalN = lenA;

        } else {
            
            double temp = sumTotalIntExp;//swap value if order if spetrua given is reversed
            sumTotalIntExp = sumTotalIntLib;
            sumTotalIntLib = temp;            
            
            map = prepareData(libSpec, expSpec);
            totalN = lenB;
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks2");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks1");
            
        }

        matchedNumPeaks = mPeaksExp.size();

        
        intensity_part = calculateIntensityPart(mPeaksExp, mPeaksLib);

        double finalScore = getfinalScore(totalN, probability, intensity_part);
        return finalScore;
    }

    private double getfinalScore(int totalN, double probability, double intensity_part) {
        double score = 0.0; // cumulative bionominal probability based score for given N,n,p.

        try {
            double probability_part = calculateCumulativeBinominalProbability(totalN, probability);
            switch (confData.getMsRobinOption()) {
                case 0:
                    probability_part = -10 * (Math.log10(probability_part));
                    intensity_part = (Math.sqrt(intensity_part));
                    score = probability_part * intensity_part;
                    break;
                case 1:
                    probability_part = -10 * (Math.log10(probability_part));
                    score = probability_part * intensity_part;
                    break;
                case 2:
                    probability_part = -(Math.log10(probability_part));
                    score = probability_part * intensity_part;
                    break;
                case 3:
                    // only probability
                    score = -10 * (Math.log10(probability_part));
                    break;
                case 4:
                    // only probability
                    score = (1 - probability_part) * intensity_part;
                    break;
                default:
                    break;
            }

            score += 0; // just making sure the value would not be negative zero           

        } catch (Exception ex) {
            
            Logger.getLogger(MSRobin.class.getName()).log(Level.SEVERE, null, ex);
        }

        return score;

    }

    private double calculateIntensityPart(ArrayList<Peak> mPeaksExp, ArrayList<Peak> mPeaksLib) {
        double int_part = 0;
        double alpha_alpha = 0,
                beta_beta = 0,
                alpha_beta = 0;
        for (int k = 0; k < matchedNumPeaks; k++) {
            alpha_alpha += mPeaksExp.get(k).getIntensity() * mPeaksExp.get(k).getIntensity();
            beta_beta += mPeaksLib.get(k).getIntensity() * mPeaksLib.get(k).getIntensity();
            alpha_beta += mPeaksExp.get(k).getIntensity() * mPeaksLib.get(k).getIntensity();

            sumMatchedIntExp += mPeaksExp.get(k).getIntensity();
            sumMatchedIntLib += mPeaksLib.get(k).getIntensity();
            
        }

        if (sumTotalIntExp == 0 || sumTotalIntLib == 0) {
            return 0;
        }
        double tmp_part_1 = sumMatchedIntExp / sumTotalIntExp,
                tmp_part_2 = sumMatchedIntLib / sumTotalIntLib;
       

        switch (confData.getIntensityOption()) {
            case 0:
                int_part = (0.5 * tmp_part_1) + (0.5 * tmp_part_2);
                break;
            case 1:
                int_part = tmp_part_1 * tmp_part_2;
                break;
            case 2:
                int_part = Math.pow(10, (1 - (tmp_part_1 * tmp_part_2)));
                break;
            case 3:if(matchedNumPeaks!=0){
                int_part = alpha_beta / (Math.sqrt(alpha_alpha * beta_beta));
            }
            break;
            default:
                break;
        }
        return int_part;
    }

    public static long calculateCombination(int n, int r) throws Exception {
        long score = 0;
        if (r == 0) {
            score = 1;
        }
        if (n >= r) {
            double upper = 1,
                    lower = 1;
            for (int i = n; i > n - r; i--) {
                upper = upper * i;
            }
            for (int i = r; i > 1; i--) {
                lower = lower * i;
            }
            score = (long) (upper / lower);
        } else {
            throw new Exception("Error! n >= r");
        }
        return score;

    }

}

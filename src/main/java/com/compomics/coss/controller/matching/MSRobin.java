package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.model.Peak;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Genet
 */
public class MSRobin extends Score {

    Map<String, ArrayList<Peak>> map = new HashMap<>(1000);
    ArrayList<Peak> mPeaksExp;
    ArrayList<Peak> mPeaksLib;

    /**
     * @param confData
     * @param log
     */
    public MSRobin(ConfigData confData, org.apache.log4j.Logger log) {
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

        double finalScore = 0;
        double intensity_part = 0;
        double probability = (double) topN / (double) confData.getMassWindow();

        synchronized (this) {
            int totalN = 0;
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
            sumMatchedIntExp = getSumIntensity(mPeaksExp);
            sumMatchedIntLib = getSumIntensity(mPeaksLib);

            intensity_part = calculateIntensityPart(mPeaksExp, mPeaksLib);
            finalScore = getfinalScore(totalN, probability, intensity_part);

        }

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
                    //
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
            //division by sumIntensities: normalizing peaks
            alpha_alpha += (mPeaksExp.get(k).getIntensity() * mPeaksExp.get(k).getIntensity());
            beta_beta += (mPeaksLib.get(k).getIntensity() * mPeaksLib.get(k).getIntensity());
            alpha_beta += mPeaksExp.get(k).getIntensity() * mPeaksLib.get(k).getIntensity();

        }

        if (matchedNumPeaks != 0) {
            int_part = alpha_beta / (Math.sqrt(alpha_alpha * beta_beta));
        }

        return int_part;
    }

}

package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.Peak;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class CosineSimilarity extends Score {

    /**
     * @param confData
     * @param log
     */
    public CosineSimilarity(ConfigData confData, org.apache.log4j.Logger log) {
        super(confData, log);

    }

    /**
     *
     * @param lenA number of peaks in experimental spectrum
     * @param lenB number of peaks in library spectrum
     * @param topN Top intense peaks selected from each spectrum
     * @return returns intScore of the comparison
     */
    @Override
    public double calculateScore(ArrayList<Peak> expSpec, ArrayList<Peak> libSpec, int lenA, int lenB, int topN) {

         double probability = (double) topN / (double) confData.getMassWindow();
         int totalN=0;
        Map<String, ArrayList<Peak>> map = new TreeMap<>();
        ArrayList<Peak> mPeaksExp;
        ArrayList<Peak> mPeaksLib;
        if (lenB < lenA) {
            map = prepareData(expSpec, libSpec);
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks1");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks2");
            totalN=lenA;

        } else {

            double temp = sumFilteredIntExp;//swap value if order if spetrua given is reversed
            sumFilteredIntExp = sumFilteredIntLib;
            sumFilteredIntLib = temp;

            map = prepareData(libSpec, expSpec);
            mPeaksExp = (ArrayList< Peak>) map.get("Matched Peaks2");
            mPeaksLib = (ArrayList< Peak>) map.get("Matched Peaks1");
            totalN=lenB;

        }

        matchedNumPeaks = mPeaksExp.size();

        double intScore=-1;
        try{
            intScore=cosineScore(mPeaksExp, mPeaksLib);// * (mPeaksExp.size()/(double)lenA);
        }catch(ArithmeticException ex){
            System.out.println(ex.toString());
        }
        
        double probability_part=0;
        try {
            probability_part = calculateCumulativeBinominalProbability(totalN, probability);
        } catch (Exception ex) {
            Logger.getLogger(CosineSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        double log_probability = -10 * (Math.log10(probability_part));
                     
        double finalScore = intScore * log_probability ;  //score controlled by probability of mathced peaks
       // double finalScore = intScore * matchedNumPeaks;// (matchedNumPeaks/filteredNumPeaksExp); //score controlled by number of matched peaks
        
        return (finalScore);// (intScore);
    }

    private double cosineScore(List<Peak> v1, List<Peak> v2) { // parameters vector1 and vector2
        double score = -1;
        if (matchedNumPeaks != 0) {
            double productSum = 0;
            double v1SquareSum = 0;
            double v2SquareSum = 0;

            for (int a = 0; a < matchedNumPeaks; a++) {
                productSum += v1.get(a).getIntensity() * v2.get(a).getIntensity(); //summation(vector1*vector2)
                v1SquareSum += v1.get(a).getIntensity() * v1.get(a).getIntensity();//summation of squares of vector1
                v2SquareSum += v2.get(a).getIntensity() * v2.get(a).getIntensity();// summation of squares of vector2

                sumMatchedIntExp += v1.get(a).getIntensity();
                sumMatchedIntLib += v2.get(a).getIntensity();
            }

            double sqrtV1 = Math.sqrt(v1SquareSum);
            double sqrtV2 = Math.sqrt(v2SquareSum);
            score = productSum / (sqrtV1 * sqrtV2);
            //score*= (matchedNumPeaks*matchedNumPeaks);
        }
        return score;
    }

}

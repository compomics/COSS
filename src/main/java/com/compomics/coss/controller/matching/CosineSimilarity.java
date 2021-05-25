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
        sumMatchedIntExp = getSumIntensity(mPeaksExp);
        sumMatchedIntLib =getSumIntensity(mPeaksLib);

        double intScore=-1;
        try{
            intScore=cosineScore(mPeaksExp, mPeaksLib);// * (mPeaksExp.size()/(double)lenA);
            
        }catch(ArithmeticException ex){
            System.out.println(ex.toString());
        }
        
       
        //double finalScore=intScore*matchedNumPeaks;        
        double finalScore= matchedNumPeaks;   //removed intscore for glycan
        return finalScore;
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
            }

            double sqrtV1 = Math.sqrt(v1SquareSum);
            double sqrtV2 = Math.sqrt(v2SquareSum);
            score = productSum / (sqrtV1 * sqrtV2);

        }
        return score;
    }

   

}

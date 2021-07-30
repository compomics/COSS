package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.model.Peak;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Genet
 */
public class DotProduct extends Score{
    Map<String, ArrayList<Peak>> map = new HashMap<>(1000);
    ArrayList<Peak> mPeaksExp;
    ArrayList<Peak> mPeaksLib;

    /**
     * @param confData
     * @param log
     */
    public DotProduct(ConfigData confData, org.apache.log4j.Logger log) {
        super(confData, log);

    }

    /**
     *
     * @param topN Top intense peaks selected from each spectrum
     * @return returns score of the comparison
     */
    @Override
    public double calculateScore(ArrayList<Peak> expSpec, ArrayList<Peak> libSpec,int topN, int transform) {

        double score = 0;
        
        synchronized (this) {
            
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
            mPeaksExp = normalizePeaks(mPeaksExp);
            mPeaksLib = normalizePeaks(mPeaksLib);    
            
            score = dotProduct(normalizePeaks(mPeaksExp), normalizePeaks(mPeaksLib));

        }
        return score;
    }

    private double dotProduct(List<Peak> v1, List<Peak> v2) { // parameters vector1 and vector2
        double productSum = 0;
        if (matchedNumPeaks != 0) {           

            for (int a = 0; a < matchedNumPeaks; a++) {
                productSum += v1.get(a).getIntensity() * v2.get(a).getIntensity(); //summation(vector1*vector2)             
            }
        }
        return productSum;
    }
    
}

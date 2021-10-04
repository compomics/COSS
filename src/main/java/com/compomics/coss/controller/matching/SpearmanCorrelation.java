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
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;


/**
 *
 * @author Genet
 */
public class SpearmanCorrelation extends Score{
       /**
     * @param confData
     * @param log
     */
    public SpearmanCorrelation(ConfigData confData, org.apache.log4j.Logger log) {
        super(confData, log);

    }

    /**
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

        double score=-1;
        try{
            score=spearmanCorrelation(normalizePeaks(mPeaksExp), normalizePeaks(mPeaksLib));// * (mPeaksExp.size()/(double)lenA);
        }catch(ArithmeticException ex){
            System.out.println(ex.toString());
        }                   
        return score;
    }

    private double spearmanCorrelation(List<Peak> v1, List<Peak> v2) { // parameters vector1 and vector2
        double cor_coef = -1;
        if (matchedNumPeaks >= 2) {
            double[] v1_intensity=new double[matchedNumPeaks];
            double[] v2_intensity=new double[matchedNumPeaks];
            
            for(int i=0;i<matchedNumPeaks; i++){
                v1_intensity[i]= v1.get(i).getIntensity();
                v2_intensity[i]= v2.get(i).getIntensity();
                
            }
            cor_coef=new SpearmansCorrelation().correlation(v1_intensity, v2_intensity);
        }
        return cor_coef;
    }
    
}

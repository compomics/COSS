package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.model.Peak;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Sule
 */
public abstract class Score {

    protected double sumMatchedIntExp;
    protected double sumMatchedIntLib;
    protected double sumFilteredIntExp;
    protected double sumFilteredIntLib;
    protected int matchedNumPeaks;
    protected int filteredNumPeaksExp;
    protected int filteredNumPeaksLib;

    protected final ConfigData confData;
    protected final org.apache.log4j.Logger log;

    public Score(ConfigData confData, org.apache.log4j.Logger lg) {
        this.confData = confData;
        this.log = lg;

    }

    // public abstract void InpArgs(java.lang.String... args);
    /**
     * To calculate a score
     *
     * @param expSpec
     * @param libSpec
     * @param lenExp
     * @param lenLib
     * @param topN
     * @return score
     */
    public abstract double calculateScore(ArrayList<Peak> expSpec, ArrayList<Peak> libSpec, int lenExp, int lenLib, int topN);

    protected Map prepareData(ArrayList<Peak> filteredExpMS2_1, ArrayList<Peak> filteredExpMS2_2) {

        return (new MatchedPeaks()).getMatchedPeaks(filteredExpMS2_1, filteredExpMS2_2, confData.getfragTol());

    }
    
    /**
     * calculate and return sum of intensities of the peaks in the given spectrum
     * @param mPeaksExp
     * @return 
     */
    protected double getSumIntensity(ArrayList<Peak> mPeaksExp){
        
        double sum=0;
        for(Peak p : mPeaksExp){
            sum+=p.getIntensity();
        }
       return sum;        
    }


    /**
     * To calculate CumulativeBinominalProbability with given n,N and p values.
     * n is inclusive during cumulative binominal probability function
     *
     * @return
     * @throws Exception
     */
    // @Override
    protected double calculateCumulativeBinominalProbability(int N, double p) throws Exception {
        double probability = 0;
        for (int k = matchedNumPeaks; k < N + 1; k++) {
            double factorial_part = calculateCombination(N, k);// Num.combination(N, k);
            double tmp_probability = factorial_part * (Math.pow(p, k)) * (Math.pow((1 - p), (N - k)));
            probability += tmp_probability;
        }
        return probability;
    }

    protected static long calculateCombination(int n, int r) throws Exception {
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

    public void setSumMatchedIntExp(double sumMInt) {
        this.sumMatchedIntExp = sumMInt;
    }

    public void setSumMatchedIntLib(double sumMInt) {
        this.sumMatchedIntLib = sumMInt;
    }

    public double getSumMatchedIntExp() {
        return this.sumMatchedIntExp;
    }

    public double getSumMatchedIntLib() {
        return this.sumMatchedIntLib;
    }

    public void setSumFilteredIntExp(double sumTotalInt) {
        this.sumFilteredIntExp = sumTotalInt;
    }

    public void setSumFilteredIntLib(double sumTotalInt) {
        this.sumFilteredIntLib = sumTotalInt;
    }

    public double getSumFilteredIntExp() {
        return this.sumFilteredIntExp;
    }

    public double getSumFilteredIntLib() {
        return this.sumFilteredIntLib;
    }

    public int getNumMatchedPeaks() {
        return this.matchedNumPeaks;
    }

    public void setNumMatchedPeaks(int numMatchedPeaks) {
        this.matchedNumPeaks = numMatchedPeaks;
    }
    
    public int getNumFilteredPeaksExp() {
        return this.filteredNumPeaksExp;
    }

    public void setNumFilteredPeaksExp(int numFilteredPeaks) {
        this.filteredNumPeaksExp = numFilteredPeaks;
    }
    
    public int getNumFilteredPeaksLib() {
        return this.filteredNumPeaksLib;
    }

    public void setNumFilteredPeaksLib(int numFilteredPeaks) {
        this.filteredNumPeaksLib = numFilteredPeaks;
    }

}

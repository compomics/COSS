package com.compomics.coss.controller.matching;

import com.compomics.coss.model.ConfigData;
import com.compomics.ms2io.Peak;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Sule
 */
public abstract class Score {

    protected double sumMatchedIntExp;
    protected double sumMatchedIntLib;
    protected double sumTotalIntExp;
    protected double sumTotalIntLib;
    protected int matchedNumPeaks;

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

    protected double calculateTotalIntensity(ArrayList<Peak> peaks) {
        double sum = 0;
//        for(Peak p:peaks){
//            sum+=p.getIntensity();
//        }
        sum = peaks.stream().map((p) -> p.getIntensity()).reduce(sum, (accumulator, _item) -> accumulator + _item);

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

    public void setSumTotalIntExp(double sumTotalInt) {
        this.sumTotalIntExp = sumTotalInt;
    }

    public void setSumTotalIntLib(double sumTotalInt) {
        this.sumTotalIntLib = sumTotalInt;
    }

    public double getSumTotalIntExp() {
        return this.sumTotalIntExp;
    }

    public double getSumTotalIntLib() {
        return this.sumTotalIntLib;
    }

    public int getNumMatchedPeaks() {
        return this.matchedNumPeaks;
    }

    public void setNumMatchedPeaks(int numMatchedPeaks) {
        this.matchedNumPeaks = numMatchedPeaks;
    }

}

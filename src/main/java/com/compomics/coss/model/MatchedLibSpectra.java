package com.compomics.coss.model;

import com.compomics.ms2io.model.Spectrum;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Genet
 */
public class MatchedLibSpectra implements Comparable<MatchedLibSpectra>, Serializable {

    public MatchedLibSpectra() {
    }

    // private static MatchedLibSpectra this_obj;
    Spectrum spectrum;
    double score;
    double score_cosinesim;
    double score_mse_int;
    double score_mse_mz;
    double corr_spearman;
    double corr_pearson;
    double corr_pearson_log2;
    double score_dotproduct;
    
    int source;
    String sequence;

    int totalFilteredNumPeaks_query = 0;
    int totalFilteredNumPeaks_lib = 0;
    double totalFilteredInt_query = 0;
    double totalFilteredInt_lib = 0;

    int numMatchedPeaks;

    double sumMatchedIntA;
    double sumMatchedIntB;

//    public void finalyze(){
//        this_obj=this;
//    }
    public Spectrum getSpectrum() {
        return this.spectrum;
    }

    public void setSpectrum(Spectrum spec) {
        this.spectrum = spec;
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }

//Additional scores
    public double getScore_cosinesim() {
        return this.score_cosinesim;
    }

    public void setScore_cosinesim(double score) {
        this.score_cosinesim = score;
    }

    public double getScore_mse_int() {
        return this.score_mse_int;
    }

    public void setScore_mse_int(double score) {
        this.score_mse_int = score;
    }
    
    public double getScore_mse_mz() {
        return this.score_mse_mz;
    }

    public void setScore_mse_mz(double score) {
        this.score_mse_mz = score;
    }
    
    public double getCorrelation_pearson() {
        return this.corr_pearson;
    }

    public void setCorrelation_pearson(double score) {
        this.corr_pearson = score;
    }
    
    
    public double getCorrelation_pearson_log2() {
        return this.corr_pearson_log2;
    }

    public void setCorrelation_pearson_log2(double score) {
        this.corr_pearson_log2 = score;
    }
    
    
    public double getCorrelation_spearman() {
        return this.corr_spearman;
    }

    public void setCorrelation_spearman(double score) {
        this.corr_spearman = score;
    }

    public int getSource() {
        return this.source;
    }

    public void setSource(int src) {
        this.source = src;
    }

    public String getSequence() {
        return this.sequence;
    }

    public void setSequence(String seq) {
        this.sequence = seq;
    }

    public void setSumMatchedInt_Exp(double n) {
        this.sumMatchedIntA = n;
    }

    public double getSumMatchedInt_Exp() {
        return this.sumMatchedIntA;
    }

    public void setSumMatchedInt_Lib(double n) {
        this.sumMatchedIntB = n;
    }

    public double getSumMatchedInt_Lib() {
        return this.sumMatchedIntB;
    }

    public void setNumMathcedPeaks(int n) {
        this.numMatchedPeaks = n;
    }

    public int getNumMatchedPeaks() {
        return this.numMatchedPeaks;
    }

    public void settotalFilteredNumPeaks_Exp(int n) {
        this.totalFilteredNumPeaks_query = n;
    }

    public int getTotalFilteredNumPeaks_Exp() {
        return this.totalFilteredNumPeaks_query;
    }

    public void settotalFilteredNumPeaks_Lib(int n) {
        this.totalFilteredNumPeaks_lib = n;
    }

    public int getTotalFilteredNumPeaks_Lib() {
        return this.totalFilteredNumPeaks_lib;
    }

    public void setSumFilteredIntensity_Exp(double d) {
        this.totalFilteredInt_query = d;
    }

    public double getSumFilteredIntensity_Exp() {
        return this.totalFilteredInt_query;
    }

    public void setSumFilteredIntensity_Lib(double d) {
        this.totalFilteredInt_lib = d;
    }

    public double getSumFilteredIntensity_Lib() {
        return this.totalFilteredInt_lib;
    }

    @Override
    public int compareTo(MatchedLibSpectra t) {

        BigDecimal bd1 = BigDecimal.valueOf(this.score);
        BigDecimal bd2 = BigDecimal.valueOf(t.score);
        int isEqual = bd1.compareTo(bd2);
        return isEqual;

    }

}

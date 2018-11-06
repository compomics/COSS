/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.model;

import com.compomics.ms2io.Spectrum;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Genet
 */
public class MatchedLibSpectra implements Comparable<MatchedLibSpectra>, Serializable {

    public MatchedLibSpectra() {
    }
    
    private Spectrum spectrum;
    private double score;
    private int source;
    private String sequence;

    private int totalFilteredNumPeaks_query = 0;
    private int totalFilteredNumPeaks_lib = 0;
    private double totalFilteredInt_query = 0;
    private double totalFilteredInt_lib = 0;

    int numMatchedPeaks;

    double sumMatchedIntA ;
    double sumMatchedIntB ;

    
     public Spectrum getSpectrum() {
        return this.spectrum;
    }

    public void setSpectrum(Spectrum spec) {
        this.spectrum = spec;
    }
    
    public double getScore(){
        return this.score;
    }
    
    public void setScore(double score){
        this.score=score;
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
        
        BigDecimal bd1=BigDecimal.valueOf(this.score);
        BigDecimal bd2=BigDecimal.valueOf(t.score);       
        int isEqual=bd1.compareTo(bd2);
        return isEqual;
        
    }

   
}

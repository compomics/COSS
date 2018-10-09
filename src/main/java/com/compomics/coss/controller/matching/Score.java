/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller.matching;

import com.compomics.coss.controller.UpdateListener;
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
     

    public Score(ConfigData confData, org.apache.log4j.Logger log) {
        this.confData=confData;     
        this.log=log;

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
            
        return (new MatchedPeaks()).getMatchedPeaks(filteredExpMS2_1, filteredExpMS2_2, confData.getfragTol()) ;
      
    }
    
    protected double getTotalIntensity(ArrayList<Peak> peaks){
        double sum=0;
        sum = peaks.stream().map((p) -> p.getIntensity()).reduce(sum, (accumulator, _item) -> accumulator + _item);
        
        return sum;
        
    }
   
    
    public void setSumMatchedIntExp(double sumMInt){
        this.sumMatchedIntExp=sumMInt;
    }
    
    public void setSumMatchedIntLib(double sumMInt){
        this.sumMatchedIntLib=sumMInt;
    }
    
    public double getSumMatchedIntExp(){
        return this.sumMatchedIntExp;
    }
    
    public double getSumMatchedIntLib(){
        return this.sumMatchedIntLib;
    }   
    
    
    public void setSumTotalIntExp(double sumTotalInt){
        this.sumTotalIntExp=sumTotalInt;
    }
    
    public void setSumTotalIntLib(double sumTotalInt){
        this.sumTotalIntLib=sumTotalInt;
    }
    
    public double getSumTotalIntExp(){
        return this.sumTotalIntExp;
    }
    
    public double getSumTotalIntLib(){
        return this.sumTotalIntLib;
    }
    
    public int getNumMatchedPeaks(){
        return this.matchedNumPeaks;
    }
    
    public void setNumMatchedPeaks(int numMatchedPeaks){
        this.matchedNumPeaks=numMatchedPeaks;
    }
    
   
}

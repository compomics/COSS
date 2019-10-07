///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.compomics.coss.controller.matching;
//
//import com.compomics.coss.controller.featureExtraction.DivideAndTopNPeaks;
//import com.compomics.coss.model.ComparisonResult;
//import com.compomics.coss.model.MatchedLibSpectra;
//import com.compomics.ms2io.model.Peak;
//import com.compomics.ms2io.model.Spectrum;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// *
// * @author Genet
// */
//public class MatcherOnly_Thread implements Callable<ComparisonResult> {
//
//    ArrayList libSpec;
//    Spectrum expSpec;
//    int massWindow;
//    Score algorithm;
//    org.apache.log4j.Logger log;
//
//    public MatcherOnly_Thread(Score al, Spectrum spec, ArrayList<Spectrum> libSpecs, int windowSize, org.apache.log4j.Logger lg) {
//        this.log = lg;
//        this.algorithm = al;
//        this.expSpec = spec;
//        this.libSpec = libSpecs;
//        this.massWindow = windowSize;
//
//    }
//
//    @Override
//    public ComparisonResult call() throws Exception {
//        MatchedLibSpectra mSpec = new MatchedLibSpectra();
//
//        int i = 0;
//        int numDecoy = 0;
//
//        ArrayList<Peak> selectedPeaks_lib = new ArrayList<>(1000);
//        ArrayList<Peak> selectedPeaks_exp = new ArrayList<>(1000);
//        DivideAndTopNPeaks obj = new DivideAndTopNPeaks(massWindow, log);
//
//        Spectrum sp2;
//        double mIntA = 0;
//        double mIntB = 0;
//        double tIntA = 0;
//        double tIntB = 0;
//        int mNumPeaks = 0;
//        int tempLenA = 0;
//        int tempLenB = 0;
//        double tempScore = -1;
//
//        int len_specLib = this.libSpec.size();
//        ArrayList<MatchedLibSpectra> specResult = new ArrayList<>();
//        specResult.ensureCapacity(len_specLib);
//        ComparisonResult compResult = new ComparisonResult();
//        
//        while (i < len_specLib) {
//            //10 score results out which the maximum is going to be taken
//            List<Double> scores = new ArrayList<>(10);
//
//            try {
//                sp2 = (Spectrum) this.libSpec.get(i);
//                if (sp2.getComment().contains("Decoy") || sp2.getProtein().contains("DECOY")) {
//                    numDecoy++;
//                }
//
//                for (int topN = 1; topN < 11; topN++) { // highest score from 1 - 10 peaks selection
//                    //int topN=10;    //only for the top 10 peaks
//
//                    selectedPeaks_lib = obj.getFeatures(sp2, topN);
//                    selectedPeaks_exp = obj.getFeatures(expSpec, topN);
//
//                    int lenA = selectedPeaks_exp.size();
//                    int lenB = selectedPeaks_lib.size();
//                    algorithm.setSumTotalIntExp(algorithm.getSumIntensity(selectedPeaks_exp));
//                    algorithm.setSumTotalIntLib(algorithm.getSumIntensity(selectedPeaks_lib));
//
//                    double score = algorithm.calculateScore(selectedPeaks_exp, selectedPeaks_lib, lenA, lenB, topN);
//
//                    if (score > tempScore) { //tempScore<score for MSRobin and cosine similarity
//                        tempScore = score;
//                        mIntA = algorithm.getSumMatchedIntExp();
//                        mIntB = algorithm.getSumMatchedIntLib();
//                        tIntA = algorithm.getSumTotalIntExp();
//                        tIntB = algorithm.getSumTotalIntLib();
//                        tempLenA = lenA;
//                        tempLenB = lenB;
//                        mNumPeaks = algorithm.getNumMatchedPeaks();
//                    }
//
//                    scores.add(score);
//
//                }
//                double finalScore = Collections.max(scores);//max for MSRobin and cosine similarity, min for MSE
//                finalScore = (double) Math.round(finalScore * 1000d) / 1000d;
//
//                mSpec.setScore(finalScore);//(Collections.max(scores));
//                mSpec.setSequence(sp2.getSequence());
//                if (sp2.getComment().contains("Decoy") || sp2.getProtein().contains("DECOY")) {
//                    mSpec.setSource(1);
//                } else {
//                    mSpec.setSource(0);
//                }
//                
//                
//                mSpec.setNumMathcedPeaks(mNumPeaks);
//                mSpec.setSpectrum(sp2);
//                mSpec.setSumFilteredIntensity_Exp(tIntA);
//                mSpec.setSumFilteredIntensity_Lib(tIntB);
//                mSpec.setSumMatchedInt_Exp(mIntA);
//                mSpec.setSumMatchedInt_Lib(mIntB);
//                mSpec.settotalFilteredNumPeaks_Exp(tempLenA);
//                mSpec.settotalFilteredNumPeaks_Lib(tempLenB);
//                specResult.add(mSpec);
//
//            } catch (Exception ex) {
//
//                Logger.getLogger(MatcherOnly_Thread.class.getName()).log(Level.SEVERE, "\n Description : " + ex);
//            }
//
//            i++;
//        }
//
//        if (!specResult.isEmpty()) {            
//            compResult.setExpSpectrum(this.);
//            Collections.sort(specResult);
//            Collections.reverse(specResult);//decending order for MSRobin and Cosine similarity, it should be in accending for MSE
//
//            //only top ten results are recorded, if existed
//            List<MatchedLibSpectra> tempMatch = new ArrayList<>(20);
//            int tempResSize = specResult.size();
//            int tempLen = 0;
//            int c = 0;
//            while (tempLen < tempResSize && tempLen < 10) {
//                tempMatch.add(specResult.get(c));
//                tempLen = tempMatch.size();
//                c++;
//            }
//
//            if (!tempMatch.isEmpty()) {
//                compResult.setMatchedLibSpec(tempMatch);
//                compResult.setTopScore(tempMatch.get(0).getScore());
//
//            }
//
//        }
//
//        return compResult;
//    }
//}

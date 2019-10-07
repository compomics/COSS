///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.compomics.coss.controller.matching;
//
//import com.compomics.coss.controller.UpdateListener;
//import com.compomics.coss.model.ConfigData;
//import com.compomics.coss.model.MatchedLibSpectra;
//import com.compomics.ms2io.model.Spectrum;
//import java.util.ArrayList;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// *
// * @author Genet
// */
//public class StartMatching {
//
//    private final ConfigData confData;
//    private final double precTolerance;
//    private boolean stillReading;
//    private boolean cancelled;
//    ArrayList<MatchedLibSpectra> specResult;
//    org.apache.log4j.Logger log;
//
//    public StartMatching(ConfigData confData, UpdateListener lstner, org.apache.log4j.Logger log) {
//        this.confData = confData;
//        this.precTolerance = confData.getPrecTol();
//        stillReading = true;
//        cancelled = false;
//        specResult = new ArrayList<>();
//        this.log=log;
//    }
//
//    public void startMatching() {
//        int numTasks = confData.getExpSpecCount();
//        Future<MatchedLibSpectra> future;
//        ExecutorService executor = Executors.newFixedThreadPool(8);
//        Score scoringAlgorithm = getScoringAlgorithm();
//
//        if (confData.getExpFileformat().equals("ms2io")) {
//
//            Spectrum expSpec;
//            ArrayList libSpec;
//            MatchedLibSpectra mSpec;
//            for (int a = 0; a < numTasks; a++) {
//
//                try {
//                    expSpec = confData.getExpSpecReader().readAt(confData.getExpSpectraIndex().get(a).getPos());
//                    double mass = expSpec.getPCMass();
//                    double da_error = mass * this.precTolerance;
//                    
//                    libSpec = confData.getLibSpecReader().readPart(mass, da_error);
//                    
//                    MatcherOnly_Thread mtch = new MatcherOnly_Thread(scoringAlgorithm,expSpec,libSpec,100,log);
//                    
//                    // getDecoy = new GetDecoySpectrum(spectrum, "");
//                    future = executor.submit(mtch);
//                    mSpec = future.get();
//                    
//                    synchronized (this) {
//                        this.specResult.add(mSpec);
//                        System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + Integer.toString(a));
//                    }
//                    
//                    if (cancelled) {
//                        break;
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(StartMatching.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (ExecutionException ex) {
//                    Logger.getLogger(StartMatching.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//            }
//        }
//    }
//
//    private Score getScoringAlgorithm() {
//        
//        Score scoreObj = null;
//        switch (confData.getScoringFunction()) {
//            case 0:
//                scoreObj = new MSRobin(this.confData, this.log);
//                //scoreObj = new TestScore(confData, log);
//                break;
//            case 1:
//                scoreObj = new CosineSimilarity(this.confData, this.log);
//                break;
//            case 2:
//                scoreObj = new MeanSquareError(this.confData, this.log);
//                break;
//            default:
//                scoreObj = new MSRobin(this.confData, this.log);
//                break;
//
//        }
//        
//        return  scoreObj;       
//        
//    }
//
//}

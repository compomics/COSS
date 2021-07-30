package com.compomics.coss.controller.matching;

import com.compomics.coss.model.TheDataUnderComparison;
import com.compomics.coss.controller.Dispatcher;
import com.compomics.coss.controller.UpdateListener;
import com.compomics.coss.controller.featureExtraction.DivideAndTopNPeaks;
import com.compomics.coss.controller.preprocessing.Transformation.Normalize;
import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.ConfigData;
import com.compomics.coss.model.MatchedLibSpectra;
import com.compomics.ms2io.model.Peak;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.compomics.ms2io.model.Spectrum;

/**
 *
 * @author Genet
 */
public class Matcher implements Callable<List<ComparisonResult>> {

    final ConfigData confData;
    final TheDataUnderComparison data;
    final DataProducer producer;
    final org.apache.log4j.Logger log;
    final Score algorithm;
    final UpdateListener listener;
    boolean cancelled;
    Score matcher_cosinesim;
    Score matcher_mse_intensity;
    Score matcher_mse_mz;
//    Score matcher_dotproduct;
    Score pearson_correlaion;
    Score spearman_correlaion;
    
    

    public Matcher(Score al, DataProducer dp, TheDataUnderComparison dt, ConfigData cfd, UpdateListener lsr, org.apache.log4j.Logger lg) {
        this.algorithm = al;
        this.producer = dp;
        this.data = dt;
        this.confData = cfd;
        this.listener = lsr;
        this.log = lg;
        cancelled = false;

    }

    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public List<ComparisonResult> call() {

        ObjectOutputStream oos = null;
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream("temp.txt");
            oos = new ObjectOutputStream(fos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Additional similarity measures for matching spectra used in percolator
        matcher_cosinesim=new CosineSimilarity(confData, log);
        matcher_mse_intensity=new Intensity_MSE(confData, log);
        matcher_mse_mz=new MZ_MSE(confData, log);
//        matcher_dotproduct=new DotProduct(confData, log);
        pearson_correlaion =new PearsonCorrelation(confData, log);
        spearman_correlaion=new SpearmanCorrelation(confData, log);
        Normalize normalize = new Normalize();
        double score_cosinesim=0;
        double score_mse_int=0;
        double score_mse_mz=0;
        double corr_pearson=0;
        double corr_pearson_log2=0;
        double corr_spearman=0;
        double score_main=0;
        
        int taskCompleted = 0;
        int numDecoy = 0;
        int massWindow = confData.getMassWindow();
        double percent = 100.0 / (double) confData.getExpSpecCount();

        int listSize = confData.getSpectraLibraryIndex().size() / 4;
        ArrayList<MatchedLibSpectra> specResult = new ArrayList<>();
        List<MatchedLibSpectra> tempMatch = new ArrayList<>(20);

        ArrayList<Peak> selectedPeaks_lib = new ArrayList<>(1000);
        ArrayList<Peak> selectedPeaks_exp = new ArrayList<>(1000);

        Spectrum sp1;
        ArrayList sb;

        double mIntA ;
        double mIntB;
        double tIntA;
        double tIntB;
        int mNumPeaks;
        int tempLenA;
        int tempLenB;
        double tempScore;
        
        
        DivideAndTopNPeaks obj = new DivideAndTopNPeaks(massWindow, log);
         List<ComparisonResult> simResult = null;
            if (confData.getExpSpectraIndex() != null) {
                simResult = new ArrayList<>(confData.getExpSpectraIndex().size());
            } else if (confData.getEbiReader() != null) {
                simResult = new ArrayList<>(confData.getEbiReader().getSpectraCount());
           }
        while (this.producer.isReading() || (!data.getExpSpec().isEmpty() && !data.getLibSelectedSpec().isEmpty())) {
            try {

                if (data.getExpSpec().isEmpty() || data.getLibSelectedSpec().isEmpty()) {

                    continue;
                }

                synchronized (data) {

                    sp1 = data.pollExpSpec();
                    sb = data.pollLibSpec();
                }

                InnerIteratorSync<Spectrum> iteratorSpectra = new InnerIteratorSync(sb.iterator());
                specResult.ensureCapacity(listSize);

                while (iteratorSpectra.iter.hasNext()) {
                    //10 score_main results out which the maximum is going to be taken
                    List<Double> scores = new ArrayList<>(10);

                    try {
                        Spectrum sp2 = (Spectrum) iteratorSpectra.iter.next();
                        if (sp2.getComment().contains("Decoy") || sp2.getProtein().contains("DECOY") || sp2.getComment().contains("DECOY")) {
                            numDecoy++;
                        }

                        mIntA = 0;
                        mIntB = 0;
                        tIntA = 0;
                        tIntB = 0;
                        mNumPeaks = 0;
                        tempLenA = 0;
                        tempLenB = 0;
                        tempScore = -1;
                        
//                        sp1 = normalize.transform(sp1);
//                        sp2 = normalize.transform(sp2);

                        for (int topN = 1; topN < 11; topN++) { // highest score_main from 1 - 10 peaks selection
                            //int topN=10;    //only for the top 10 peaks

                            selectedPeaks_lib = obj.getFeatures(sp2, topN);
                            selectedPeaks_exp = obj.getFeatures(sp1, topN);

                            
                            algorithm.setSumTotalIntExp(algorithm.getSumIntensity(selectedPeaks_exp));
                            algorithm.setSumTotalIntLib(algorithm.getSumIntensity(selectedPeaks_lib));

                            score_main = algorithm.calculateScore(selectedPeaks_exp, selectedPeaks_lib, topN,0);
                            
                            if (score_main > tempScore) { //tempScore<score for MSRobin and cosine similarity
                                
                                score_cosinesim= matcher_cosinesim.calculateScore(selectedPeaks_exp, selectedPeaks_lib, topN,0);
                                score_mse_int=matcher_mse_intensity.calculateScore(selectedPeaks_exp, selectedPeaks_lib, topN,0);
                                score_mse_mz=matcher_mse_mz.calculateScore(selectedPeaks_exp, selectedPeaks_lib, topN,0);
                                corr_pearson=pearson_correlaion.calculateScore(selectedPeaks_exp, selectedPeaks_lib, topN,0);
                                corr_spearman=spearman_correlaion.calculateScore(selectedPeaks_exp, selectedPeaks_lib, topN,0);
                                corr_pearson_log2=pearson_correlaion.calculateScore(selectedPeaks_exp, selectedPeaks_lib, topN, 2);
                                
                                
                                
                                
                                
                                mIntA = algorithm.getSumMatchedIntExp();
                                mIntB = algorithm.getSumMatchedIntLib();
                                tIntA = algorithm.getSumTotalIntExp();
                                tIntB = algorithm.getSumTotalIntLib();
                                tempLenA = selectedPeaks_exp.size();
                                tempLenB = selectedPeaks_lib.size();
                                mNumPeaks = algorithm.getNumMatchedPeaks();
                            }
                            scores.add(score_main);
                        }
                        double finalScore = Collections.max(scores);//max for MSRobin and cosine similarity, min for MSE
                        finalScore = (double) Math.round(finalScore * 10000d) / 10000d;
                        score_cosinesim = (double) Math.round(score_cosinesim * 10000d) / 10000d;
                        score_mse_int = (double) Math.round(score_mse_int * 10000d) / 10000d;
                        score_mse_mz = (double) Math.round(score_mse_mz * 10000d) / 10000d;
                        corr_spearman = (double) Math.round(corr_spearman * 10000d) / 10000d;
                        corr_pearson = (double) Math.round(corr_pearson * 10000d) / 10000d;
                        corr_pearson_log2 = (double) Math.round(corr_pearson_log2 * 10000d) / 10000d;
                        
                        
                        MatchedLibSpectra mSpec = new MatchedLibSpectra();
                        
                        mSpec.setScore(finalScore);
                        
                        //additional scores added for percolator
                        mSpec.setScore_cosinesim(score_cosinesim);
                        mSpec.setScore_mse_int(score_mse_int);
                        mSpec.setScore_mse_mz(score_mse_mz);
                        mSpec.setCorrelation_spearman(corr_spearman);
                        mSpec.setCorrelation_pearson(corr_pearson);
                        mSpec.setCorrelation_pearson_log2(corr_pearson_log2);
                        
                        mSpec.setSequence(sp2.getSequence());
                        if (sp2.getComment().contains("Decoy") || sp2.getProtein().contains("DECOY")) {
                            mSpec.setSource(0);
                        } else {
                            mSpec.setSource(1);
                        }
                        mSpec.setNumMathcedPeaks(mNumPeaks);
                        mSpec.setSpectrum(sp2);
                        mSpec.setSumFilteredIntensity_Exp(tIntA);
                        mSpec.setSumFilteredIntensity_Lib(tIntB);
                        mSpec.setSumMatchedInt_Exp(mIntA);
                        mSpec.setSumMatchedInt_Lib(mIntB);
                        mSpec.settotalFilteredNumPeaks_Exp(tempLenA);
                        mSpec.settotalFilteredNumPeaks_Lib(tempLenB);
                        specResult.add(mSpec);

                        // }
                    } catch (Exception ex) {

                        Logger.getLogger(Matcher.class.getName()).log(Level.SEVERE, "\n Description : " + ex);
                    }

                }

                ++taskCompleted;
                listener.updateprogress(taskCompleted, percent);

                if (!specResult.isEmpty()) {
                    ComparisonResult compResult = new ComparisonResult();
                    compResult.setExpSpectrum(sp1);
                    Collections.sort(specResult);
                    Collections.reverse(specResult);//decending order for MSRobin and Cosine similarity, it should be in accending for MSE

                    //only top ten results are recorded, if existed
                    //List<MatchedLibSpectra> tempMatch = new ArrayList<>(20);
                    int tempResSize = specResult.size();
                    int tempLen = 0;
                    int c = 0;
                    while (tempLen < tempResSize && tempLen < 10) {
                        tempMatch.add(specResult.get(c));
                        tempLen = tempMatch.size();
                        c++;
                    }

                    if (!tempMatch.isEmpty()) {
                        compResult.setMatchedLibSpec(new ArrayList<>(tempMatch));
                        compResult.setTopScore(tempMatch.get(0).getScore());
                        simResult.add(compResult);  

//                        oos.writeObject(compResult);
//                        oos.flush();

                        tempMatch.clear();
                        specResult.clear();
                        compResult=null;

                    }
                }

            } catch (Exception ex) {//InterruptedException | IOException ex) {
                Logger.getLogger(Matcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (cancelled) {
                break;
            }

        }

//        try {
//            if (oos != null) {
//                oos.close();
//            }
//            if (fos != null) {
//                fos.close();
//            }
//
//        } catch (IOException ex) {
//            Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, "closing file writing" + ex);
//        }
//
//        List<ComparisonResult> simResult = null;
        if (!cancelled && !this.producer.isCancelled()) {
            System.out.print("\b\b\b\b\b\b search completed \n");

            confData.setDecoyAvailability(false);
            if (numDecoy != 0) {
                confData.setDecoyAvailability(true);
            }

//            if (confData.getExpSpectraIndex() != null) {
//                simResult = new ArrayList<>(confData.getExpSpectraIndex().size());
//            } else if (confData.getEbiReader() != null) {
//                simResult = new ArrayList<>(confData.getEbiReader().getSpectraCount());
//            }
            //  listener.updateprogress(confData.getExpSpectraIndex().size());

//            log.info("Getting results.");
//
//            FileInputStream fis = null;
//            ObjectInputStream ois = null;
//
//            try {
//                fis = new FileInputStream("temp.txt");
//                ois = new ObjectInputStream(fis);
//                ComparisonResult r = (ComparisonResult) ois.readObject();
//                while (r != null) {
//                    r = (ComparisonResult) ois.readObject();
//                    simResult.add(r);
//                }
//            } catch (FileNotFoundException | ClassNotFoundException ex) {
//                Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, "opening file for reading result" + ex);
//            } catch (EOFException ex) {
//                // System.out.println("End of file reached");
//
//            } catch (IOException ex) {
//                Logger.getLogger(Matcher.class.getName()).log(Level.SEVERE, null, ex);
//            } finally {
//                try {
//                    if (ois != null) {
//                        ois.close();
//                    }
//                    if (fis != null) {
//                        fis.close();
//                    }
//
//                    File file = new File("temp.txt");
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
        } else if (cancelled || this.producer.isCancelled()) {

            log.info("Process cancelled.");
        }
//        File fis = new File("temp.txt");
//        if (fis.exists()) {
//            fis.delete();
//
//        }

        return simResult;
    }

    private class InnerIteratorSync<T> {

        protected Iterator<T> iter = null;

        public InnerIteratorSync(Iterator<T> aIterator) {
            iter = aIterator;
        }

        public synchronized T next() {
            T result = null;
            if (iter.hasNext()) {
                result = iter.next();
            }
            return result;
        }
    }

}

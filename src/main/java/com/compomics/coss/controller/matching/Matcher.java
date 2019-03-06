package com.compomics.coss.controller.matching;

import com.compomics.coss.model.TheDataUnderComparison;
import com.compomics.coss.controller.Dispartcher;
import com.compomics.coss.controller.UpdateListener;
import com.compomics.coss.controller.featureExtraction.DivideAndTopNPeaks;
import com.compomics.coss.model.ComparisonResult;
import com.compomics.coss.model.ConfigData;
import com.compomics.coss.model.MatchedLibSpectra;
import com.compomics.ms2io.Peak;
import com.compomics.ms2io.Spectrum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class Matcher implements Callable<List<ComparisonResult>> {

    private final ConfigData confData;
    private final TheDataUnderComparison data;
    private final DataProducer procucer;
    private final org.apache.log4j.Logger log;
    private final Score algorithm;
    private final UpdateListener listener;
    private boolean cancelled;

    public Matcher(Score al, DataProducer dp, TheDataUnderComparison dt, ConfigData cfd, UpdateListener lsr, org.apache.log4j.Logger lg) {
        this.algorithm = al;
        this.procucer = dp;
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
            Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        int taskCompleted = 0;
        int numDecoy = 0;
        int numTarget = 0;
        int massWindow = confData.getMassWindow();

        ArrayList<MatchedLibSpectra> specResult = new ArrayList<>(0);
        while (this.procucer.isReading() || (!data.getExpSpec().isEmpty() && !data.getLibSelectedSpec().isEmpty())) {
            try {

                if (data.getExpSpec().isEmpty() || data.getLibSelectedSpec().isEmpty()) {

                    continue;
                }
                Spectrum sp1 = data.pollExpSpec();
                ArrayList sb = data.pollLibSpec();                
                InnerIteratorSync<Spectrum> iteratorSpectra = new InnerIteratorSync(sb.iterator());
                int listSize=sb.size();
               specResult.ensureCapacity(listSize);

                
                while (iteratorSpectra.iter.hasNext()) {
                    //10 score results out which the maximum is going to be taken
                     List<Double> scores = new ArrayList<>(10);

                    try {
                        Spectrum sp2 = (Spectrum) iteratorSpectra.iter.next();
                        if (sp2.getTitle().contains("decoy") || sp2.getProtein().contains("DECOY")) {
                            numDecoy++;
                        }
                        
                        else{
                            numTarget++;
                        }
                        
                        
                        double mIntA = 0;
                        double mIntB = 0;
                        double tIntA = 0;
                        double tIntB = 0;
                        int mNumPeaks = 0;
                        int tempLenA = 0;
                        int tempLenB = 0;
                        double tempScore = 0;

                        for (int topN = 1; topN < 11; topN++) {
                            DivideAndTopNPeaks obj = new DivideAndTopNPeaks(sp1, topN, massWindow, log);
                            ArrayList<Peak> selectedPeaks_exp = obj.getFeatures();
                            obj = new DivideAndTopNPeaks(sp2, topN, massWindow, log);
                            ArrayList<Peak> selectedPeaks_lib = obj.getFeatures();

                            int lenA = selectedPeaks_exp.size();
                            int lenB = selectedPeaks_lib.size();
                            algorithm.setSumTotalIntExp(algorithm.calculateTotalIntensity(selectedPeaks_exp));
                            algorithm.setSumTotalIntLib(algorithm.calculateTotalIntensity(selectedPeaks_lib));
                            

                            double score = algorithm.calculateScore(selectedPeaks_exp, selectedPeaks_lib, lenA, lenB, topN);

                            selectedPeaks_exp.clear();
                            selectedPeaks_lib.clear();

                            if (tempScore < score) {
                                tempScore = score;
                                mIntA = algorithm.getSumMatchedIntExp();
                                mIntB = algorithm.getSumMatchedIntLib();
                                tIntA = algorithm.getSumTotalIntExp();
                                tIntB = algorithm.getSumTotalIntLib();
                                tempLenA = lenA;
                                tempLenB = lenB;
                                mNumPeaks = algorithm.getNumMatchedPeaks();
                            }
                            
                            
                            scores.add(score);
                            //double intensity_part = object.getIntensity_part();
                            //double probability_part = object.getProbability_part();
                        }
                        double finalScore = Collections.max(scores);
                        finalScore = (double)Math.round(finalScore * 1000d) / 1000d;
                        //scores.clear();
//                            if(finalScore>maxScore){
//                                maxScore=finalScore;
//                            }
                        if (finalScore > 1) {
                            MatchedLibSpectra mSpec = new MatchedLibSpectra();
                            mSpec.setScore(finalScore);//(Collections.max(scores));
                            mSpec.setSequence(sp2.getSequence());
                            if (sp2.getTitle().contains("decoy") || sp2.getProtein().contains("DECOY")) {
                                mSpec.setSource(1);
                            } else {
                                mSpec.setSource(0);
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
                        
                        }

                    } catch (Exception ex) {

                        Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, "\n Description: " + ex);
                    }

                }

                taskCompleted++;
                listener.updateprogress(taskCompleted);

                if (!specResult.isEmpty()) {
                    ComparisonResult compResult = new ComparisonResult();
                    compResult.setExpSpectrum(sp1);
                    Collections.sort(specResult);
                    Collections.reverse(specResult);

                    //only top 10 scores returned if exists
                    //int len = specResult.size();

                   //int num_top10Res= numSelectedLibSpec > 10? 10 : numSelectedLibSpec;
                    List<MatchedLibSpectra> tempMatch = new ArrayList<>(10);
                    int tempResSize=specResult.size();
                    int tempLen = 0;
                    int c = 0;
                    while (tempLen < tempResSize && tempLen < 10) {
                        tempMatch.add(specResult.get(c));
                        tempLen = tempMatch.size();
                        c++;
                    }
                    //specResult.clear();
                    if (!tempMatch.isEmpty()) {
                        compResult.setMatchedLibSpec(tempMatch);
                        compResult.setTopScore(tempMatch.get(0).getScore());

                        //simResult.add(compResult);  
                        oos.writeObject(compResult);
                        oos.flush();
                     
                       specResult.clear();
                    }
                    
                
                }

            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (cancelled) {
                break;
            }

            // specCount++;
        }

        if (numDecoy == 0) {
            log.info("No decoy spectra found to validate result");
            confData.setDecoyAvailability(false);
        }
        if (numDecoy < numTarget) {
            log.info("Number of decoy spectra is too small to validate result");
            confData.setDecoyAvailability(false);
        } else {
            confData.setDecoyAvailability(true);
        }
        
        
        List<ComparisonResult> simResult = new ArrayList<>(confData.getExpSpectraIndex().size());

        if (!cancelled) {
            //listener.updateprogress(confData.getExpSpectraIndex().size());
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                log.info("Search completed succesfully.");
            } catch (IOException ex) {
                Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, null, ex);
            }

            log.info(
                    "Getting results.");

            FileInputStream fis = null;
            ObjectInputStream ois = null;

            try {
                fis = new FileInputStream("temp.txt");
                ois = new ObjectInputStream(fis);
                ComparisonResult r = (ComparisonResult) ois.readObject();
                while (r != null) {
                    r = (ComparisonResult) ois.readObject();
//                        List<MatchedLibSpectra> mspec=r.getMatchedLibSpec();
//                        for(MatchedLibSpectra ms: mspec){
//                            double d= ms.getScore();
//                            d/=maxScore;
//                            d*=(double)100;
//                            ms.setScore(d);
//                        }
//                        r.setTopScore(r.getMatchedLibSpec().get(0).getScore());
                    simResult.add(r);
                }
            } catch (FileNotFoundException | ClassNotFoundException ex) {
                Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                // 
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }

                    File file = new File("temp.txt");
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Dispartcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            File fis = new File("temp.txt");
            if (fis.exists()) {
                fis.delete();

            }
        }

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

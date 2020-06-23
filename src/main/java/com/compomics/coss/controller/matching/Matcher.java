package com.compomics.coss.controller.matching;

import com.compomics.coss.model.TheDataUnderComparison;
import com.compomics.coss.controller.Dispatcher;
import com.compomics.coss.controller.UpdateListener;
import com.compomics.coss.controller.featureExtraction.DivideAndTopNPeaks;
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

    private final ConfigData confData;
    private final TheDataUnderComparison data;
    private final DataProducer producer;
    private final org.apache.log4j.Logger log;
    private final Score algorithm;
    private final UpdateListener listener;
    private boolean cancelled;

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

        int taskCompleted = 0;
        int numDecoy = 0;
        int massWindow = confData.getMassWindow();
        double percent = 100.0 / (double) confData.getExpSpecCount();

        int listSize = confData.getSpectraLibraryIndex().size() / 4;
        ArrayList<MatchedLibSpectra> specResult = new ArrayList<>();
        List<MatchedLibSpectra> tempMatch = new ArrayList<>(20);

        ArrayList<Peak> selectedPeaks_lib = new ArrayList<>(1000);
        ArrayList<Peak> selectedPeaks_exp = new ArrayList<>(1000);

        Spectrum sp1 = null;
        ArrayList sb = null;

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
                    //10 score results out which the maximum is going to be taken
                    List<Double> scores = new ArrayList<>(10);

                    try {
                        Spectrum sp2 = (Spectrum) iteratorSpectra.iter.next();
                        if (sp2.getComment().contains("Decoy") || sp2.getProtein().contains("DECOY") || sp2.getComment().contains("DECOY")) {
                            numDecoy++;
                        }

                        double mIntA = 0;
                        double mIntB = 0;
                        double tIntA = 0;
                        double tIntB = 0;
                        int mNumPeaks = 0;
                        int tempLenA = 0;
                        int tempLenB = 0;
                        double tempScore = -1;

                        for (int topN = 1; topN < 11; topN++) { // highest score from 1 - 10 peaks selection
                            //int topN=10;    //only for the top 10 peaks

                            selectedPeaks_lib = obj.getFeatures(sp2, topN);
                            selectedPeaks_exp = obj.getFeatures(sp1, topN);

                            int lenA = selectedPeaks_exp.size();
                            int lenB = selectedPeaks_lib.size();
                            algorithm.setSumTotalIntExp(algorithm.getSumIntensity(selectedPeaks_exp));
                            algorithm.setSumTotalIntLib(algorithm.getSumIntensity(selectedPeaks_lib));

                            double score = algorithm.calculateScore(selectedPeaks_exp, selectedPeaks_lib, lenA, lenB, topN);

                            if (score > tempScore) { //tempScore<score for MSRobin and cosine similarity
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

                        }
                        double finalScore = Collections.max(scores);//max for MSRobin and cosine similarity, min for MSE
                        finalScore = (double) Math.round(finalScore * 1000d) / 1000d;

                        MatchedLibSpectra mSpec = new MatchedLibSpectra();
                        mSpec.setScore(finalScore);//(Collections.max(scores));
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
